package org.example.storemanager.shared.security;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * Tự động giải quyết branchId hợp lệ:
     * - Nếu User có quyền xem tất cả chi nhánh -> cho phép dùng requestBranchId do client gửi lên.
     * - Nếu User là nhân viên chi nhánh cố định -> BẮT BUỘC ghi đè bằng branchId của User.
     */
    public static Long getEffectiveBranchId(Long requestBranchId) {
        UserContext ctx = getContext();
        if (ctx == null) {
            return requestBranchId;
        }
        if (ctx.canViewAllBranches()) {
            return requestBranchId;
        }
        return ctx.getBranchId() != null ? ctx.getBranchId() : requestBranchId;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
