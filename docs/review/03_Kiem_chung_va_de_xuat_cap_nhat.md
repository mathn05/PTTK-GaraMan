# Kiểm chứng và đề xuất cập nhật

Review ngày 12/11/2026 theo snapshot **ab485f1be8360b8dc1e1b1c9d9f0151c052f145c0ca8a0c4dc2adff642fe0ffb**. Không áp dụng sửa source/SQL/config/script/test/tài liệu cũ. V3 là hiện trạng, không phải thiết kế mục tiêu.

## 1. Kết quả và môi trường thực sự chạy

| Hạng mục | Kết quả thực tế / bằng chứng |
|---|---|
| Máy | Windows 10 Pro 10.0.19045 amd64, PowerShell 5.1; native java.exe/chrome.exe/mysqld.exe trên máy người dùng. Không chạy cloud/WSL/container. |
| Workspace | C:\Users\admin\Documents\tai lieu nam 4\PTTK\BTL\garaman; không có Git. 147 file gốc được giữ hash: [snapshot-verification.json](evidence/snapshot-verification.json). |
| Java/Maven | Temurin OpenJDK 21.0.10+7; Maven Wrapper 3.10.0. pom release21/-parameters, WAR garaman6.war, jakarta.servlet6.0, JSTL3, Connector/J9.4. |
| DB | SELECT DATABASE(),VERSION(),@@port,@@session.time_zone trên test connection: garaman6_it,9.4.0,3306,+07:00. Live metadata main trong [schema-live.json](evidence/schema-live.json), 23 object, lower_case_table_names=1. |
| MySQL process | Project-owned mysqld trên 127.0.0.1:3306; Windows service MySQL94 đang Stopped, không được bật/tắt trong review. Không nhầm service dừng với DB native process không chạy. |
| Main Tomcat | Instance đã có ở localhost:8080/garaman6/login, PID7568; không stop/redeploy instance này. Kiểm tra cuối 10:49:59 nhận HTTP200, listener8080/3306 còn, listener8082 test đã dừng: [final-runtime-health.json](evidence/final-runtime-health.json). |
| Test Tomcat | Instance riêng tmp/review-runtime/tomcat, Tomcat10.1.61, 127.0.0.1:8082, cùng WAR vừa build nhưng -Dgaraman.config trỏ .local/test.properties. [review-tomcat.log](evidence/review-tomcat.log) xác nhận OS/JVM/Tomcat. Đã dừng đúng process test do review tạo sau mỗi lượt. |
| Build/JUnit | BUILD SUCCESS; 21 test, 0 failure/error/skip. Maven total26.593s; thời gian subprocess29.591s. Kết thúc 2026-11-12T10:00:16+07:00. [log](evidence/maven-package.log), [exit/timing](evidence/build-result.json). |
| Browser/HTTP | Native Chrome157.0.8103.51 headless qua Playwright. 49 kiểm tra cũ pass trên testDB + 6 kiểm tra hành trình SERVICE trước, PART sau pass. [runtime-review.json](evidence/runtime-review.json), [ordered-module2.json](evidence/ordered-module2.json). |
| Probe bổ sung | Bốn probe tái hiện D049–D052. Các kỳ vọng đúng của probe **không đạt**; không gộp thành “toàn bộ test pass”. D053 là phát hiện tĩnh, chưa chạy đổi cấu hình. |
| Phạm vi source review | 55 lớp, JSP/JSPF, JS/CSS, mapping/filter, SQL/migration/seed/live schema, dependency và hai test class; call graph và mọi bước V3 có source evidence trong file02. |

## 2. Lệnh và cách thực hiện

Các lệnh dưới đây đã chạy; đường dẫn dài được giữ trong biến/đối số subprocess, không có secret trên command line hoặc trong báo cáo.

1. Đọc nguồn bằng Get-Content -Encoding UTF8, rg/rg --files, xem ảnh nguồn, trích nội dung/render PDF hướng dẫn; snapshot SHA-256 trước review. Kiểm tra java -version, .\mvnw.cmd -version, Get-CimInstance Win32_OperatingSystem, Get-Service *mysql*, Get-NetTCPConnection. MySQL server identity lấy từ query, không suy từ mysql --version.
2. Chạy **.\mvnw.cmd -B -ntp package** tại workspace. JUnit DatabaseIntegrationTest.targetTestSchema tự đặt garaman.config=.local/test.properties, assert DATABASE()='garaman6_it' trước mọi test ghi; source test:23–30. Build không reset seed/schema.
3. Chạy **javap -p -classpath target/classes** cho 55 tên class. Chạy reflection exporter trong một thư mục Java tạm, chỉ đổi đích báo cáo trong bản tạm sang docs/review/evidence/contracts-reflection.md/.puml; không chạy exporter gốc ghi đè docs/CONTRACTS_GENERATED.md/classes-final.puml.
4. Đọc schema bằng SHOW CREATE TABLE và INFORMATION_SCHEMA trên connection local; chỉ lưu metadata, không dump dữ liệu/credential.
5. Chuẩn bị slot chỉ trong testDB bằng lệnh Java CLI tương đương PowerShell:

~~~powershell
java "-Dgaraman.config=$PWD\.local\test.properties" -Duser.timezone=Asia/Ho_Chi_Minh -cp "target/classes;target/garaman6/WEB-INF/lib/*" garaman.util.SlotSetup6
~~~

6. Dùng Python subprocess.Popen(...,creationflags=CREATE_NO_WINDOW) khởi chạy Java Bootstrap instance test với các đối số dưới đây (catalina.home/base là đường dẫn tuyệt đối tương ứng trong workspace):

~~~text
java -Dcatalina.home=<workspace>\.runtime\apache-tomcat-10.1.61
     -Dcatalina.base=<workspace>\tmp\review-runtime\tomcat
     -Dgaraman.config=<workspace>\.local\test.properties
     -Duser.timezone=Asia/Ho_Chi_Minh -Dfile.encoding=UTF-8
     -cp "<home>\bin\bootstrap.jar;<home>\bin\tomcat-juli.jar"
     org.apache.catalina.startup.Bootstrap start
~~~

7. Harness Python được chạy qua stdin: import scripts/browser_test.py bằng importlib; override **trong bộ nhớ** BASE=http://127.0.0.1:8082/garaman6, OUT=docs/review/evidence/browser và hàm count_appointments để query garaman6_it. Không sửa script trên disk. Lưu 49 check vào runtime-review.json vì gọi main() khi import không đi qua khối __main__ ghi evidence mặc định. Chạy bốn probe trong cùng phạm vi test, rồi restore fixture/cleanup.
8. Một lượt Chrome đọc riêng chạy theo đúng thứ tự mong muốn M2: home → menu → filter ALL → list → SERVICE DV0001 → back list → PART PT0005 → invoice usageId=1 → HD0001, sáu check PASS. Lượt này chỉ login và GET nghiệp vụ, không tạo xe/lịch/usage/payment. Instance test được khởi chạy/dừng riêng, log ở ordered-tomcat.log.
9. So sánh hash 147 file gốc sau khi hoàn tất tài liệu và kiểm tra bảng bằng chứng/đánh số/link/số dòng.

Không chạy scripts/local_runtime.py setup/reset/stop, run_tomcat.py start/stop main, ExportContracts.java bản gốc, export_schema.py/write_evidence.py ghi đè tài liệu cũ hoặc runtime_smoke.py. runtime_smoke.py thực sự có lệnh stop/start DB nên không phù hợp review giữ runtime main. Không chạy nguyên browser_test.py trên target mặc định vì nó ghi main và không tự dọn.

## 3. JUnit: 21 test chạy thật, không skip

19 test dưới đây ở src/test/java/garaman/DatabaseIntegrationTest.java. Fixture dùng garaman6_it; AfterEach dọn các slot/lịch/xe được test tạo, các test tiền/dữ liệu thay đổi dùng finally khôi phục.

| Test | Dòng source | Kiểm chứng thực tế |
|---|---|---|
| authenticateRealCustomerManagerWrongAndInactive | 53 | Đúng subtype customer/manager, sai mật khẩu, inactive và username không có |
| mappingUsesSharedMemberIdAndNullableYear | 59 | Shared Member.id, year NULL, quan hệ customer/brand |
| concurrentHoldOnlyOneConnectionWins | 66 | 12 tác vụ dùng connection riêng tranh một slot; chỉ 1 thắng |
| wrongTokenCannotReleaseOrConsumeAndBorrowedConnectionStaysOpen | 73 | Token sai không release/consume; DAO mượn không đóng caller connection |
| expiredHoldCannotPersistButCanBeReclaimed | 82 | Hết hạn không INSERT lịch; có thể giữ lại bằng token mới |
| expiredHoldAppearsAvailableWithoutGetMutatingDatabase | 89 | GET slot hết hạn chỉ đọc; không tự UPDATE trạng thái |
| concurrentFinalConfirmationCreatesExactlyOneAppointment | 96 | 8 tác vụ xác nhận cùng hold, connection riêng; đúng 1 lịch commit |
| failedOwnershipRollsBackConsumedSlot | 107 | Xe khác khách gây rollback slot đã consume trong tx |
| insertFailureRollsBackSlotAndSequence | 113 | Lỗi INSERT rollback slot và bộ đếm sequence |
| vehicleNormalizationNullableYearDefaultMileageAndDuplicate | 124 | Chuẩn hóa biển, NULL year, mileage0 theo Q08, duplicate toàn hệ thống |
| concurrentVehicleCodesAreDistinct | 132 | 6 tác vụ thêm xe nhận mã duy nhất |
| servicePartAllRevenueMatchIndependentHandCalculation | 138 | Ba loại báo cáo khớp số tính độc lập, không dùng giá hiện tại |
| dateBoundariesUseInvoiceNotUsageOrPaymentTime | 150 | issueDate gồm ngày cuối đến microsecond; usage/payment khác kỳ không đổi căn cứ |
| lifetimeDiffersFromPaidHistoryAndHydratesCustomerVehicleOrder | 158 | Count toàn thời gian khác history PAID trong kỳ; đủ order/customer/vehicle |
| bothUsageTypesResolveFullInvoiceWithSeparateSalespersonAndCashier | 165 | Cả service/part tra đúng invoice, đủ hai mảng, nhân viên hai FK khác nhau |
| paidInvoiceMismatchAndSqlFailureAreExceptionsNotEmptySuccess | 172 | PAID lệch tiền và lỗi SQL ném exception; không biến thành rỗng/thành công |
| pageClampingAndWholeResultTotalsRemainIndependent | 181 | Clamp trang, 20 dòng, tổng toàn tập |
| monetaryFractionsSurviveAggregationAndInvoiceMapping | 185 | Fixture .25 với tiền mức nhỏ giữ được; không chứng minh an toàn mọi DECIMAL |
| equalRevenueAndSameCodeAcrossTypesSortDeterministically | 199 | Tie tiền/code giữa hai loại có itemType tie-breaker |

Hai test tại PasswordAndContractTest.java xác minh PBKDF2 salt/verify và reflection tên/kiểu/mảng/tham số. Tổng kết thực sự của Surefire ở maven-package.log:19+2=21 PASS, không assumption skip. Đây là concurrency **đã chạy**, không chỉ đánh giá tĩnh atomic SQL.

## 4. Browser/HTTP và số liệu đối chiếu

Bộ browser cũ có cả happy path, edit/add vehicle, quyền và lỗi; không phải mỗi check là một testcase JUnit. Kết quả chi tiết:

| # | Kiểm tra sẵn có chạy lại trên testDB | Kết quả |
|---|---|---|
| 1 | Unauthenticated booking redirects to login | PASS |
| 2 | Wrong password receives generic login error | PASS |
| 3 | Login restores previously requested protected booking route | PASS |
| 4 | Customer home renders after successful login | PASS |
| 5 | Customer blocked from manager reports | PASS |
| 6 | POST without CSRF is blocked | PASS |
| 7 | Profile fields are readonly | PASS |
| 8 | Future slots loaded from database | PASS |
| 9 | Changing date keeps entered request and vehicle | PASS |
| 10 | Add vehicle returns to booking with new vehicle selected | PASS |
| 11 | Add vehicle preserves date and request | PASS |
| 12 | Hold form accepts valid ownership and ignores tampered profile | PASS |
| 13 | Confirmation escapes user text | PASS |
| 14 | Confirmation displays hold expiry | PASS |
| 15 | Edit restores selected vehicle and input | PASS |
| 16 | Edit releases chosen slot | PASS |
| 17 | Forged foreign vehicle receives 403 | PASS |
| 18 | Concurrent double-submit returns successful recorded result | PASS |
| 19 | Only one appointment persisted after five submissions | PASS |
| 20 | Refreshing success retains same appointment code | PASS |
| 21 | New tab receives a different booking context | PASS |
| 22 | Unknown booking context is rejected | PASS |
| 23 | Mobile booking has no document overflow | PASS |
| 24 | Manager login opens manager home | PASS |
| 25 | Manager blocked from customer booking | PASS |
| 26 | Out-of-scope customer statistics is disabled | PASS |
| 27 | ALL report matches independent expected total | PASS |
| 28 | Report displays twenty rows and two pages | PASS |
| 29 | Revenue sorted with tyre first | PASS |
| 30 | No customer rating displayed on report | PASS |
| 31 | Second page has remaining seven rows and full total | PASS |
| 32 | Shared detail renders part branch first | PASS |
| 33 | Part lifetime count differs from filtered history | PASS |
| 34 | Part detail has category brand stock specification image | PASS |
| 35 | Invoice lookup redirects to correct invoice code | PASS |
| 36 | Invoice displays all service and part lines | PASS |
| 37 | Invoice has salesperson and cashier from different real employees | PASS |
| 38 | Invoice total uses all historical lines | PASS |
| 39 | Invoice Back preserves part and report context | PASS |
| 40 | Shared detail renders service with no rating | PASS |
| 41 | Service lifetime versus filtered history is correct | PASS |
| 42 | Service usage also opens full same invoice | PASS |
| 43 | SERVICE total and page clamping | PASS |
| 44 | PART total and page clamping | PASS |
| 45 | Empty report has page 1 / 0 and zero total | PASS |
| 46 | Invalid date range receives 400 filter form | PASS |
| 47 | Logout returns to login | PASS |
| 48 | Logged-out session cannot access booking | PASS |
| 49 | Browser JavaScript has no uncaught errors | PASS |

Bổ sung 6 check của [hành trình đúng thứ tự M2](evidence/ordered-module2.json): ALL có cả hai dòng chọn; SERVICE trước lifetime5/history2; Back giữ ALL/page1; PART sau lifetime3/history2; chung invoice HD0001 đủ4 dòng/tổng3.600.000; cả người bán và người thu tiền khác nhau. Các ảnh lượt này: [dịch vụ](evidence/browser/12-ordered-service.png), [hóa đơn](evidence/browser/13-ordered-invoice.png).

Số liệu fixture/SQL được JUnit đối chiếu độc lập, browser xác nhận render:

| Đối tượng/kỳ tháng 10/2026 | Số dòng đối tượng / usage | Doanh thu / kết quả |
|---|---|---|
| ALL | 27 dòng thống kê, 29 dòng usage | 5.741.000; trang1 có20, trang2 có7; tổng không đổi |
| SERVICE | 24 dịch vụ,25 dòng usage | 2.301.000 |
| PART | 3 phụ tùng,4 dòng usage | 3.440.000 |
| DV0001 | lifetime5, history PAID trong kỳ2 | 1.050.000 theo historical saleUnitPrice |
| PT0005 | lifetime3, history2; qty2 và1 | 3.100.000; count dùng là2, không phải qty3 |
| PT0001 | history1, qty3 | 300.000 |
| HD0001 | 2 service +2 part | Tổng3.600.000; order.ss Lê Thị Lan, payment.ss Vũ Thu Ngân |

HD0002 có issueDate 31/10/2026 23:59:59.999999 được tính dù usageTime sang 01/11. Pending/failed không tính doanh thu; usage chưa có invoice vẫn được lifetime tính; ngày ngoài kỳ bị loại. byOrder hóa đơn không nhận bộ lọc kỳ nên không cắt bớt dòng. ALL hỗ trợ đúng source; không giả danh sách SERVICE đơn lẻ chứa PART.

Ảnh browser lượt đầu ở evidence/browser/01–11: login, form, confirm, success, mobile, bộ lọc, list/trang2, part, invoice, service. Các ảnh có đường dẫn thật, không phải mockup.

Đã xem lại trực tiếp hai ảnh12/13 của lượt đúng thứ tự: chi tiết dịch vụ không có đánh giá; hóa đơn hiển thị người bán Lê Thị Lan và người thu tiền Vũ Thu Ngân, đủ4 dòng và tổng3.600.000.

## 5. Lỗi đã tái hiện và lỗi tĩnh

| Mã / ưu tiên đề xuất | Thao tác thực tế trên test | Kỳ vọng so với hiện trạng | Bằng chứng / trạng thái |
|---|---|---|---|
| D049 / P2 | CUSTOMER POST /vehicle với context/CSRF hợp lệ, vehicleBrand=abc, model/plate hợp lệ, year rỗng | 400 form lỗi; thực nhận500 vì EL so raw string với brand.id số tại JSP:9 | runtime-review.json observations[0]; [HTML phản hồi](evidence/invalid-brand-response.html), review-tomcat.log:57,118,177. Đã tái hiện, chưa sửa. |
| D050 / P3 | MANAGER GET /service-part?action=invoice&usageId=0&itemType=PART&itemCode=PT0005 với bộ lọc hợp lệ | 400/404; thực nhận200 render chi tiết vì target=0 bỏ nhánh invoice | observations[1]; ServicePartServlet6.java:21,40. Đã tái hiện, chưa sửa. |
| D051 / P1 về độ đúng tiền | Test-only ServiceUsage id1.saleUnitPrice=9007199254740991.01 và Payment id1.paymentAmount=9007199257840991.01; đọc HD0001 | vwInvoice exact9007199257840991.01; data-value UI9.007199257840992E15 =9007199257840992, lệch0.99 | observations[2]; [ảnh](evidence/large-money-invoice.png); InvoiceDAO6.java:22,28. Cả hai giá trị hợp lệ trong DECIMAL(18,2). Đã restore hai fixture sau probe; chưa sửa code. |
| D052 / P2 | Tạo hold hợp lệ; chỉ override Date.now của browser nhanh600000ms, reload confirm | Button phải còn cho thử xác nhận theo server; thực disabled=true trong khi SQL đếm hold hợp lệ=1 | observations[3]; app.js:7,12. Không chỉnh đồng hồ OS/server; release hold bằng edit và dọn sau; chưa sửa. |
| D053 / P3 | Đọc caption JSP và config SlotSetup6 | Caption giờ phải theo config; hiện in cứng08–17/nghỉ12–13 | BookAppointmentView6.jsp:19; SlotSetup6.java:12–18. **Tĩnh**, chưa chạy đổi config; default đang khớp. |

Bốn probe tìm bug được ghi riêng với expected/actual, không bị 49/49 check pass che mất. Severity là đề xuất review, chưa phải quyết định nghiệp vụ thay đổi.

## 6. Transaction, quyền, injection và cleanup

**Ghi lịch:** validateHold trước DAO chỉ là kiểm tra phản hồi sớm. createAppointment sở hữu tx thực hiện consumeHold có WHERE token/expiry/startTime, khóa row bằng UPDATE, ownership FOR SHARE, sequence FOR UPDATE, INSERT và readback; commit rồi mới trả saved. UNIQUE activeSlotId là bảo vệ bổ sung. Có rollback được test khi ownership/INSERT lỗi. Với connection mượn, caller phải commit; handler production không dùng overload này.

**Double-submit:** cùng context đồng bộ trên c; c.completed giữ kết quả, các lần POST sau redirect về cùng mã. Browser đã gửi5 request và đếm chỉ tăng1 lịch. Test8 connection ngoài session xác minh slot/token/unique chỉ cho1 commit. Không suy rộng thành durable idempotency qua mất session/restart/cluster; completed là state bộ nhớ.

**Giữ/đổi lựa chọn:** hold3 phút theo DB NOW; token UUID; release matched id+token; expire không cần job UPDATE để slot lại hiện khả dụng. POST edit release xong, giữ scalar ngày/xe/mô tả trong c, clear draft/token/hạn; lần hold mới nhận token mới. Session listener/logout có releaseAll; đóng tab không tự gửi sự kiện server, dựa expiry.

**Quyền và dữ liệu client:** Browser đã kiểm tra hai role chặn chéo, chưa đăng nhập về login, CSRF thiếu403, contact/customerId giả mạo không đổi khách, xe khác chủ403, context lạ400, logout mất quyền. Ownership còn kiểm trong transaction. Cơ chế không nhận Appointment6 Java từ client; HTTP chỉ params/context. Không kiểm chứng live role revocation ở giữa session hoặc tính năng admin ngoài phạm vi.

**SQL injection và XSS:** Đã đọc mọi query của DAO/helper: dữ liệu id/code/date/token/username dùng PreparedStatement; tên bảng trong getInvoiceCodeByUsage lấy từ switch SERVICE/PART, không nối bảng tùy ý từ input; sort cố định. Không tìm thấy nối trực tiếp dữ liệu request thành SQL trong hai module. Đây là kết luận tĩnh, không tuyên bố đã chạy bộ fuzz SQLi. Browser payload mô tả HTML được escape và không có uncaught JS error trong luồng chuẩn. Exception DB/probe JSP log ở server, body probe trả thông báo chung; không công bố bảo mật toàn hệ thống.

**Tiền/null/timezone:** DECIMAL/BigDecimal SQL đúng fixture nhỏ và phép đối chiếu PAID, song D051 đã chứng minh biên double không an toàn toàn miền. manufactureYear NULL được bảo toàn; payment/appointment.ss null theo đúng FK. currentMileage chưa nullable vì Q08 (D042 cần chốt). Zone mặc định Việt Nam đã chạy; DAO +07 và JSP Vietnam cố định nên không tuyên bố các cấu hình timezone khác đã đúng.

**Cô lập và dọn dữ liệu:** Runtime harness guard test database, ghi chỉ garaman6_it, dọn1 lịch,1 xe và234 slot tạm đã chuẩn bị; restore các slot có trước probe bị đổi hold và hai khoản tiền thử. Sequence/auto-increment của schema test có thể có khoảng trống hợp lệ, không reset bộ đếm. Fingerprint toàn bộ21 base table DB main trước/sau lượt runtime bằng nhau, cleanup.mainDatabaseUnchanged=true trong runtime-review.json. Các hash chỉ chứa fingerprint/count, không xuất rows/credential. Lượt ordered M2 bổ sung chỉ đọc. Giữ nguyên main MySQL/Tomcat; chỉ terminate đúng PID instance test do review tạo.

## 7. Đề xuất cập nhật thiết kế/code, chưa triển khai

| Nhóm cần xử lý | Đề xuất cụ thể | Ảnh hưởng sơ đồ/tài liệu và kiểm thử |
|---|---|---|
| D049/D050 | Typed validation brand khi render lỗi; tách action invoice khỏi sentinel usageId0; reject id≤0 cho action invoice | UI/HTTP contract, activity nhánh lỗi; regression malformed input/empty/0/âm và không có500 không cần thiết |
| D051 | Chốt miền tiền/hành vi round và biên double có kiểm soát; hoặc chuyển đồng bộ public money sang BigDecimal sau khi quyết định cập nhật API | Class field/get/set/DAO/view/test/DDL mapping; kiểm tiền lớn, .01/.25, tổng nhiều dòng, payment exact. Không tự đổi public type để che khác thiết kế |
| D052/D053 | TTL/độ lệch đồng hồ lấy từ server hoặc để server quyết định xác nhận; render caption giờ từ cùng config sinh slot | UI JS/time contract, activity hết hạn; kiểm client±10phút, đúng lúc expire, cấu hình giờ/nghỉ đổi |
| D042 | Chốt Km nullable hay Q08 tiếp tục giữ default0 | Nếu nullable: Integer/getObject thay getInt, DDL NULL, param NULL khi chưa biết, nonnegative khi có số; kiểm thêm xe thiếu km và null roundtrip. Không tự đổi toàn bộ giá trị0 cũ thành NULL |
| D003–D012,D025–D028 | Cập nhật controller/helper/mapper/DAO phụ, thứ tự history-before-lifetime, lần đọc lại invoice, HTML fetch và PRG theo V3 hiện trạng | Class chi tiết, activity, communication/sequence; activation và return qua đúng caller, không entity tự query |
| D013–D024,D038,D060,D061 | Ghi các quyết định đã chốt: auth, readonly, PENDING/NULL staff, code/sequence, Integer year, order relation, bỏ rating, staff/cashier, views, Payment/tiền SQL và phạm vi subset | Class/DB mapping/UI/state và tài liệu quyết định; không gán nhãn bug cho thay đổi có căn cứ |
| D029–D037,D039,D040–D041,D056,D058–D059 | Đồng bộ spelling control/param, kiểu UI, format ngày, tên/case bảng, cardinality, kích thước/null/default/kiểu SQL | Ảnh/UML, UI dictionary/HTTP contract, physical schema; phân biệt lịch sử slot và tối đa1 active |
| D043–D046 | Bổ sung SQL V1 gốc, đúng version ảnh và các sơ đồ phân tích/hoạt động/trạng thái/sequence cùng mẫu V3 độc lập | Mở lại phần so sánh provenance/migration; không coi baseline reconstructed là SQL gốc đã được bảo toàn |
| D047–D048 | Sơ đồ sinh cần mô tả helper/package-private/constructor/UI hoặc ghi rõ chỉ là phần public; giữ V3 mới chi tiết làm source vẽ sequence | Tài liệu class/sequence; tránh exporter tự chứng minh code khớp chính tài liệu từ code |
| D054/D057 | Tham số hóa target/testDB guard/cleanup cho browser; kiểm marker/ràng buộc live khi tái chạy setup | Deployment/test tooling; không trỏ browser mặc định vào main hoặc đoán schema đúng chỉ vì có vehicleCode |
| D055 | Chốt giữ returnRoute chỉ path hay khôi phục đầy đủ filter/context được whitelist | Auth/HTTP/session contract; test session hết hạn ở list/detail/invoice và chống open redirect |

### Phụ lục: lựa chọn vị trí đóng gói cho D001–D002

**Hiện trạng:** BookAppointmentView6.jsp POST params → BookingServlet6 tạo Appointment6 tạm → ConfirmView6 render → POST context → AppointmentServlet6 lấy lại draft → AppointmentDAO6 ghi. AddNewVehicleView6.jsp POST params → VehicleServlet6 tạo Vehicle6/VehicleBrand6 → VehicleDAO6 ghi. Không có JSP tạo hai object này. Mapper sau SELECT tạo graph là bước đọc hoàn toàn khác.

**Phương án cập nhật thiết kế giữ source:** chấp nhận vị trí đóng gói trong Servlet theo ví dụ LAB trang13, ghi rõ kiến trúc này trong thiết kế. Khi đó hai V3 hiện trạng đã mô tả đúng; vẫn ghi rằng không đáp ứng mẫu vị trí JSP được yêu cầu kiểm tra. Không tự cho rằng phương án đã được người dùng chốt.

**Phương án thay đổi tối thiểu để đúng yêu cầu JSP đóng gói — chỉ đề xuất, chưa có trong project:**

1. Bổ sung JSP xử lý server riêng dưới WEB-INF cho việc dựng draft lịch và xe, cùng action/điểm tiếp nhận Servlet phù hợp. Các thành phần này **chưa tồn tại**, không xuất hiện như lớp thật trong V3 hiện trạng.
2. Controller/filter xác thực role, CSRF/context và lấy customer/vehicle/slot/brand đáng tin cậy; trong cùng request forward dữ liệu đã kiểm soát đến JSP xử lý. JSP dùng cơ chế JavaBean/tag phía server tạo entity và gọi setter thật, giữ tham chiếu rồi đặt request attribute, forward đồng bộ đến Servlet tiếp tục. Không truyền object Java qua URL/form/redirect, không gọi setter từ JavaScript rồi coi là Java object.
3. Với đặt lịch, controller hiện giữ slot trước dựng draft; sau khi JSP dựng, Servlet tiếp nhận phải gắn vào đúng c.draft trong map session rồi render Confirm. POST xác nhận lấy chính draft đó; không tạo lại object vô cớ. Với thêm xe, Servlet tiếp nhận lấy Vehicle6 từ request cùng scope, xác minh customerId phía server và gọi DAO; redirect chỉ sau save thành công.
4. Giữ check chủ sở hữu/brand/slot/time trong backend và guard trong transaction DAO; object đã đóng gói không thay thế authorization. Request attribute phải do forward nội bộ tạo, direct request tới action tiếp nhận thiếu marker/object/context hợp lệ phải bị từ chối. Không nhận một object serialized của client hoặc mass-assign field cus/ss/id/code/status từ request.
5. Giữ synchronized context/tokens/session scope; mọi request mới sau redirect lấy context từ session, không kỳ vọng request attr tồn tại. Không mở transaction JDBC qua thời gian khách xem Confirm. Expiry/release/unique/sequence vẫn giữ ở DAO.
6. web.xml hiện scripting-invalid=true: không thể chèn scriptlet Java vào JSP rồi cho rằng chạy được. Ưu tiên JavaBean/custom tag phù hợp policy; nếu chọn scriptlet phải xem lại cấu hình và thiết kế riêng, không tắt bảo vệ toàn cục một cách ngầm định. JSP xử lý không render output trước forward.
7. Phải cập nhật class/interface/action/HTTP scope, activity, communication/sequence và inventory. Test thêm create/set đúng thứ tự, forward cùng request, attributes mất qua redirect, CSRF/role/direct entry/foreign vehicle/forged customer, nhiều tab/edit/expiry/double-submit/rollback; thử lỗi JSP khi đang có hold để release/expiry vẫn đúng.

Phương án này thay đổi vị trí trách nhiệm và tăng thành phần xử lý; không phải chỉ sửa một câu trong scenario. Review không tự chọn hoặc áp dụng phương án tương lai.

## 8. Chưa kiểm chứng và điều kiện trước khi chốt

- Chưa có SQL V1/phiên bản ảnh gốc/sơ đồ gốc thiếu để kết luận tương đương toàn bộ thiết kế hay migration dữ liệu cũ.
- Chưa đo tải lớn/N+1, memory khi query toàn bộ rồi slice, isolation khi dữ liệu báo cáo đang thay đổi đồng thời.
- Chưa thử mọi cấu hình lịch/timezone, OS/Linux/WSL/cloud, Tomcat cluster, restart mất session/crash ngay sau commit. Không chạy database outage bằng dừng main DB trong lượt review.
- Không chứng minh mọi giá DECIMAL chính xác khi public double; có counterexample D051. Không chứng minh caption đúng mọi config; có source mismatch D053.
- Cần xử lý5 lỗi đã phân loại, chốt mâu thuẫn Km và lựa chọn kiến trúc đóng gói; bổ sung nguồn thiếu trước khi gọi thiết kế là bản cuối đồng bộ. Các đề xuất đều chưa áp dụng.

## 9. Tự kiểm đầu ra

- Giữ nguyên147 file gốc theo SHA-256; không reset/checkout/commit hay sửa source trong review.
- Inventory55 lớp đầy đủ declaration; 61 mục có phân loại và căn cứ; không tạo lỗi SaleStaff/SalesStaff hay createAt/createdAt giả.
- V3 M1 134 bước, M2 236 bước; đánh số liên tục mỗi module, chỉ happy case; phần hai lượt chi tiết SERVICE rồi PART và common invoice; Back được một bước tham chiếu thực thi lại list.
- Bảng truy vết370 hàng, có caller/receiver/method/args/return/source và invocation grouping; số dòng/đường dẫn được rà lại; không setter trả entity, không container dispatch thành self-call.
- Đã tách static/readback/memory entity khỏi SQL persistence; success sau owner commit; scalar UPDATE/boolean/String/int không bịa entity.
- Build21 PASS,49+6 browser check PASS và4 counterexample báo riêng; cleanup và bảo toàn main có evidence. Không tuyên bố “khớp100%”.
