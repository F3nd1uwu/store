"""Package locally built applications for manual Linux server deployment."""
from pathlib import Path
import zipfile
root=Path(__file__).resolve().parents[1]
output=root/"build"/"store-yasakovd-release.zip"
files=["Dockerfile.runtime","docker-compose.prod.yml","docker-compose.proxy.yml","init-multiple-dbs.sh","deploy/nginx.conf","scripts/deploy-manual.sh",".env.example","docs/deployment.md"]
for service in ["auth-service","order-service","inventory-service"]:
    jars=list((root/service/"build"/"libs").glob("*.jar"))
    if len(jars)!=1:raise SystemExit(f"Run Gradle bootJar first: {service}")
    files.append(jars[0].relative_to(root).as_posix())
output.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(output,"w",compression=zipfile.ZIP_DEFLATED) as archive:
    for name in files:archive.write(root/name,name)
print(output)
