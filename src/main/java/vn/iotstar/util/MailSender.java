package vn.iotstar.util;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public final class MailSender {
    private MailSender() {
    }

    public static void sendOtp(String to, String fullName, String purpose, String code)
            throws MessagingException {
        String host = setting("MAIL_HOST", "");
        String port = setting("MAIL_PORT", "587");
        String username = setting("MAIL_USERNAME", "");
        String password = setting("MAIL_PASSWORD", "");
        String from = setting("MAIL_FROM", username);
        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new MessagingException("Configure MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM first.");
        }

        boolean ssl = "465".equals(port);
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put(ssl ? "mail.smtp.ssl.enable" : "mail.smtp.starttls.enable", "true");
        if (!ssl) properties.put("mail.smtp.starttls.required", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        message.setSubject("Mã OTP cho Web Homework", StandardCharsets.UTF_8.name());
        String explanation = switch (purpose) {
            case "ACTIVATION" -> "để kích hoạt tài khoản";
            case "EMAIL_CHANGE" -> "để xác nhận thay đổi email";
            default -> "để đặt lại mật khẩu";
        };
        message.setText("Xin chào " + safeName(fullName) + ",\n\n"
                + "Mã OTP " + explanation + " của bạn là: " + code + "\n"
                + "Mã có hiệu lực trong 10 phút và chỉ dùng được một lần.\n\n"
                + "Nếu bạn không yêu cầu mã này, hãy bỏ qua email.", StandardCharsets.UTF_8.name());
        Transport.send(message);
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) value = System.getProperty(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String safeName(String value) {
        return value == null || value.isBlank() ? "bạn" : value.replaceAll("[\\r\\n]", " ");
    }
}
