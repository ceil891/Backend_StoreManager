package org.example.storemanager.dto.response.catalog.combo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboSaveResponse {
    private ComboResponse combo;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /** Mã cảnh báo nhẹ cho Frontend, ví dụ COMBO_PRICE_ABOVE_RETAIL */
    private String warningCode;
}
