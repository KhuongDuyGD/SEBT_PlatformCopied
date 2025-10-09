package project.swp.spring.sebt_platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project.swp.spring.sebt_platform.dto.request.SupportRequestDTO;
import project.swp.spring.sebt_platform.service.MailService;

/**
 * Controller xử lý các yêu cầu liên quan đến hỗ trợ khách hàng
 * Bao gồm việc nhận form hỗ trợ và gửi email đến admin
 */
@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SupportController {

    @Autowired
    private MailService mailService;

    /**
     * Endpoint nhận form hỗ trợ từ frontend và gửi email đến admin
     * POST /api/support/send
     * 
     * @param supportRequest Dữ liệu form hỗ trợ từ khách hàng
     * @return ResponseEntity với thông báo kết quả
     */

    @PostMapping("/send")
    public ResponseEntity<?> sendSupportRequest(@RequestBody SupportRequestDTO supportRequest) {
        try {
            // Kiểm tra tính hợp lệ của dữ liệu đầu vào
            if (!supportRequest.isValid()) {
                return ResponseEntity.badRequest()
                    .body(new SupportResponse(false, "Vui lòng điền đầy đủ thông tin yêu cầu"));
            }

            // Kiểm tra format email cơ bản
            if (!isValidEmail(supportRequest.email())) {
                return ResponseEntity.badRequest()
                    .body(new SupportResponse(false, "Email không hợp lệ"));
            }

            // Gửi email hỗ trợ đến admin
            mailService.sendSupportEmail(supportRequest);

            // Trả về thông báo thành công
            return ResponseEntity.ok(
                new SupportResponse(true, "Yêu cầu hỗ trợ đã được gửi thành công. Chúng tôi sẽ phản hồi trong vòng 24 giờ.")
            );

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi yêu cầu hỗ trợ: " + e.getMessage());
            e.printStackTrace();

            // Trả về thông báo lỗi
            return ResponseEntity.internalServerError()
                .body(new SupportResponse(false, "Có lỗi xảy ra khi gửi yêu cầu. Vui lòng thử lại sau."));
        }
    }

    /**
     * Endpoint lấy danh sách FAQ (Câu hỏi thường gặp)
     * GET /api/support/faq
     * 
     * @return ResponseEntity chứa danh sách FAQ
     */
    @GetMapping("/faq")
    public ResponseEntity<?> getFAQ() {
        try {
            FAQItem[] faqItems = {
                new FAQItem(
                    "Làm sao để đăng tin bán xe/pin?",
                    "Bạn cần đăng nhập tài khoản, sau đó click vào \"Đăng tin\" và điền đầy đủ thông tin sản phẩm. Hệ thống sẽ xem xét và phê duyệt tin đăng của bạn trong vòng 24 giờ."
                ),
                new FAQItem(
                    "Tại sao tài khoản tôi bị khóa?",
                    "Tài khoản có thể bị khóa do vi phạm quy định như: đăng tin giả mạo, spam, hoặc có hành vi gian lận. Vui lòng liên hệ support để được hỗ trợ mở khóa."
                ),
                new FAQItem(
                    "Làm sao để thay đổi thông tin cá nhân?",
                    "Vào trang Profile, click \"Chỉnh sửa\" và cập nhật thông tin mới. Nhớ lưu lại sau khi chỉnh sửa."
                ),
                new FAQItem(
                    "Quên mật khẩu phải làm sao?",
                    "Click vào \"Quên mật khẩu\" tại trang đăng nhập, nhập email và làm theo hướng dẫn được gửi qua email."
                ),
                new FAQItem(
                    "Tôi không nhận được email xác thực?",
                    "Kiểm tra thư mục spam/junk mail. Nếu vẫn không có, hãy thử đăng ký lại hoặc liên hệ support."
                ),
                new FAQItem(
                    "Làm sao để tìm kiếm sản phẩm phù hợp?",
                    "Sử dụng thanh tìm kiếm ở đầu trang, hoặc dùng bộ lọc nâng cao để tìm theo giá, loại xe, khu vực..."
                ),
                new FAQItem(
                    "Tôi có thể tin tưởng người bán không?",
                    "Kiểm tra thông tin hồ sơ người bán, đánh giá từ người dùng khác, và luôn gặp mặt trực tiếp khi giao dịch."
                ),
                new FAQItem(
                    "Phí dịch vụ của platform là bao nhiều?",
                    "Hiện tại việc đăng tin và tìm kiếm đều miễn phí. Chúng tôi chỉ thu phí cho các tính năng nâng cao."
                )
            };

            return ResponseEntity.ok(new FAQResponse(true, faqItems));

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách FAQ: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new FAQResponse(false, new FAQItem[0]));
        }
    }

    /**
     * Kiểm tra format email cơ bản
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Response class cho API support
     */
    public static record SupportResponse(boolean success, String message) {}

    /**
     * Response class cho API FAQ
     */
    public static record FAQResponse(boolean success, FAQItem[] data) {}

    /**
     * Class đại diện cho một câu hỏi FAQ
     */
    public static record FAQItem(String question, String answer) {}
}
