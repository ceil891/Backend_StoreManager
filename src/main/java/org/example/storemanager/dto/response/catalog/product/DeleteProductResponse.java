package org.example.storemanager.dto.response.catalog.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductResponse {
    private Long id;
    private String productCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
