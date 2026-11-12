"""Regenerate the existing V3 documents for the Servlet-packaging architecture."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/final';Q=chr(96)
CHECKPOINT=ROOT/'.local/checkpoints/servlet-architecture-20261112_172201'
REMOVED={
 'RowMapper6':'CustomerDAO6.readCustomer/findById, MemberDAO6.fillMember/findEmployeeById/findSaleStaffById/findTechnicianById, VehicleDAO6.readVehicle, SlotDAO6.readSlot, ServiceDAO6.readService, SparePartDAO6.readPart, InvoiceDAO6.findOrderById, AppointmentDAO6.findById',
 'UsageMapper6':'ServiceUsageDAO6.readUsage và PartUsageDAO6.readUsage (private)',
 'BusinessCode6':'Vehicle6.getVehicleCode dẫn xuất X; AppointmentDAO6.temporaryCode và UPDATE mã LH trước commit',
 'DateUtil6':'Các phương thức private date/localDate/start/endExclusive trong Servlet/DAO sử dụng ngày',
 'Web6':'Các phương thức private đọc param/validation/forward/URL/phân trang trong từng Servlet; error forward trong filter',
 'BookingContext6':'BookingServlet6 quản lý session Map<String,Map<String,Object>>, mỗi tab/context một Map, khóa synchronized(c)',
 'Config6':'DAO6.load/setting/number/zone; cấu hình vẫn từ system property → environment → file private, không hardcode secret',
 'PasswordUtil6':'MemberDAO6.hashPassword/verifyPassword/derive; PBKDF2-HMAC-SHA256, salt riêng, 600000 vòng; không plaintext',
 'SlotSetup6':'SlotDAO6.prepareSlots/main; scripts/prepare_slots.py gọi trực tiếp DAO CLI',
 'PrepareAppointment6.jsp':'BookingServlet6.doPost tạo Appointment6 sau validation/ownership/hold, c.put("draft",draft), forward ConfirmView6',
 'PrepareVehicle6.jsp':'VehicleServlet6.doPost tạo Vehicle6 sau validation, gọi VehicleDAO6.saveVehicle rồi redirect booking'
}
def write(name,text):
 p=OUT/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text.replace('§',Q).strip()+'\n',encoding='utf-8')
def field_rows():
 rows=[]
 for p in sorted((ROOT/'src/main/java/garaman/entity').glob('*.java')):
  for typ,name in re.findall(r'^\s*private (Integer|BigDecimal) (\w+)',p.read_text(encoding='utf-8'),re.M):
   cap=name[0].upper()+name[1:];rows.append((p.stem,name,typ,'get'+cap+'(): '+typ,'set'+cap+'('+typ+' '+name+'): void'))
 return rows
def make_diagrams():
 contracts=(OUT/'01_Hop_dong_lop.md').read_text(encoding='utf-8')
 blocks={n.rsplit('.',1)[1]:(n,b) for n,b in re.findall(r'## ([\w.]+)\s+§§§java\n(.*?)\n§§§'.replace('§',Q),contracts,re.S)}
 def short(s):return re.sub(r'\b(?:java\.(?:lang|util|time|sql|io|math|net|nio\.\w+)|jakarta\.servlet(?:\.http)?|garaman\.\w+)\.','',s)
 def box(n):
  full,body=blocks[n];entity='.entity.' in full;result=['class '+n+' <<'+full.split('.')[1]+'>> {']
  for line in body.splitlines()[1:]:
   text=line.strip()
   if text=='}':continue
   access='+' if text.startswith('public ') else '-' if text.startswith('private ') else '#' if text.startswith('protected ') else '~'
   if '(' not in text and text.endswith(';'):
    words=text[:-1].split()
    if 'static' in words:continue
    result.append(access+words[-1]+' : '+short(words[-2]))
   elif '(' in text:
    before=text[:text.index('(')];method=before.split()[-1]
    if entity and method!=n and method not in ['getVehicleCode','getLineAmount']:continue
    if not entity and access=='-' and method not in ['form','confirm','list','filter','readVehicle','readSlot','readService','readPart','readUsage','temporaryCode']:continue
    args=text[text.index('(')+1:text.index(')')]
    result.append(access+method+'('+short(args)+')'+('' if method==n else ' : '+short(before.split()[-2])))
  return '\n'.join(result+['}'])
 base=['Account6','Member6','Customer6','Employee6','SaleStaff6','Manager6','Technician6','Vehicle6','VehicleBrand6','Appointment6','AppointmentSlot6','DAO6','MemberDAO6','CustomerDAO6','VehicleDAO6','VehicleBrandDAO6','SlotDAO6','AppointmentDAO6']
 specs={
  1:(base+['BookingServlet6','AppointmentServlet6','VehicleServlet6'],['CustomerHomeView6','BookAppointmentView6','AddNewVehicleView6','ConfirmView6'],[
   ('CustomerHomeView6','BookingServlet6','GET /booking'),('BookAppointmentView6','BookingServlet6','GET slots / POST hold,addVehicle'),('ConfirmView6','AppointmentServlet6','POST /appointment'),('ConfirmView6','BookingServlet6','POST edit'),('AddNewVehicleView6','VehicleServlet6','POST /vehicle'),
   ('BookingServlet6','CustomerDAO6','getCustomerInfo'),('BookingServlet6','VehicleDAO6','getVehiclesOfCustomer / findById'),('BookingServlet6','SlotDAO6','findById / getAvailableSlots / holdSlot / remainingHoldMillis'),('BookingServlet6','Appointment6','new + setters after validated hold'),('BookingServlet6','BookAppointmentView6','forward'),('BookingServlet6','ConfirmView6','forward'),
   ('VehicleServlet6','VehicleBrandDAO6','findById / getVehicleBrands'),('VehicleServlet6','CustomerDAO6','getCustomerInfo'),('VehicleServlet6','Vehicle6','new + trusted setters'),('VehicleServlet6','VehicleDAO6','saveVehicle'),('VehicleServlet6','BookingServlet6','redirect /booking'),('VehicleServlet6','AddNewVehicleView6','forward'),
   ('AppointmentServlet6','BookingServlet6','context / confirm / PRG'),('AppointmentServlet6','CustomerDAO6','fresh customer'),('AppointmentServlet6','SlotDAO6','validateHold'),('AppointmentServlet6','AppointmentDAO6','createAppointment'),
   ('AppointmentDAO6','SlotDAO6','consumeHold / findById, borrowed con'),('AppointmentDAO6','CustomerDAO6','findById'),('AppointmentDAO6','VehicleDAO6','findById'),('AppointmentDAO6','MemberDAO6','findSaleStaffById')
  ]),
  2:(base+['Service6','SparePart6','PartBrand6','Category6','ServiceUsage6','PartUsage6','RepairOrder6','Invoice6','Payment6','RevenueStats6','ServiceDAO6','SparePartDAO6','ServiceUsageDAO6','PartUsageDAO6','InvoiceDAO6','RevenueDAO6','RevenueServlet6','ServicePartServlet6','InvoiceServlet6'],['ManagerHomeView6','ReportMenuView6','RevenueStatisticsFilterView6','RevenueStatisticsListView6','ServicePartDetailView6','InvoiceDetailView6'],[
   ('ManagerHomeView6','RevenueServlet6','GET menu'),('ReportMenuView6','RevenueServlet6','GET filter'),('RevenueStatisticsFilterView6','RevenueServlet6','GET list / ALL,SERVICE,PART'),('RevenueServlet6','RevenueDAO6','getRevenueStatistics'),('RevenueServlet6','RevenueStatisticsListView6','forward'),('RevenueServlet6','ReportMenuView6','forward'),('RevenueServlet6','RevenueStatisticsFilterView6','forward'),
   ('RevenueDAO6','RevenueStats6','new + setters per row'),('RevenueStatisticsListView6','ServicePartServlet6','GET itemType,itemCode,filter,page'),('ServicePartServlet6','ServiceDAO6','getServiceInfo'),('ServicePartServlet6','SparePartDAO6','getPartInfo'),('ServicePartServlet6','ServiceUsageDAO6','history then lifetime'),('ServicePartServlet6','PartUsageDAO6','history then lifetime'),('ServicePartServlet6','InvoiceDAO6','getInvoiceCodeByUsage'),('ServicePartServlet6','ServicePartDetailView6','forward'),('ServicePartDetailView6','ServicePartServlet6','GET action=invoice,usageId + context'),('ServicePartServlet6','InvoiceServlet6','redirect invoiceCode + context'),('InvoiceServlet6','InvoiceDAO6','getInvoiceDetail'),('InvoiceServlet6','InvoiceDetailView6','forward'),
   ('InvoiceDAO6','ServiceUsageDAO6','byOrder, same con'),('InvoiceDAO6','PartUsageDAO6','byOrder, same con'),('InvoiceDAO6','RepairOrder6','findOrderById -> new/setters'),('InvoiceDAO6','Invoice6','new + setters'),('InvoiceDAO6','Payment6','new + setters from Invoice row'),
   ('ServiceUsageDAO6','ServiceDAO6','findById'),('PartUsageDAO6','SparePartDAO6','findById'),('ServiceUsageDAO6','InvoiceDAO6','findOrderById'),('PartUsageDAO6','InvoiceDAO6','findOrderById'),('ServiceUsageDAO6','MemberDAO6','findTechnicianById'),('PartUsageDAO6','MemberDAO6','findTechnicianById'),('InvoiceDAO6','CustomerDAO6','findById'),('InvoiceDAO6','VehicleDAO6','findById'),('InvoiceDAO6','MemberDAO6','findSaleStaffById'),('InvoiceDAO6','AppointmentDAO6','optional appointment graph')
  ])
 }
 optional={('Member6','acc'),('Invoice6','payment'),('Appointment6','ss'),('RepairOrder6','appointment')}
 for number,(names,uis,deps) in specs.items():
  names=list(dict.fromkeys(names+['LoginServlet6','AuthFilter6','AppFilter6','BookingSessionListener6']))
  uis=['LoginView6']+uis
  out=['@startuml','hide empty members','skinparam classAttributeIconSize 0','skinparam defaultFontName Arial','skinparam linetype polyline','title GaraMan V3 — Servlet packaging — module '+str(number),'legend left','Solid arrow: entity reference field association. Dashed: actual call / HTTP / forward.','UI boxes are JSP artifacts, not invented Java View fields.','Integer is java.lang.Integer; money is BigDecimal; Date is java.util.Date.','No standalone business helpers or intermediate packaging JSPs.','endlegend','package "JSP views" {']
  out += ['class "'+n+'.jsp" as '+n+' <<JSP>>' for n in uis];out+=['}']
  for package in ['entity','servlet','dao','filter','util']:
   selected=[n for n in names if blocks[n][0].split('.')[1]==package]
   if not selected:continue
   out+=['package "'+('Servlet container listener' if package=='util' else 'garaman.'+package)+'" {']+[box(n) for n in selected]+['}']
  for n in names:
   full,body=blocks[n];parent=re.search(r' extends garaman\.\w+\.(\w+)',body.splitlines()[0])
   if parent and parent.group(1) in names:out+=[parent.group(1)+' <|-- '+n]
   if '.entity.' in full:
    for typ,field in re.findall(r'^\s*private (garaman\.[\w.]+(?:\[\])?) (\w+);',body,re.M):
     target=typ.replace('[]','').rsplit('.',1)[1]
     if target in names:out+=[n+' --> "'+('0..*' if typ.endswith('[]') else '0..1' if (n,field) in optional else '1')+'" '+target+' : '+field]
  out+=['class "java.sql.Connection" as JDBC','DAO6 --> JDBC : con / owner']
  deps+= [('LoginView6','LoginServlet6','POST login'),('LoginServlet6','MemberDAO6','authenticate'),('MemberDAO6','CustomerDAO6','readCustomer / borrowed con'),('CustomerDAO6','MemberDAO6','fillMember'),('VehicleDAO6','CustomerDAO6','findById'),('VehicleDAO6','VehicleBrandDAO6','findById'),('BookingSessionListener6','BookingServlet6','releaseAll on session destruction')] if number==1 else [('LoginView6','LoginServlet6','POST login'),('LoginServlet6','MemberDAO6','authenticate')]
  out += [a+' ..> '+b+' : '+label for a,b,label in deps if (a in names or a in uis) and (b in names or b in uis)]
  out+=['note right of DAO6','Owns connection/config, not every business responsibility.','Default constructor owns con; Connection constructor borrows.','Test mode verifies actual catalog before business SQL.','end note','note right of Vehicle6','ID before save is null. Derived getVehicleCode is null-safe.','currentMileage/manufactureYear: Integer nullable.','No physical vehicleCode column.','end note']
  if number==1:out+=['note right of BookingServlet6','Session bookingContexts: Map of context UUID to Map<String,Object>.','Each map retains id/date/vehicleId/slotId/requestDescription,','draft, holdCode, holdExpiresAt and completed; synchronize per map.','Servlet constructs draft after successful hold; final confirmation reuses it.','end note','note right of AppointmentDAO6','One transaction: consume token + ownership FOR SHARE + valid placeholder INSERT','+ generated key + final LH UPDATE + graph readback + commit.','Generated unique activeSlotId stays in DB, not an entity setter.','end note']
  else:out+=['note right of ServicePartServlet6','Actual sequence: item info -> PAID period history -> lifetime.','Invoice action repeats reads and validates usage membership by Objects.equals.','No rating; back URLs preserve filter/page/usagePage.','end note','note right of Payment6','Optional value object folded into Invoice columns.','UNPAID -> Invoice.payment=null; PAID id logically equals Invoice.id.','Cashier FK on Invoice differs from salesperson FK on RepairOrder.','end note','note right of RevenueStats6','DTO, no table/PK. Counts rows, revenue uses historical prices.','PAID + issueDate; DESC revenue then ASC code/type.','Whole-result BigDecimal total, page size 20.','end note']
  out+=['@enduml'];write('design/module'+str(number)+'-v3.puml','\n'.join(out))
def main():
 image_hashes={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (ROOT/'input').glob('*.jpg')}
 write('00_Nguon_chuan_hieu_luc.md',"""# Nguồn hiệu lực — cách A: Servlet đóng gói

Quyết định trực tiếp mới nhất của người dùng ưu tiên cao nhất: JSP gửi scalar → Servlet validate/đóng gói → DAO transaction → Servlet forward/redirect JSP. Quyết định này thay thế hoàn toàn yêu cầu JSP đóng gói trước đây. Không JSP gọi DAO/SQL hay tạo/gán entity nghiệp vụ; không live Java object qua form/URL.

Đã đọc bản thiết kế mới input/tk_md1.jpg, tk_md2.jpg, test_tk.jpg (mtime 12/11/2026 17:18–17:19) và ảnh DB input/grm_db.jpg, đối chiếu code. Không ghi đè các ảnh. Các prompt/Quyết định v2 cũ chỉ còn hiệu lực ở phần không mâu thuẫn: hold 3 phút/sức chứa 1, nullable Km/năm, Payment folded, generated unique, tiền chính xác, auth/CSRF/ownership/context, nghiệp vụ báo cáo.

Quy ước mới: integer → java.lang.Integer; decimal → java.math.BigDecimal; Date → java.util.Date; getAvailableSlots nhận java.time.LocalDate. Tất cả ID entity chưa lưu là null; count thiếu dữ liệu trả Integer 0. boolean/long giữ đúng mục đích.

Thiết kế bài giảng/LAB đã đọc ở lượt trước tiếp tục dùng cho cách diễn đạt giao diện → handler → DAO/entity → trả → hiển thị. Bản V3 chính nhóm đóng gói thuộc tính con/lặp, bảng kỹ thuật riêng có source anchors; không dùng ví dụ JSP cũ để phủ định quyết định mới.

Checkpoint trước sửa: §.local/checkpoints/servlet-architecture-20261112_172201/source-before.zip§ và manifest.json (336 file); database-before.json chứa fingerprints, không đưa secret vào docs. Không có Git repository. Bản sửa này không chạy DDL/migration/seed trên main. Source/SQL/tools/tài liệu cũ trong checkpoint có thể dùng đối chiếu; input và docs/review, docs/scenario lịch sử giữ nguyên so với checkpoint hiện tại.

Native Windows PowerShell, JDK Temurin21, Maven Wrapper3.10.0, MySQL9.4.0, Tomcat10.1.61; không thay bằng môi trường cloud/WSL. Config private giữ nguyên, không hardcode mật khẩu.

SQL gốc GaraMan_CSDL(1).sql và một số sơ đồ state/sequence độc lập chưa được cung cấp. Schema V3 hiện hành đã được kiểm trên DB thực; không tuyên bố khớp toàn bộ hệ thống/nguồn chưa có. Các tài liệu final hiện tại thay bản cùng tên trước đây; checkpoint giữ lịch sử.
"""+'\nSHA-256 ảnh đã đọc:\n\n'+'\n'.join('- '+n+': §'+h+'§.' for n,h in image_hashes.items()))
 matrix="""# Những ô cần sửa trên hai sơ đồ

Bản này thay bảng cùng tên trước đây theo quyết định Servlet đóng gói. Hợp đồng đầy đủ, gồm cả constructor/phương thức private/package-private/throws/thứ tự và tên tham số, ở [01](01_Hop_dong_lop.md). Không tạo lớp khác tên để thay helper.

## Các field integer/decimal — tên Java chính xác

Các hàng dưới đây được liệt kê từ field thật của 21 entity; là checklist cập nhật từng ô, không phải bằng chứng độc lập rằng ảnh gốc đúng. id thuộc superclass chỉ khai báo ở superclass; các subclass thừa kế, không khai báo trùng id. Field Integer mới mặc định null cho đến khi hydrate/set; count query không có dữ liệu trả Integer 0.

| Lớp | Field | Kiểu Java | Getter | Setter |
|---|---|---|---|---|
"""
 for cls,field,typ,get,setter in field_rows():matrix+=f'| {cls} | {field} | §{typ}§ | §{get}§ | §{setter}§ |\n'
 matrix+="""
## Hợp đồng thao tác cần ghi lại

| Ô | Nội dung chính xác sau sửa |
|---|---|
| Vehicle6.getVehicleCode | §public String getVehicleCode()§; null nếu id null/<=0, ngược lại X + String.format(Locale.ROOT,"%04d",id); không field/setter/cột vehicleCode, không cắt >9999. |
| VehicleBrandDAO6.getVehicleBrands | §public VehicleBrand6[] getVehicleBrands()§ — không phải VehicleBrandDAO6[]. |
| VehicleBrandDAO6.findById | §public VehicleBrand6 findById(Integer id)§. |
| VehicleDAO6 | §public Vehicle6[] getVehiclesOfCustomer(Integer customerId)§; §public Vehicle6 findById(Integer vehicleId)§; §public Vehicle6 saveVehicle(Vehicle6 vehicle, Integer customerId)§. |
| SlotDAO6 | §getAvailableSlots(LocalDate appointmentDate): AppointmentSlot6[]§; §findById(Integer slotId): AppointmentSlot6§; §holdSlot(Integer slotId): AppointmentSlot6§; §validateHold(Integer slotId,String holdCode): boolean§; §releaseSlot(Integer slotId,String holdCode): boolean§; §consumeHold(Integer slotId,String holdCode): boolean§; §remainingHoldMillis(Integer slotId,String holdCode): long§. Tất cả public; long/boolean không đổi sang Integer. |
| AppointmentDAO6 | §public Appointment6 createAppointment(Appointment6 appointment,String holdCode)§; §findById(Integer id): Appointment6§ package-private; §private static String temporaryCode()§. Placeholder INSERT hợp lệ, getGeneratedKeys, hoàn tất LH trước commit; không sequence/MAX+1. |
| ServiceUsageDAO6 | §public Integer getTotalUsageCount(String serviceCode)§; §public ServiceUsage6[] getAllServiceUsageOfService(String serviceCode,Date startDate,Date endDate)§; §ServiceUsage6[] byOrder(Integer orderId)§ package-private; private readUsage(ResultSet). |
| PartUsageDAO6 | §public Integer getTotalUsageCount(String partCode)§; §public PartUsage6[] getAllPartUsageOfPart(String partCode,Date startDate,Date endDate)§; §PartUsage6[] byOrder(Integer orderId)§ package-private; private readUsage(ResultSet). |
| InvoiceDAO6 | §public String getInvoiceCodeByUsage(String itemType,Integer usageId)§; §public Invoice6 getInvoiceDetail(String invoiceCode)§; §RepairOrder6 findOrderById(Integer id)§ package-private dùng chung cho graph invoice/history, không tạo lớp DAO giả tên helper. |
| RevenueDAO6 | §public RevenueStats6[] getRevenueStatistics(Date startDate,Date endDate,String statisticType)§. |
| CustomerDAO6 | §public Customer6 getCustomerInfo(String customerCode)§; §Customer6 findById(Integer id)§ và §Customer6 readCustomer(ResultSet rs)§ package-private cho graph/hydration. |
| MemberDAO6 | §public Member6 authenticate(String username,String password)§; §public static String hashPassword(String password)§; fillMember/findEmployeeById/findSaleStaffById/findTechnicianById package-private; verifyPassword/derive private. |
| DAO6 | Giữ con:Connection và owner:boolean final, constructor DAO6()/DAO6(Connection); config load private, setting/number/zone static public; close chỉ đóng connection sở hữu. Không gom cả logic nghiệp vụ vào DAO6. |
| BookingServlet6 | doGet/doPost public; tạo Appointment6 bằng constructor/setCus/setVehicle/setSlot/setRequestDescription ngay trong doPost sau hold; giữ draft phía server. context/confirm static package-private; contexts/release và các hàm đọc params/view private; releaseAll public static cho listener. |
| VehicleServlet6 | doPost kiểm dữ liệu → new Vehicle6/setCus/setBrand/setLicensePlate/setVehicleModel/setManufactureYear → saveVehicle → redirect booking; id/Km null, không mass-assign client. |
| AppointmentServlet6 | Lấy draft/context server, ownership Objects.equals, validateHold, createAppointment, completed + xóa hold/draft, PRG; không new draft lần hai. |
| Servlet báo cáo | Các hàm param/date/URL/forward/pagination chuyển thành private tại Servlet sử dụng; JSP chỉ render/gửi request. Không đổi control/bố cục. |
| Session context | §bookingContexts: Map<String,Map<String,Object>>§, UUID mỗi tab. Map con giữ id, appointmentDate, vehicleId, slotId, requestDescription, draft, holdCode, holdExpiresAt, completed. Đồng bộ trên từng Map. Không có lớp context thay tên. |
| Thành phần container | Giữ AppFilter6/AuthFilter6 cho UTF-8/CSRF/role/error; BookingSessionListener6 gọi BookingServlet6.releaseAll khi session bị hủy. Listener không phải helper nghiệp vụ. |

Không dùng == giữa hai Integer: ownership và membership dùng Objects.equals. JDBC/HTTP status, vòng lặp, iteration PBKDF và giá trị kỹ thuật nội bộ có thể dùng int; chúng không phải field integer nghiệp vụ trên sơ đồ. ID/query/count contract thuộc thiết kế dùng Integer. Đã thử các ID >127 qua HTTP và DB thật.

## Quan hệ giữ đúng

- Member6.acc → Account6 0..1; account không có thì cả bốn cột null; có thì username/password/status hợp lệ, profileImage tùy chọn.
- Appointment6.cus/vehicle/slot bắt buộc khi lưu; ss 0..1; Slot có nhiều lịch sử, tối đa 1 active (generated unique trong DB).
- RepairOrder6.appointment 0..1 với FK nullable UNIQUE; Invoice6.order 1, FK UNIQUE giới hạn một invoice/order.
- Invoice6.payment 0..1; UNPAID null, PAID dựng từ Invoice columns, id logic=Invoice.id; không Payment table.
- RepairOrder6.ss là người bán, Payment6.ss là người thu với hai FK riêng. Không có rating.
- Usage.order/service-or-part/tech và các graph con do DAO phù hợp hydrate. Nét liền chỉ field reference, nét đứt cho dependency gọi thật, kế thừa dùng tam giác.

## Xóa và chuyển trách nhiệm

| Lớp/JSP đã xóa | Nơi tiếp nhận trách nhiệm |
|---|---|
"""
 matrix+='\n'.join('| '+n+' | '+dest+' |' for n,dest in REMOVED.items())
 matrix+="""

Đã bỏ các action nội bộ, PACKAGING_GUARD, trusted*/prepared* request attrs và acceptDraft/savePrepared chỉ phục vụ vòng forward cũ. Không thêm màn hình. Các input/contact readonly, CSRF, ownership, giới hạn context/tab, expiry/edit/double-submit và transaction vẫn giữ.

Các phân loại D017/D018/D038/D042/D057/D059 đã chốt theo prompt trước vẫn giữ: mã xe dẫn xuất, không sequence/marker table, Payment folded, Km nullable, account optional/status chuẩn. Phần yêu cầu JSP đóng gói của lượt trước nay bị thay thế. D049–D054 validation/money/countdown/guard không bị bỏ trong refactor.
"""
 write('02_Bang_thay_doi_thiet_ke.md',matrix)
 schema=json.loads((ROOT/'sql/schema-contract.json').read_text(encoding='utf-8'))
 db="""# Mapping DB và ràng buộc — giữ nguyên schema V3

Lượt này không chạy DDL/migration/seed trên main. Schema vẫn 18 bảng + 2 view, physical FK dài theo input/grm_db.jpg. SQL INT tương ứng entity/contract Integer; SQL DECIMAL(18,2) tương ứng BigDecimal; TIMESTAMP(6) đọc Timestamp như java.util.Date. Nullable Km/năm bảo toàn bằng getObject(...,Integer.class); ghi setObject(...,Types.INTEGER).

Không có vehicleCode/Payment/CodeSequence/SchemaVersion/statistics table vật lý. Vehicle.getVehicleCode dẫn xuất null-safe từ id. Payment6 giữ Java, Invoice.payment null khi UNPAID; id logic bằng Invoice.id. Member.acc tùy chọn, Account.id logic bằng Member.id. Invoice.totalAmount từ vwInvoice6, SparePart.stockQuantity từ vwSparePart6, PartUsage.getLineAmount dẫn xuất bằng BigDecimal.

## Tên cột và Java mapping

| Cột vật lý | Thuộc tính Java / trách nhiệm |
|---|---|
| tblCustomer6.tblMember6id, tblEmployee6.tblMember6id | id kế thừa Member6; đọc Member/Account ở MemberDAO6, Customer ở CustomerDAO6. |
| tblCustomer6tblMember6id | cus.id trong Vehicle/Appointment/RepairOrder. |
| tblVehicleBrand6id | Vehicle.brand.id; VehicleBrandDAO6 nạp brand. |
| tblVehicle6id / tblAppointmentSlot6id | Appointment.vehicle.id / slot.id; DAO tương ứng nạp graph. |
| tblEmployee6tblMember6id | Appointment.ss (nullable); RepairOrder.ss người bán; Invoice→Payment.ss người thu; Usage.tech kỹ thuật viên; không nhập nhằng vai trò. |
| tblRepairOrder6id | Invoice.order.id hoặc Usage.order.id; InvoiceDAO6.findOrderById nạp graph phiếu. |
| tblService6id / tblSparePart6id | Usage.service.id / sparePart.id. |
| tblPartBrand6id / tblCategory6id | SparePart.brand.id / cat.id. |
| technicalSpec | SparePart.technicalSpecifications alias Java. |
| Invoice paymentAmount/time/method/status/reference/cashier | Payment6 amount/time/method/status/reference/ss trên cùng ResultSet, không query Payment. |

## Kiểu/null/default/generated hiện hành (tất cả cột)

| Bảng/view | Cột | SQL type | Nullable | Default | Extra/generated |
|---|---|---|---|---|---|
"""
 for t,pos,col,typ,null,default,extra,expr,*_ in schema['columns']:db+=f'| {t} | {col} | {typ} | {null} | {default} | {extra} {expr} |\n'
 db+='\n## Foreign keys\n\n| Bảng.cột | Parent | UPDATE / DELETE |\n|---|---|---|\n'
 for t,col,_,parent,pcol,up,delete,pos in schema['foreignKeys']:db+=f'| {t}.{col} | {parent}.{pcol} | {up}/{delete} |\n'
 db+='\n## Index/UNIQUE\n\n| Bảng | Unique | Cột theo thứ tự |\n|---|---|---|\n'
 for t,nonunique,cols in schema['indexes']:db+=f'| {t} | {nonunique==0} | '+', '.join(c[1] for c in cols)+' |\n'
 db+='\n## CHECK\n\n'+'\n'.join('- §'+t+'§: §'+expr.replace("\\'","'")+'§.' for t,expr in schema['checks'])
 db+="""

activeSlotId GENERATED STORED nullable UNIQUE: PENDING/CONFIRMED/RECEIVED → physical tblAppointmentSlot6id, COMPLETED/CANCELLED → NULL; không NOW/token trong generated. AppointmentSlot hold độc lập bằng token + NOW(6)+3 phút. Consume + ownership lock + valid placeholder INSERT + generated ID + final LH UPDATE + readback cùng transaction. Rollback giữ nguyên hold khi ghi thất bại, không mã tạm ra ngoài, không transaction chờ khách.

UNPAID bắt buộc toàn nhóm thanh toán NULL; PAID bắt amount/time/method/cashier IS NOT NULL, amount>=0, method không trống, reference được NULL. CHECK so sánh binary cho trạng thái chuẩn viết hoa, tránh collation/Java bất nhất và SQL UNKNOWN. InvoiceDAO compareTo total/paid chính xác.

Tiền bảy field BigDecimal, setter scale2 UNNECESSARY, getBigDecimal, cộng/nhân/compareTo BigDecimal, không double/Double biểu diễn tiền. Hai module không có nghiệp vụ ghi đơn giá/thanh toán; chỉ đọc các cột tiền DECIMAL. Không thêm ghi tài chính ngoài scope. Các DAO ghi xe/lịch chỉ ghi field tương ứng, không chuyển tiền qua double. Fixture money dùng Decimal/SQL DECIMAL.

Date helper đã chuyển thành private tại nơi dùng; tất cả dùng DAO6.zone() Việt Nam, connection SET +07:00. Bộ lọc [đầu ngày bắt đầu, đầu ngày sau kết thúc), issueDate và PAID, lịch sử giá. Lifetime đếm mọi dòng, không lọc kỳ/PAID. Tổng toàn tập trước slice20, ORDER BY revenue DESC/code ASC/type ASC. Invoice nạp đầy đủ dòng theo order, không dùng kỳ báo cáo để cắt invoice.

Migration V3 của lượt trước và backup/receipt vẫn được giữ trong .local/backups; không chạy lại migration trong lượt refactor này. Quản lý schema ngoài DB bằng .local/schema-state.json và đối chiếu metadata type/null/default/precision/generated/index/FK/CHECK/view dependencies, không tạo bảng kỹ thuật. DDL/backup cũ có implicit commit, không hứa rollback migration bằng một transaction.
"""
 write('03_Anh_xa_DB_va_rang_buoc.md',db)
 write('07_Khac_biet_va_gioi_han_con_lai.md',"""# Khác biệt và giới hạn còn lại

- Cách A Servlet đóng gói đã thực hiện; không còn vấn đề chờ chấp thuận JSP/Servlet. Không có helper nghiệp vụ thay tên. Các phương thức private tại DAO/Servlet là nội bộ của đúng lớp chịu trách nhiệm; config ở DAO6 và password ở MemberDAO6.
- Input không có SQL gốc GaraMan_CSDL(1).sql và một số biểu đồ state/sequence độc lập. Có thể đối chiếu code/ảnh mới/schema live nhưng không khẳng định 100% toàn hệ thống.
- TIMESTAMP(6) giữ theo DB design, zone Việt Nam; miền thời gian có giới hạn quanh2038. Test tương lai dùng2030/2031. Không âm thầm đổi DATETIME.
- Km lịch sử0 được giữ vì không biết 0 thật hay default cũ; xe mới thiếu Km là null. Năm cũng nullable.
- D055: sau hết session, returnRoute whitelist chỉ giữ path /booking,/vehicle,/revenue, chưa giữ đầy đủ query của màn hình detail/invoice. Khi session còn, Back giữ kỳ/loại/item/page/usagePage.
- DAO nạp toàn tập rồi Servlet slice20; có các query graph con N+1. Chưa load test hoặc tối ưu SQL pagination/batch ngoài scope.
- Đồng bộ mỗi context/tab trên một JVM và ràng buộc DB chống tranh slot; chưa kiểm cluster/distributed session/crash giữa commit và HTTP hoặc idempotency bền qua restart. Session restart mất draft; hold DB tự hết hạn.
- Cấu hình runtime Việt Nam/Windows đã kiểm, không tuyên bố các môi trường Linux/WSL/cloud hay mọi timezone đã chạy.
- 47 lớp là inventory Java hiện tại, không buộc toàn hệ thống/sơ đồ tổng chỉ có47 lớp. Supplier/receipt/warehouse chỉ DDL/fixture cần cho stock, không xây module ngoài scope.
- Không đổi bố cục/control/JSP hiển thị trong lượt này. UI date HTTP là String ISO, DAO dùng Date hoặc LocalDate theo contract; các thuộc tính UI trên ảnh không phải instance field Java của JSP.
- Trong khi sửa, main phát sinh thêm LH0006 lúc17:24:08. Đối chiếu cho thấy nguyên5 lịch trước checkpoint còn nguyên, chỉ thêm lịch mới và slot liên quan chuyển BOOKED; 16 bảng khác không đổi. Không xóa/hoàn nguyên hoạt động đồng thời này. Chi tiết evidence/main/concurrent-activity.json; 11 kiểm tra sau deploy chỉ đọc không làm đổi bảng nghiệp vụ.

Xem kết quả thực tế và bằng chứng tại [06](06_Ket_qua_kiem_thu.md). Tài liệu review/scenario lịch sử giữ nguyên; docs/final được ghi đè theo yêu cầu, bản trước được lưu trong checkpoint.
""")
 make_diagrams()
 print('Updated existing source/rule/design/DB/limits documents and both module PlantUML files.')
if __name__=='__main__':main()
