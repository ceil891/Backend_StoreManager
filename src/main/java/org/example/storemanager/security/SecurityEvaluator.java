package org.example.storemanager.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityEvaluator")
public class SecurityEvaluator {

    public boolean hasPermission(String permission) {
        // 1. LẤY THÔNG TIN ĐĂNG NHẬP HIỆN TẠI TỪ SECURITY CONTEXT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. NẾU CHƯA CÓ TOKEN HOẶC LÀ ANONYMOUS -> CHẶN NGAY LẬP TỨC
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }

        // 3. LOGIC CHECK QUYỀN THỰC TẾ SẼ VIẾT Ở ĐÂY
        // Ví dụ:
        // String username = (String) auth.getPrincipal();
        // Lấy User từ DB -> Lấy Role -> Lấy List Permission -> So sánh với chuỗi 'permission' truyền vào

        // Tạm thời trả về true cho tài khoản đã có Token
        return true;
    }
}