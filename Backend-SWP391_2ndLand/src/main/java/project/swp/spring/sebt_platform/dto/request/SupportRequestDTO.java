package project.swp.spring.sebt_platform.dto.request;

/**
 * DTO để nhận dữ liệu từ form hỗ trợ người dùng
 * Chứa thông tin cần thiết để gửi email hỗ trợ
 */
public record SupportRequestDTO(
    String fullName,        // Họ và tên khách hàng
    String email,           // Email khách hàng để phản hồi
    String requestType,     // Loại yêu cầu: technical, account, listing, payment, other
    String subject,         // Tiêu đề yêu cầu
    String description      // Mô tả chi tiết vấn đề
) {
    
    /**
     * Validation method để kiểm tra dữ liệu đầu vào
     * @return true nếu dữ liệu hợp lệ
     */
    public boolean isValid() {
        return fullName != null && !fullName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               requestType != null && !requestType.trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               description != null && !description.trim().isEmpty();
    }
    
    /**
     * Lấy loại yêu cầu dưới dạng text tiếng Việt
     * @return Tên loại yêu cầu bằng tiếng Việt
     */
    public String getRequestTypeInVietnamese() {
        return switch (requestType) {
            case "technical" -> "Hỗ trợ kỹ thuật";
            case "account" -> "Vấn đề tài khoản";
            case "listing" -> "Vấn đề về listing";
            case "payment" -> "Vấn đề thanh toán";
            case "other" -> "Khác";
            default -> requestType;
        };
    }
}
