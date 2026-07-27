package org.example.storemanager.modules.catalog.dto.request.combo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateComboRequest {

    @NotBlank
    @Size(max = 50)
    private String comboCode;

    @NotBlank
    @Size(max = 150)
    private String comboName;

    private String barcode;
    private String description;
    private ComboType comboType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @NotNull
    private BigDecimal price;

    private Boolean isActive;

    @NotNull
    @Valid
    private List<ComboDetailRequest> details;
}
