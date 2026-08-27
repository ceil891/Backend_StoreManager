package org.example.storemanager.modules.partnerarea.service;

import org.example.storemanager.modules.common.service.CloudinaryService;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CreateCustomerRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.CreateCustomerResponse;
import org.example.storemanager.modules.partnerarea.dto.response.customer.CustomerDetailResponse;
import org.example.storemanager.modules.partnerarea.dto.response.customer.UpdateCustomerResponse;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.partnerarea.repository.AreaRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.partnerarea.repository.PartnerGroupRepository;
import org.example.storemanager.modules.partnerarea.service.customer.Impl.CustomerServiceImpl;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.example.storemanager.shared.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartnerGroupRepository partnerGroupRepository;

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .customerCode("CUST-00001")
                .name("Trần Văn Khách")
                .phone("0912345678")
                .email("khach@gmail.com")
                .isActive(true)
                .membershipRank("Đồng")
                .points(100.0)
                .totalSpend(500000.0)
                .build();
        sampleCustomer.setId(10L);
        sampleCustomer.setIsDeleted(false);
    }

    @Nested
    @DisplayName("1. Tạo mới khách hàng (Create Customer)")
    class CreateCustomerTests {

        @Test
        @DisplayName("Tạo khách hàng thành công")
        void createCustomer_Success() {
            CreateCustomerRequest req = CreateCustomerRequest.builder()
                    .name("Trần Văn Khách")
                    .phone("0912345678")
                    .email("khach@gmail.com")
                    .build();

            when(customerRepository.existsByPhone("0912345678")).thenReturn(false);
            when(customerRepository.existsByEmail("khach@gmail.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
            when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));

            CreateCustomerResponse res = customerService.createCustomer(req);

            assertThat(res).isNotNull();
            assertThat(res.getName()).isEqualTo("Trần Văn Khách");
            verify(customerRepository, times(1)).save(any(Customer.class));
        }

        @Test
        @DisplayName("Tạo khách hàng thất bại khi trùng số điện thoại")
        void createCustomer_DuplicatePhone_ThrowsException() {
            CreateCustomerRequest req = CreateCustomerRequest.builder()
                    .name("Trần Văn Khách")
                    .phone("0912345678")
                    .build();

            when(customerRepository.existsByPhone("0912345678")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(req));
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Cập nhật & Truy vấn khách hàng")
    class QueryCustomerTests {

        @Test
        @DisplayName("Lấy chi tiết khách hàng theo ID")
        void getCustomerById_Success() {
            when(customerRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(sampleCustomer));

            CustomerDetailResponse res = customerService.getCustomerById(10L);

            assertThat(res).isNotNull();
            assertThat(res.getId()).isEqualTo(10L);
            assertThat(res.getName()).isEqualTo("Trần Văn Khách");
        }

        @Test
        @DisplayName("Cập nhật trạng thái hoạt động khách hàng")
        void updateStatus_Success() {
            when(customerRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

            UpdateCustomerResponse res = customerService.updateStatus(10L, false);

            assertThat(res).isNotNull();
            assertThat(sampleCustomer.getIsActive()).isFalse();
            verify(customerRepository, times(1)).save(sampleCustomer);
        }
    }
}
