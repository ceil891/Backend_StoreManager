package org.example.storemanager.modules.catalog.dto.request.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchInventoryRequest {
    private Long branchId;
    private Long warehouseZoneId;
    private Long productId;
    private Long categoryId;
    private Long departmentId;
    private Long sizeId;
    private Long colorId;
    private String size; // size code or name filter
    private String color; // color code or name filter
    private String search; // generic search on product name/code or branch name
    @Min(0)
    private Integer page;
    @Min(1)
    private Integer sizePerPage;
    private String sort = "id,desc";
}
