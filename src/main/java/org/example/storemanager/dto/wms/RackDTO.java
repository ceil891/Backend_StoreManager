package org.example.storemanager.dto.wms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs cho Rack (Kệ hàng)
 */
public class RackDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Mã kệ không được trống")
        @Size(max = 50)
        private String rackCode;

        @NotBlank(message = "Tên kệ không được trống")
        @Size(max = 150)
        private String rackName;

        private BigDecimal maxWeightKg;
        private BigDecimal maxVolumeM3;
        private Integer maxPallet;
        private String description;
        private Boolean isActive = true;

        @NotNull(message = "Area ID không được trống")
        private Long areaId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String rackCode;
        private String rackName;
        private BigDecimal maxWeightKg;
        private BigDecimal maxVolumeM3;
        private Integer maxPallet;
        private String description;
        private Boolean isActive;
        // Area info
        private Long areaId;
        private String areaCode;
        private String areaName;
        // Zone info (denormalized)
        private Long zoneId;
        private String zoneCode;
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
