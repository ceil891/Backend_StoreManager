package org.example.storemanager.shared.security;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Data Scope Specification:
 * Tự động tạo điều kiện truy vấn SQL/JPA dựa theo UserContext:
 * - Phạm vi ALL: Không giới hạn (xem toàn bộ hệ thống)
 * - Phạm vi BRANCH: Bắt buộc branch.id = user.branchId hoặc branchId = user.branchId
 * - Phạm vi PERSONAL: Bắt buộc createdBy = user.username
 */
public class DataScopeSpecification {

    public static <T> Specification<T> byUserScope() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            UserContext ctx = UserContextHolder.getContext();
            if (ctx == null || ctx.canViewAllBranches()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Nếu user bị giới hạn theo chi nhánh
            if (ctx.getBranchId() != null) {
                try {
                    // Thử khớp root.get("branch").get("id")
                    if (hasPath(root, "branch")) {
                        predicates.add(cb.equal(root.get("branch").get("id"), ctx.getBranchId()));
                    } else if (hasPath(root, "branchId")) {
                        predicates.add(cb.equal(root.get("branchId"), ctx.getBranchId()));
                    }
                } catch (Exception ignored) {}
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasPath(Root<?> root, String attributeName) {
        try {
            return root.get(attributeName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
