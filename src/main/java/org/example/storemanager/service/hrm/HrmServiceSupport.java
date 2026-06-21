package org.example.storemanager.service.hrm;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

public final class HrmServiceSupport {

    private HrmServiceSupport() {
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    public static Sort parseSort(String sortParam, String defaultProperty) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(defaultProperty).descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    public static boolean isActive(Boolean isLocked) {
        return !Boolean.TRUE.equals(isLocked);
    }

    public static void applySoftDelete(org.example.storemanager.entity.BaseEntity entity) {
        String username = getCurrentUsername();
        entity.setIsDeleted(true);
        entity.setIsLocked(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(username);
        entity.setUpdatedBy(username);
    }

    public static void requireInactiveBeforeDelete(org.example.storemanager.entity.BaseEntity entity, String label) {
        if (isActive(entity.getIsLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa '" + label + "' vì bản ghi vẫn đang HOẠT ĐỘNG. Vui lòng tắt hoạt động trước."
            );
        }
    }
}
