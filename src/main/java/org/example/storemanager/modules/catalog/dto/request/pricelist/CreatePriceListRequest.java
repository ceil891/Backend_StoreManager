package org.example.storemanager.modules.catalog.dto.request.pricelist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceListRequest {

    @NotBlank
    @Size(max = 50)
    private String listCode;

    @NotBlank
    @Size(max = 150)
    private String listName;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long branchId;
    private Boolean isActive;

    @Valid
    private List<PriceListDetailRequest> details;
}
