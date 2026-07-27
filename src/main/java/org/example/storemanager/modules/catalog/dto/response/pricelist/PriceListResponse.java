package org.example.storemanager.modules.catalog.dto.response.pricelist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceListResponse {
    private Long id;
    private String listCode;
    private String listName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Long branchId;
    private String branchName;
    private List<PriceListDetailResponse> details;
    private LocalDateTime createdAt;
}
