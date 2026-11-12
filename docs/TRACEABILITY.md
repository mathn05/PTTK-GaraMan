# Truy vết trước triển khai

| Scenario / UI | Servlet → DAO / dữ liệu | Kiểm thử bắt buộc |
|---|---|---|
| M1 1–9, home/profile/xe | BookingServlet6 GET → CustomerDAO6,VehicleDAO6 | browser home/form; ownership readonly |
| M1 10–14 ngày/slot | Booking GET → SlotDAO6.getAvailableSlots | DB future/date/expired hold |
| M1 15–23 confirm/hold/draft | Booking POST → holdSlot, new Appointment6 session context | concurrent hold, form validation, edit/new token |
| M1 24–31 lưu/success | Appointment POST → validateHold/createAppointment transaction | expire/wrong token/double submit/rollback/unique |
| Nhánh thêm xe | Vehicle GET/POST → brands/saveVehicle/code sequence | chuẩn biển/duplicate/year NULL/km0/concurrent code/preserve form |
| M2 1–12 menu/filter/list | Revenue GET → getRevenueStatistics | SERVICE/PART/ALL, boundaries, PAID, COUNT, historical price, paging |
| M2 13–24 dịch vụ | ServicePart GET → ServiceDAO6 + ServiceUsageDAO6 | lifetime/history, shallow order, no rating |
| M2 25–36 phụ tùng | ServicePart GET → SparePartDAO6 + PartUsageDAO6 | category/brand/stock/spec/image/quantity, nullable other branch |
| M2 37–42 hóa đơn | ServicePart lookup → Invoice GET → InvoiceDAO6 | all lines/cus/vehicle/repair/payment/sale staff, back context |
| Auth + CSRF + error | Login/AppFilter/AuthFilter | wrong password/inactive/403/logout/CSRF/500, JSP compilation |

Tên phép thử, expected/actual và lệnh chạy sẽ được ghi trong TEST_EVIDENCE.md từ lần chạy thực.

## Kết quả và tên kiểm thử cuối

Đã chạy: [21 JUnit + 49 Chrome + 4 runtime checks](TEST_EVIDENCE.md). Hợp đồng có tên/thứ tự tham số được kiểm bằng reflection `publicContractsPreserveFieldsArraysAndParameterNames` và xuất CONTRACTS_GENERATED.md.

| Nhóm trên | JUnit / Chrome thực tế đối chiếu |
|---|---|
| M1 1–9 / 10–14 | `mappingUsesSharedMemberIdAndNullableYear`, `expiredHoldAppearsAvailableWithoutGetMutatingDatabase`; Chrome Profile fields readonly / Future slots / Changing date keeps request |
| M1 15–23 | `concurrentHoldOnlyOneConnectionWins`, `wrongTokenCannotReleaseOrConsumeAndBorrowedConnectionStaysOpen`; Chrome tampered profile / edit release / foreign vehicle403 / XSS escaped |
| M1 24–31 | `expiredHoldCannotPersistButCanBeReclaimed`, `concurrentFinalConfirmationCreatesExactlyOneAppointment`, `failedOwnershipRollsBackConsumedSlot`, `insertFailureRollsBackSlotAndSequence`; Chrome năm POST cùng context / countDB+1 / refresh same code |
| Thêm xe | `vehicleNormalizationNullableYearDefaultMileageAndDuplicate`, `concurrentVehicleCodesAreDistinct`; Chrome add vehicle / selected new vehicle / preserved input |
| M2 1–12 | `servicePartAllRevenueMatchIndependentHandCalculation`, `dateBoundariesUseInvoiceNotUsageOrPaymentTime`, `equalRevenueAndSameCodeAcrossTypesSortDeterministically`, `monetaryFractionsSurviveAggregationAndInvoiceMapping`; Chrome ALL/SERVICE/PART / page2 / empty1/0 / invalid date400 |
| M2 13–36 | `lifetimeDiffersFromPaidHistoryAndHydratesCustomerVehicleOrder`; Chrome PART first / SERVICE branch / lifetime-vs-filter / no rating / stock/image/category/brand |
| M2 37–42 | `bothUsageTypesResolveFullInvoiceWithSeparateSalespersonAndCashier`; Chrome both usage types open HD0001 / all4 lines / total / salesperson+cashier / Back context |
| Auth/CSRF/lỗi | `authenticateRealCustomerManagerWrongAndInactive`, `passwordHashUsesSaltAndRejectsWrongOrMalformedPassword`, `paidInvoiceMismatchAndSqlFailureAreExceptionsNotEmptySuccess`; Chrome401/403/logout; runtime DBdown500/sanitized/recovery/persistence |
