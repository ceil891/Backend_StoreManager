package org.example.storemanager.shared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // === Authentication & Authorization ===
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa xác thực, vui lòng đăng nhập"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "Tài khoản đã bị khóa"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Tài khoản đã bị vô hiệu hóa"),

    // === Resource Not Found ===
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"),
    SUPPLIER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp"),
    BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy chi nhánh"),
    UNIT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn vị tính"),
    COMBO_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy gói combo"),
    PRICE_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy bảng giá"),
    BIN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy ô/kệ kho"),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy vị trí sản phẩm"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên"),

    // === Validation ===
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "Nội dung request không đúng định dạng"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức HTTP không được hỗ trợ"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Định dạng dữ liệu không được hỗ trợ"),

    // === Business Logic ===
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST, "Lỗi nghiệp vụ"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Tài nguyên đã tồn tại"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Số lượng tồn kho không đủ"),
    COMBO_PRICE_ABOVE_RETAIL(HttpStatus.OK, "Giá combo cao hơn tổng giá bán lẻ"),
    PRICE_LIST_DATE_OVERLAP(HttpStatus.CONFLICT, "Bảng giá trùng khoảng thời gian trên cùng chi nhánh"),
    COMBO_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Loại combo chưa hỗ trợ trừ tồn kho tự động"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Trạng thái chuyển đổi không hợp lệ"),
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "Lỗi tải file lên"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Dung lượng file vượt quá giới hạn cho phép"),

    // === Data Integrity ===
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "Dữ liệu vi phạm ràng buộc toàn vẹn"),

    // === Server ===
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
