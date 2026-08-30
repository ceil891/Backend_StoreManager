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

    public void sendManagerNotificationEmail(String managerEmail, String managerName, String employeeName, String employeeEmail, String employeeRole, String branchName) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(managerEmail);
                helper.setSubject("Thông báo nhân sự mới được thêm vào hệ thống RetailHub");

                String branchText = branchName != null ? branchName : "Toàn hệ thống";
                String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                        "  <h2 style=\"color: #1a73e8; text-align: center;\">Thông Báo Nhân Sự Mới</h2>" +
                        "  <p>Kính gửi Quản lý <strong>" + managerName + "</strong>,</p>" +
                        "  <p>Hệ thống xin thông báo một nhân sự mới vừa được cấp tài khoản và chỉ định thuộc phạm vi quản lý của bạn:</p>" +
                        "  <div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #34a853;\">" +
                        "    <p style=\"margin: 5px 0;\"><strong>Họ và tên nhân sự:</strong> " + employeeName + "</p>" +
                        "    <p style=\"margin: 5px 0;\"><strong>Email liên hệ:</strong> " + employeeEmail + "</p>" +
                        "    <p style=\"margin: 5px 0;\"><strong>Vai trò:</strong> " + employeeRole + "</p>" +
                        "    <p style=\"margin: 5px 0;\"><strong>Chi nhánh làm việc:</strong> " + branchText + "</p>" +
                        "  </div>" +
                        "  <p>Bạn có thể đăng nhập vào hệ thống để phân quyền chi tiết hoặc theo dõi hoạt động của nhân sự này.</p>" +
                        "  <hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\" />" +
                        "  <p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email thông báo tự động từ hệ thống RetailHub.</p>" +
                        "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Đã gửi email thông báo nhân sự mới đến quản lý [{}]", managerEmail);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email thông báo quản lý đến [{}]: {}", managerEmail, e.getMessage(), e);
            }
        });
    }

    public void sendWelcomeCustomerEmail(String toEmail, String fullName, String username, String voucherCode) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(toEmail);
                helper.setSubject("Chào mừng bạn gia nhập RetailHub - Nhận ngay Voucher ưu đãi!");

                String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                        "  <h2 style=\"color: #059669; text-align: center;\">Chào Mừng Bạn Đến Với RetailHub!</h2>" +
                        "  <p>Xin chào <strong>" + fullName + "</strong>,</p>" +
                        "  <p>Cảm ơn bạn đã đăng ký tài khoản thành viên tại <strong>RetailHub</strong>. Tài khoản <strong>" + username + "</strong> của bạn đã sẵn sàng sử dụng.</p>" +
                        "  <div style=\"background-color: #ecfdf5; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px dashed #059669; text-align: center;\">" +
                        "    <h3 style=\"margin-top: 0; color: #047857;\">Món quà chào mừng dành riêng cho bạn 🎁</h3>" +
                        "    <p style=\"margin: 5px 0; color: #333;\">Sử dụng mã Voucher giảm giá khi mua sắm:</p>" +
                        "    <div style=\"display: inline-block; background: #059669; color: #ffffff; font-size: 22px; font-weight: bold; padding: 10px 24px; border-radius: 6px; letter-spacing: 2px; margin: 10px 0;\">" +
                        voucherCode +
                        "    </div>" +
                        "    <p style=\"margin: 5px 0; font-size: 13px; color: #065f46;\">Giảm 10% cho đơn hàng đầu tiên (tối đa 50.000đ)</p>" +
                        "  </div>" +
                        "  <p>Chúc bạn có những trải nghiệm mua sắm tuyệt vời tại RetailHub!</p>" +
                        "  <hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\" />" +
                        "  <p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email tự động từ hệ thống RetailHub.</p>" +
                        "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Đã gửi email chào mừng thành công đến khách hàng [{}]", toEmail);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email chào mừng đến [{}]: {}", toEmail, e.getMessage(), e);
            }
        });
    }

    public void sendForgotPasswordOtpEmail(String toEmail, String fullName, String otpCode) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(toEmail);
                helper.setSubject("Mã xác thực OTP đặt lại mật khẩu - RetailHub");

                String displayName = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";
                String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                        "  <h2 style=\"color: #d97706; text-align: center;\">Yêu Cầu Đặt Lại Mật Khẩu</h2>" +
                        "  <p>Xin chào <strong>" + displayName + "</strong>,</p>" +
                        "  <p>Chúng tôi nhận được yêu cầu cấp lại mật khẩu cho tài khoản liên kết với địa chỉ email này trên hệ thống <strong>RetailHub</strong>.</p>" +
                        "  <p>Mã xác thực OTP của bạn là:</p>" +
                        "  <div style=\"background-color: #fffbeb; padding: 15px; border-radius: 6px; margin: 20px 0; text-align: center; border: 1px solid #fde68a;\">" +
                        "    <span style=\"font-family: monospace; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #b45309;\">" + otpCode + "</span>" +
                        "    <p style=\"margin: 8px 0 0 0; font-size: 13px; color: #92400e;\">Mã OTP có hiệu lực trong vòng <strong>10 phút</strong>.</p>" +
                        "  </div>" +
                        "  <p style=\"color: #dc2626; font-size: 13px;\"><em>* Tuyệt đối không chia sẻ mã OTP này cho bất kỳ ai để đảm bảo an toàn cho tài khoản của bạn.</em></p>" +
                        "  <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc liên hệ với bộ phận Quản trị hệ thống.</p>" +
                        "  <hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\" />" +
                        "  <p style=\"font-size: 12px; color: #888; text-align: center;\">Đây là email tự động từ hệ thống RetailHub.</p>" +
                        "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Đã gửi email mã OTP đặt lại mật khẩu thành công đến [{}]", toEmail);
            } catch (Exception e) {
                log.error("Lỗi khi gửi email OTP đến [{}]: {}", toEmail, e.getMessage(), e);
            }
        });
    }
}
