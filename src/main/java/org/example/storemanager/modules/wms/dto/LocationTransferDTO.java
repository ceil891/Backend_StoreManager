package org.example.storemanager.modules.wms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs cho LocationTransfer (Chuyển vị trí nội bộ kho)
 */
public class LocationTransferDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "Sản phẩm không được trống")
        private Long productVariantId;

        @NotNull(message = "Ô kệ nguồn không được trống")
        private Long fromBinId;

        @NotNull(message = "Ô kệ đích không được trống")
        private Long toBinId;

        @NotNull(message = "Chi nhánh không được trống")
        private Long branchId;

        @NotNull(message = "Số lượng không được trống")
        @DecimalMin(value = "0.001", message = "Số lượng phải lớn hơn 0")
        private BigDecimal quantity;

        private String reason;
        private String executedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String transferCode;
        private LocalDateTime transferDate;
        private String status;
        private String reason;
        private BigDecimal quantity;
        private String executedBy;
        // Product info
        private Long productVariantId;
        private String productName;
        private String sku;
        // From bin info
        private Long fromBinId;
        private String fromBinCode;
        private String fromBinLocation; // Full path: Zone-Area-Rack-Bin
        // To bin info
        private Long toBinId;
        private String toBinCode;
        private String toBinLocation;
        // Branch info
        private Long branchId;
        private String branchName;
        // Audit info
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;
    }
}
