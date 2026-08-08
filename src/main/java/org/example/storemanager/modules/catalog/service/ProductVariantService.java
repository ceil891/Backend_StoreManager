package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.variant.CreateSingleVariantRequest;
import org.example.storemanager.modules.catalog.dto.request.variant.CreateVariantRequest;
import org.example.storemanager.modules.catalog.dto.request.variant.UpdateVariantRequest;
import org.example.storemanager.modules.catalog.dto.response.variant.CreateVariantResponse;
import org.example.storemanager.modules.catalog.dto.response.variant.VariantResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;

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

    VariantResponse getBySku(String sku);

    VariantResponse getByBarcode(String barcode);

    VariantResponse createSingleVariant(Long productId, CreateSingleVariantRequest request);

    List<VariantResponse> getAllVariants();

    ProductVariant buildVariantFromInput(Product product, org.example.storemanager.modules.catalog.dto.request.product.CreateProductRequest.CreateVariantInput input);

    void createAttributeMappings(ProductVariant variant, List<Long> attributeValueIds, String username);

    void bulkInitializeBalances(List<ProductVariant> variants, List<org.example.storemanager.modules.system.entity.Branch> activeBranches, String username);

    ProductVariant ensureDefaultVariant(Product product, String username);
}

