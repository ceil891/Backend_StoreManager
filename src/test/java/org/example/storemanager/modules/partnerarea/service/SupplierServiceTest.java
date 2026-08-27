package org.example.storemanager.modules.partnerarea.service;

import org.example.storemanager.modules.partnerarea.dto.request.supplier.CreateSupplierRequest;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.CreateSupplierResponse;
import org.example.storemanager.modules.partnerarea.dto.response.supplier.SupplierDetailResponse;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.partnerarea.repository.SupplierRepository;
import org.example.storemanager.modules.partnerarea.service.supplier.impl.SupplierServiceImpl;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Unit Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository repository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier sampleSupplier;

    @BeforeEach
    void setUp() {
        sampleSupplier = Supplier.builder()
                .supplierCode("SUP-001")
                .name("Công Ty TNHH May Mặc Việt")
                .phone("0987654321")
                .email("supplier@vietgarment.com")
                .contactPerson("Nguyễn Giám Đốc")
                .isActive(true)
                .build();
        sampleSupplier.setId(100L);
    }

    @Nested
    @DisplayName("1. Tạo mới nhà cung cấp (Create Supplier)")
    class CreateSupplierTests {

        @Test
        @DisplayName("Tạo nhà cung cấp thành công với email và số điện thoại hợp lệ")
        void createSupplier_Success() {
            CreateSupplierRequest req = new CreateSupplierRequest();
            req.setName("Công Ty TNHH May Mặc Việt");
            req.setPhone("0987654321");
            req.setEmail("supplier@vietgarment.com");
            req.setContactPerson("Nguyễn Giám Đốc");

            when(repository.existsByPhone("0987654321")).thenReturn(false);
            when(repository.existsByEmail("supplier@vietgarment.com")).thenReturn(false);
            when(repository.save(any(Supplier.class))).thenReturn(sampleSupplier);

            CreateSupplierResponse res = supplierService.create(req);

            assertThat(res).isNotNull();
            assertThat(res.getName()).isEqualTo("Công Ty TNHH May Mặc Việt");
            verify(repository, times(1)).save(any(Supplier.class));
        }

        @Test
        @DisplayName("Tạo NCC thất bại khi trùng số điện thoại")
        void createSupplier_DuplicatePhone_ThrowsException() {
            CreateSupplierRequest req = new CreateSupplierRequest();
            req.setName("Công Ty Test");
            req.setPhone("0987654321");

            when(repository.existsByPhone("0987654321")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> supplierService.create(req));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Tạo NCC thất bại khi email không đúng định dạng")
        void createSupplier_InvalidEmail_ThrowsException() {
            CreateSupplierRequest req = new CreateSupplierRequest();
            req.setName("Công Ty Test");
            req.setEmail("invalid-email-format");

            assertThrows(RuntimeException.class, () -> supplierService.create(req));
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Lấy chi tiết nhà cung cấp (Get By ID)")
    class GetSupplierTests {

        @Test
        @DisplayName("Lấy thông tin NCC thành công")
        void getById_Success() {
            when(repository.findById(100L)).thenReturn(Optional.of(sampleSupplier));

            SupplierDetailResponse res = supplierService.getById(100L);

            assertThat(res).isNotNull();
            assertThat(res.getId()).isEqualTo(100L);
            assertThat(res.getName()).isEqualTo("Công Ty TNHH May Mặc Việt");
        }
    }
}
