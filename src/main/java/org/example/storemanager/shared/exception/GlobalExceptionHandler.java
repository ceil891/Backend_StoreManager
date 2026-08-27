package org.example.storemanager.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.shared.enums.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== Business Exception ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("BusinessException: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorCode errorCode = ex.getErrorCode();
        return buildResponse(errorCode, ex.getMessage(), request);
    }

    // ==================== Resource Not Found ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("ResourceNotFound: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return buildResponse(ex.getErrorCode(), ex.getMessage(), request);
    }

    // ==================== Duplicate Resource ====================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("DuplicateResource: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return buildResponse(ex.getErrorCode(), ex.getMessage(), request);
    }

    // ==================== JWT Authentication ====================

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(
            JwtAuthenticationException ex, HttpServletRequest request) {

        log.warn("JwtAuth: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return buildResponse(ex.getErrorCode(), ex.getMessage(), request);
    }

    // ==================== Spring Security ====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("AccessDenied | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.ACCESS_DENIED, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        log.warn("BadCredentials | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.INVALID_CREDENTIALS, request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocked(
            LockedException ex, HttpServletRequest request) {

        log.warn("AccountLocked | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.ACCOUNT_LOCKED, request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(
            DisabledException ex, HttpServletRequest request) {

        log.warn("AccountDisabled | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.ACCOUNT_DISABLED, request);
    }

    // ==================== Validation (@Valid DTO) ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String combinedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        if (combinedMessage.isEmpty()) {
            combinedMessage = ErrorCode.VALIDATION_ERROR.getDefaultMessage();
        }

        log.warn("Validation: {} errors | Path: {}", fieldErrors.size(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.fail(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                combinedMessage,
                request.getRequestURI(),
                fieldErrors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ==================== Validation (@PathVariable, @RequestParam) ====================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> extractFieldName(v),
                        ConstraintViolation::getMessage,
                        (msg1, msg2) -> msg1
                ));

        String combinedMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        if (combinedMessage.isEmpty()) {
            combinedMessage = ErrorCode.VALIDATION_ERROR.getDefaultMessage();
        }

        log.warn("ConstraintViolation: {} errors | Path: {}", fieldErrors.size(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.fail(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                combinedMessage,
                request.getRequestURI(),
                fieldErrors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ==================== Missing / Invalid Request Params ====================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("MissingParam: {} | Path: {}", ex.getParameterName(), request.getRequestURI());

        String message = String.format("Thiếu tham số bắt buộc: '%s'", ex.getParameterName());
        return buildResponse(ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("TypeMismatch: param={}, value={} | Path: {}", ex.getName(), ex.getValue(), request.getRequestURI());

        String message = String.format("Tham số '%s' có giá trị '%s' không đúng kiểu dữ liệu", ex.getName(), ex.getValue());
        return buildResponse(ErrorCode.VALIDATION_ERROR, message, request);
    }

    // ==================== Malformed Request Body ====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("MessageNotReadable | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.INVALID_REQUEST_BODY, request);
    }

    // ==================== HTTP Method Not Supported ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("MethodNotSupported: {} | Path: {}", ex.getMethod(), request.getRequestURI());

        String message = String.format("Phương thức %s không được hỗ trợ cho endpoint này", ex.getMethod());
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, message, request);
    }

    // ==================== Unsupported Media Type ====================

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("MediaTypeNotSupported: {} | Path: {}", ex.getContentType(), request.getRequestURI());

        String message = String.format("Content-Type '%s' không được hỗ trợ", ex.getContentType());
        return buildResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, message, request);
    }

    // ==================== File Upload Exceed ====================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("MaxUploadSizeExceeded | Path: {}", request.getRequestURI());

        return buildResponse(ErrorCode.FILE_TOO_LARGE, request);
    }

    // ==================== Data Integrity (DB Constraint) ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("DataIntegrityViolation | Path: {} | Cause: {}", request.getRequestURI(), getRootCauseMessage(ex));

        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, request);
    }

    // ==================== Optimistic Locking Failure ====================

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(
            Exception ex, HttpServletRequest request) {

        log.warn("OptimisticLockingFailure | Path: {} | Cause: {}", request.getRequestURI(), getRootCauseMessage(ex));

        String message = "Dữ liệu đã được cập nhật bởi một thao tác khác. Vui lòng làm mới trang và thử lại.";
        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION, message, request);
    }

    // ==================== 404 Endpoint Not Found ====================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {

        log.warn("EndpointNotFound | Path: {}", request.getRequestURI());

        String message = String.format("Không tìm thấy endpoint: %s", request.getRequestURI());
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, message, request);
    }

    // ==================== Illegal Argument & Illegal State ====================

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentAndState(
            RuntimeException ex, HttpServletRequest request) {

        log.warn("BusinessRuleViolation: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        return buildResponse(ErrorCode.VALIDATION_ERROR, ex.getMessage(), request);
    }

    // ==================== Response Status Exception (Bắt lỗi khi xóa mềm bị chặn) ====================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {

        log.warn("ResponseStatusException: {} | Path: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.fail(
                ex.getStatusCode().value(),
                HttpStatus.valueOf(ex.getStatusCode().value()).name(), // Sẽ ra chữ "CONFLICT"
                ex.getReason(), // Lấy đúng đoạn text "Không thể xóa quyền..."
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    // ==================== Database Connection & Transaction Exceptions ====================

    @ExceptionHandler({
        org.springframework.orm.jpa.JpaSystemException.class,
        org.springframework.transaction.CannotCreateTransactionException.class,
        org.springframework.transaction.TransactionException.class,
        org.springframework.dao.DataAccessResourceFailureException.class,
        java.sql.SQLException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleDatabaseConnectionException(
            Exception ex, HttpServletRequest request) {

        log.error("DatabaseConnectionError | Path: {} | Cause: {}", request.getRequestURI(), getRootCauseMessage(ex));

        String userFriendlyMsg = "Kết nối cơ sở dữ liệu tạm thời gián đoạn. Hệ thống đang tự động kết nối lại, vui lòng thử lại sau vài giây.";
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, userFriendlyMsg, request);
    }

    // ==================== Fallback ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {

        log.error("UnhandledException | Path: {} | Type: {} | Error: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage() != null ? ex.getMessage() : ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), request);
    }

    // ==================== Private Helpers ====================

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode, HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponse.fail(
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                errorCode.getDefaultMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode, String message, HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponse.fail(
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    private String extractFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
    }

    private String getRootCauseMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}