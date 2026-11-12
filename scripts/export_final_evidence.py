"""Refresh existing evidence/docs for architecture A; never runs tests or mutates business data."""
from pathlib import Path
import datetime,hashlib,json,os,platform,re,socket,zipfile,xml.etree.ElementTree as ET
from schema_tools import connect,fingerprints,shape,verify_schema,write_json
from generate_final_design import ROOT,OUT,CHECKPOINT,REMOVED,write,Q
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def combined(files):return hashlib.sha256(''.join(f['path']+'\0'+f['sha256']+'\n' for f in sorted(files,key=lambda x:x['path'])).encode()).hexdigest()
def load(name):return json.loads((OUT/'evidence'/name).read_text(encoding='utf-8'))
def main():
 original=json.loads((CHECKPOINT/'manifest.json').read_text(encoding='utf-8'))
 before={'time':original['time'],'root':str(ROOT),'checkpoint':str(CHECKPOINT),'files':[f for f in original['files'] if not f['path'].startswith('docs/final/')]}
 write_json(OUT/'evidence/snapshot-before.json',before);old={f['path']:f for f in before['files']}
 chosen={ROOT/p for p in old if (ROOT/p).is_file()}
 for folder in ['src','scripts','sql','.mvn','input']:chosen.update(p for p in (ROOT/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts)
 chosen.add(ROOT/'README.md')
 files=[{'path':p.relative_to(ROOT).as_posix(),'bytes':p.stat().st_size,'sha256':digest(p)} for p in sorted(chosen)]
 now={f['path']:f for f in files};protected=[p for p in old if p.startswith(('input/','docs/review/','docs/scenario/'))]
 bad=[p for p in protected if p not in now or now[p]['sha256']!=old[p]['sha256']]
 if bad:raise RuntimeError('Input/history changed: '+str(bad))
 code=[f for f in files if f['path'].startswith(('src/','scripts/','sql/','.mvn/')) or f['path'] in ['pom.xml','mvnw','mvnw.cmd']]
 app=[f for f in files if f['path'].startswith('src/main/') or f['path']=='pom.xml']
 code_hash=combined(code);app_hash=combined(app)
 changes=[{'path':p,'change':'added' if p not in old else 'deleted' if p not in now else 'modified','before':old.get(p,{}).get('sha256'),'after':now.get(p,{}).get('sha256')} for p in sorted(set(old)|set(now)) if old.get(p,{}).get('sha256')!=now.get(p,{}).get('sha256')]
 after={'time':datetime.datetime.now().astimezone().isoformat(),'root':str(ROOT),'nativeOS':os.name,'git':'No repository','sourceHash':code_hash,'applicationSourceHash':app_hash,'snapshotHash':combined(files),'protectedFilesChecked':len(protected),'protectedFilesUnchanged':True,'files':files,'changes':changes}
 write_json(OUT/'evidence/snapshot-after.json',after)
 junit=[]
 for p in sorted((ROOT/'target/surefire-reports').glob('TEST-*.xml')):
  r=ET.parse(p).getroot();junit.append({'suite':r.get('name'),**{k:int(r.get(k)) for k in ['tests','failures','errors','skipped']},'time':r.get('time'),'timestamp':r.get('timestamp'),'cases':[c.attrib for c in r.findall('testcase')]})
 totals={k:sum(x[k] for x in junit) for k in ['tests','failures','errors','skipped']}
 if totals!={'tests':27,'failures':0,'errors':0,'skipped':0}:raise RuntimeError('Current JUnit failed/incomplete')
 write_json(OUT/'evidence/junit-summary.json',{'totals':totals,'suites':junit,'sourceHash':code_hash})
 pipeline=load('full-verification.json');browser=load('browser/browser-results.json');reg=load('regression/results.json');guard=load('guardrails/results.json');mainproof=load('main/results.json')
 if pipeline['exitCode']!=0:raise RuntimeError('Pipeline failed')
 for name,data,n in [('browser',browser,49),('regression',reg,38),('guards',guard,4),('main read-only',mainproof,11)]:
  if len(data['checks'])!=n or not all(c['pass'] for c in data['checks']):raise RuntimeError(name+' failed/incomplete')
 if not browser['cleanup']['allRestored'] or not reg['cleanup']['allRestored'] or not guard['mainDatabaseUnchanged'] or not mainproof['businessTablesUnchanged']:raise RuntimeError('Preservation/cleanup failed')
 if load('runtime-smoke.json')['databaseShutdownPerformed']:raise RuntimeError('Unexpected DB shutdown')
 java=list((ROOT/'src/main/java').rglob('*.java'))
 with zipfile.ZipFile(ROOT/'target/garaman6.war') as z:
  entries=z.namelist();forbidden=[n for n in entries if any(x in n for x in REMOVED)]
  if forbidden:raise RuntimeError('Removed artifacts in WAR: '+str(forbidden))
  if any('jsp:useBean' in p.read_text(encoding='utf-8') or 'jsp:setProperty' in p.read_text(encoding='utf-8') or 'garaman.dao.' in p.read_text(encoding='utf-8') for p in (ROOT/'src/main/webapp').rglob('*.jsp')):raise RuntimeError('JSP packages entities or imports DAO')
 display_paths=[p for p in old if p.startswith('src/main/webapp/') and not any(n in p for n in ['PrepareAppointment6.jsp','PrepareVehicle6.jsp'])]
 unchanged=all(p in now and now[p]['sha256']==old[p]['sha256'] for p in display_paths)
 if not unchanged:raise RuntimeError('Display layout/control assets unexpectedly changed')
 source_proof=[]
 for file,needle in [('BookingServlet6.java','Appointment6 draft=new Appointment6()'),('VehicleServlet6.java','Vehicle6 v=new Vehicle6()'),('AppointmentServlet6.java','Appointment6 draft=(Appointment6)c.get("draft")')]:
  p=ROOT/'src/main/java/garaman/servlet'/file;s=p.read_text(encoding='utf-8');pos=s.index(needle)
  source_proof.append({'source':p.relative_to(ROOT).as_posix(),'line':s[:pos].count('\n')+1,'snippet':needle,'sha256':digest(p)})
 write_json(OUT/'evidence/jsp-packaging-proof.json',{'architecture':'Servlet packages; JSP only render/send scalar','removedArtifactsAbsentFromSourceAndWar':True,'scriptingInvalid':True,'displayAssetsUnchanged':unchanged,'proof':source_proof,'sourceHash':code_hash})
 with connect('garaman6') as c:
  schema_hash=verify_schema(c,'garaman6');test_hash=verify_schema(c,'garaman6_v3_it');metadata=shape(c,'garaman6')
  with c.cursor() as q:q.execute('SELECT DATABASE(),VERSION(),@@port,@@session.time_zone,@@lower_case_table_names,@@datadir');runtime=q.fetchone()
  current=fingerprints(c,'garaman6')
 baseline=json.loads((CHECKPOINT/'database-before.json').read_text(encoding='utf-8'))
 if metadata!=baseline['schema']:raise RuntimeError('Main schema changed')
 activity=load('main/concurrent-activity.json')
 if not all(activity[k] for k in ['originalAppointmentRowsPreserved','onlyAssociatedSlotStateChanged','other16TablesUnchanged']):raise RuntimeError('Original main rows not preserved')
 ports={}
 for port in [int(runtime[2]),8080,8082]:
  with socket.socket() as s:s.settimeout(1);ports[str(port)]=s.connect_ex(('127.0.0.1',port))==0
 war_hash=digest(ROOT/'target/garaman6.war')
 write_json(OUT/'evidence/final-runtime.json',{'platform':platform.platform(),'nativeOS':os.name,'database':runtime[0],'serverVersion':runtime[1],'port':runtime[2],'timeZone':runtime[3],'lowerCaseTableNames':runtime[4],'datadir':runtime[5],'mainSchemaHash':schema_hash,'testSchemaHash':test_hash,'schemaUnchanged':True,'mainBusinessRows':current,'concurrentActivity':activity,'portsListening':ports,'javaClasses':len(java),'mainWarSha256':war_hash,'sourceHash':code_hash,'applicationSourceHash':app_hash,'schema':metadata})
 log=(OUT/'evidence/full-verification.log').read_text(encoding='utf-8',errors='replace')
 if 'BUILD SUCCESS' not in log or 'Compiling 47 source files' not in log:raise RuntimeError('No clean 47-class build proof')
 write_json(OUT/'evidence/build-result.json',{'command':pipeline['command'],'exitCode':0,'finishedAt':pipeline['finishedAt'],'log':pipeline['log'],'junit':totals,'sourceHash':code_hash,'applicationSourceHash':app_hash,'warSha256':war_hash,'javaClasses':len(java)})
 for name in ['01_Hop_dong_lop.md','Scenario_V3_Module1.md','Scenario_V3_Module2.md','Scenario_V3_Ky_thuat_Module1.md','Scenario_V3_Ky_thuat_Module2.md']:
  p=OUT/name;s=p.read_text(encoding='utf-8');s=re.sub(r'\nSource SHA-256: .*?\n','\n',s);title,body=s.split('\n',1)
  p.write_text(title+'\n\nSource SHA-256: '+Q+code_hash+Q+'; [manifest](evidence/snapshot-after.json).\n\n'+body.lstrip('\n'),encoding='utf-8')
 trace=load('scenario-trace-final.json');trace['sourceHash']=code_hash;write_json(OUT/'evidence/scenario-trace-final.json',trace)
 filetable='# File thay đổi so với checkpoint cách A\n\nCheckpoint: §.local/checkpoints/servlet-architecture-20261112_172201/§. Input và review/scenario lịch sử giữ nguyên. Các tài liệu docs/final được ghi đè theo yêu cầu, bản trước có trong ZIP checkpoint.\n\n| File | Loại | SHA trước (rút gọn) | SHA sau (rút gọn) |\n|---|---|---|---|\n'
 for x in changes:filetable+=f'| {x["path"]} | {x["change"]} | {(x["before"] or "")[:16]} | {(x["after"] or "")[:16]} |\n'
 write('08_File_thay_doi.md',filetable)
 report=f"""# Kết quả sửa kiến trúc A và kiểm thử — Windows thực

Đã chuyển đóng gói về Servlet, phân bổ trách nhiệm trước khi xóa9 lớp helper/CLI và2 JSP. Không thêm lớp thay tên, không thêm màn hình. Mã chính hiện47 lớp; code/source/diagram/class contracts đồng bộ. [Bảng chuyển trách nhiệm và ô cần sửa](02_Bang_thay_doi_thiet_ke.md).

Lệnh thực tế:

§§§powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/Test-GaraMan.ps1 -Smoke
§§§

Script thực hiện §mvnw.cmd -B -ntp clean package§ → prepare slots --test bằng SlotDAO6 CLI → Tomcat test riêng → browser → regression → guard/cleanup → smoke → finally dừng đúng Tomcat test. Lượt cuối {pipeline['finishedAt']}, exit0, {pipeline['seconds']} giây; [log](evidence/full-verification.log), [kết quả lệnh](evidence/full-verification.json). Không SHUTDOWN MySQL để thử outage.

| Kiểm tra | Kết quả thực | Evidence |
|---|---|---|
| Clean build/JUnit | 27 test, 0 fail/error/skipped;47 lớp main,2 lớp test | [JUnit từng case](evidence/junit-summary.json), [WAR/build](evidence/build-result.json) |
| Browser hai module | 49/49, desktop/mobile/auth/CSRF/ownership/2 tab/edit/expiry/double-submit/báo cáo/invoice/Back | [JSON](evidence/browser/browser-results.json) |
| Hồi quy | 38/38, gồm3 ca ID>127 mới | [JSON](evidence/regression/results.json) |
| Guard/cleanup | 4/4 child exit1 đúng lý do; main không đổi qua các probe; cố ý fail sau tạo xe vẫn cleanup | [JSON/fingerprints](evidence/guardrails/results.json) |
| Smoke test | App thực nối garaman6_v3_it/+07:00, login200, DB shutdown=false | [JSON](evidence/runtime-smoke.json) |
| Main sau deploy | 11/11 chỉ đọc; WAR deploy hash trùng, /test-runtime404, business rows trước/sau probe bằng nhau | [JSON](evidence/main/results.json), [hóa đơn main](evidence/main/invoice.png) |
| Kiến trúc/JSP | Không các artifact xóa trong source/WAR; Servlet có new/set và reuse draft; mọi asset/control hiển thị giữ nguyên | [Bằng chứng source](evidence/jsp-packaging-proof.json) |
| UML/tài liệu | Hai sơ đồ check/render SVG; trace line/mapping/numbering/contracts/links kiểm riêng | [PlantUML](evidence/plantuml-validation.json), [QA tài liệu](evidence/document-validation.json) |
| Bảo toàn input/history | {len(protected)} file giữ nguyên SHA so với checkpoint, gồm ảnh lớp mới17:19 | [Manifest](evidence/snapshot-after.json) |

Các ca được chạy: 12 connection tranh hold,8 confirm tranh cùng token,5 HTTP submit cùng context chỉ một lịch; token sai/hết hạn, ownership sai rollback consume, final-code collision rollback slot/placeholder, nhiều tab/edit/release/PRG. Borrowed con không bị close/commit hộ.

Kiểu dữ liệu: field và method reflection kiểm Integer/BigDecimal/Date/arrays/parameter names; VehicleBrandDAO6.getVehicleBrands trả VehicleBrand6[]. ID trước lưu null/getVehicleCode null-safe, ID10001 → X10001. HTTP tạo fixture customer/vehicle >127 và kiểm ownership qua hai graph object độc lập, xác nhận cùng draft thành công; usageId>127 lookup invoice thành công nhờ Objects.equals. Count không có dữ liệu trả Integer0.

NULL Km/0/Km biết roundtrip, nullable năm; input giả customer/id/Km/code không được mass-assign. Bảy field tiền BigDecimal, setter scale2 UNNECESSARY, không double trung gian. Phần lẻ và số lớn vượt miền chính xác double kiểm cả JDBC/model/report và JSP: unit §9007199254740991.01§, invoice §9007199257840991.01§, total report §9007199259981991.01§; ảnh [tiền lớn](evidence/regression/large-money-fixed.png). Hai module không có DAO ghi đơn giá/thanh toán, không xây thêm nghiệp vụ tiền ngoài scope.

Payment folded UNPAID/PAID, từng trường NULL bắt buộc bị CHECK từ chối, Account all-null/partial account, binary status uppercase, generated activeSlotId INSERT/đổi đủ5 status/unique chống ghi SQL trực tiếp. Timestamp6 microseconds/instant với JVM UTC+DB07; issueDate cuối kỳ .999999 và usageTime khác ngày vẫn đúng.

Validation brand abc/rỗng/âm/0/id không tồn tại/injection đều400 và escape input; unknown action400. Invoice usageId rỗng/abc/0/âm400, sai item/kỳ/notfound404. Đồng hồ client Date.now lệch ±10 phút không khóa hold còn hiệu lực; rút expiry DB thật, UI hết TTL và server409 không INSERT. Đổi ngày kiểm fetch thật/DOM/DATE(slot), giữ bản vá form.action.

Báo cáo SERVICE/PART/ALL dùng giá lịch sử, count dòng, PAID/issueDate, tổng toàn tập5.741.000 với fixture tháng10/2026; service2.301.000/part3.440.000,27 đối tượng/29 usages, trang20. Lifetime DV0001=5/PT0005=3 độc lập2 history trong kỳ. HD0001 đủ2 service+2 part; người bán Lê Thị Lan/người thu Vũ Thu Ngân tách biệt, không rating. Back giữ context.

Cleanup test finally so sánh8 bảng gồm member/customer mới cho ca ID lớn, xóa đúng fixture/dependent records, không reset counter. Checkpoint main không được restore để đè hoạt động đồng thời: LH0006 tạo17:24 trong lúc làm việc,5 lịch cũ giữ nguyên,16 bảng khác không đổi, chỉ slot của lịch mới đổi BOOKED. [Đối chiếu](evidence/main/concurrent-activity.json).

**Main:** đã deploy WAR mới bằng §python scripts/run_tomcat.py start§ và chạy §python scripts/verify_main_readonly.py --main§; không DDL/migration/seed trong lượt này. Main http://localhost:8080/garaman6/login; MySQL3306 tiếp tục chạy, test8082 đã dừng. Schema main/test hash §{schema_hash}§ không đổi so với checkpoint. [Runtime cuối](evidence/final-runtime.json).

Source SHA §{code_hash}§, application source SHA §{app_hash}§; bytecode/47 classes từ clean build, WAR SHA trong evidence. Các lỗi trong giai đoạn chuyển API (test còn int.class/tham chiếu helper) đã sửa và clean build/test lại, không coi lần fail là pass. [Giới hạn còn lại](07_Khac_biet_va_gioi_han_con_lai.md) gồm D055, TIMESTAMP2038, nguồn thiếu và hiệu năng/cluster/crash chưa chứng minh.
"""
 write('06_Ket_qua_kiem_thu.md',report)
 write('README.md',f"""# GaraMan — bản hiệu lực cách A: Servlet đóng gói

Ghi đè tài liệu cùng tên theo quyết định mới. Bản trước sửa trong §.local/checkpoints/servlet-architecture-20261112_172201/source-before.zip§. Input/ảnh gốc và review/scenario lịch sử giữ nguyên.

| File | Nội dung |
|---|---|
| [00 nguồn hiệu lực](00_Nguon_chuan_hieu_luc.md) | Quyết định mới nhất và checkpoint. |
| [01 hợp đồng47 lớp](01_Hop_dong_lop.md) | Field/constructor/method/params/return/visibility/throws từ bytecode có -parameters. |
| [02 bảng sửa sơ đồ/chuyển trách nhiệm](02_Bang_thay_doi_thiet_ke.md) | Từng ô Integer/BigDecimal, method, relation;9 lớp +2 JSP đã xóa và nơi tiếp nhận. |
| [03 DB/ràng buộc](03_Anh_xa_DB_va_rang_buoc.md) | Schema giữ nguyên; tất cả column/type/FK/index/CHECK/generated/null mapping. |
| [UML module1](design/module1-v3.puml) · [SVG](design/module1-v3.svg) | Không helper/JSP trung gian; context bằng Java Map. |
| [UML module2](design/module2-v3.puml) · [SVG](design/module2-v3.svg) | DAO hydrate đúng thực thể, tiền/Integer và role staff. |
| [V3 module1](Scenario_V3_Module1.md) · [truy vết](Scenario_V3_Ky_thuat_Module1.md) | 29 bước happy case; Servlet tạo draft, final handler dùng lại. |
| [V3 module2](Scenario_V3_Module2.md) · [truy vết](Scenario_V3_Ky_thuat_Module2.md) | 34 bước liên tục, đúng hai lượt SERVICE rồi PART, đoạn trước/sau chung. |
| [05 nhánh hỗ trợ](05_Nhanh_ho_tro_Module1.md) | Servlet tạo Vehicle6, edit/logout/listener và graph optional. |
| [06 kết quả thực](06_Ket_qua_kiem_thu.md) | 27 JUnit +49 browser +38 regression +4 guard +11 main chỉ đọc. |
| [07 giới hạn](07_Khac_biet_va_gioi_han_con_lai.md) | Nguồn thiếu, D055, miền thời gian, hiệu năng và hoạt động đồng thời main. |
| [08 file thay đổi](08_File_thay_doi.md) | So với checkpoint trước lượt sửa kiến trúc. |
| [Evidence](evidence/) | Log/screenshots/hashes/runtime/proof/trace/QA tài liệu. |

Ứng dụng main: http://localhost:8080/garaman6/login. Schema/dữ liệu gốc không bị reset; lịch phát sinh đồng thời được giữ. Config/backup chứa credential nằm ngoài docs công khai. Inventory47 lớp chỉ mô tả phạm vi code hiện có, không ép toàn hệ thống/sơ đồ tổng thành47 lớp.
""")
 print(json.dumps({'sourceHash':code_hash,'applicationSourceHash':app_hash,'protectedFiles':len(protected),'changedFiles':len(changes),'classes':len(java),'junit':totals,'mainSchemaUnchanged':True,'ports':ports}))
if __name__=='__main__':main()
