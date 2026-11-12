"""Prepare slots with native Java; test mode validates both config and the actual JDBC catalog."""
from pathlib import Path
import argparse, os, subprocess
from schema_tools import ROOT, connect, verify_schema
def properties(path):
    return {k.strip():v.strip() for line in Path(path).read_text(encoding='utf-8-sig').splitlines()
            if line.strip() and not line.lstrip().startswith('#') and '=' in line for k,v in [line.split('=',1)]}
p=argparse.ArgumentParser();p.add_argument('--test',action='store_true');a=p.parse_args()
config=ROOT/('.local/test.properties' if a.test else '.local/garaman.properties')
args=['java','-Dgaraman.config='+str(config),'-Duser.timezone=Asia/Ho_Chi_Minh']
if a.test:
    cfg=properties(config);db=cfg.get('GARAMAN_TEST_DATABASE','')
    if not db.startswith('garaman6') or not db.endswith('_it') or '/'+db+'?' not in cfg.get('GARAMAN_DB_URL',''):raise RuntimeError('Unsafe test configuration')
    with connect(db) as con:verify_schema(con,db)
    args+=['-DGARAMAN_TEST_MODE=true','-DGARAMAN_TEST_DATABASE='+db]
args+=['-cp',str(ROOT/'target/classes')+';'+str(ROOT/'target/garaman6/WEB-INF/lib/*'),'garaman.dao.SlotDAO6']
r=subprocess.run(args,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
print(r.stdout.decode("utf-8",errors="replace"));raise SystemExit(r.returncode)

