package org.example.storemanager.modules.catalog.dto.request.variant;

import lombok.Data;
import java.util.List;

@Data
public class CreateSingleVariantRequest {
    private String sku;
    private String barcode;
    private List<AttributeInput> attributes;

    @Data
    public static class AttributeInput {
        private Long attributeId;
        private Long valueId;
    }
}
