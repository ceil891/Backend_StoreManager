package org.example.storemanager.modules.catalog.dto.response.combo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.shared.enums.catalog.ComboType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboResponse {
    private Long id;
    private String comboCode;
    private String comboName;
    private String barcode;
    private String description;
    private ComboType comboType;
    private BigDecimal price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private List<ComboDetailResponse> items;
    private LocalDateTime createdAt;
}
