package project.swp.spring.sebt_platform.service;

import project.swp.spring.sebt_platform.dto.request.SupportRequestDTO;

public interface MailService {
    public void sendVerificationEmail(String toEmail, String verificationToken);
    
    /**
     * Gửi email hỗ trợ từ khách hàng đến admin
     * @param supportRequest Thông tin yêu cầu hỗ trợ từ khách hàng
     */
    public void sendSupportEmail(SupportRequestDTO supportRequest);
}
