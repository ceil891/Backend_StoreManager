package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.variant.CreateVariantRequest;
import org.example.storemanager.dto.request.catalog.variant.UpdateVariantRequest;
import org.example.storemanager.dto.response.catalog.variant.CreateVariantResponse;
import org.example.storemanager.dto.response.catalog.variant.VariantResponse;

import java.util.List;

public interface ProductVariantService {

    /**
     * Tạo (các) biến thể cho sản phẩm.
     * - VariantStrategy.NONE → sinh 1 variant mặc định.
     * - VariantStrategy.ATTRIBUTE_BASED → sinh N variants từ attributeCombinations.
     */
    List<CreateVariantResponse> createVariants(CreateVariantRequest request);

    /**
     * Cập nhật thông tin biến thể (barcode, ảnh, giá override).
     * SKU và variantCode là immutable → không thay đổi.
     */
    VariantResponse updateVariant(Long id, UpdateVariantRequest request);

    /**
     * Xóa mềm một biến thể (chỉ khi đã tắt hoạt động).
     */
    void deleteVariant(Long id);

    /**
     * Bật/tắt hoạt động biến thể.
     */
    VariantResponse toggleStatus(Long id, Boolean isActive);

    /**
     * Xem chi tiết 1 biến thể kèm thuộc tính.
     */
    VariantResponse getById(Long id);

    /**
     * Lấy toàn bộ biến thể của 1 sản phẩm.
     */
    List<VariantResponse> getByProductId(Long productId);
}
