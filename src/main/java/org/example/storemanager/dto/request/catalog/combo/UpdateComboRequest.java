package org.example.storemanager.dto.request.catalog.combo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storemanager.enums.catalog.ComboType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComboRequest {

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

    @Valid
    private List<ComboDetailRequest> details;
}
