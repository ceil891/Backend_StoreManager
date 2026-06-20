package org.example.storemanager.dto.response.catalog.categories;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCategoriesResponse {
    private Long id;
    private String categoryCode;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
