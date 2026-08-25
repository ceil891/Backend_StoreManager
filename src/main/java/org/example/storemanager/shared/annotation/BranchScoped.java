package org.example.storemanager.shared.annotation;

import java.lang.annotation.*;

/**
 * Đánh dấu Controller Method hoặc Controller Class cần tự động kiểm soát phạm vi chi nhánh (Branch Scoping).
 * Khi phương thức được gọi, BranchScopeAspect sẽ tự động đọc UserContext/JWT/Headers để ép buộc branchId tương ứng.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BranchScoped {
    /**
     * Tên tham số chi nhánh (mặc định: "branchId")
     */
    String paramName() default "branchId";

    /**
     * Cho phép tài khoản Super Admin hoặc có quyền system:branch:view_all bỏ qua bộ lọc chi nhánh
     */
    boolean allowSuperAdminBypass() default true;
}
