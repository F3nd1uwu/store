"""Generate source navigation for the project report."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
text=["# Основные операции в исходном коде", "", "Номера строк соответствуют текущим исходникам. Для обновления карты: `python scripts/generate-code-map.py`.", ""]
for service in ["auth-service","order-service","inventory-service","contracts"]:
 text.extend(["## "+service,""])
 for p in sorted((root/service/"src/main/java").rglob("*.java")):
  lines=p.read_text(encoding="utf-8").splitlines()
  methods=[]
  for number,line in enumerate(lines,1):
   stripped=line.strip()
   if re.match(r"public .*\(",stripped) and not re.match(r"public (class|interface|record|enum) ",stripped):
    methods.append((number,stripped.split("{")[0].strip()))
  relative=p.relative_to(root).as_posix()
  text.extend(["### "+p.stem,"",f"Файл: [{relative}](../{relative}).",""])
  if methods:
   for number,method in methods:text.append(f"- Строка {number}: `{method}`.")
  else:text.append("Доменный тип, интерфейс порта или конфигурация; подробное назначение приведено в отчёте сервиса.")
  text.append("")
(root/"docs/code-map.md").write_text("\n".join(text),encoding="utf-8")
