package org.example.storemanager.dto.response.sales.saleOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteSaleOrderResponse {
    private Long id;
    private String orderCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}