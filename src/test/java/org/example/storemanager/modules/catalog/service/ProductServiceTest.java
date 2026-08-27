package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.response.product.DeleteProductResponse;
import org.example.storemanager.modules.catalog.dto.response.product.UpdateProductResponse;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductCategory;
import org.example.storemanager.modules.catalog.entity.Unit;
import org.example.storemanager.modules.catalog.repository.*;
import org.example.storemanager.modules.catalog.service.impl.ProductServiceImpl;
import org.example.storemanager.modules.common.service.CloudinaryService;
import org.example.storemanager.modules.inventory.repository.InventoryBalanceRepository;
import org.example.storemanager.modules.inventory.repository.SizeInventoryRepository;
import org.example.storemanager.modules.inventory.repository.StockLedgerRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoriesRepository categoriesRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private SizeInventoryRepository sizeInventoryRepository;

    @Mock
    private ProductUnitService productUnitService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private InventoryBalanceRepository inventoryBalanceRepository;

    @Mock
    private ProductVariantService productVariantService;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductCategory sampleCategory;
    private Unit sampleUnit;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin_user", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleCategory = ProductCategory.builder()
                .categoryName("Thời trang nam")
                .categoryCode("CAT01")
                .build();
        sampleCategory.setId(1L);

        sampleUnit = Unit.builder()
                .unitName("Cái")
                .unitCode("CAI")
                .build();
        sampleUnit.setId(1L);

        sampleProduct = Product.builder()
                .name("Áo Polo Cao Cấp")
                .productCode("POLO001")
                .basePrice(new BigDecimal("250000"))
                .costPrice(new BigDecimal("150000"))
                .category(sampleCategory)
                .baseUnit(sampleUnit)
                .build();
        sampleProduct.setId(100L);
        sampleProduct.setIsActive(true);
        sampleProduct.setIsDeleted(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("1. Cập nhật trạng thái sản phẩm (updateStatus)")
    class UpdateStatusTests {

        @Test
        @DisplayName("Cập nhật trạng thái kích hoạt thành công")
        void updateStatus_Success() {
            when(productRepository.findByIdAndIsDeletedFalse(100L))
                    .thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            UpdateProductResponse res = productService.updateStatus(100L, false);

            assertThat(res).isNotNull();
            assertThat(sampleProduct.getIsActive()).isFalse();
            verify(productRepository, times(1)).save(sampleProduct);
        }

        @Test
        @DisplayName("Cập nhật trạng thái thất bại khi không tìm thấy sản phẩm")
        void updateStatus_ProductNotFound_ThrowsException() {
            when(productRepository.findByIdAndIsDeletedFalse(999L))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> productService.updateStatus(999L, true));
        }
    }

    @Nested
    @DisplayName("2. Xóa mềm sản phẩm (deleteProduct)")
    class DeleteProductTests {

        @Test
        @DisplayName("Xóa thành công khi sản phẩm đã tắt hoạt động và tồn kho bằng 0")
        void deleteProduct_SuccessWhenInactiveAndZeroStock() {
            sampleProduct.setIsActive(false); // đã tắt hoạt động

            when(productRepository.findByIdAndIsDeletedFalse(100L))
                    .thenReturn(Optional.of(sampleProduct));
            when(sizeInventoryRepository.findByProductIdAndIsDeletedFalse(100L))
                    .thenReturn(Collections.emptyList());
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            DeleteProductResponse res = productService.deleteProduct(100L);

            assertThat(res).isNotNull();
            assertThat(sampleProduct.getIsDeleted()).isTrue();
            verify(productRepository, times(1)).save(sampleProduct);
        }

        @Test
        @DisplayName("Xóa thất bại khi sản phẩm vẫn đang ACTIVE -> Báo lỗi Conflict (409)")
        void deleteProduct_FailsWhenActive() {
            sampleProduct.setIsActive(true); // vẫn active

            when(productRepository.findByIdAndIsDeletedFalse(100L))
                    .thenReturn(Optional.of(sampleProduct));

            assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(100L));
            verify(productRepository, never()).save(any());
        }
    }
}
