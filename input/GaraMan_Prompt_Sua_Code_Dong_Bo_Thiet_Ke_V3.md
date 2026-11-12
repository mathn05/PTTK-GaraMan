# Prompt sửa code, đồng bộ thiết kế và sinh lại Scenario V3 GaraMan

Tôi đã cập nhật ảnh CSDL `grm_db(9).jpg` vào input của project. Hãy làm việc trực tiếp trên project, thực hiện sửa chữa và kiểm chứng, không dừng ở báo cáo đề xuất. Phạm vi vẫn là hai module đặt lịch hẹn và thống kê doanh thu dịch vụ/phụ tùng, cùng đăng nhập và thành phần hỗ trợ cần thiết.

## 1. Nguồn chuẩn và nhiệm vụ

Đọc ảnh CSDL mới, các biểu đồ lớp thiết kế cuối trong input, yêu cầu trong prompt này và ba báo cáo review: `01_Doi_chieu_thiet_ke_va_code.md`, `02_Truy_vet_Scenario_V3.md`, `03_Kiem_chung_va_de_xuat_cap_nhat.md`. Đọc tài liệu bài giảng và LAB thực hành khi cập nhật thiết kế/V3.

Ưu tiên: chỉ dẫn trực tiếp trong prompt này → ảnh CSDL mới cùng các hiệu chỉnh được ghi dưới đây → biểu đồ lớp thiết kế cuối → tài liệu quyết định cũ còn hiệu lực. Không lấy Q07/Q08/Q17 cũ làm căn cứ phủ định yêu cầu mới. Không lấy tài liệu sinh từ code làm bằng chứng độc lập rằng code đúng thiết kế cũ.

Ảnh mới không thể hiện đầy đủ CHECK, DEFAULT, công thức generated và chính sách transaction. Những ràng buộc đó phải được triển khai trong DDL/code và ghi trong chú thích thiết kế. Đừng kết luận thiếu hình vẽ công thức là không thể triển khai.

Chụp trạng thái ban đầu bằng Git nếu đã có repository hoặc manifest hash nếu chưa có. Giữ các báo cáo review cũ làm lịch sử. Không sửa các file input gốc để xóa khác biệt; tạo thiết kế hiệu chỉnh riêng, ghi rõ phiên bản thay thế.

## 2. Các quyết định CSDL bắt buộc

### 2.1. Payment gộp vào Invoice

- Không có bảng vật lý `tblPayment6` trong schema mục tiêu. Không có `paymentId` trên Invoice.
- Các cột thanh toán nằm trong `tblInvoice6` như ảnh: paymentAmount, paymentTime, paymentMethod, paymentStatus, transactionReference và FK nhân viên thu tiền.
- Hiệu chỉnh rõ đối với ảnh: `paymentAmount` dùng DECIMAL(18,2), không dùng DOUBLE; FK nhân viên thu tiền được NULL trước khi thanh toán nhưng vẫn có FK.
- paymentStatus NOT NULL DEFAULT UNPAID, dùng UNPAID/PAID. Khi UNPAID, nhóm dữ liệu thanh toán để NULL. Khi PAID, amount/time/method/nhân viên thu tiền phải có, amount không âm; transactionReference có thể NULL với tiền mặt. Bổ sung CHECK tường minh phù hợp quy tắc, không để SQL UNKNOWN làm lọt dữ liệu thiếu.
- Giữ entity `Payment6` nếu thiết kế lớp có nó. InvoiceDAO dựng Payment6 từ các cột của Invoice; Invoice6.payment có thể NULL khi chưa thanh toán. Gộp bảng không buộc xóa lớp.
- Người bán trên RepairOrder và người thu tiền trên Invoice là hai vai trò/FK khác nhau; không gộp nhầm.
- Nếu mỗi RepairOrder tối đa một Invoice, bổ sung UNIQUE cho FK RepairOrder trong Invoice. Nếu mỗi Appointment tối đa một RepairOrder, FK Appointment trong RepairOrder nullable và UNIQUE, cho phép phiếu sửa chữa không xuất phát từ lịch hẹn.

### 2.2. Km nullable

- `Vehicle6.currentMileage` dùng Integer; SQL INT NULL DEFAULT NULL; có giá trị thì >= 0.
- Mapper bảo toàn NULL bằng getObject hoặc wasNull; không dùng getInt rồi coi NULL là 0. JDBC ghi SQL NULL khi không biết số Km.
- Form không có ô Km thì thêm xe mới để NULL, không tự ghi 0. Không bắt buộc bổ sung ô Km ngoài thiết kế.
- Không đổi mọi số 0 cũ thành NULL vì không thể biết 0 nào là dữ liệu thật. Ghi giới hạn dữ liệu lịch sử sau migration.
- manufactureYear giữ nullable; kiểm kiểu entity, mapper và DDL tương ứng.

### 2.3. Mã và bảng kỹ thuật

- Không có cột vật lý vehicleCode. Mã X0001 là định dạng từ id; có thể dùng giá trị dẫn xuất/getter hiển thị, phải ghi rõ không persistence. Không cắt số khi id vượt 9999.
- Không có bảng CodeSequence, không tạo thêm bảng kỹ thuật trong schema mục tiêu. Không giữ CodeSequenceDAO6 trong luồng mới nếu không còn nguồn dữ liệu tương ứng.
- Mã nghiệp vụ hiện có của khách, nhân viên, dịch vụ, phụ tùng, lịch hẹn, phiếu sửa chữa và hóa đơn phải bảo toàn. Không đổi mã cũ chỉ để khớp cách định dạng mới.
- Với bản ghi mới cần cấp mã, thiết kế cơ chế an toàn dựa trên ID tự tăng hoặc cách tương đương không cần bảng sequence. Không dùng MAX+1 khi chạy đồng thời. Phải xử lý đầy đủ NOT NULL/UNIQUE trong cả bước INSERT và hoàn tất mã, không chỉ nêu “lấy ID rồi update” trong khi INSERT không hợp lệ. Không trả mã tạm ra ngoài transaction. Kiểm thử concurrency và rollback. Mã tăng dần được phép có khoảng trống, không yêu cầu liên tục tuyệt đối.
- Nếu script hiện tạo tblSchemaVersion6, chuyển việc quản lý sang cơ chế không tạo bảng trong schema ứng dụng; công cụ migration tự tạo bảng lịch sử cũng phải được xem là bảng kỹ thuật. Ghi rõ cơ chế thay thế và cách kiểm chứng schema, không chỉ kiểm tra tồn tại vehicleCode. Không xóa bảng cũ trước khi xác minh việc thay thế và bảo toàn thông tin cần thiết.

### 2.4. Tài khoản và tên cột

- Account là quan hệ tùy chọn 0..1 của Member: username/password/status/profileImage cùng NULL khi chưa có tài khoản; khi có tài khoản thì username/password/status đầy đủ, username unique và trạng thái hợp lệ. Người đăng nhập cần tài khoản ACTIVE. Không bắt Member chưa có tài khoản mang status ACTIVE mặc định.
- Giữ tên bảng/cột theo ảnh mới, gồm cả các FK dài được công cụ sinh. Không tự đổi sang customerId/repairOrderId rồi tuyên bố khớp tên 100%. Khi cần tên ngắn trong Java hoặc SQL alias, lập bảng ánh xạ rõ; đó không phải đổi tên cột vật lý.
- Với các cột thời gian ảnh ghi TIMESTAMP, ưu tiên triển khai đúng kiểu đó, kiểm timezone và precision thật. Nếu cần đổi sang DATETIME(6), phải chỉ ra lý do kỹ thuật, tác động và ghi thành khác biệt thiết kế đề xuất; không âm thầm đổi. Không coi trường Date Java đồng nghĩa duy nhất một kiểu SQL.

## 3. activeSlotId và sức chứa slot

`activeSlotId` là cột GENERATED trong DDL, nullable, UNIQUE. Công thức sinh dựa vào trạng thái và FK slot của chính dòng lịch hẹn:
- Lịch đang chiếm chỗ: nhận FK slot.
- Lịch không còn chiếm chỗ theo quy tắc trạng thái: nhận NULL.

Dùng tên cột vật lý đúng ảnh trong biểu thức. Xác định và tài liệu hóa đầy đủ tập trạng thái từ code/state diagram; không bỏ sót trạng thái làm slot đang bận trở nên khả dụng. Không thêm HOLD token hay thời gian NOW vào biểu thức generated này. Hold là cơ chế riêng ở AppointmentSlot.

Không để activeSlotId chỉ là field Java hay cột thường do code tự gán. Database phải tự bảo đảm công thức và UNIQUE cho mọi đường ghi dữ liệu. Sơ đồ chỉ cần chú thích “generated, unique; active → slot FK; inactive → NULL”.

Giữ sức chứa 1, hold 3 phút theo server/DB, atomic guarded hold, consume + INSERT Appointment cùng transaction, rollback khi lỗi, kiểm tra xe thuộc khách và double-submit. Không mở transaction trong 3 phút chờ khách xác nhận. Không bỏ FK nhân viên chỉ vì nhân viên có thể NULL.

## 4. Sửa các lỗi đã review

- D049: parse/validate selected brand ID tại backend; JSP render lại form không ép raw chuỗi như abc thành số gây HTTP500. Test abc/rỗng/âm/ID không tồn tại và giữ input an toàn.
- D050: action=invoice bắt buộc usageId hợp lệ và >0; không dùng 0 để coi nhầm thành action xem chi tiết. Phân biệt ID sai cú pháp và dữ liệu không tồn tại; không bỏ kiểm tra dòng sử dụng thuộc item/bộ lọc.
- D051: chuyển các thuộc tính tiền liên quan hai module sang BigDecimal, xuyên suốt field/getter/setter/mapper/DAO/tổng hợp/JSP/test. SQL dùng DECIMAL; không đổi qua double trung gian, không new BigDecimal(double). Kiểm tra kiểu attribute và format hiển thị. Quy định scale/rounding ở điểm nghiệp vụ cần làm tròn, không làm tròn để che mất dữ liệu.
- D052: countdown không phụ thuộc tuyệt đối đồng hồ máy khách; dùng TTL/thời gian server và elapsed time phù hợp. Server xác minh hiệu lực cuối cùng. Không khóa xác nhận chỉ vì Date.now của client nhanh/chậm; test clock skew và thời điểm hết hạn thực.
- D053: caption giờ lấy cùng cấu hình sinh slot hoặc bỏ mô tả ghi cứng. Đổi cấu hình không làm giao diện nói sai lịch.
- D054: script test mặc định chạy schema test, có guard trước mọi thao tác ghi; kiểm tra app thật cũng nối đúng test DB. Có cleanup kể cả test fail. Không chạy script mặc định vào DB chính hoặc dừng DB chính để test outage.

Giữ nghiệp vụ thống kê: issueDate, chỉ PAID, giá lịch sử, count số dòng, phân trang 20, doanh thu DESC rồi code ASC và tie-breaker ổn định. Tổng cả tập kết quả, không tổng trang. Lịch sử trong chi tiết theo kỳ báo cáo; lifetime không bị lọc theo kỳ. Hóa đơn hiển thị đủ dòng, giữ ngữ cảnh Quay lại. Không thêm rating.

## 5. Vị trí đóng gói đối tượng

Yêu cầu đặc biệt trước đây của tôi chưa bị hủy: khi lưu dữ liệu từ form, giao diện xử lý phía server gọi thực thể để đóng gói, nhận/giữ đối tượng rồi chuyển yêu cầu qua Servlet để DAO lưu.

Code hiện tạo Appointment6 ở BookingServlet6 và Vehicle6 ở VehicleServlet6, nên chưa đáp ứng vị trí đóng gói này. Hãy xử lý khác biệt một cách thực chất, không viết scenario giả rằng JSP tạo object.

Tìm phương án thay đổi tối thiểu phù hợp cấu trúc hiện tại. Nếu cần JSP xử lý nội bộ hoặc tag/JavaBean hỗ trợ, khai báo rõ thành phần mới và cập nhật thiết kế. JSP phía server khác HTML/JavaScript trình duyệt. Không truyền đối tượng Java sống qua form/URL; dùng request attribute trong cùng request/forward hoặc session đúng scope. JSP xử lý không render output trước forward.

Xác thực role/CSRF/context và lấy dữ liệu đáng tin cậy ở backend trước khi dùng đối tượng; không mass-assign customer/status/staff/id từ client. Không tắt scripting-invalid toàn cục để chèn scriptlet tùy tiện. Giữ ownership/hold/transaction safeguards. Không tạo lại draft lần hai ở xác nhận nếu draft cũ đã tồn tại và vẫn hợp lệ.

Nếu không thể đáp ứng yêu cầu này trong kiến trúc hiện tại mà không đổi đáng kể, nêu chính xác điểm xung đột và phương án cụ thể; tiếp tục hoàn thành các phần độc lập. Không tự coi phương án Servlet tạo object đã được tôi chấp thuận thay cho yêu cầu JSP.

## 6. Migration và kiểm chứng

Không drop/reset/truncate DB người dùng. Chuẩn bị migration và chạy thử trên bản sao/schema test trước. Khi gộp Payment vào Invoice, kiểm tra cardinality, bản ghi mồ côi, trạng thái, FK và sao chép dữ liệu; đối chiếu dữ liệu trước/sau trước khi bỏ cấu trúc cũ. MySQL DDL có thể implicit commit; không hứa rollback toàn bộ migration chỉ bằng một transaction. Không xóa bảng nguồn ngay khi còn dữ liệu hoặc dependency chưa xử lý.

Tự thực hiện sửa source, DDL cài mới, migration, mapper, seed, config và test trong phạm vi cần thiết. Với DB chính, chỉ áp dụng thao tác khi có phương án bảo toàn/khôi phục đã kiểm chứng; nếu chưa đủ điều kiện thì để nguyên DB chính và nêu rõ migration đang chờ, không tuyên bố đã đồng bộ môi trường đó.

Chạy build/JUnit/browser trên schema test; có regression cho các lỗi và quy tắc mới: NULL Km roundtrip, unpaid/paid sau gộp, số tiền lớn và phần lẻ, activeSlotId tự sinh khi INSERT/đổi status, tranh slot/confirm đồng thời, mã không trùng, nhiều tab/edit/expiry, quyền/CSRF và cả hai loại thống kê. Lưu evidence thật; không chỉ sửa expected để làm test pass.

Không yêu cầu triển khai thêm module thanh toán/nhập kho/CRUD ngoài phạm vi; chỉ cần mapping và fixture cần cho hai module.

## 7. Đồng bộ thiết kế và Scenario V3 sau khi sửa

Sau khi code đã ổn định:
1. Sửa lại phân loại D017/D018/D038/D042/D057/D059 theo nguồn chuẩn mới. Báo cáo cũ là hiện trạng lịch sử, không phải nguồn cho phép phủ định yêu cầu mới.
2. Sinh bảng hợp đồng lớp đầy đủ từ source thực tế: package, thuộc tính, kiểu, constructor, tên/thứ tự/kiểu tham số và return. Chỉ đề xuất/cập nhật phần liên quan hai module, không ép cả hệ thống thành 55 lớp triển khai.
3. Tạo bản PlantUML thiết kế hiệu chỉnh riêng cho từng module, bao gồm UI, Servlet, DAO, entity và helper cần thiết; chỉ rõ thay đổi so với hình input, không ghi đè hình gốc. Phân biệt dependency gọi phương thức với association do thuộc tính tham chiếu; không nối mọi lớp với nhau chỉ vì dữ liệu đi qua.
4. Bổ sung bảng mapping entity–DB, chú thích generated/nullable/unique và trách nhiệm đóng gói. Ghi cụ thể những ô tôi cần sửa trên sơ đồ vẽ tay, đặc biệt Integer và BigDecimal. Không yêu cầu tôi tự đoán tên thuộc tính hoặc phương thức.
5. Viết lại Scenario V3 từ source SAU sửa, theo format bài giảng: người dùng thao tác → giao diện gửi yêu cầu → handler → gọi DAO/phương thức → truy vấn/đóng gói → trả kết quả → forward/redirect → hiển thị. Ghi đúng actual caller/return và constructor/setter void. Không tự tạo lời gọi handler vào chính nó khi container chỉ dispatch.
6. Tạo hai mức: bản V3 kỹ thuật có truy vết; bản V3 dùng trong bài báo cáo có nhóm bước đóng gói thuộc tính con/lặp mapper, tập trung giao diện–Servlet–DAO–entity và các hàm nghiệp vụ. Nhóm bước có mapping về bản kỹ thuật; không đảo thứ tự hoặc đổi trách nhiệm cho đẹp. Không bắt bản báo cáo khai triển từng setter/filter/connection thành bước riêng.
7. Module 2 chỉ khai triển hai lượt xem chi tiết SERVICE rồi PART, đánh số liên tục; phần trước/sau chung. Nếu code hỗ trợ ALL thì dùng ALL; nếu không có, mô tả chuyển loại trung thực bằng tham chiếu đoạn chung, không giả danh sách chứa hai loại.

Các file đầu ra nên nằm trong `docs/final/`, tối thiểu gồm: nguồn chuẩn hiệu lực; hợp đồng lớp; thiết kế hiệu chỉnh của hai module; ánh xạ DB/ràng buộc; Scenario V3 bài nộp của hai module; truy vết kỹ thuật; kết quả test; danh sách khác biệt còn lại.

Cuối lượt trả lời ngắn: đã sửa gì trong code/DB test; DB chính đã thay đổi hay chưa; thiết kế cần sửa đúng những thuộc tính/quan hệ/phương thức nào; vị trí đóng gói thực tế sau sửa; kiểm thử nào đã chạy; vấn đề còn lại và link các file. Không công bố “khớp 100%” khi vẫn còn ngoại lệ hoặc thiếu nguồn. Không dừng ở việc đề xuất sửa những phần đã được prompt này cho phép thực hiện.
