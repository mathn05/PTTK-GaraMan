"""Read-only smoke of the dedicated test app. Never shuts down any MySQL server."""
import json
from urllib.request import urlopen
from schema_tools import ROOT
from test_support import GuardedTest
state=json.loads((ROOT/'.local/tomcat-test.json').read_text())
if state.get('mode')!='test':raise RuntimeError('Refuse main runtime')
base='http://127.0.0.1:'+str(state['port'])+'/garaman6'
test=GuardedTest(base)
try:
    status=urlopen(base+'/login').status
    if status!=200:raise RuntimeError('Login unavailable')
    result={'target':base,'actualDatabase':test.db,'schemaHash':test.schema_hash,'loginStatus':status,'databaseShutdownPerformed':False}
    (ROOT/'docs/final/evidence/runtime-smoke.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result))
finally:test.cleanup()

