package project.swp.spring.sebt_platform.util;

import jakarta.servlet.http.HttpServletRequest;
import jdk.jshell.execution.Util;
import org.springframework.stereotype.Component;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class Utils {

    public static String generateSalt(){
        byte[] array = new byte[16];
        new java.security.SecureRandom().nextBytes(array);
        return java.util.Base64.getEncoder().encodeToString(array);
    }

    public static String generatePins(){
        int pinLength = 6;
        StringBuilder pin = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < pinLength; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    public static String encript(String password,String salt){
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

    public static Long getUserIdFromSession(HttpServletRequest request) {
        return (Long) (request.getSession(false) != null ? request.getSession(false).getAttribute("userId") : null);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String createOrderId(WalletPurpose purpose, Long userId) {
        String prefix = "ORDER";
        if (purpose != null) {
            prefix = purpose.name();
        }
        String orderId = prefix +"0"+userId+"0";
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String randomDigits = getRandomNumber(6);
        return orderId+ timestamp + randomDigits;
    }
}
