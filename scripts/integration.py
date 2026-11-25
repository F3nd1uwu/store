"""Integration checks against the running Compose stack; creates isolated test records."""
import json, time, uuid, urllib.request, urllib.error, os
host = os.environ.get("TEST_HOST", "localhost")
api_base = os.environ.get("API_BASE_URL", "").rstrip("/")
compose = ["docker", "compose"]
for compose_file in os.environ.get("COMPOSE_FILES", "docker-compose.yml").split(","):
    compose.extend(["-f", compose_file])
def request(port, path, body=None, token=None, expected=200, method=None):
    headers={"Content-Type":"application/json"}
    if token: headers["Authorization"]="Bearer "+token
    base = api_base or f"http://{host}:{port}"
    req=urllib.request.Request(f"{base}/api/v1/{path}",data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=10) as res:
            assert res.status==expected,(res.status,expected)
            return json.load(res)
    except urllib.error.HTTPError as e:
        if e.code==expected: return None
        raise
for attempt in range(90):
    try:
        request(8083,"products/"+str(uuid.uuid4()),expected=401)
        request(8082,"orders/"+str(uuid.uuid4()),expected=401)
        token=request(8081,"auth/register",{"email":str(uuid.uuid4())+"@example.com","password":"integration-password"},expected=201)["accessToken"]
        break
    except (OSError,AssertionError):
        if attempt==89: raise
        time.sleep(2)

def create_product(quantity):
    return request(8083,"products",{"title":"Integration book","price":10,"currency":"RUB","quantity":quantity},token,201)
def place(product,quantity=1,identity=None):
    return request(8082,"orders",{"currency":"RUB","items":[{"productId":product["id"],"quantity":quantity,"price":0.01}]},identity or token,201)
def wait_order(order,status,identity=None):
    for _ in range(90):
        actual=request(8082,"orders/"+order["orderId"],token=identity or token)
        if actual["status"]==status:return actual
        if actual["status"] in ("PAID","CANCELLED"):raise AssertionError(actual)
        time.sleep(1)
    raise AssertionError("Saga did not finish: "+str(actual))

# Supporting aggregate operations
request(8081,"profile",{"name":"Student","address":"Demo street","phone":"123"},token,method="PUT")
assert request(8081,"profile",token=token)["address"]=="Demo street"
category_id=str(uuid.uuid4())
request(8083,"categories",{"id":category_id,"title":"Books"},token)
assert request(8083,"categories/"+category_id,token=token)["title"]=="Books"
request(8083,"wallet/deposit",{"currency":"RUB","amount":100},token)
product=create_product(5)
for _ in range(2):assert request(8083,"products/"+product["id"],token=token)["quantity"]==5
request(8082,"cart",[{"productId":product["id"],"quantity":2}],token,method="PUT")
assert request(8082,"cart",token=token)["items"][0]["quantity"]==2
coupon_id=str(uuid.uuid4())
request(8082,"coupons",{"id":coupon_id,"code":"DEMO","percent":10},token)
assert float(request(8082,"coupons/"+coupon_id+"/quote?amount=100",token=token))==90
order=place(product,2)
actual=wait_order(order,"PAID")
assert float(actual["totalAmount"])==20,actual
assert request(8083,"products/"+product["id"],token=token)["quantity"]==3
assert float(request(8083,"wallet",token=token)["balance"])==80
other=request(8081,"auth/register",{"email":str(uuid.uuid4())+"@example.com","password":"integration-password"},expected=201)["accessToken"]
request(8082,"orders/"+order["orderId"],token=other,expected=404)

# Payment refusal must restore stock in a separate local transaction
failed=place(product,1,other)
wait_order(failed,"CANCELLED",other)
assert request(8083,"products/"+product["id"],token=token)["quantity"]==3
rejected=place(product,100)
wait_order(rejected,"CANCELLED")
assert request(8083,"products/"+product["id"],token=token)["quantity"]==3

# Duplicate the exact original event; inbox must prevent second debit/reserve
import subprocess
payload=subprocess.check_output([*compose,"exec","-T","postgres","psql","-U","postgres","-d","order_db","-Atc",f"SELECT payload FROM outbox_events WHERE aggregate_id='{order['orderId']}'"],text=True).strip()
subprocess.run([*compose,"exec","-T","-e","KAFKA_HEAP_OPTS=-Xms16m -Xmx64m","kafka","kafka-console-producer","--bootstrap-server","kafka:29092","--topic","store.order.v1"],input=payload+"\n",text=True,check=True,stdout=subprocess.DEVNULL)
time.sleep(4)
assert request(8083,"products/"+product["id"],token=token)["quantity"]==3
assert float(request(8083,"wallet",token=token)["balance"])==80
# Competing customers must not oversell the last unit
from concurrent.futures import ThreadPoolExecutor
request(8083,"wallet/deposit",{"currency":"RUB","amount":100},other)
last=create_product(1)
with ThreadPoolExecutor(max_workers=2) as pool:
    first=pool.submit(place,last,1,token)
    second=pool.submit(place,last,1,other)
    candidates=[(first.result(),token),(second.result(),other)]
results=[]
for candidate,identity in candidates:
    for _ in range(90):
        status=request(8082,"orders/"+candidate["orderId"],token=identity)["status"]
        if status in ("PAID","CANCELLED"):break
        time.sleep(1)
    results.append(status)
assert sorted(results)==["CANCELLED","PAID"],results
assert request(8083,"products/"+last["id"],token=token)["quantity"]==0
print("PASS: JWT, gRPC authoritative price, saga success, insufficient stock, payment compensation, inbox duplicate, owner isolation, PostgreSQL and Redis")
