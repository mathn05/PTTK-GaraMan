"""Prove failure cleanup and refusal of main DB/HTTP targets. Expected child failures are assertions here."""
import json, os, subprocess, sys
from pathlib import Path
from schema_tools import ROOT, connect, fingerprints, write_json
from test_support import properties
OUT=ROOT/'docs/final/evidence/guardrails';OUT.mkdir(parents=True,exist_ok=True)
cfg=properties(ROOT/'.local/garaman.properties')
main_state=json.loads((ROOT/'.local/tomcat.json').read_text(encoding='utf-8'))
main_url='http://127.0.0.1:'+str(main_state['port'])+'/garaman6'
with connect('garaman6') as con:before=fingerprints(con,'garaman6')
checks=[]
def child(name,script,changes,reason):
    env=dict(os.environ);env.update(PYTHONIOENCODING='utf-8',PYTHONUTF8='1',GARAMAN_TEST_OUT=str(OUT/name));env.update(changes)
    r=subprocess.run([sys.executable,script]+(['--test'] if script.endswith('prepare_slots.py') else []),cwd=ROOT,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    log=r.stdout.decode('utf-8',errors='replace');(OUT/(name+'.log')).write_text(log,encoding='utf-8')
    report_path=OUT/name/'browser-results.json';report=json.loads(report_path.read_text(encoding='utf-8')) if report_path.exists() else {}
    passed=r.returncode!=0 and reason in (log+(report.get('error') or ''))
    if name=='cleanup-failure':passed=passed and report.get('cleanup',{}).get('allRestored') is True
    checks.append({'name':name,'expectedExit':'nonzero','actualExit':r.returncode,'pass':passed,'cleanup':report.get('cleanup')})
    if not passed:raise AssertionError(name+' did not fail for the intended reason')
try:
    child('main-config','scripts/browser_test.py',{'GARAMAN_TEST_CONFIG':str(ROOT/'.local/garaman.properties')},'Refuse writes outside explicitly configured test schema')
    child('main-http','scripts/browser_test.py',{'GARAMAN_BASE_URL':main_url},'HTTP Error 404')
    child('main-jdbc','scripts/prepare_slots.py',{'GARAMAN_DB_URL':cfg['GARAMAN_DB_URL'],'GARAMAN_DB_USER':cfg['GARAMAN_DB_USER'],'GARAMAN_DB_PASSWORD':cfg['GARAMAN_DB_PASSWORD']},'Refuse writes outside the configured test schema')
    child('cleanup-failure','scripts/browser_test.py',{'GARAMAN_TEST_FAIL_AFTER_VEHICLE':'1'},'Intentional cleanup verification failure')
finally:
    with connect('garaman6') as con:after=fingerprints(con,'garaman6')
    preserved=before==after
    write_json(OUT/'results.json',{'checks':checks,'mainDatabaseUnchanged':preserved,'before':before,'after':after})
    if not preserved:raise AssertionError('Main database changed during test guard verification')
print(json.dumps({'checks':checks,'mainDatabaseUnchanged':preserved},ensure_ascii=True))

