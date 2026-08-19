package org.example.storemanager.shared.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendAccountInfoEmail(String toEmail, String fullName, String username, String password) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(toEmail);
                helper.setSubject("Thông tin tài khoản đăng nhập hệ thống RetailHub");

                String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                        "  <h2 style=\"color: #1a73e8; text-align: center;\">Chào mừng bạn đến với RetailHub</h2>" +
                        "  <p>Xin chào <strong>" + fullName + "</strong>,</p>" +
                        "  <p>Tài khoản làm việc của bạn trên hệ thống quản lý <strong>RetailHub</strong> đã được khởi tạo thành công. Dưới đây là thông tin đăng nhập của bạn:</p>" +
                        "  <div style=\"background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #1a73e8;\">" +
                        "    <p style=\"margin: 5px 0;\"><strong>Email đăng nhập:</strong> " + toEmail + "</p>" +
                        "    <p style=\"margin: 5px 0;\"><strong>Tên đăng nhập:</strong> " + username + "</p>" +
                        "    <p style=\"margin: 5px 0;\"><strong>Mật khẩu khởi tạo:</strong> <span style=\"font-family: monospace; font-size: 16px; color: #d93025; font-weight: bold;\">" + password + "</span></p>" +
                        "  </div>" +
                        "  <p style=\"color: #e81123;\"><em>* Lưu ý: Vì lý do bảo mật, bạn bắt buộc phải thay đổi mật khẩu ngay trong lần đầu tiên đăng nhập.</em></p>" +
                        "  <p>Nếu bạn không yêu cầu tài khoản này hoặc có bất kỳ câu hỏi nào, vui lòng liên hệ với bộ phận Quản trị hệ thống.</p>" +
                        "  <hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\" />" +
                        "  <p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email tự động từ hệ thống RetailHub. Vui lòng không phản hồi email này.</p>" +
                        "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Đã gửi email thông tin tài khoản thành công đến [{}]", toEmail);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email thông tin tài khoản đến [{}]: {}", toEmail, e.getMessage(), e);
            }
        });
    }
}
