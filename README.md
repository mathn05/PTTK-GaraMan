# GaraMan6

Project Java web thực tế tại `C:\Users\admin\Documents\tai lieu nam 4\PTTK\BTL\garaman`. Đã build WAR và chạy **trực tiếp trên Windows 10**, không dùng WSL/cloud/Docker để thay kết quả máy đích. Kiến trúc Entity → DAO/JDBC → Servlet → JSP/JSTL, không Spring/JPA.

**Bản hiệu lực — cách A: Servlet đóng gói:** [hồ sơ bàn giao](docs/final/README.md), [bảng sửa sơ đồ cụ thể](docs/final/02_Bang_thay_doi_thiet_ke.md), [Scenario module 1](docs/final/Scenario_V3_Module1.md), [Scenario module 2](docs/final/Scenario_V3_Module2.md). Tài liệu cũ ngoài docs/final được giữ làm lịch sử, không dùng để phủ định quyết định mới.

Đã refactor cách viết hai module để dễ trình bày: [file sửa, trước/sau và hướng dẫn giải thích](docs/final/08_File_thay_doi.md), [truy vết65 bước đặt lịch](docs/final/Scenario_V3_Ky_thuat_Module1.md), [truy vết84 bước báo cáo](docs/final/Scenario_V3_Ky_thuat_Module2.md). Giữ nguyên nghiệp vụ, API, CSDL và control giao diện.

## Mở ứng dụng hiện đang chạy

**http://localhost:8080/garaman6/login**

| Vai trò | Tên đăng nhập | Mật khẩu demo local |
|---|---|---|
| Khách hàng | `customer6` | `GaraMan6@Demo` |
| Quản lý | `manager6` | `GaraMan6@Demo` |
| Khách hàng khác để kiểm thử ownership | `customer_other6` | `GaraMan6@Demo` |

Tài khoản `inactive6` cùng mật khẩu bị chặn. DB lưu PBKDF2-HMAC-SHA256, salt riêng, 600.000 vòng; không chọn role giả. Tài khoản DB `garaman_app` khác các tài khoản đăng nhập ứng dụng.

Demo báo cáo: **01/10/2026–31/10/2026**, loại Tất cả. Tổng kỳ vọng **5.741.000 VNĐ**, 27 đối tượng, 20 dòng/trang. Dịch vụ 2.301.000, phụ tùng 3.440.000. Hóa đơn HD0001: 2 dịch vụ + 2 phụ tùng, tổng 3.600.000; bán hàng Lê Thị Lan, thu tiền Vũ Thu Ngân. Xem [bảng expected](sql/seed-expected.json). Lịch hẹn dùng ngày thực tế của máy và slot tương lai trong 30 ngày; hiện có từ ngày 12/11/2026. Nên chọn ngày mai để thấy đủ 8 khung giờ.

## Chức năng

- Khách: trang chủ → đặt lịch, hồ sơ readonly, chọn xe/ngày/giờ, nội dung, giữ chỗ 3 phút, chỉnh sửa/release, xác nhận cuối, lịch PENDING với mã tăng dần và nhân viên NULL; chống tranh chấp và double-submit.
- Thêm xe: hãng/dòng/biển, năm tùy chọn; chuẩn hóa biển và unique toàn hệ thống, mã X dẫn xuất từ ID, Km chưa biết là NULL; VehicleServlet6 kiểm tra và tạo Vehicle6 rồi gọi DAO lưu, redirect về form giữ dữ liệu. BookingServlet6 tạo Appointment6 tạm sau khi hold, AppointmentServlet6 dùng lại draft từ Map context/tab của session để xác nhận.
- Quản lý: menu → bộ lọc → thống kê SERVICE/PART/ALL, doanh thu giảm dần, tổng toàn kỳ, phân trang; chi tiết chung hai nhánh, lifetime và lịch sử lọc; hóa đơn toàn bộ và Back giữ context.
- Đã bỏ phần đánh giá. Hóa đơn bổ sung **nhân viên bán hàng** và **nhân viên thu tiền** lấy từ DB. Đăng nhập/đăng xuất, role, CSRF, escape HTML, lỗi hợp lệ và lỗi DB được xử lý thật.

## Phiên bản đã chạy trên máy này

| Thành phần | Kết quả kiểm tra |
|---|---|
| OS | Windows 10 Pro 10.0.19045, x64 |
| JDK/Javac | Eclipse Temurin 21.0.10+7-LTS, compiler release21 |
| Maven / Wrapper | 3.10.0 / 3.3.4, `mvnw.cmd` và `mvnw` trong project |
| Tomcat | 10.1.61, Servlet6 / JSP3.1 / Jakarta |
| MySQL server thật | `SELECT VERSION()` → 9.4.0, cổng3306, session +07:00 |
| Connector/J | 9.4.0 |
| JSTL | API3.0.2 / implementation3.0.1 |
| UI test | Chrome cài trên Windows, Playwright1.65.1 / Python3.12 |

Đã tải Maven/Tomcat từ Apache và xác minh SHA-512. Các version pin trong pom/wrapper/scripts; không coi việc truy cập trang tải là bằng chứng chạy server. Log và kiểm thử V3 thực tế ở [kết quả kiểm thử cuối](docs/final/06_Ket_qua_kiem_thu.md).

## Chạy lại trên Windows / IntelliJ

Mở `pom.xml` trong IntelliJ, đặt Project SDK21 và reload Maven. Không cần plugin Tomcat trả phí. Chạy từ PowerShell tại root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Start-GaraMan.ps1
```

Khi vừa sửa code:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Start-GaraMan.ps1 -Build
```

Dừng đúng hai tiến trình riêng của project:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Stop-GaraMan.ps1
```

Script chạy Java/MySQL ẩn cửa sổ, kiểm tra PID/command line của Tomcat trước dừng; không dừng tiến trình lạ. Cổng HTTP mặc định8080, nếu bận chọn8081..8090 và in URL thật; xem `.local/tomcat.json`. MySQL mặc định3306, nếu bận lúc setup chọn3307/3308. `.runtime/mysql/data` là **data directory riêng**, không dùng `C:\ProgramData\MySQL\...`; service MySQL94 có sẵn vẫn được giữ nguyên.

Nếu dựng mới sau khi sao chép source (cần JDK21, Python3.12 và MySQL binary đã cài):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Setup-GaraMan.ps1
# Nếu binary ở chỗ khác:
powershell -ExecutionPolicy Bypass -File .\scripts\Setup-GaraMan.ps1 -MySqlBin 'D:\MySQL\bin'
```

Setup tải/checksum công cụ, cài thư viện kiểm thử, khởi tạo instance riêng, cấp quyền, nạp DDL/seed khi DB rỗng, build/test, chuẩn bị slot rồi chạy Tomcat. Chạy lại giữ dữ liệu, không DROP/reset schema hoặc ghi đè mật khẩu tài khoản thật. Seed SQL hiện cố định tháng10/2026; `scripts/seed_data.py` chỉ dùng khi chủ động chuẩn bị fixture cho **một DB mới** và phải đồng bộ expected, không dùng để cập nhật DB demo đang có.

## Cấu hình và SQL

- `.local/garaman.properties`: JDBC URL/user/password ngẫu nhiên riêng máy, không commit. Mẫu ở [garaman.properties.example](src/main/resources/garaman.properties.example).
- `.local/test.properties`: trỏ schema **garaman6_v3_it**, user `garaman_test`, TEST_MODE=true và TEST_DATABASE trùng target; schema garaman6_it cũ không còn là target mặc định.
- `.local/runtime.json`: credential quản trị chỉ của instance demo riêng; không in/đưa vào repo. App chỉ dùng `garaman_app` với SELECT/INSERT/UPDATE trên garaman6; không root/DDL/DELETE.
- Config ưu tiên system property → environment → file local. Chọn file bằng `-Dgaraman.config=...` hoặc `GARAMAN_CONFIG`. Các key: GARAMAN_DB_URL, GARAMAN_DB_USER, GARAMAN_DB_PASSWORD, GARAMAN_TIME_ZONE; lịch làm việc gồm GARAMAN_SLOT_DAYS, GARAMAN_OPEN_HOUR, GARAMAN_CLOSE_HOUR, GARAMAN_BREAK_START, GARAMAN_BREAK_END, GARAMAN_SLOT_MINUTES.
- Mặc định 08–17, nghỉ12–13, bước60 phút, 30 ngày. Chỉnh config rồi chạy SlotDAO6 với CLI; GET không sinh slot. Seed slot idempotent, không sửa/đè slot bận.
- MySQL Windows chưa nạp timezone tables tên vùng: JDBC diễn giải Asia/Ho_Chi_Minh, không force tên vùng vào server; mỗi connection thực hiện `SET time_zone='+07:00'`. JVM/hiển thị cũng múi giờ Việt Nam.
- [schema.sql](sql/schema.sql): DDL V3 cài mới (18 bảng + 2 view), [seed](sql/seed.sql), [slot fixtures](sql/seed-slots.sql). Payment gộp cột vào Invoice nhưng giữ Payment6 tùy chọn; tiền DECIMAL/BigDecimal; Km/năm nullable; không vehicleCode/CodeSequence/SchemaVersion vật lý; TIMESTAMP(6). `vwInvoice6`/`vwSparePart6` tính tổng/tồn kho. V1 tái dựng và V2 được giữ làm lịch sử, không chạy chuỗi đó để cài V3.

**Migration đã áp dụng trên DB chính garaman6** sau backup/restore/rollback/reapply trên bản sao. Bảng nguồn nguyên vẹn ở `garaman6_archive_20261112_145017_825186_main`, dump/receipt trong `.local/backups/v3-main-20261112_145013_750178`. [Mapping/ràng buộc/quy trình phục hồi](docs/final/03_Anh_xa_DB_va_rang_buoc.md). DDL có implicit commit, không hứa transaction rollback. Không chạy fresh DDL/seed lên DB đang có dữ liệu. Schema verifier đối chiếu đầy đủ metadata thay vì chỉ nhìn marker. SQL gốc GaraMan_CSDL(1).sql vẫn thiếu; bảo toàn đã kiểm với DB Windows thực có trước sửa, không kết luận cho nguồn chưa được cung cấp.

## Build, kiểm thử và hợp đồng

```powershell
.\mvnw.cmd -version
.\mvnw.cmd -B -ntp package
python scripts/browser_test.py
```

Lệnh browser riêng yêu cầu Tomcat test đã được khởi động bằng `python scripts/run_tomcat.py start --test`. Nên chạy trọn bộ, sau khi MySQL project đang chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Test-GaraMan.ps1 -Smoke
```

JUnit thiếu config thì fail rõ, không skip thành pass. Guard actual DATABASE() và HTTP /test-runtime trước ghi, chỉ target garaman6_v3_it; probe chỉ được chèn từ src/test vào WAR test, không có trong WAR chính. Browser/regression dọn dữ liệu trong finally kể cả test fail. Script tổng dùng Tomcat test riêng, dừng đúng Tomcat test ở finally; không SHUTDOWN MySQL để thử lỗi. Lượt refactor cuối: 29 JUnit + 49 browser + 38 regression + 4 guard/cleanup đều đạt. Main có thêm 11 kiểm tra chỉ đọc, dữ liệu nghiệp vụ không đổi qua kiểm tra này.

Kết quả: target/surefire-reports; [evidence cuối](docs/final/evidence/), screenshots desktop/mobile và hồi quy. WAR: **target/garaman6.war**. Log main `.runtime/tomcat-base/logs`, test `.runtime/tomcat-test-base/logs`; MySQL `.runtime/mysql/server.log`.

Sinh lại tài liệu cuối từ source/bytecode (không ghi đè input hay review lịch sử):

```powershell
java -cp 'target/classes;target/garaman6/WEB-INF/lib/*;.runtime/apache-tomcat-10.1.61/lib/*' scripts/ExportContracts.java
python scripts/generate_final_scenarios.py
python scripts/export_final_evidence.py
```

Thiết kế hiệu chỉnh: [hợp đồng đầy đủ](docs/final/01_Hop_dong_lop.md), [bảng thay đổi](docs/final/02_Bang_thay_doi_thiet_ke.md), [DB](docs/final/03_Anh_xa_DB_va_rang_buoc.md), [UML module1](docs/final/design/module1-v3.puml), [UML module2](docs/final/design/module2-v3.puml), [giới hạn còn lại](docs/final/07_Khac_biet_va_gioi_han_con_lai.md). Hai bản V3 bài nộp và hai bản kỹ thuật có mapping tại [mục lục](docs/final/README.md).

## Deploy WAR bằng Tomcat startup.bat ngoài IDE

Đường đã kiểm thử là script Start-GaraMan.ps1 (chạy native Bootstrap ẩn). Có thể tự dùng bản Tomcat10.1 riêng và `startup.bat` theo cách sau; đây là đường thao tác thủ công, không cần IntelliJ Ultimate. Dừng instance project trước khi dùng cùng cổng, chuẩn bị schema/config như trên. Không trỏ manual base vào data/runtime khác.

```powershell
$projectPath = (Get-Location).Path
$env:JAVA_HOME = Split-Path (Split-Path (Get-Command javac).Source)
$env:CATALINA_HOME = Join-Path $projectPath '.runtime\apache-tomcat-10.1.61'
$env:CATALINA_BASE = Join-Path $projectPath '.runtime\tomcat-manual'
New-Item -ItemType Directory -Force -Path $env:CATALINA_BASE | Out-Null
Copy-Item -LiteralPath (Join-Path $env:CATALINA_HOME 'conf') -Destination $env:CATALINA_BASE -Recurse -Force
foreach ($folder in 'webapps','logs','temp','work') { New-Item -ItemType Directory -Force -Path (Join-Path $env:CATALINA_BASE $folder) | Out-Null }
Copy-Item -LiteralPath '.\target\garaman6.war' -Destination (Join-Path $env:CATALINA_BASE 'webapps\garaman6.war')
$env:CATALINA_OPTS = '-Duser.timezone=Asia/Ho_Chi_Minh -Dfile.encoding=UTF-8 "-Dgaraman.config=' + (Join-Path $projectPath '.local\garaman.properties') + '"'
& (Join-Path $env:CATALINA_HOME 'bin\startup.bat')
# Dừng bản manual bằng shutdown.bat với cùng CATALINA_HOME/BASE:
& (Join-Path $env:CATALINA_HOME 'bin\shutdown.bat')
```

Nếu thay WAR mà JSP vẫn là bản cũ vì timestamp ZIP reproducible, dừng Tomcat và xóa **chỉ** cache generated `work\Catalina\localhost\garaman6` trong manual base đã kiểm tra đường dẫn. Script start của project đã làm bước này an toàn. Không công bố production hoặc module ngoài phạm vi hai luồng.

Kiến trúc hiện tại không có lớp helper nghiệp vụ riêng hay JSP xử lý trung gian. ID/count theo thiết kế là Integer; ID chưa lưu null; so sánh giá trị Integer dùng Objects.equals. Schema không đổi trong lượt refactor cách A; dữ liệu phát sinh đồng thời được bảo toàn. Chi tiết chuyển trách nhiệm và V3 mới ở docs/final.
