"""Structural QA of final handover. Source-linked traces are not runtime method instrumentation."""
from pathlib import Path
import hashlib,json,re,urllib.parse,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/final'
def main():
    checks=[]
    def check(name,ok,details=None):
        checks.append({'name':name,'pass':bool(ok),'details':details})
        if not ok:raise AssertionError(name+': '+str(details))
    snapshot=json.loads((OUT/'evidence/snapshot-after.json').read_text(encoding='utf-8'))
    mismatches=[f['path'] for f in snapshot['files'] if not (ROOT/f['path']).is_file() or hashlib.sha256((ROOT/f['path']).read_bytes()).hexdigest()!=f['sha256']]
    check('Snapshot files still match final hashes',not mismatches,mismatches)
    java_names={'.'.join(p.relative_to(ROOT/'src/main/java').with_suffix('').parts) for p in (ROOT/'src/main/java').rglob('*.java')}
    contract=(OUT/'01_Hop_dong_lop.md').read_text(encoding='utf-8')
    contract_names=set(re.findall(r'^## (garaman\.[\w.]+)$',contract,re.M))
    check('All 47 source classes have complete contract sections',len(java_names)==47 and java_names==contract_names,{'source':len(java_names),'contracts':len(contract_names)})
    trace=json.loads((OUT/'evidence/scenario-trace-final.json').read_text(encoding='utf-8'))
    for key in ['module1','module2']:
        data=trace[key];report=data['report'];technical=data['technical']
        check(key+' report numbering contiguous',[r['n'] for r in report]==list(range(1,len(report)+1)),len(report))
        check(key+' technical numbering contiguous',[r['n'] for r in technical]==list(range(1,len(technical)+1)),len(technical))
        covered=[]
        for r in report:
            part=list(range(r['technicalFrom'],r['technicalTo']+1));covered+=part
            if not all(technical[n-1]['report']==r['n'] for n in part):raise AssertionError('Wrong group mapping')
        check(key+' mapping covers every technical step exactly once',covered==list(range(1,len(technical)+1)))
        bad=[]
        for t in technical:
            p=ROOT/t['path'];lines=p.read_text(encoding='utf-8').splitlines()
            if t['line']<1 or t['line']>len(lines) or lines[t['line']-1].strip()!=t['source']:bad.append(t['n'])
        check(key+' all source anchors match actual lines',not bad,bad)
        md=(OUT/f'Scenario_V3_Module{data["module"]}.md').read_text(encoding='utf-8')
        techmd=(OUT/f'Scenario_V3_Ky_thuat_Module{data["module"]}.md').read_text(encoding='utf-8')
        check(key+' both levels bind to final source hash',snapshot['sourceHash'] in md and snapshot['sourceHash'] in techmd)
    broken=[];placeholders=[];link_count=0
    for p in OUT.rglob('*.md'):
        text=p.read_text(encoding='utf-8')
        if '§' in text or '¤' in text:placeholders.append(p.name)
        for target in re.findall(r'\]\(([^)\n]+)\)',text):
            target=target.strip('<>')
            if target.startswith(('http://','https://','mailto:','#')):continue
            path=urllib.parse.unquote(target.split('#',1)[0])
            if not path:continue
            link_count+=1
            if not (p.parent/path).exists():broken.append({'file':str(p.relative_to(OUT)),'target':target})
    check('Every relative Markdown link resolves',not broken,{'checked':link_count,'broken':broken})
    check('No document template placeholders remain',not placeholders,placeholders)
    forbidden=['RowMapper6','UsageMapper6','BusinessCode6','DateUtil6','Web6','BookingContext6','Config6','PasswordUtil6','SlotSetup6','PrepareAppointment6','PrepareVehicle6','CodeSequenceDAO6','tblPayment6','tblCodeSequence6','tblSchemaVersion6','doubleValue()','new BigDecimal(double','jsp:useBean','jsp:setProperty']
    bad=[]
    for p in (ROOT/'src/main').rglob('*'):
        if p.suffix not in ['.java','.jsp','.js']:continue
        s=p.read_text(encoding='utf-8')
        for word in forbidden:
            if word in s:bad.append({'file':str(p.relative_to(ROOT)),'text':word})
    check('Runtime source has no removed tables/DAO or double conversion',not bad,bad)
    for n in [1,2]:
        p=OUT/f'design/module{n}-v3.puml';svg=p.with_suffix('.svg')
        body=p.read_text(encoding='utf-8');s=svg.read_text(encoding='utf-8');root=ET.fromstring(s)
        # Exception is a legitimate Java parameter type in rollback(Exception cause).
        check(f'Module {n} SVG has diagram content and no renderer failure','java.lang.UnsupportedOperationException' not in s and 'An error has occured' not in s and 'An error has occurred' not in s and root.tag.endswith('svg'),{'bytes':svg.stat().st_size,'width':root.get('width'),'height':root.get('height')})
        check(f'Module {n} models JSP/Servlet/DAO/entity and no removed helpers',all(x in body for x in ['<<JSP>>','<<servlet>>','<<dao>>','<<entity>>',' ..> ',' --> ']) and not any(word in body for word in forbidden[:11]))
    evidence={'success':all(c['pass'] for c in checks),'sourceHash':snapshot['sourceHash'],'checks':checks}
    (OUT/'evidence/document-validation.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'success':evidence['success'],'checks':len(checks),'relativeLinks':link_count,'sourceHash':snapshot['sourceHash']}))
if __name__=='__main__':main()
