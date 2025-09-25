package project.swp.spring.sebt_platform.service;

public interface MailService {
    public void sendVerificationEmail(String toEmail, String verificationToken);
}
