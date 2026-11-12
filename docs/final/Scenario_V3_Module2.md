# Scenario V3 — Module 2: Thống kê dịch vụ/phụ tùng theo doanh thu

Source SHA-256: `f0c79f6eb835cc5a8145068a161d6e15a97f682e65176925e039de5c334e736b`; [manifest](evidence/snapshot-after.json).

Tiền điều kiện: đăng nhập role MANAGER. Chọn ALL, kỳ minh họa 01/10/2026–31/10/2026. Chỉ khai triển hai lượt xem chi tiết SERVICE DV0001 rồi PART PT0005; phần đầu/cuối chung. Hóa đơn ví dụ HD0001 có RepairOrder.appointment=null; các nhánh optional khác ghi riêng, không bịa lời gọi vào happy case.

Các bước dưới đây chỉ là happy case. Constructor khởi tạo tham chiếu, setter void; bên gọi giữ object. Nhóm hydrate graph con/lặp được diễn đạt gọn, nguồn chi tiết nằm ở bảng truy vết riêng. HTTP được container dispatch, không có lời gọi handler vào chính nó.

1. Tại ManagerHomeView6.jsp, nhân viên chọn Xem báo cáo, trình duyệt GET /revenue?action=menu.
2. Container qua filters rồi dispatch RevenueServlet6.doGet; handler forward ReportMenuView6.jsp.
3. Nhân viên chọn Thống kê dịch vụ/phụ tùng theo doanh thu; trình duyệt GET /revenue?action=filter.
4. RevenueServlet6 gọi filter, đặt bộ lọc mặc định tháng trước/ALL và forward RevenueStatisticsFilterView6.jsp.
5. Nhân viên chọn ALL, startDate, endDate rồi bấm Thống kê; form GET /revenue action=list.
6. RevenueServlet6.doGet kiểm bộ lọc bằng private reportFilter rồi gọi list.
7. RevenueServlet6.list chuyển ngày sang Date, gọi RevenueDAO6.getRevenueStatistics(startDate,endDate,statisticType).
8. RevenueDAO6 tổng hợp PAID theo issueDate và giá lịch sử; ALL UNION ALL dịch vụ/phụ tùng; sort doanh thu DESC, mã ASC, loại ASC.
9. RevenueDAO6 khởi tạo từng RevenueStats6, gán mã/tên/loại/count Integer/doanh thu BigDecimal, trả RevenueStats6[] cho Servlet.
10. RevenueServlet6 cộng BigDecimal toàn tập, clamp trang, slice20 và đặt tổng/trang/URL bộ lọc, forward RevenueStatisticsListView6.jsp.
11. RevenueStatisticsListView6.jsp hiển thị danh sách ALL và phân trang; nhân viên chọn SERVICE DV0001.
12. ServicePartServlet6 kiểm bộ lọc/loại/mã/action, gọi ServiceDAO6.getServiceInfo(serviceCode).
13. ServiceDAO6 query và private readService đóng gói Service6 từ ResultSet, trả dịch vụ về handler.
14. ServicePartServlet6 gọi ServiceUsageDAO6.getAllServiceUsageOfService(code,start,end) trước khi lấy lifetime.
15. ServiceUsageDAO6 lọc PAID theo issueDate/kỳ và code, đóng gói từng ServiceUsage6 cùng Service/Technician/RepairOrder và graph con; trả mảng history.
16. ServicePartServlet6 slice history và gọi ServiceUsageDAO6.getTotalUsageCount(code), nhận Integer tổng số dòng toàn thời gian.
17. ServicePartServlet6 đặt lifetime/trang/Back/link invoice rồi forward ServicePartDetailView6.jsp; JSP hiển thị dịch vụ và lịch sử, không đánh giá.
18. Nhân viên chọn Quay lại; RevenueServlet6 tải lại danh sách ALL cùng kỳ/trang bằng đoạn chung các bước6–10.
19. Nhân viên chọn PART PT0005 trên danh sách ALL; trình duyệt GET /service-part với item/filter/page.
20. ServicePartServlet6 kiểm tham số rồi gọi SparePartDAO6.getPartInfo(partCode).
21. SparePartDAO6 query view/brand/category, private readPart đóng gói SparePart6 và các thuộc tính con, trả về handler.
22. ServicePartServlet6 gọi PartUsageDAO6.getAllPartUsageOfPart(code,start,end) trước lifetime.
23. PartUsageDAO6 lọc PAID theo issueDate/kỳ và code, đóng gói từng PartUsage6 cùng SparePart/Technician/RepairOrder và graph con rồi trả mảng.
24. ServicePartServlet6 slice history rồi gọi PartUsageDAO6.getTotalUsageCount(code), nhận Integer lifetime không lọc kỳ.
25. ServicePartServlet6 chuẩn bị ảnh hợp lệ/trang/URL, forward ServicePartDetailView6.jsp hiển thị phụ tùng/lifetime/history, không rating.
26. Nhân viên chọn Xem hóa đơn tại dòng thuộc HD0001; JSP gửi GET /service-part?action=invoice cùng usageId và ngữ cảnh.
27. ServicePartServlet6 kiểm usageId Integer >0, tải lại thông tin phụ tùng → history → lifetime rồi kiểm dòng thuộc item/kỳ bằng Objects.equals.
28. Sau membership hợp lệ, ServicePartServlet6 gọi InvoiceDAO6.getInvoiceCodeByUsage(itemType,usageId), nhận String mã hóa đơn.
29. ServicePartServlet6 redirect /invoice với invoiceCode và item/filter/page/usagePage.
30. Container dispatch InvoiceServlet6.doGet; handler kiểm mã rồi gọi InvoiceDAO6.getInvoiceDetail(invoiceCode).
31. InvoiceDAO6 đọc vwInvoice6, tạo Invoice6 và gán thông tin/tổng BigDecimal, gọi findOrderById để lấy phiếu/khách/xe/người bán.
32. InvoiceDAO6 gọi ServiceUsageDAO6.byOrder rồi PartUsageDAO6.byOrder để nạp đầy đủ hai mảng dòng hóa đơn, gán cho order/Invoice.
33. Với hóa đơn PAID, InvoiceDAO6 tạo Payment6 từ cùng dòng Invoice và nạp người thu tiền qua MemberDAO6; kiểm tổng bằng compareTo, setPayment rồi trả Invoice6.
34. InvoiceServlet6 đặt invoice, dựng Back URL giữ ngữ cảnh và forward InvoiceDetailView6.jsp; JSP hiển thị đầy đủ mọi dòng, khách/xe, người bán, thanh toán/người thu.

## Mapping truy vết

| Bước V3 | Dòng truy vết |
|---|---|
| 1 | 1–1 |
| 2 | 2–2 |
| 3 | 3–3 |
| 4 | 4–5 |
| 5 | 6–6 |
| 6 | 7–7 |
| 7 | 8–8 |
| 8 | 9–10 |
| 9 | 11–11 |
| 10 | 12–13 |
| 11 | 14–14 |
| 12 | 15–15 |
| 13 | 16–17 |
| 14 | 18–18 |
| 15 | 19–21 |
| 16 | 22–23 |
| 17 | 24–25 |
| 18 | 26–26 |
| 19 | 27–27 |
| 20 | 28–28 |
| 21 | 29–30 |
| 22 | 31–31 |
| 23 | 32–33 |
| 24 | 34–35 |
| 25 | 36–37 |
| 26 | 38–38 |
| 27 | 39–40 |
| 28 | 41–42 |
| 29 | 43–43 |
| 30 | 44–44 |
| 31 | 45–46 |
| 32 | 47–48 |
| 33 | 49–51 |
| 34 | 52–53 |

[Bảng truy vết source](Scenario_V3_Ky_thuat_Module2.md).
