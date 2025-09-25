package project.swp.spring.sebt_platform.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Utils {

    public String generateSalt(){
        byte[] array = new byte[16];
        new java.security.SecureRandom().nextBytes(array);
        return java.util.Base64.getEncoder().encodeToString(array);
    }

    public String generatePins(){
        int pinLength = 6;
        StringBuilder pin = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < pinLength; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    public String encript(String password,String salt){
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hashedBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public LocalDateTime getExpireTimeByDuration(int duration) {
        return LocalDateTime.now().plusMinutes(duration);
    }
}
