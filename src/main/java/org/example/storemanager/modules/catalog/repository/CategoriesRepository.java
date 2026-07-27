package org.example.storemanager.modules.catalog.repository;

import org.example.storemanager.modules.catalog.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByIdAndIsDeletedFalse(Long id);

    boolean existsByCategoryCodeAndIsDeletedFalse(String categoryCode);

    boolean existsByCategoryCodeAndIdNotAndIsDeletedFalse(String categoryCode, Long id);

    List<ProductCategory> findByParentIsNullAndIsDeletedFalse();

    List<ProductCategory> findByParentIdAndIsDeletedFalse(Long parentId);

    // Danh sách có hỗ trợ tìm kiếm, lọc, phân trang, kể cả đã xóa
    @Query("SELECT c FROM ProductCategory c WHERE " +
           "(:includeDeleted = true OR c.isDeleted = false) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.categoryCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductCategory> findAllCategoriesIncludeDeleted(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);

    // Lấy tất cả chưa bị xóa để build tree
    @Query("SELECT c FROM ProductCategory c WHERE c.isDeleted = false")
    List<ProductCategory> findAllForTree();
}
