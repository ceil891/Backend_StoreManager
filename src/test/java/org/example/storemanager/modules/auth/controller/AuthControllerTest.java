package org.example.storemanager.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storemanager.modules.auth.dto.request.LoginRequest;
import org.example.storemanager.modules.auth.dto.request.RegisterRequest;
import org.example.storemanager.modules.auth.dto.response.LoginResponse;
import org.example.storemanager.modules.auth.dto.response.UserInfoResponse;
import org.example.storemanager.modules.auth.service.AuthService;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController WebMvc Integration Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("200 OK - Đăng nhập thành công trả về LoginResponse")
        void login_ValidCredentials_Returns200() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("admin");
            req.setPassword("admin123");

            UserInfoResponse userInfo = UserInfoResponse.builder()
                    .id(1L)
                    .name("Quản Trị Viên")
                    .email("admin@storemanager.com")
                    .role("SUPER_ADMIN")
                    .permissions(List.of("catalog:product:create", "sales:order:view"))
                    .build();

            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken("mocked-jwt-token")
                    .refreshToken("mocked-refresh-token")
                    .user(userInfo)
                    .build();

            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accessToken").value("mocked-jwt-token"))
                    .andExpect(jsonPath("$.data.user.role").value("SUPER_ADMIN"));
        }

        @Test
        @DisplayName("401 Unauthorized - Sai mật khẩu hoặc tài khoản không tồn tại")
        void login_InvalidCredentials_Returns401() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("wronguser");
            req.setPassword("wrongpass");

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Tên đăng nhập hoặc mật khẩu không đúng"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_CREDENTIALS.name()));
        }

        @Test
        @DisplayName("400 Bad Request - Request Body thiếu username/password")
        void login_EmptyFields_Returns400() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setUsername("");
            req.setPassword("");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("201 Created - Đăng ký thành công trả về token")
        void register_ValidRequest_Returns201() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setUsername("newcustomer");
            req.setPassword("secret123");
            req.setFullName("Khách Hàng Mới");
            req.setEmail("newcust@gmail.com");
            req.setPhone("0912345678");

            LoginResponse response = LoginResponse.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .build();

            when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }
    }
}
