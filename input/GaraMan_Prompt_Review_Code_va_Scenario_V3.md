# PROMPT REVIEW CODE GARAMAN VÀ VIẾT SCENARIO V3 TỪ CODE

Sao chép toàn bộ nội dung từ mục “Vai trò và nhiệm vụ” trở xuống vào AI đang mở project GaraMan đã triển khai. Cung cấp cùng lúc code đầy đủ, tài liệu thiết kế cuối và file mẫu scenario. Prompt này yêu cầu review và viết tài liệu; không tự ý sửa code để làm mất dấu vết khác biệt.

## Vai trò và nhiệm vụ

Bạn là người review mã nguồn Java web JSP–Servlet–DAO–Entity và đối chiếu hồ sơ phân tích, thiết kế hệ thống GaraMan. Project hiện đã được triển khai. Hãy thực hiện trọn vẹn hai công việc:

1. Review toàn bộ phần code liên quan đến hai module, so khớp với thiết kế cũ và các quyết định nghiệp vụ được chốt sau thiết kế, phát hiện mọi khác biệt có thể kiểm chứng, kể cả khác biệt nhỏ về tên, kiểu dữ liệu, chữ hoa/thường, tham số, thứ tự xử lý, quan hệ và dữ liệu hiển thị.
2. Viết Scenario V3 cho hai module từ code thực tế, theo cách trình bày chi tiết của mẫu tôi cung cấp và tài liệu bài giảng, có đầy đủ tầng Servlet của bài tôi.

Hai module:
- Module 1: Khách hàng đặt lịch hẹn trực tuyến.
- Module 2: Nhân viên quản lý thống kê dịch vụ/phụ tùng theo doanh thu, xem lịch sử sử dụng và hóa đơn tương ứng.

Không suy đoán code từ tên lớp. Phải đọc file triển khai, lần theo lời gọi và truy vấn. Không sao chép Scenario V2/V3 mẫu rồi đổi tên hàm. Không công bố “khớp 100%” nếu còn thành phần chưa kiểm tra hoặc không có bằng chứng. Mục tiêu là truy vết được mọi bước của Scenario V3 về code, và nêu rõ mức độ đã kiểm chứng.

## 1. Đầu vào và cách xác định nguồn chuẩn

Trước khi viết, lập danh mục các nguồn đã đọc trong project, đặc biệt thư mục `docs/input/` nếu có:
- Biểu đồ lớp thực thể toàn hệ thống pha thiết kế.
- Biểu đồ lớp chi tiết pha phân tích và pha thiết kế của từng module.
- Biểu đồ trạng thái, hoạt động, communication/sequence và thiết kế giao diện nếu được cung cấp.
- Scenario V2 cuối cùng, file mẫu Scenario V3 và các ghi chú điều chỉnh nghiệp vụ.
- Bài giảng hướng dẫn phân tích, thiết kế; tài liệu thực hành J2EE/JSP–Servlet–JDBC.
- DDL, migration, seed, schema thực tế nếu truy cập được DB.
- Toàn bộ mã nguồn ứng dụng, cấu hình mapping Servlet, JSP, JavaScript, bộ lọc đăng nhập, DAO, entity, hàm hỗ trợ, dependency và kiểm thử.

Đừng coi mtime mới nhất là bằng chứng duy nhất về phiên bản cuối. Liệt kê các nguồn mâu thuẫn và quyết định nào đã thay thế quyết định nào.

Tách biệt hai loại chuẩn:
- Chuẩn để đánh giá đúng/sai nghiệp vụ: yêu cầu và quyết định cuối cùng tôi đã chốt. Khi thiết kế cũ khác quyết định mới thì ghi lại cả hai, không đánh đồng là lỗi code.
- Chuẩn để mô tả Scenario V3 hiện trạng: mã nguồn thực tế tại thời điểm review. Code sai vẫn phải được mô tả đúng hiện trạng, đồng thời đánh dấu lỗi ở báo cáo review; không sửa câu chữ để che lỗi.

Nếu thiếu một biểu đồ hoặc không đọc được ảnh, ghi rõ phần nào chưa thể đối chiếu. Tiếp tục review các phần còn đủ dữ liệu. Không tự điền tên thuộc tính, phương thức hoặc quan hệ từ trí nhớ.

Ghi nhận commit hiện tại nếu có Git; nếu có thay đổi chưa commit thì ghi rõ và lập danh sách file ảnh hưởng. Không reset, checkout đè, tự commit hoặc sửa mã nguồn. Chỉ tạo các file báo cáo trong phạm vi nhiệm vụ này. Nếu code thay đổi trong lúc review thì kiểm tra lại các bằng chứng và số dòng trước khi hoàn tất.

## 2. Đọc tài liệu và rút quy tắc trước khi áp dụng

Đọc kỹ phần thiết kế chi tiết module, thiết kế hoạt động, Scenario V3 và ví dụ tương ứng. Với tài liệu “BG HP TTTN 2 CNPM 2020 final (1).pdf”, các nội dung liên quan nằm ở trang in 96–113, đặc biệt:
- Trang in 96–103: giao diện, thuộc tính tường minh/ẩn, DAO, tham số vào/ra, thực thể và quan hệ.
- Trang in 103: hành động phải tương ứng phương thức đã thiết kế; kịch bản thiết kế và thời gian hoạt động phương thức; ví dụ sách có lược bỏ bước đóng gói thuộc tính con cho gọn.
- Trang in 104–106: ví dụ đăng ký học, luồng truy vấn → đóng gói → trả kết quả → hiển thị.
- Trang in 110: ví dụ nhập điểm có bước giao diện xử lý gọi thực thể để gán dữ liệu, nhận lại đối tượng rồi gọi DAO lưu.
- Trang in 110–113: ví dụ xem thống kê và đi sâu qua các màn hình chi tiết.

Phân biệt số trang in với số trang trong trình đọc PDF; với bản PDF này trang in thường lệch một trang so với vị trí PDF. Tự kiểm tra tiêu đề và nội dung, không chỉ dựa vào số.

Đọc cả tài liệu thực hành J2EE được cung cấp: ví dụ controller Servlet xử lý request, khởi tạo entity rồi gọi DAO và forward/redirect. Phải chỉ ra phần nào của ví dụ là quy ước trình bày, phần nào là cấu trúc chương trình. Không lấy ví dụ JSP gọi DAO trực tiếp của sách để xóa tầng Servlet đang có trong bài tôi; cũng không lấy ví dụ Servlet để khẳng định code của tôi chắc chắn tạo đối tượng trong Servlet khi chưa đọc code.

Đầu báo cáo, viết ngắn các quy tắc đã rút ra, kèm tên tài liệu và trang/mục đã thực sự đọc. Nếu thiếu tài liệu thì báo thiếu, không tuyên bố đã đọc.

## 3. Yêu cầu đặc biệt: đóng gói khi lưu dữ liệu

Tôi muốn kiểm tra riêng luồng sau:

Giao diện xử lý lưu → gọi thực thể để đóng gói dữ liệu → nhận đối tượng về giao diện → tiếp tục yêu cầu DAO lưu, thông qua Servlet nếu đó là đường gọi thực tế của project.

Cụ thể với bài có Servlet, một cách triển khai có thể là:
1. JSP phía server tạo/cập nhật đối tượng bằng constructor hoặc setter thực sự tồn tại.
2. JSP nhận tham chiếu đối tượng sau khi khởi tạo, lưu/chuyển đối tượng theo cơ chế có thật trong code.
3. JSP chuyển yêu cầu đến Servlet theo cơ chế có thật.
4. Servlet lấy đối tượng hoặc dựng lại đối tượng theo đúng code và gọi DAO lưu.
5. DAO lưu bằng transaction, trả kết quả theo đúng chữ ký.

Đây là yêu cầu cần kiểm chứng, KHÔNG phải mô tả sẵn về code hiện có. Nếu code đang là JSP submit → Servlet → tạo entity → DAO thì:
- Báo riêng là “khác yêu cầu vị trí đóng gói”; nêu file, dòng, constructor/setter và đối tượng thực tế.
- Scenario V3 hiện trạng vẫn phải viết Servlet là nơi tạo entity. Không được gán hành động đó cho JSP để làm giống mẫu.
- Viết đề xuất thay đổi tối thiểu ở phụ lục, nêu cả tác động tới thiết kế, session/request, an toàn dữ liệu và kiểm thử; không áp dụng thay đổi trong lượt review này.
- Nếu việc thực hiện đúng yêu cầu cần một JSP xử lý trung gian hay một thay đổi kiến trúc, nói rõ đó là thành phần đề xuất, không đưa vào kịch bản hiện trạng như thành phần đã tồn tại.

Kiểm tra kỹ giới hạn kỹ thuật:
- JSP xử lý trên server khác với HTML/JavaScript chạy trên trình duyệt. Submit HTTP thông thường chỉ gửi giá trị tham số, không tự truyền một đối tượng Java sống sang request kế tiếp.
- Chỉ mô tả truyền đối tượng Java giữa JSP và Servlet khi code có bằng chứng: request attribute và forward trong cùng request, session attribute, hoặc cơ chế triển khai tương ứng. Redirect tạo request mới; request attribute không tự tồn tại qua redirect.
- Đối tượng lịch hẹn tạm được tạo trước trang xác nhận có thể chỉ được lấy lại ở lúc xác nhận. Không bịa một lần tạo mới ở nút Xác nhận nếu code không tạo mới.
- Gọi `setX(...)` trả `void` không có nghĩa setter trả về entity. Có thể mô tả hoàn tất gán dữ liệu và bên gọi tiếp tục giữ tham chiếu; chỉ constructor hoặc phương thức trả entity mới được mô tả là trả đối tượng tương ứng.
- Chỉ cập nhật field trong entity chưa phải cập nhật DB. Chỉ ghi “lưu/cập nhật CSDL” tại đúng lệnh SQL hoặc phương thức persistence thực sự thực hiện nó.
- Các lệnh UPDATE trạng thái, giữ chỗ, tiêu thụ chỗ có thể dùng tham số SQL trực tiếp, không bắt buộc tạo entity mới. Nếu không đóng gói entity thì không tự bổ sung bước đóng gói cho đẹp.
- Dữ liệu khách hàng và quyền sở hữu xe phải được xác minh bằng session/DB phía server; không tin đối tượng hay ID do client gửi chỉ vì đã đóng gói.

Lập một bảng riêng cho từng thao tác ghi DB có thật: thao tác, điểm tạo/cập nhật entity, nơi giữ đối tượng, đường đi đến DAO, phương thức lưu, phạm vi transaction, kết luận có/không khớp yêu cầu này.

## 4. Phạm vi review chi tiết

### 4.1. Lớp, thuộc tính và chữ ký

Lập inventory tất cả lớp tham gia hai luồng, kể cả lớp phụ trợ, filter và DTO thực sự được dùng. So từng lớp với thiết kế:
- Tên, package, hậu tố `6`, visibility, kế thừa, thuộc tính và quan hệ.
- Tên field chính xác, kiểu nguyên thủy/nullable wrapper, mảng/List, String/enum, Date/LocalDate/Timestamp và ánh xạ SQL.
- Constructor, getter/setter, tên hàm, thứ tự và kiểu tham số, kiểu trả về, checked exception và overload.
- `id` và mã nghiệp vụ `code`: UI gửi gì, Servlet đọc gì, DAO nhận gì, SQL lọc cột nào. Không viết `id` trong scenario nếu code nhận `serviceCode`, và ngược lại.
- Thuộc tính UI tường minh/ẩn được triển khai qua form control, request/session attribute, model hoặc query parameter thế nào. Field khai báo trên sơ đồ JSP không nhất thiết là Java instance field; đối chiếu đúng vai trò dữ liệu, không kết luận thiếu chỉ vì không có khai báo Java.
- Tên control, action URL, tên tham số request và tên attribute phân biệt chữ hoa/thường.
- Trường dẫn xuất, field bổ sung hoặc thiếu; constructor/method chỉ khai báo mà không được gọi phải tách khỏi luồng thực thi.

Không coi mọi khác biệt là lỗi: phân loại “khác biệt hình thức”, “thay đổi triển khai hợp lý cần cập nhật thiết kế”, “thay đổi nghiệp vụ đã được chấp thuận”, “lỗi cần sửa”, “chưa đủ bằng chứng”. Mọi khác biệt vẫn phải được ghi nhận.

### 4.2. Giao diện, Servlet và điều hướng

Theo từng action, lần từ link/form/JavaScript đến URL mapping, filter, `doGet`/`doPost`, helper method, DAO, entity và JSP đích.
- Phân biệt submit GET/POST, AJAX/fetch, forward, redirect và include.
- JSP dưới `WEB-INF` thường được forward từ Servlet; không viết người dùng mở trực tiếp JSP đó nếu route không cho phép.
- Không viết hai JSP tự gọi nhau nếu code điều hướng qua Servlet.
- Không viết “Servlet gọi doGet/doPost” như một self-call thật nếu đó chỉ là container dispatch vào handler. Có thể dùng câu theo format mẫu: “Lớp X tiếp nhận yêu cầu tại phương thức doGet(...)”. Nếu code `doPost` gọi `doGet` thì mới có lời gọi đó.
- Kiểm tra trang menu báo cáo có thực sự tồn tại; không tự thêm hoặc xóa trang để khớp một bản thiết kế cũ.
- Theo dõi dữ liệu mất qua redirect, điều kiện lọc, trang hiện tại và hành vi Quay lại.

### 4.3. DAO, truy vấn và transaction

Đọc truy vấn thực tế, mapper và helper, không chỉ chữ ký method:
- Thứ tự query, join, điều kiện ngày, điều kiện thanh toán, tổng hợp và thứ tự sort.
- Chỗ khởi tạo từng entity/DTO, setter, quan hệ được nạp và dữ liệu liên quan.
- Đối tượng do mapper/helper tạo thì ghi đúng mapper/helper; không gán cho entity khả năng tự truy vấn hoặc tự tạo danh sách nếu không có.
- DAO/mapper tạo collection và thêm từng phần tử. Constructor entity trả từng đối tượng; không nói entity trả cả List khi code không có hành vi đó.
- Trả scalar `int`, `boolean`, `String` không tự sinh bước tạo entity.
- Kết nối, commit/rollback, autocommit, row lock/atomic update, generated key, số dòng affected và đường đi lỗi ảnh hưởng tính đúng.
- Nếu DAO này gọi DAO khác thì phải đưa đúng DAO phụ vào call graph, không mô tả gom hết vào DAO đầu.
- Phương thức `void` hoàn tất bình thường chỉ trả quyền điều khiển, không tự có kết quả `true`, ID hay entity.

### 4.4. CSDL và ánh xạ đối tượng

So sánh DDL, mapping và schema thực tế khi truy cập được: tên bảng/cột, kiểu, nullable, default, PK, FK, unique, generated column và index có ý nghĩa nghiệp vụ.
- Không nhầm entity tồn tại với bảng vật lý tồn tại: Payment có thể nằm trong Invoice ở DB nhưng vẫn là đối tượng trong Java; tương tự Account và Member.
- Không yêu cầu bảng thống kê hoặc ID của RevenueStats nếu đây là kết quả truy vấn tổng hợp.
- Nếu chỉ đọc migration mà chưa kết nối DB thì ghi “đã kiểm tra DDL, chưa xác nhận schema đang chạy”.
- Không chạy reset/drop/truncate, không đổi mật khẩu và không sửa dữ liệu người dùng. Kiểm thử ghi chỉ dùng schema test riêng hoặc dữ liệu kiểm thử cô lập có dọn dẹp.

## 5. Quy tắc nghiệp vụ cần truy vết

Dùng các quyết định cuối trong hồ sơ của project để xác nhận lại danh sách dưới đây. Nếu hồ sơ có thay đổi mới, ghi rõ chỗ thay thế; không tự áp dụng một giả định chưa được xác nhận.

### Module 1
- Người dùng đã đăng nhập là khách hàng; lấy khách hàng từ session/DB, thông tin liên hệ không cho sửa theo quyết định cuối.
- Chỉ sử dụng xe thuộc khách hàng đang đăng nhập.
- Số Km có thể NULL, không tự đổi “chưa biết” thành 0; không âm khi đã nhập.
- Ngày/khung giờ hợp lệ, còn khả dụng; mỗi slot sức chứa 1.
- Giữ chỗ 3 phút, có token/mã giữ chỗ và thời điểm hết hạn; hai người không thể giữ thành công cùng một slot trong cùng thời gian hiệu lực.
- Khi đổi lựa chọn, giải phóng chỗ cũ rồi giữ chỗ mới theo nghiệp vụ đã chốt.
- Xác nhận phải kiểm tra giữ chỗ đúng token, còn hiệu lực và đúng người/phiên theo cơ chế thực tế; kiểm tra trước rồi ghi ở hai bước rời rạc không được gây double booking.
- Ghi lịch hẹn và tiêu thụ chỗ phải bảo đảm tính nguyên tử; chỉ thành công sau commit thực sự.
- Nhân viên bán hàng có thể NULL khi mới đặt; NULL không có nghĩa bỏ FK.
- Mã tăng dần phải theo cơ chế an toàn trong code; phân biệt mã hiển thị từ ID với mã lưu trong DB; không tự thêm bảng cấp mã kỹ thuật.
- Không tự thêm bước thông báo cho nhân viên nếu chức năng đó đã bị bỏ và code không có.
- Thêm xe mới, chỉnh sửa, hết hạn và các ngoại lệ phải review nếu code có, nhưng không chèn vào happy case chính chỉ để liệt kê đủ chức năng.

### Module 2
- Người dùng đã đăng nhập có quyền quản lý.
- Doanh thu theo `Invoice.issueDate`, chỉ hóa đơn đã thanh toán; đối chiếu chính xác SQL và các trạng thái thực tế.
- Khoảng thời gian bao gồm cả ngày đầu và ngày cuối theo nghiệp vụ; kiểm tra xử lý giờ cuối ngày và timezone trong code.
- Doanh thu dịch vụ là tổng giá bán lịch sử trên các dòng sử dụng; phụ tùng là giá lịch sử nhân số lượng; không dùng giá hiện tại, không có thuế/giảm giá nếu không được bổ sung nghiệp vụ.
- “Số lần sử dụng” là số dòng sử dụng, không phải tổng số lượng phụ tùng.
- Tổng số lần sử dụng toàn thời gian không bị lọc bởi startDate/endDate; danh sách lịch sử trong chi tiết vẫn theo khoảng thống kê và điều kiện thanh toán đã chốt.
- Tổng doanh thu phải là tổng của cả tập kết quả thống kê, không chỉ 20 dòng ở trang hiện tại.
- Phân trang 20 dòng/trang; doanh thu giảm dần, bằng doanh thu xếp theo code; đối chiếu cả tie-breaker cuối nếu code có.
- Một màn hình chi tiết dùng chung cho dịch vụ/phụ tùng: truy vết itemType/itemCode hoặc tên thực tế tương ứng từ link → Servlet → DAO → view và nút Quay lại.
- Chi tiết hóa đơn hiển thị toàn bộ hóa đơn được chọn; không lọc bớt các dòng hóa đơn theo khoảng thống kê ban đầu. Điều kiện thống kê dùng để xác định các lần sử dụng được chọn và để giữ ngữ cảnh quay lại.
- Dữ liệu hóa đơn, phiếu sửa chữa, xe, khách hàng, các dòng sử dụng và thanh toán phải được nối đúng khóa, không gây nhân bản doanh thu do join.
- Hóa đơn có thể tồn tại khi chưa thanh toán, các trường thanh toán tương ứng được NULL. Luồng báo cáo happy case vẫn chọn hóa đơn đã thanh toán.
- Không tự thêm đánh giá/rating vào UI.

Kiểm tra số tiền theo kiểu dữ liệu đang dùng. Nếu thiết kế cũ dùng double còn code dùng BigDecimal/DECIMAL thì vẫn ghi khác biệt, đồng thời đánh giá lợi ích và yêu cầu cập nhật thiết kế. Không tự đổi tên hoặc kiểu trong scenario để che sự khác biệt.

## 6. Cách viết Scenario V3

### 6.1. Hình thức và độ chi tiết

Viết bằng tiếng Việt. Mỗi module là một danh sách đánh số liên tục từ 1 đến hết. Tên lớp, JSP, phương thức, field và tham số giữ nguyên chính tả trong code.

Mỗi bước ghi rõ chủ thể thực hiện, hành động và nơi nhận nếu có. Tách được các vai trò:
1. Người dùng thao tác trên giao diện.
2. Giao diện gửi yêu cầu qua route thực tế.
3. Servlet tiếp nhận tại handler thực tế.
4. Handler/helper gọi DAO.
5. DAO thực hiện phương thức cụ thể.
6. Phương thức truy vấn và yêu cầu khởi tạo/đóng gói entity khi thực sự có.
7. Entity được khởi tạo/gán dữ liệu đúng các constructor/setter.
8. Kết quả trở lại bên gọi và được đưa vào collection bởi đúng nơi thực hiện.
9. DAO trả kết quả cho handler/helper.
10. Servlet đặt dữ liệu vào request/session và forward/redirect theo code.
11. JSP hiển thị đúng dữ liệu cho người dùng.

Đây là khuôn cách diễn đạt, không phải bắt mọi thao tác phải đủ 11 bước. Scalar không cần đóng gói entity; route chỉ render không cần DAO; helper thêm một tầng thì phải thể hiện helper đó.

Để giữ format mẫu, có thể tách “Lớp DAO được yêu cầu lấy dữ liệu” và “Phương thức ... thực hiện ...” thành các bước diễn giải. Đó là diễn giải cùng một invocation, không phải hai cuộc gọi độc lập. Không bịa một event “hàm trả về lớp DAO” như lớp DAO là đối tượng trung gian riêng nếu code không có. Bảng truy vết phải đánh dấu các bước cùng thuộc một invocation để về sau vẽ sequence không đếm thành lời gọi giả.

Không cần tách từng lệnh JDBC như `prepareStatement`, `setString`, `ResultSet.next`, đóng connection thành nhiều bước ở kịch bản chính. Phải nêu rõ ý nghĩa truy vấn/ghi dữ liệu và điểm transaction khi ảnh hưởng nghiệp vụ. Constructor/setter có thể nhóm một bước đóng gói nếu không làm mất thứ tự giữa các đối tượng và giữa các tầng. Không tự tạo tên phương thức như `packageInfo()`/`validate()` khi code chỉ có constructor hoặc kiểm tra nội tuyến.

Chỉ happy case trong danh sách chính. Tiền điều kiện ngắn ở đầu: đã đăng nhập, có dữ liệu phù hợp, điều kiện nhập hợp lệ và các điều kiện cần để đi hết luồng. Ngoại lệ và bug ghi ở báo cáo review, không thêm `nếu/else` vào happy case. Nếu code thiếu một hành động khiến luồng không thể hoàn thành, báo luồng bị chặn tại bước nào, không bịa thành công.

Không ấn định số bước theo mẫu cũ (54, 105 hoặc bất kỳ số nào). Số bước do code và độ chi tiết yêu cầu quyết định. Không gộp nhiều invocation khác tầng vào một câu chung chung để rút số bước.

### 6.2. Các đoạn mẫu cách diễn đạt, không phải code có sẵn

Chỉ thay tên minh họa dưới đây bằng chữ ký thực tế sau khi đọc project:

1. Trang A.jsp gửi yêu cầu GET đến route ... để lấy dữ liệu ...
2. Lớp XServlet6 tiếp nhận yêu cầu tại phương thức doGet(...).
3. Phương thức doGet(...) gọi xDAO.getSomething(a, b) để lấy ...
4. Phương thức getSomething(a, b) của lớp XDAO6 thực hiện truy vấn ... và khởi tạo đối tượng Y6 bằng constructor ...
5. Đối tượng Y6 được gán các thuộc tính ... theo dữ liệu truy vấn.
6. Constructor hoàn tất, trả tham chiếu Y6 cho getSomething(...); phương thức này thêm đối tượng vào danh sách kết quả.
7. Phương thức getSomething(...) trả danh sách ... cho doGet(...) của XServlet6.
8. Phương thức doGet(...) đặt danh sách vào request với tên attribute chính xác ..., rồi forward đến B.jsp.
9. Trang B.jsp hiển thị ...

Với thao tác ghi: mô tả đúng nơi khởi tạo entity → nơi nhận đối tượng → lời gọi DAO → SQL/transaction → kết quả hoặc hoàn tất void → phản hồi/render. Kiểm tra luồng đặc biệt ở mục 3 trước khi chọn ai là bên tạo entity. Không được dùng đoạn minh họa này thay cho việc đọc mã.

### 6.3. Phạm vi riêng của Module 1

Luồng chính bắt đầu ở trang chủ khách hàng đã đăng nhập và kết thúc ở thông báo đặt lịch thành công thực tế của code. Bao gồm:
- Mở form, lấy khách hàng, danh sách xe và cách lấy slot khi chọn ngày.
- User chọn slot, xe và nhập mô tả; chỉ ghi các trường UI thực sự cho nhập theo bản code hiện tại.
- Validation thành công, giữ chỗ, tạo/lấy đối tượng tạm, chuyển trang xác nhận.
- User xác nhận trong thời gian giữ chỗ; kiểm tra giữ chỗ, lưu lịch hẹn, commit và hiển thị kết quả.
- Nếu AJAX trả JSON để cập nhật dropdown thì mô tả JSON và thao tác cập nhật UI; không bịa forward JSP.
- Nếu trang thành công riêng không tồn tại thì không tự thêm; nếu code có thì dùng đúng trang đó và đánh dấu khác thiết kế nếu có.

### 6.4. Phạm vi riêng của Module 2 và cách thể hiện hai lần xem chi tiết

Giữ yêu cầu trước đây của tôi: chỉ khai triển hai lượt ở phần xem chi tiết, lượt 1 là dịch vụ, lượt 2 là phụ tùng; phần mở báo cáo/chọn bộ lọc/thống kê trước đó và phần xem hóa đơn sau đó trình bày chung khi code cho phép. Đánh số liên tục toàn module, không quay lại số 1, không dùng câu “làm tương tự” để bỏ qua các DAO/phương thức khác của phụ tùng.

Cấu trúc mong muốn:
- Đoạn chung: trang chủ → menu báo cáo nếu có → bộ lọc → danh sách thống kê.
- Lượt dịch vụ: chọn dòng dịch vụ → lấy thông tin dịch vụ → lấy tổng số lần sử dụng toàn thời gian → lấy danh sách lần sử dụng trong kỳ → hiển thị chi tiết.
- Chỉ cần một bước chuyển ngữ cảnh Quay lại danh sách nếu code cho phép, không khai triển lại cả chuỗi quay về và dựng lại màn hình.
- Lượt phụ tùng: chọn dòng phụ tùng → lấy thông tin phụ tùng → tổng số lần sử dụng toàn thời gian → lịch sử trong kỳ → hiển thị chi tiết.
- Đoạn chung cuối: chọn Xem hóa đơn ở một dòng sử dụng đang hiển thị → route/Servlet → DAO và các entity/mapper thực tế → trả kết quả → hóa đơn chi tiết.

Tuy nhiên, ưu tiên sự thật của code: nếu danh sách chỉ chứa một loại khi statisticType=SERVICE hoặc PART thì không được nói cùng danh sách hiện tại có cả hai loại. Chọn cách mô tả sau:
- Nếu code có loại BOTH/ALL thì có thể chọn loại đó chỉ khi thực sự được hỗ trợ và đúng nghiệp vụ.
- Nếu phải đổi bộ lọc sang phụ tùng, ghi ngắn bước đổi loại và tái thực hiện đoạn lấy thống kê chung bằng dẫn chiếu chính xác số bước đã mô tả, với giá trị tham số mới. Phải ghi rõ đó là đoạn lặp được tham chiếu, không phải lời gọi bị loại bỏ khỏi thực thi.
- Nếu không thể đáp ứng “chỉ lặp phần chi tiết” mà vẫn trung thực với code, báo xung đột ở phần mở đầu; ưu tiên V3 đúng code và đề xuất cách cập nhật yêu cầu/thiết kế riêng. Không sửa code, thêm lựa chọn ALL hoặc giả vờ người dùng đã ở danh sách khác.

Nếu bộ lọc ngày áp dụng trong SQL thì ghi DAO lọc SQL; nếu code lấy tất cả rồi Servlet lọc thì ghi đúng nơi lọc và đánh giá khác biệt. Không chuyển trách nhiệm cho Servlet chỉ vì mẫu cũ viết như vậy.

## 7. Báo cáo khác biệt và bằng chứng

Mỗi khác biệt có mã `D001`, `D002`... Không chỉ liệt kê lỗi lớn. Dùng bảng:

| Mã | Module/tầng | Nguồn thiết kế và vị trí | Thiết kế/yêu cầu | Code thực tế (file:dòng) | Phân loại/mức ảnh hưởng | Tác động | Đề xuất sửa code hay sửa thiết kế |
|---|---|---|---|---|---|---|---|

Ví dụ những khác biệt nhỏ vẫn cần ghi: `SaleStaff6`/`SalesStaff6`, `InvoiceServlet`/`InvoiceServlet6`, `createAt`/`createdAt`, tên attribute trong JSP, thứ tự tham số, boolean/Boolean, mảng/List, POST/GET, format ngày, default status và cách xác định thời hạn giữ chỗ. Đây chỉ là ví dụ; không khẳng định project có các lỗi đó.

Mỗi phát hiện phải trỏ tới bằng chứng thật. Đừng ghi số dòng hoặc ảnh minh họa không tồn tại. Thiết kế dạng ảnh thì chỉ rõ tên ảnh, tên lớp và thuộc tính/phương thức đang xét; ảnh mờ ghi chưa đọc chắc chắn.

Tạo bảng truy vết riêng cho từng module:

| Bước V3 | Bên gọi | Bên nhận | Method/constructor/inline action | Tham số thực tế và nguồn | Kiểu/kết quả trả | File:dòng | Loại bước/bằng chứng |
|---|---|---|---|---|---|---|---|

- Các bước user/UI vẫn phải có bằng chứng link/form/control/render tương ứng.
- Phân biệt khai báo phương thức với call site; cần call site để chứng minh thứ tự.
- Nếu hai bước chỉ diễn giải cùng một invocation, ghi rõ để không chuyển thành hai message khác nhau khi vẽ diagram.
- Mỗi kết quả trả về phải đến đúng activation đang gọi; không trả thẳng qua tầng trung gian đã thực sự tồn tại.
- Nếu constructor trả entity nhưng DAO trả List thì thể hiện quá trình tích lũy danh sách; không đổi hai kiểu trả cho nhau.

## 8. Kiểm chứng và giới hạn

Sau khi đọc tĩnh, chạy build và các kiểm thử phù hợp đã có. Nếu môi trường cho phép, thực hiện hai happy case trên hệ thống chạy thật với DB test:
- Đặt lịch thành công; đối chiếu slot/token/status, appointment, FK khách hàng/xe và thời điểm commit.
- Thống kê dịch vụ và phụ tùng; đối chiếu kết quả với SQL, ngày cuối kỳ, chỉ đã thanh toán, lifetime count, lịch sử trong kỳ, phân trang và hóa đơn đủ dòng.

Review thêm race condition giữ chỗ/xác nhận, submit hai lần, xác thực quyền và SQL injection trong phạm vi hai module; những nhánh này không đưa vào scenario happy case nhưng phải báo nếu sai. Không tuyên bố đã chạy thử concurrency nếu chỉ đọc code.

Ghi rõ: lệnh đã chạy, kết quả, trường hợp đã kiểm tra, trường hợp không chạy được và lý do. Nếu không có DB hoặc mật khẩu thì vẫn hoàn thành review tĩnh và V3 dựa trên code, đánh dấu chưa kiểm chứng runtime. Không in secret vào báo cáo.

## 9. Đầu ra bắt buộc

Tạo các file UTF-8 sau trong project:
1. `docs/review/00_Nguon_chuan_va_pham_vi.md`: nguồn đã đọc, phiên bản, quy tắc từ hướng dẫn và giới hạn kiểm chứng.
2. `docs/review/01_Doi_chieu_thiet_ke_va_code.md`: inventory lớp/chữ ký và bảng tất cả khác biệt, gồm riêng bảng vị trí đóng gói khi ghi DB.
3. `docs/scenario/Scenario_V3_Module1.md`: kịch bản hiện trạng Module 1, đánh số liên tục, đúng format tiếng Việt, không chèn bảng evidence giữa các bước.
4. `docs/scenario/Scenario_V3_Module2.md`: kịch bản hiện trạng Module 2 theo quy tắc hai lượt xem chi tiết ở trên.
5. `docs/review/02_Truy_vet_Scenario_V3.md`: bảng truy vết bước → code cho cả hai module.
6. `docs/review/03_Kiem_chung_va_de_xuat_cap_nhat.md`: kết quả build/test, danh sách chưa kiểm chứng, danh sách sửa thiết kế hoặc sửa code được đề xuất, ảnh hưởng đến các sơ đồ.

Không xuất một bản “đã sửa code” hay một Scenario V3 mục tiêu giả định thay cho bản hiện trạng. Nếu đề xuất thêm luồng tương lai, đặt riêng và ghi rõ chưa triển khai.

Cuối cùng, trả lời ngắn trong chat:
- Đã review những nguồn/phạm vi nào; có bao nhiêu khác biệt theo từng loại.
- Luồng đóng gói “giao diện → entity → giao diện → Servlet/DAO” có thật trong code không, và ở thao tác nào.
- Scenario V3 được sinh theo commit/trạng thái code nào và mức kiểm chứng thực tế.
- Các vấn đề cần xử lý trước khi chốt lại thiết kế/triển khai.
- Liên kết tới các file đầu ra.

## 10. Tự kiểm trước khi nộp kết quả

- [ ] Không file nguồn nào bị tự sửa trong quá trình review.
- [ ] Mọi tên lớp, tên hàm và tham số trong V3 đều có trong code; constructor, setter và inline action được mô tả đúng bản chất.
- [ ] Không bịa lớp trung gian, self-call doGet/doPost, bước trả entity từ setter void, hoặc DAO gọi chính mình hai lần chỉ vì cách trình bày mẫu.
- [ ] Luồng điều hướng đúng GET/POST/forward/redirect/AJAX; dữ liệu qua request/session đúng scope.
- [ ] Phân biệt đóng gói entity khi đọc DB với đóng gói dữ liệu trước lưu; vị trí đóng gói đúng code.
- [ ] Truy vấn, mapper, helper và thứ tự gọi được kiểm tra tới phần triển khai; không chỉ đọc interface.
- [ ] Không bỏ qua khác biệt nhỏ; không coi khác biệt đã được chấp thuận là bug.
- [ ] Không viết thành công khi luồng code bị chặn hoặc thiếu commit.
- [ ] Module 2 không giả định cùng danh sách chứa hai loại khi code không hỗ trợ.
- [ ] Các bước V3 đánh số liên tục riêng từng module, không ép số bước của bản mẫu.
- [ ] Mỗi bước đều truy được về bằng chứng code; đã phân biệt kiểm chứng tĩnh và chạy thực tế.
- [ ] Không tuyên bố khớp tuyệt đối vượt quá phạm vi và bằng chứng đã kiểm tra.

Hãy bắt đầu bằng việc đọc project và các tài liệu có thật, rồi hoàn thành toàn bộ đầu ra. Chỉ yêu cầu bổ sung khi thiếu thông tin thiết yếu không thể tìm trong project; vẫn tiếp tục những phần review độc lập còn thực hiện được.
