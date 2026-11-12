# Ánh xạ CSDL dự kiến

Schema mới garaman6 trên instance project riêng, không tác động data directory/service MySQL94. SQL V1 gốc không được cung cấp: xem SOURCE_MANIFEST.

Member6/Account6 → tblMember6, cột account gộp, acc.id=Member.id. Customer/Employee shared PK memberId → Member.id; vai trò employee qua jobTitle. Không bảng Manager/SaleStaff/Technician/RevenueStats/Account.

Vehicle.cus→customerId→Customer.memberId; brand→vehicleBrandId. VehicleCode UNIQUE thêm ở V2; manufactureYear NULL→Integer. Appointment.cus→customerId; vehicle→vehicleId; ss→saleStaffId nullable; slot→appointmentSlotId. activeSlotId generated chỉ cho trạng thái hoạt động, UNIQUE, cho phép tái sử dụng slot sau hủy. Slot.startTime UNIQUE ở V2; CHECK HELD cần token+expiry, trạng thái khác xóa cả hai.

RepairOrder.cus/vehicle/ss/appointment→customerId/vehicleId/saleStaffId/appointmentId. Usage.order→repairOrderId; ServiceUsage.service→serviceId; PartUsage.sparePart→sparePartId; tech→technicianId. Invoice.order→repairOrderId UNIQUE; Invoice.payment→paymentId UNIQUE nullable; Payment.ss→saleStaffId. SparePart.brand/cat→partBrandId/categoryId. View vwSparePart6: tổng receiptQuantity trừ usedQuantity, không cột stockQuantity; vwInvoice6: SUM service prices + SUM quantity*part price, không cột totalAmount.

SQL DECIMAL(18,2), tính tổng không JOIN hai collection cùng cấp. Public money double chuyển từ ResultSet.getBigDecimal().doubleValue(); định dạng tối đa 2 số thập phân, không mất phần lẻ. Thời gian DATETIME(6), JDBC session +07:00, nghiệp vụ Asia/Ho_Chi_Minh. Báo cáo issueDate nửa mở và Payment.status=PAID; lifetime không lọc. Usage.order ở history chỉ hydrate phần order/cus/vehicle, không nạp collections ngược. Invoice hydrate đầy đủ hai collections, tech, brand/category, payment và nhân viên bán hàng.

## Field → column hoàn chỉnh trong phạm vi

Các nhóm field cùng tên dưới đây ánh xạ trực tiếp 1:1, giữ nguyên kiểu/naming ở CONTRACTS_GENERATED. DDL thực tế và mọi PK/FK/unique/check/nullability được xuất từ SHOW CREATE TABLE tại [schema-metadata.json](schema-metadata.json).

| Entity.field | Nguồn SQL / nullable |
|---|---|
| Member.id/fullName/phone/email/address | tblMember6 cột cùng tên; email/address nullable |
| Member.acc | object có thể null nếu username null; account không có bảng riêng |
| Account.id | tblMember6.id, định danh hàng Member sở hữu |
| Account.username/password/profileImage/status | tblMember6 cột cùng tên; username/password/image nullable; status NOT NULL |
| Customer.customerCode | tblCustomer6.customerCode unique, PK memberId là inherited Member.id |
| Employee.employeeCode/jobTitle | tblEmployee6 cột cùng tên; PK memberId là inherited Member.id |
| Manager/SaleStaff/Technician | không field/cột riêng; jobTitle phân subtype, không bảng role |
| VehicleBrand.id/name; PartBrand.id/name; Category.id/categoryName | tblVehicleBrand6 / tblPartBrand6 / tblCategory6 cột cùng tên |
| Vehicle.id/vehicleCode/licensePlate/vehicleModel/currentMileage/manufactureYear | tblVehicle6 cột cùng tên; năm nullable, code/plate unique, km default0 |
| Vehicle.cus/brand | customerId / vehicleBrandId NOT NULL FK |
| AppointmentSlot.id/startTime/holdCode/holdExpiresAt/status | tblAppointmentSlot6 cột cùng tên; token/expiry chỉ nonnull khi HELD; startTime unique |
| Appointment.id/appointmentCode/requestDescription/createAt/status | tblAppointment6 cột cùng tên; code unique |
| Appointment.ss/vehicle/slot/cus | saleStaffId NULL / vehicleId / appointmentSlotId / customerId NOT NULL FK |
| Appointment active uniqueness | activeSlotId generated status PENDING/CONFIRMED/RECEIVED; nullable unique, không thêm Java field |
| Service.id/serviceCode/serviceName/description/unitPrice | tblService6 cột cùng tên; description nullable, code unique |
| SparePart.id/partCode/partName/description/unit/technicalSpecifications/unitPrice/image | tblSparePart6 cột cùng tên; description/spec/image nullable; code unique |
| SparePart.brand/cat | partBrandId/categoryId NOT NULL FK |
| SparePart.stockQuantity | vwSparePart6.stockQuantity, receipt SUM - usage SUM; không cột lưu |
| RepairOrder.id/repairOrderCode/createTime/completionTime/vehicleCondition/repairDescription/repairResult/repairStatus | tblRepairOrder6 cột cùng tên; completionTime/condition/description/result nullable |
| RepairOrder.vehicle/cus/ss/appointment | vehicleId/customerId/saleStaffId NOT NULL; appointmentId nullable FK |
| RepairOrder.serviceUsage/partUsage | mảng từ tblServiceUsage6/tblPartUsage6 theo repairOrderId; history order không hydrate collections ngược |
| ServiceUsage.id/usageTime/saleUnitPrice/note | tblServiceUsage6 cột cùng tên; note nullable |
| ServiceUsage.service/tech/order | serviceId/technicianId/repairOrderId NOT NULL FK |
| PartUsage.id/usedQuantity/usageTime/saleUnitPrice/note | tblPartUsage6 cột cùng tên; quantity>0; note nullable |
| PartUsage.sparePart/tech/order | sparePartId/technicianId/repairOrderId NOT NULL FK |
| Payment.id/paymentAmount/paymentTime/paymentMethod/status/transactionReference | tblPayment6 cột cùng tên; time/method/reference nullable |
| Payment.ss | saleStaffId NOT NULL FK, jobTitle SALE_STAFF |
| Invoice.id/invoiceCode/issueDate/order/payment | tblInvoice6 id/code/date/repairOrderId unique NOT NULL/paymentId unique nullable |
| Invoice.totalAmount | vwInvoice6.totalAmount = tách aggregate hai nguồn usage rồi cộng |
| RevenueStats.itemCode/itemName/itemType/usageCount/totalRevenue | SQL aliases; serviceCode/name hoặc partCode/name; literal SERVICE/PART; COUNT(*) và SUM lịch sử; không bảng/id |

Ở mapper Invoice, mọi quan hệ phục vụ UI được hydrate; Usage.order nông có phiếu/khách/xe/nhân viên, arrays rỗng để tránh chu trình. Ảnh phụ tùng chỉ render đường /assets/ hợp lệ. FK Employee được mapper xác thực subtype kỹ thuật viên/bán hàng khi hydrate.
