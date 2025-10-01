package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import project.swp.spring.sebt_platform.service.MailService;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async ("emailExecutor")
    @Override
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🚗 EV Secondhand Marketplace - Mã xác thực OTP của bạn");

            String htmlContent = buildVerificationEmailContent(toEmail,verificationToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email", e);

        }
    }

    private String buildVerificationEmailContent(String email, String token) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Xác thực Email - EV Secondhand Marketplace</title>" +
                "    <style>" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa; line-height: 1.6; }" +
                "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }" +
                "        .header { background: linear-gradient(135deg, #416adcff 0%, #5a7ae4 100%); padding: 40px 30px; text-align: center; }" +
                "        .header h1 { color: #ffffff; font-size: 32px; font-weight: 700; margin-bottom: 10px; }" +
                "        .header p { color: #ffffff; font-size: 16px; opacity: 0.9; }" +
                "        .content { padding: 40px 30px; }" +
                "        .welcome-text { font-size: 20px; color: #333333; margin-bottom: 20px; font-weight: 600; }" +
                "        .description { color: #666666; font-size: 16px; margin-bottom: 30px; }" +
                "        .otp-container { background-color: #f8f9fa; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; border: 2px dashed #416adcff; }" +
                "        .otp-label { color: #333333; font-size: 16px; margin-bottom: 15px; font-weight: 600; }" +
                "        .otp-code { font-size: 36px; font-weight: 700; color: #416adcff; letter-spacing: 8px; font-family: 'Courier New', monospace; }" +
                "        .expiry-info { color: #e74c3c; font-size: 14px; margin-top: 15px; font-weight: 500; }" +
                "        .instructions { background-color: #e8f4fd; border-left: 4px solid #416adcff; padding: 20px; margin: 25px 0; border-radius: 8px; }" +
                "        .instructions h3 { color: #416adcff; font-size: 18px; margin-bottom: 10px; }" +
                "        .instructions ol { color: #333333; padding-left: 20px; }" +
                "        .instructions li { margin-bottom: 8px; }" +
                "        .security-notice { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 25px 0; }" +
                "        .security-notice h4 { color: #856404; margin-bottom: 10px; }" +
                "        .security-notice p { color: #856404; font-size: 14px; }" +
                "        .footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }" +
                "        .footer p { color: #666666; font-size: 14px; margin-bottom: 10px; }" +
                "        .footer .company-info { color: #333333; font-weight: 600; }" +
                "        .btn-container { text-align: center; margin: 30px 0; }" +
                "        .btn { display: inline-block; padding: 15px 30px; background-color: #416adcff; color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; }" +
                "        .social-links { margin-top: 20px; }" +
                "        .social-links a { display: inline-block; margin: 0 10px; color: #416adcff; text-decoration: none; }" +
                "        @media only screen and (max-width: 600px) {" +
                "            .container { width: 100% !important; }" +
                "            .header, .content, .footer { padding: 20px !important; }" +
                "            .otp-code { font-size: 28px; letter-spacing: 4px; }" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🚗 EV Secondhand Marketplace</h1>" +
                "            <p>Nền tảng mua bán xe điện cũ hàng đầu Việt Nam</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2 class='welcome-text'>Xin chào!</h2>" +
                "            <p class='description'>" +
                "                Cảm ơn bạn đã đăng ký tài khoản tại <strong>EV Secondhand Marketplace</strong>. " +
                "                Để hoàn tất quá trình đăng ký và bảo mật tài khoản của bạn, " +
                "                vui lòng sử dụng mã OTP bên dưới để xác thực email của bạn." +
                "            </p>" +
                "            <div class='otp-container'>" +
                "                <div class='otp-label'>Mã xác thực OTP của bạn:</div>" +
                "                <div class='otp-code'>" + token + "</div>" +
                "                <div class='expiry-info'>⏰ Mã này sẽ hết hạn sau 10 phút</div>" +
                "            </div>" +
                "            <div class='instructions'>" +
                "                <h3>📋 Hướng dẫn xác thực:</h3>" +
                "                <ol>" +
                "                    <li>Mở ứng dụng hoặc website EV Secondhand Marketplace</li>" +
                "                    <li>Nhập mã OTP gồm 6 số ở trên vào các ô tương ứng</li>" +
                "                    <li>Nhấn nút \"Xác minh\" để hoàn tất</li>" +
                "                    <li>Bắt đầu khám phá những chiếc xe điện tuyệt vời!</li>" +
                "                </ol>" +
                "            </div>" +
                "            <div class='security-notice'>" +
                "                <h4>🔒 Lưu ý bảo mật:</h4>" +
                "                <p>" +
                "                    • Không chia sẻ mã OTP với bất kỳ ai<br>" +
                "                    • EV Secondhand Marketplace sẽ không bao giờ yêu cầu mã OTP qua điện thoại<br>" +
                "                    • Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này" +
                "                </p>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p class='company-info'>EV Secondhand Marketplace</p>" +
                "            <p>Kết nối mọi người với chiếc xe điện ưng ý</p>" +
                "            <p>📧 Email: support@evsecondhand.com | 📞 Hotline: 1900-xxxx</p>" +
                "            <div class='social-links'>" +
                "                <a href='#'>Facebook</a> |" +
                "                <a href='#'>Instagram</a> |" +
                "                <a href='#'>LinkedIn</a>" +
                "            </div>" +
                "            <p style='margin-top: 20px; font-size: 12px; color: #999;'>" +
                "                © 2024 EV Secondhand Marketplace. Tất cả quyền được bảo lưu.<br>" +
                "                Email này được gửi đến: " + email +
                "            </p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
