package org.example.storemanager.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOutDTO {
    private Long id;
    private String stockOutCode;
    private String outType;
    private String warehouseName;
    private String issuedDate;
    private Integer totalVariants;
    private Integer totalItems;
    private BigDecimal totalValue;
    private String creator;
    private String status;
    private String notes;
    private List<StockOutDetailDTO> items;
}
