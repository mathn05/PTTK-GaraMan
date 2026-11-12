# Mapping DB và ràng buộc — giữ nguyên schema V3

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
| tblappointment6 | id | int | NO | None | auto_increment  |
| tblappointment6 | appointmentCode | varchar(30) | NO | None |   |
| tblappointment6 | requestDescription | varchar(255) | NO | None |   |
| tblappointment6 | createAt | timestamp(6) | NO | None |   |
| tblappointment6 | status | varchar(255) | NO | PENDING |   |
| tblappointment6 | tblEmployee6tblMember6id | int | YES | None |   |
| tblappointment6 | tblVehicle6id | int | NO | None |   |
| tblappointment6 | tblAppointmentSlot6id | int | NO | None |   |
| tblappointment6 | tblCustomer6tblMember6id | int | NO | None |   |
| tblappointment6 | activeSlotId | int | YES | None | STORED GENERATED (case when (`status` in (_utf8mb4\'PENDING\',_utf8mb4\'CONFIRMED\',_utf8mb4\'RECEIVED\')) then `tblAppointmentSlot6id` else NULL end) |
| tblappointmentslot6 | id | int | NO | None | auto_increment  |
| tblappointmentslot6 | startTime | timestamp(6) | NO | None |   |
| tblappointmentslot6 | holdCode | varchar(100) | YES | None |   |
| tblappointmentslot6 | holdExpiresAt | timestamp(6) | YES | None |   |
| tblappointmentslot6 | status | varchar(30) | NO | AVAILABLE |   |
| tblcategory6 | id | int | NO | None | auto_increment  |
| tblcategory6 | categoryName | varchar(255) | NO | None |   |
| tblcustomer6 | tblMember6id | int | NO | None |   |
| tblcustomer6 | customerCode | varchar(50) | NO | None |   |
| tblemployee6 | tblMember6id | int | NO | None |   |
| tblemployee6 | employeeCode | varchar(50) | NO | None |   |
| tblemployee6 | jobTitle | varchar(50) | NO | None |   |
| tblinvoice6 | id | int | NO | None | auto_increment  |
| tblinvoice6 | invoiceCode | varchar(30) | NO | None |   |
| tblinvoice6 | issueDate | timestamp(6) | NO | None |   |
| tblinvoice6 | tblRepairOrder6id | int | NO | None |   |
| tblinvoice6 | paymentAmount | decimal(18,2) | YES | None |   |
| tblinvoice6 | paymentTime | timestamp(6) | YES | None |   |
| tblinvoice6 | paymentMethod | varchar(255) | YES | None |   |
| tblinvoice6 | paymentStatus | varchar(255) | NO | UNPAID |   |
| tblinvoice6 | transactionReference | varchar(100) | YES | None |   |
| tblinvoice6 | tblEmployee6tblMember6id | int | YES | None |   |
| tblmember6 | id | int | NO | None | auto_increment  |
| tblmember6 | fullName | varchar(255) | NO | None |   |
| tblmember6 | phone | varchar(20) | NO | None |   |
| tblmember6 | email | varchar(255) | YES | None |   |
| tblmember6 | address | varchar(255) | YES | None |   |
| tblmember6 | username | varchar(50) | YES | None |   |
| tblmember6 | password | varchar(255) | YES | None |   |
| tblmember6 | profileImage | varchar(255) | YES | None |   |
| tblmember6 | status | varchar(50) | YES | None |   |
| tblpartbrand6 | id | int | NO | None | auto_increment  |
| tblpartbrand6 | name | varchar(255) | NO | None |   |
| tblpartreceipt6 | id | int | NO | None | auto_increment  |
| tblpartreceipt6 | receiptDate | timestamp(6) | NO | None |   |
| tblpartreceipt6 | tblSupplier6id | int | NO | None |   |
| tblpartreceipt6 | tblEmployee6tblMember6id | int | NO | None |   |
| tblpartusage6 | id | int | NO | None | auto_increment  |
| tblpartusage6 | usedQuantity | int | NO | None |   |
| tblpartusage6 | usageTime | timestamp(6) | NO | None |   |
| tblpartusage6 | saleUnitPrice | decimal(18,2) | NO | None |   |
| tblpartusage6 | note | varchar(255) | YES | None |   |
| tblpartusage6 | tblSparePart6id | int | NO | None |   |
| tblpartusage6 | tblRepairOrder6id | int | NO | None |   |
| tblpartusage6 | tblEmployee6tblMember6id | int | NO | None |   |
| tblreceiptdetail6 | id | int | NO | None | auto_increment  |
| tblreceiptdetail6 | receiptQuantity | int | NO | None |   |
| tblreceiptdetail6 | purchaseUnitPrice | decimal(18,2) | NO | None |   |
| tblreceiptdetail6 | tblPartReceipt6id | int | NO | None |   |
| tblreceiptdetail6 | tblSparePart6id | int | NO | None |   |
| tblrepairorder6 | id | int | NO | None | auto_increment  |
| tblrepairorder6 | repairOrderCode | varchar(30) | NO | None |   |
| tblrepairorder6 | createTime | timestamp(6) | NO | None |   |
| tblrepairorder6 | completionTime | timestamp(6) | YES | None |   |
| tblrepairorder6 | vehicleCondition | varchar(255) | YES | None |   |
| tblrepairorder6 | repairDescription | varchar(255) | YES | None |   |
| tblrepairorder6 | repairResult | varchar(255) | YES | None |   |
| tblrepairorder6 | repairStatus | varchar(255) | NO | None |   |
| tblrepairorder6 | tblVehicle6id | int | NO | None |   |
| tblrepairorder6 | tblCustomer6tblMember6id | int | NO | None |   |
| tblrepairorder6 | tblEmployee6tblMember6id | int | NO | None |   |
| tblrepairorder6 | tblAppointment6id | int | YES | None |   |
| tblservice6 | id | int | NO | None | auto_increment  |
| tblservice6 | serviceCode | varchar(30) | NO | None |   |
| tblservice6 | serviceName | varchar(255) | NO | None |   |
| tblservice6 | description | varchar(255) | YES | None |   |
| tblservice6 | unitPrice | decimal(18,2) | NO | None |   |
| tblserviceusage6 | id | int | NO | None | auto_increment  |
| tblserviceusage6 | usageTime | timestamp(6) | NO | None |   |
| tblserviceusage6 | saleUnitPrice | decimal(18,2) | NO | None |   |
| tblserviceusage6 | note | varchar(255) | YES | None |   |
| tblserviceusage6 | tblService6id | int | NO | None |   |
| tblserviceusage6 | tblRepairOrder6id | int | NO | None |   |
| tblserviceusage6 | tblEmployee6tblMember6id | int | NO | None |   |
| tblsparepart6 | id | int | NO | None | auto_increment  |
| tblsparepart6 | partCode | varchar(30) | NO | None |   |
| tblsparepart6 | partName | varchar(255) | NO | None |   |
| tblsparepart6 | description | varchar(255) | YES | None |   |
| tblsparepart6 | unit | varchar(50) | NO | None |   |
| tblsparepart6 | technicalSpec | varchar(255) | YES | None |   |
| tblsparepart6 | unitPrice | decimal(18,2) | NO | None |   |
| tblsparepart6 | image | varchar(255) | YES | None |   |
| tblsparepart6 | tblPartBrand6id | int | NO | None |   |
| tblsparepart6 | tblCategory6id | int | NO | None |   |
| tblsupplier6 | id | int | NO | None | auto_increment  |
| tblsupplier6 | supplierName | varchar(255) | NO | None |   |
| tblsupplier6 | phone | varchar(20) | NO | None |   |
| tblsupplier6 | email | varchar(255) | YES | None |   |
| tblsupplier6 | address | varchar(255) | YES | None |   |
| tblvehicle6 | id | int | NO | None | auto_increment  |
| tblvehicle6 | licensePlate | varchar(20) | NO | None |   |
| tblvehicle6 | vehicleModel | varchar(255) | NO | None |   |
| tblvehicle6 | currentMileage | int | YES | None |   |
| tblvehicle6 | manufactureYear | int | YES | None |   |
| tblvehicle6 | tblCustomer6tblMember6id | int | NO | None |   |
| tblvehicle6 | tblVehicleBrand6id | int | NO | None |   |
| tblvehiclebrand6 | id | int | NO | None | auto_increment  |
| tblvehiclebrand6 | name | varchar(25) | NO | None |   |
| vwinvoice6 | id | int | NO | 0 |   |
| vwinvoice6 | invoiceCode | varchar(30) | NO | None |   |
| vwinvoice6 | issueDate | timestamp(6) | NO | None |   |
| vwinvoice6 | tblRepairOrder6id | int | NO | None |   |
| vwinvoice6 | paymentAmount | decimal(18,2) | YES | None |   |
| vwinvoice6 | paymentTime | timestamp(6) | YES | None |   |
| vwinvoice6 | paymentMethod | varchar(255) | YES | None |   |
| vwinvoice6 | paymentStatus | varchar(255) | NO | UNPAID |   |
| vwinvoice6 | transactionReference | varchar(100) | YES | None |   |
| vwinvoice6 | tblEmployee6tblMember6id | int | YES | None |   |
| vwinvoice6 | totalAmount | decimal(51,2) | NO | 0.00 |   |
| vwsparepart6 | id | int | NO | 0 |   |
| vwsparepart6 | partCode | varchar(30) | NO | None |   |
| vwsparepart6 | partName | varchar(255) | NO | None |   |
| vwsparepart6 | description | varchar(255) | YES | None |   |
| vwsparepart6 | unit | varchar(50) | NO | None |   |
| vwsparepart6 | technicalSpec | varchar(255) | YES | None |   |
| vwsparepart6 | unitPrice | decimal(18,2) | NO | None |   |
| vwsparepart6 | image | varchar(255) | YES | None |   |
| vwsparepart6 | tblPartBrand6id | int | NO | None |   |
| vwsparepart6 | tblCategory6id | int | NO | None |   |
| vwsparepart6 | stockQuantity | decimal(33,0) | NO | 0 |   |

## Foreign keys

| Bảng.cột | Parent | UPDATE / DELETE |
|---|---|---|
| tblappointment6.tblAppointmentSlot6id | tblappointmentslot6.id | NO ACTION/NO ACTION |
| tblappointment6.tblCustomer6tblMember6id | tblcustomer6.tblMember6id | NO ACTION/NO ACTION |
| tblappointment6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblappointment6.tblVehicle6id | tblvehicle6.id | NO ACTION/NO ACTION |
| tblcustomer6.tblMember6id | tblmember6.id | NO ACTION/NO ACTION |
| tblemployee6.tblMember6id | tblmember6.id | NO ACTION/NO ACTION |
| tblinvoice6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblinvoice6.tblRepairOrder6id | tblrepairorder6.id | NO ACTION/NO ACTION |
| tblpartreceipt6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblpartreceipt6.tblSupplier6id | tblsupplier6.id | NO ACTION/NO ACTION |
| tblpartusage6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblpartusage6.tblRepairOrder6id | tblrepairorder6.id | NO ACTION/NO ACTION |
| tblpartusage6.tblSparePart6id | tblsparepart6.id | NO ACTION/NO ACTION |
| tblreceiptdetail6.tblPartReceipt6id | tblpartreceipt6.id | NO ACTION/NO ACTION |
| tblreceiptdetail6.tblSparePart6id | tblsparepart6.id | NO ACTION/NO ACTION |
| tblrepairorder6.tblAppointment6id | tblappointment6.id | NO ACTION/NO ACTION |
| tblrepairorder6.tblCustomer6tblMember6id | tblcustomer6.tblMember6id | NO ACTION/NO ACTION |
| tblrepairorder6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblrepairorder6.tblVehicle6id | tblvehicle6.id | NO ACTION/NO ACTION |
| tblserviceusage6.tblEmployee6tblMember6id | tblemployee6.tblMember6id | NO ACTION/NO ACTION |
| tblserviceusage6.tblRepairOrder6id | tblrepairorder6.id | NO ACTION/NO ACTION |
| tblserviceusage6.tblService6id | tblservice6.id | NO ACTION/NO ACTION |
| tblsparepart6.tblCategory6id | tblcategory6.id | NO ACTION/NO ACTION |
| tblsparepart6.tblPartBrand6id | tblpartbrand6.id | NO ACTION/NO ACTION |
| tblvehicle6.tblCustomer6tblMember6id | tblcustomer6.tblMember6id | NO ACTION/NO ACTION |
| tblvehicle6.tblVehicleBrand6id | tblvehiclebrand6.id | NO ACTION/NO ACTION |

## Index/UNIQUE

| Bảng | Unique | Cột theo thứ tự |
|---|---|---|
| tblappointment6 | True | activeSlotId |
| tblappointment6 | True | appointmentCode |
| tblappointment6 | True | id |
| tblappointment6 | False | tblAppointmentSlot6id |
| tblappointment6 | False | tblCustomer6tblMember6id |
| tblappointment6 | False | tblEmployee6tblMember6id |
| tblappointment6 | False | tblVehicle6id |
| tblappointmentslot6 | True | id |
| tblappointmentslot6 | True | startTime |
| tblappointmentslot6 | False | startTime, status |
| tblcategory6 | True | categoryName |
| tblcategory6 | True | id |
| tblcustomer6 | True | customerCode |
| tblcustomer6 | True | tblMember6id |
| tblemployee6 | True | employeeCode |
| tblemployee6 | True | tblMember6id |
| tblinvoice6 | True | id |
| tblinvoice6 | True | invoiceCode |
| tblinvoice6 | True | tblRepairOrder6id |
| tblinvoice6 | False | issueDate |
| tblinvoice6 | False | tblEmployee6tblMember6id |
| tblmember6 | True | id |
| tblmember6 | True | username |
| tblpartbrand6 | True | id |
| tblpartbrand6 | True | name |
| tblpartreceipt6 | True | id |
| tblpartreceipt6 | False | tblEmployee6tblMember6id |
| tblpartreceipt6 | False | tblSupplier6id |
| tblpartusage6 | True | id |
| tblpartusage6 | False | tblEmployee6tblMember6id |
| tblpartusage6 | False | tblRepairOrder6id |
| tblpartusage6 | False | tblSparePart6id, tblRepairOrder6id |
| tblreceiptdetail6 | True | id |
| tblreceiptdetail6 | False | tblPartReceipt6id |
| tblreceiptdetail6 | False | tblSparePart6id |
| tblrepairorder6 | True | id |
| tblrepairorder6 | True | repairOrderCode |
| tblrepairorder6 | True | tblAppointment6id |
| tblrepairorder6 | False | tblCustomer6tblMember6id |
| tblrepairorder6 | False | tblEmployee6tblMember6id |
| tblrepairorder6 | False | tblVehicle6id |
| tblservice6 | True | id |
| tblservice6 | True | serviceCode |
| tblserviceusage6 | True | id |
| tblserviceusage6 | False | tblEmployee6tblMember6id |
| tblserviceusage6 | False | tblRepairOrder6id |
| tblserviceusage6 | False | tblService6id, tblRepairOrder6id |
| tblsparepart6 | True | id |
| tblsparepart6 | True | partCode |
| tblsparepart6 | False | tblCategory6id |
| tblsparepart6 | False | tblPartBrand6id |
| tblsupplier6 | True | id |
| tblvehicle6 | True | id |
| tblvehicle6 | True | licensePlate |
| tblvehicle6 | False | tblCustomer6tblMember6id |
| tblvehicle6 | False | tblVehicleBrand6id |
| tblvehiclebrand6 | True | id |
| tblvehiclebrand6 | True | name |

## CHECK

- `tblappointment6`: `(cast(`status` as char charset binary) in (_binary'PENDING',_binary'CONFIRMED',_binary'RECEIVED',_binary'COMPLETED',_binary'CANCELLED'))`.
- `tblappointmentslot6`: `(((`status` = _utf8mb4'HELD') and (`holdCode` is not null) and (`holdExpiresAt` is not null)) or ((`status` <> _utf8mb4'HELD') and (`holdCode` is null) and (`holdExpiresAt` is null)))`.
- `tblappointmentslot6`: `(cast(`status` as char charset binary) in (_binary'AVAILABLE',_binary'HELD',_binary'BOOKED',_binary'CLOSED'))`.
- `tblemployee6`: `(cast(`jobTitle` as char charset binary) in (_binary'MANAGER',_binary'SALE_STAFF',_binary'TECHNICIAN',_binary'WAREHOUSE_STAFF'))`.
- `tblinvoice6`: `(((cast(`paymentStatus` as char charset binary) = _binary'UNPAID') and (`paymentAmount` is null) and (`paymentTime` is null) and (`paymentMethod` is null) and (`transactionReference` is null) and (`tblEmployee6tblMember6id` is null)) or ((cast(`paymentStatus` as char charset binary) = _binary'PAID') and (`paymentAmount` is not null) and (`paymentAmount` >= 0) and (`paymentTime` is not null) and (`paymentMethod` is not null) and (char_length(trim(`paymentMethod`)) > 0) and (`tblEmployee6tblMember6id` is not null)))`.
- `tblmember6`: `(((`username` is null) and (`password` is null) and (`status` is null) and (`profileImage` is null)) or ((`username` is not null) and (char_length(trim(`username`)) > 0) and (`password` is not null) and (char_length(`password`) > 0) and (`status` is not null) and (cast(`status` as char charset binary) in (_binary'ACTIVE',_binary'INACTIVE'))))`.
- `tblpartusage6`: `(`saleUnitPrice` >= 0)`.
- `tblpartusage6`: `(`usedQuantity` > 0)`.
- `tblreceiptdetail6`: `(`purchaseUnitPrice` >= 0)`.
- `tblreceiptdetail6`: `(`receiptQuantity` > 0)`.
- `tblservice6`: `(`unitPrice` >= 0)`.
- `tblserviceusage6`: `(`saleUnitPrice` >= 0)`.
- `tblsparepart6`: `(`unitPrice` >= 0)`.
- `tblvehicle6`: `((`currentMileage` is null) or (`currentMileage` >= 0))`.
- `tblvehicle6`: `((`manufactureYear` is null) or (`manufactureYear` >= 1886))`.

activeSlotId GENERATED STORED nullable UNIQUE: PENDING/CONFIRMED/RECEIVED → physical tblAppointmentSlot6id, COMPLETED/CANCELLED → NULL; không NOW/token trong generated. AppointmentSlot hold độc lập bằng token + NOW(6)+3 phút. Consume + ownership lock + valid placeholder INSERT + generated ID + final LH UPDATE + readback cùng transaction. Rollback giữ nguyên hold khi ghi thất bại, không mã tạm ra ngoài, không transaction chờ khách.

UNPAID bắt buộc toàn nhóm thanh toán NULL; PAID bắt amount/time/method/cashier IS NOT NULL, amount>=0, method không trống, reference được NULL. CHECK so sánh binary cho trạng thái chuẩn viết hoa, tránh collation/Java bất nhất và SQL UNKNOWN. InvoiceDAO compareTo total/paid chính xác.

Tiền bảy field BigDecimal, setter scale2 UNNECESSARY, getBigDecimal, cộng/nhân/compareTo BigDecimal, không double/Double biểu diễn tiền. Hai module không có nghiệp vụ ghi đơn giá/thanh toán; chỉ đọc các cột tiền DECIMAL. Không thêm ghi tài chính ngoài scope. Các DAO ghi xe/lịch chỉ ghi field tương ứng, không chuyển tiền qua double. Fixture money dùng Decimal/SQL DECIMAL.

Date helper đã chuyển thành private tại nơi dùng; tất cả dùng DAO6.zone() Việt Nam, connection SET +07:00. Bộ lọc [đầu ngày bắt đầu, đầu ngày sau kết thúc), issueDate và PAID, lịch sử giá. Lifetime đếm mọi dòng, không lọc kỳ/PAID. Tổng toàn tập trước slice20, ORDER BY revenue DESC/code ASC/type ASC. Invoice nạp đầy đủ dòng theo order, không dùng kỳ báo cáo để cắt invoice.

Migration V3 của lượt trước và backup/receipt vẫn được giữ trong .local/backups; không chạy lại migration trong lượt refactor này. Quản lý schema ngoài DB bằng .local/schema-state.json và đối chiếu metadata type/null/default/precision/generated/index/FK/CHECK/view dependencies, không tạo bảng kỹ thuật. DDL/backup cũ có implicit commit, không hứa rollback migration bằng một transaction.
