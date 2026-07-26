package org.example.storemanager.dto.wms;

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
public class PackingListDTO {
    private Long id;
    private String packCode;
    private LocalDateTime packDate;
    private BigDecimal weight;
    private String dimensions;
    private String status;
    private Long orderId;
    private String orderCode;

    private String pickingStartedBy;
    private LocalDateTime pickingStartedAt;
    private String pickedBy;
    private LocalDateTime pickedAt;
    private String packingStartedBy;
    private LocalDateTime packingStartedAt;
    private String packedBy;
    private LocalDateTime packedAt;

    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long id;
        private Long productVariantId;
        private String sku;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal pickedQuantity;
        private BigDecimal packedQuantity;
    }
}
