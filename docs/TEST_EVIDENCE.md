# Bằng chứng kiểm thử thực tế

Ghi nhận 2026-11-12T07:40:32 trên **native Windows**; không phải kết quả WSL/container/cloud. Target http://127.0.0.1:8080/garaman6.

Kết quả: **21/21 JUnit**, **49/49 kiểm tra Chrome**, **4/4 kiểm tra DB outage/restart**. Báo cáo không tự đổi fail/skip thành pass.

## Môi trường đã quan sát

Windows 10 Pro 10.0.19045 x64; Temurin JDK/Javac21.0.10+7; Maven3.10.0, Wrapper3.3.4; Tomcat10.1.61; Connector/J9.4.0; JSTL API3.0.2/impl3.0.1; Python3.12/Chrome Windows/Playwright1.65.1.

SQL server thật: VERSION()=9.4.0; @@port=3306; @@session.time_zone=+07:00. Data directory: `C:\Users\admin\Documents\tai lieu nam 4\PTTK\BTL\garaman\.runtime\mysql\data\`. Dịch vụ MySQL94 đã có trên máy không bị dùng data directory hay thay cấu hình.

App connection cũng được kiểm tra +07:00 qua DAO6 trong JUnit. SHOW CREATE TABLE của 23 tables/views ở [schema-metadata.json](schema-metadata.json). Không chứa credential/data rows.

## Lệnh đã chạy và phạm vi

```powershell
java -version
javac -version
.\mvnw.cmd -version
python scripts/local_runtime.py setup
.\mvnw.cmd -B -ntp package
.\mvnw.cmd -B -ntp exec:java -Dexec.mainClass=garaman.util.SlotSetup6
python scripts/run_tomcat.py start
python scripts/browser_test.py
python scripts/runtime_smoke.py
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Stop-GaraMan.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Start-GaraMan.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Setup-GaraMan.ps1
```

JUnit dùng schema garaman6_it, có guard tên database trước ghi; tests không mock DB. Browser dùng Tomcat/JSP thật và DB ứng dụng, thêm xe/lịch demo thật. Năm request xác nhận đồng thời cùng context trả cùng lịch, count DB chỉ tăng1. GET query không sinh slot. Setup chạy lại bảo toàn dữ liệu; Stop/Start và Setup được thực thi bằng Windows PowerShell5.1, không chỉ suy ra từ code.

## Doanh thu tính tay: expected / actual

Kỳ `2026-10-01`–`2026-10-31`, đủ hai đầu theo Invoice.issueDate; chỉ Payment.status=PAID. Giá hiện tại khác giá lịch sử. HD0001 có nhiều dòng hai loại để bắt JOIN nhân bản.

| Đối tượng | Expected số lần / doanh thu VNĐ | Actual |
|---|---|---|
| SERVICE DV0001 | 2 / 1.050.000 | 2 / 1.050.000 |
| SERVICE DV0002 | 1 / 800.000 | 1 / 800.000 |
| 22 dịch vụ kiểm tra | 22 / 451.000 | 22 / 451.000 |
| PART PT0005 | 2 / 3.100.000 (quantity2 + quantity1) | 2 / 3.100.000 |
| PART PT0001 | 1 / 300.000 (quantity3) | 1 / 300.000 |
| PART DV0001 | 1 / 40.000 (quantity2) | 1 / 40.000 |
| SERVICE tổng | 24 đối tượng / 25 usage / 2.301.000 | khớp |
| PART tổng | 3 đối tượng / 4 usage / 3.440.000 | khớp |
| ALL tổng | 27 đối tượng / 29 usage / 5.741.000 | khớp, trang1=20/trang2=7, tổng hai trang không đổi |
| HD0001 | 2 service + 2 part / 3.600.000 | đủ4 dòng, bán hàng Lê Thị Lan / thu tiền Vũ Thu Ngân |
| Lifetime SERVICE DV0001 / PART PT0005 | 5 / 3, history có2 mỗi loại | khớp |

Con số trên được assert trong DatabaseIntegrationTest và browser-results; expected độc lập ở sql/seed-expected.json. Test tiền lẻ dùng 500000.25 và invoice3600000.25, tổng5741000.25; rollback sau kiểm tra. Test tie cho PART/SERVICE cùng DV0001 và cùng doanh thu, thứ tự PART trước SERVICE theo itemType ASC.

## JUnit (expected: pass, actual từ XML)

| Lớp | Test | Actual |
|---|---|---|
| DatabaseIntegrationTest | `expiredHoldAppearsAvailableWithoutGetMutatingDatabase` | PASS |
| DatabaseIntegrationTest | `concurrentVehicleCodesAreDistinct` | PASS |
| DatabaseIntegrationTest | `pageClampingAndWholeResultTotalsRemainIndependent` | PASS |
| DatabaseIntegrationTest | `lifetimeDiffersFromPaidHistoryAndHydratesCustomerVehicleOrder` | PASS |
| DatabaseIntegrationTest | `servicePartAllRevenueMatchIndependentHandCalculation` | PASS |
| DatabaseIntegrationTest | `vehicleNormalizationNullableYearDefaultMileageAndDuplicate` | PASS |
| DatabaseIntegrationTest | `wrongTokenCannotReleaseOrConsumeAndBorrowedConnectionStaysOpen` | PASS |
| DatabaseIntegrationTest | `bothUsageTypesResolveFullInvoiceWithSeparateSalespersonAndCashier` | PASS |
| DatabaseIntegrationTest | `expiredHoldCannotPersistButCanBeReclaimed` | PASS |
| DatabaseIntegrationTest | `concurrentFinalConfirmationCreatesExactlyOneAppointment` | PASS |
| DatabaseIntegrationTest | `dateBoundariesUseInvoiceNotUsageOrPaymentTime` | PASS |
| DatabaseIntegrationTest | `insertFailureRollsBackSlotAndSequence` | PASS |
| DatabaseIntegrationTest | `equalRevenueAndSameCodeAcrossTypesSortDeterministically` | PASS |
| DatabaseIntegrationTest | `failedOwnershipRollsBackConsumedSlot` | PASS |
| DatabaseIntegrationTest | `authenticateRealCustomerManagerWrongAndInactive` | PASS |
| DatabaseIntegrationTest | `paidInvoiceMismatchAndSqlFailureAreExceptionsNotEmptySuccess` | PASS |
| DatabaseIntegrationTest | `mappingUsesSharedMemberIdAndNullableYear` | PASS |
| DatabaseIntegrationTest | `concurrentHoldOnlyOneConnectionWins` | PASS |
| DatabaseIntegrationTest | `monetaryFractionsSurviveAggregationAndInvoiceMapping` | PASS |
| PasswordAndContractTest | `publicContractsPreserveFieldsArraysAndParameterNames` | PASS |
| PasswordAndContractTest | `passwordHashUsesSaltAndRejectsWrongOrMalformedPassword` | PASS |

XML và stack trace của lần chạy cuối nằm tại `target/surefire-reports/TEST-*.xml`. Source assertion: `src/test/java/garaman/DatabaseIntegrationTest.java`, `PasswordAndContractTest.java`.

## Chrome end-to-end (expected: true)

| Kiểm tra | Actual |
|---|---|
| Unauthenticated booking redirects to login | PASS |
| Wrong password receives generic login error | PASS |
| Login restores previously requested protected booking route | PASS |
| Customer home renders after successful login | PASS |
| Customer blocked from manager reports | PASS |
| POST without CSRF is blocked | PASS |
| Profile fields are readonly | PASS |
| Future slots loaded from database | PASS |
| Changing date keeps entered request and vehicle | PASS |
| Add vehicle returns to booking with new vehicle selected | PASS |
| Add vehicle preserves date and request | PASS |
| Hold form accepts valid ownership and ignores tampered profile | PASS |
| Confirmation escapes user text | PASS |
| Confirmation displays hold expiry | PASS |
| Edit restores selected vehicle and input | PASS |
| Edit releases chosen slot | PASS |
| Forged foreign vehicle receives 403 | PASS |
| Concurrent double-submit returns successful recorded result | PASS |
| Only one appointment persisted after five submissions | PASS |
| Refreshing success retains same appointment code | PASS |
| New tab receives a different booking context | PASS |
| Unknown booking context is rejected | PASS |
| Mobile booking has no document overflow | PASS |
| Manager login opens manager home | PASS |
| Manager blocked from customer booking | PASS |
| Out-of-scope customer statistics is disabled | PASS |
| ALL report matches independent expected total | PASS |
| Report displays twenty rows and two pages | PASS |
| Revenue sorted with tyre first | PASS |
| No customer rating displayed on report | PASS |
| Second page has remaining seven rows and full total | PASS |
| Shared detail renders part branch first | PASS |
| Part lifetime count differs from filtered history | PASS |
| Part detail has category brand stock specification image | PASS |
| Invoice lookup redirects to correct invoice code | PASS |
| Invoice displays all service and part lines | PASS |
| Invoice has salesperson and cashier from different real employees | PASS |
| Invoice total uses all historical lines | PASS |
| Invoice Back preserves part and report context | PASS |
| Shared detail renders service with no rating | PASS |
| Service lifetime versus filtered history is correct | PASS |
| Service usage also opens full same invoice | PASS |
| SERVICE total and page clamping | PASS |
| PART total and page clamping | PASS |
| Empty report has page 1 / 0 and zero total | PASS |
| Invalid date range receives 400 filter form | PASS |
| Logout returns to login | PASS |
| Logged-out session cannot access booking | PASS |
| Browser JavaScript has no uncaught errors | PASS |

Dữ liệu máy đọc: [browser-results.json](evidence/browser-results.json). Đã xem trực quan ảnh desktop/mobile: tiếng Việt đúng, không overlap/clipping; mobile không overflow document. Ảnh xác nhận có payload script dạng text là phép thử escape, không chạy mã.

Ảnh: [Booking](evidence/02-booking.png), [Confirm](evidence/03-confirm.png), [Success](evidence/04-booking-success.png), [Mobile](evidence/05-booking-mobile.png), [Filter](evidence/06-revenue-filter.png), [List](evidence/07-revenue-list.png), [Page2](evidence/08-revenue-page2.png), [Part](evidence/09-part-detail.png), [Invoice](evidence/10-invoice.png), [Service](evidence/11-service-detail.png).

## Runtime (expected: pass)

| Kiểm tra | Actual |
|---|---|
| Database outage returns HTTP 500, no fake login success | PASS: 500 |
| Database outage response does not expose SQL/stack/password | PASS: sanitized error page |
| MySQL restart preserves persisted appointments and vehicles | PASS: {'appointments': 3, 'vehicles': 4} |
| Login recovers after native MySQL restart | PASS: 200 |

Dữ liệu máy đọc: [runtime-results.json](evidence/runtime-results.json). Lần restart SQL dùng credential ngẫu nhiên local và kiểm chứng dữ liệu còn nguyên. HTTP500 trong phép outage là kết quả mong đợi; stack trace chỉ nằm log server, không UI.

## Build và artifact

WAR: `target/garaman6.war`, 7,785,245 bytes. SHA-256: `8207f258915f4fa3a7dc6b1b6df712a0bf56b759b9044cab7812f5138d3dc131`.

Log runtime giữ trong `.runtime/tomcat-base/logs` và `.runtime/mysql/server.log`. Log còn lịch sử các lần lỗi lúc triển khai (JSTL prefix/UTF8 cache, timezone tên vùng), không xóa dấu vết hoặc coi lần thất bại là pass. Những lỗi đó đã được sửa và kiểm tra lại trên WAR cuối.

## Giới hạn có thật

- SQL V1 gốc không được cung cấp. Đã kiểm thử cài mới bằng V1 tái dựng + migrationV2 trên garaman6 và garaman6_it; **chưa kiểm chứng migration từ file V1 gốc hay dữ liệu thật**. Không DROP/chuyển đổi instance MySQL94 cũ.
- Kiểm thử là local Windows/Chrome, không phải load test production hay deploy public. Manual startup.bat trong README là lựa chọn hướng dẫn; đường đã thực thi là Python/native Bootstrap và các script PowerShell.
- Session giữ draft/kết quả theo context; restart Tomcat làm mất phiên, hold chưa consume tự hết hạn ở DB sau3 phút. Khi còn phiên, retry cùng context trả kết quả đã lưu; UNIQUE/transaction ngăn trùng lịch kể cả khác connection.
- Public money type vẫn double theo thiết kế; SQL DECIMAL và tổng BigDecimal được kiểm tra với tiền lẻ, chuyển đổi chỉ ở biên mapper.
