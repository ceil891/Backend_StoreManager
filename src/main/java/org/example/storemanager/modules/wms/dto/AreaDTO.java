package org.example.storemanager.modules.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs cho Area (Bãi kho)
 */
public class AreaDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Mã bãi không được trống")
        @Size(max = 50)
        private String areaCode;

        @NotBlank(message = "Tên bãi không được trống")
        @Size(max = 150)
        private String areaName;

        private String description;

        private Boolean isActive = true;

        @NotNull(message = "Zone ID không được trống")
        private Long zoneId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String areaCode;
        private String areaName;
        private String description;
        private Boolean isActive;
        // Zone info
        private Long zoneId;
        private String zoneCode;
        private String zoneName;
        // Branch info (denormalized)
        private Long branchId;
        private String branchName;
        // Audit info
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;
    }
}
