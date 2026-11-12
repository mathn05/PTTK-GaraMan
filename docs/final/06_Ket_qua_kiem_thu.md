# Kết quả sửa kiến trúc A và kiểm thử — Windows thực

Đã chuyển đóng gói về Servlet, phân bổ trách nhiệm trước khi xóa9 lớp helper/CLI và2 JSP. Không thêm lớp thay tên, không thêm màn hình. Mã chính hiện47 lớp; code/source/diagram/class contracts đồng bộ. [Bảng chuyển trách nhiệm và ô cần sửa](02_Bang_thay_doi_thiet_ke.md).

Lệnh thực tế:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/Test-GaraMan.ps1 -Smoke
```

Script thực hiện `mvnw.cmd -B -ntp clean package` → prepare slots --test bằng SlotDAO6 CLI → Tomcat test riêng → browser → regression → guard/cleanup → smoke → finally dừng đúng Tomcat test. Lượt cuối 2026-11-12T17:34:55+0700, exit0, 61.325 giây; [log](evidence/full-verification.log), [kết quả lệnh](evidence/full-verification.json). Không SHUTDOWN MySQL để thử outage.

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
| Bảo toàn input/history | 112 file giữ nguyên SHA so với checkpoint, gồm ảnh lớp mới17:19 | [Manifest](evidence/snapshot-after.json) |

Các ca được chạy: 12 connection tranh hold,8 confirm tranh cùng token,5 HTTP submit cùng context chỉ một lịch; token sai/hết hạn, ownership sai rollback consume, final-code collision rollback slot/placeholder, nhiều tab/edit/release/PRG. Borrowed con không bị close/commit hộ.

Kiểu dữ liệu: field và method reflection kiểm Integer/BigDecimal/Date/arrays/parameter names; VehicleBrandDAO6.getVehicleBrands trả VehicleBrand6[]. ID trước lưu null/getVehicleCode null-safe, ID10001 → X10001. HTTP tạo fixture customer/vehicle >127 và kiểm ownership qua hai graph object độc lập, xác nhận cùng draft thành công; usageId>127 lookup invoice thành công nhờ Objects.equals. Count không có dữ liệu trả Integer0.

NULL Km/0/Km biết roundtrip, nullable năm; input giả customer/id/Km/code không được mass-assign. Bảy field tiền BigDecimal, setter scale2 UNNECESSARY, không double trung gian. Phần lẻ và số lớn vượt miền chính xác double kiểm cả JDBC/model/report và JSP: unit `9007199254740991.01`, invoice `9007199257840991.01`, total report `9007199259981991.01`; ảnh [tiền lớn](evidence/regression/large-money-fixed.png). Hai module không có DAO ghi đơn giá/thanh toán, không xây thêm nghiệp vụ tiền ngoài scope.

Payment folded UNPAID/PAID, từng trường NULL bắt buộc bị CHECK từ chối, Account all-null/partial account, binary status uppercase, generated activeSlotId INSERT/đổi đủ5 status/unique chống ghi SQL trực tiếp. Timestamp6 microseconds/instant với JVM UTC+DB07; issueDate cuối kỳ .999999 và usageTime khác ngày vẫn đúng.

Validation brand abc/rỗng/âm/0/id không tồn tại/injection đều400 và escape input; unknown action400. Invoice usageId rỗng/abc/0/âm400, sai item/kỳ/notfound404. Đồng hồ client Date.now lệch ±10 phút không khóa hold còn hiệu lực; rút expiry DB thật, UI hết TTL và server409 không INSERT. Đổi ngày kiểm fetch thật/DOM/DATE(slot), giữ bản vá form.action.

Báo cáo SERVICE/PART/ALL dùng giá lịch sử, count dòng, PAID/issueDate, tổng toàn tập5.741.000 với fixture tháng10/2026; service2.301.000/part3.440.000,27 đối tượng/29 usages, trang20. Lifetime DV0001=5/PT0005=3 độc lập2 history trong kỳ. HD0001 đủ2 service+2 part; người bán Lê Thị Lan/người thu Vũ Thu Ngân tách biệt, không rating. Back giữ context.

Cleanup test finally so sánh8 bảng gồm member/customer mới cho ca ID lớn, xóa đúng fixture/dependent records, không reset counter. Checkpoint main không được restore để đè hoạt động đồng thời: LH0006 tạo17:24 trong lúc làm việc,5 lịch cũ giữ nguyên,16 bảng khác không đổi, chỉ slot của lịch mới đổi BOOKED. [Đối chiếu](evidence/main/concurrent-activity.json).

**Main:** đã deploy WAR mới bằng `python scripts/run_tomcat.py start` và chạy `python scripts/verify_main_readonly.py --main`; không DDL/migration/seed trong lượt này. Main http://localhost:8080/garaman6/login; MySQL3306 tiếp tục chạy, test8082 đã dừng. Schema main/test hash `8fcec9478e621e8776faa9c1695bb9040a1fb42b81ef9457fa343a3e44425e02` không đổi so với checkpoint. [Runtime cuối](evidence/final-runtime.json).

Source SHA `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`, application source SHA `9117444a4dff2904ccd9832e5a40e7bf571abc36def764775524bc6d668042c7`; bytecode/47 classes từ clean build, WAR SHA trong evidence. Các lỗi trong giai đoạn chuyển API (test còn int.class/tham chiếu helper) đã sửa và clean build/test lại, không coi lần fail là pass. [Giới hạn còn lại](07_Khac_biet_va_gioi_han_con_lai.md) gồm D055, TIMESTAMP2038, nguồn thiếu và hiệu năng/cluster/crash chưa chứng minh.
