package org.example.storemanager.repository.advancedaccounting;

import org.example.storemanager.entity.advancedaccounting.ChartOfAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {
    boolean existsByAccountCode(String accountCode);

    // Tìm các tài khoản cha (parentId is null) để hiển thị danh sách cấp 1
    Page<ChartOfAccount> findByParentIsNull(Pageable pageable);
}
