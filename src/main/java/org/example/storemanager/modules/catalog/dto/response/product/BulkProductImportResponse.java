package org.example.storemanager.modules.catalog.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkProductImportResponse {

    private int totalSubmitted;
    private int successCount;
    private int failedCount;
    private List<Long> createdProductIds;
    private List<BulkImportError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkImportError {
        private int rowIndex;
        private String productCode;
        private String productName;
        private String errorMessage;
    }
}
