package org.example.storemanager.modules.advancedaccounting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class FixedAsset extends BaseEntity {

    @Column(name = "asset_code", nullable = false, unique = true, length = 50)
    private String assetCode;

    @Column(name = "asset_name", nullable = false, length = 150)
    private String assetName;

    @Column(length = 100)
    private String category;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate; // Ngày mua/Ghi nhận tài sản

    @Column(name = "purchase_price", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal purchasePrice = BigDecimal.ZERO; // Nguyên giá

    @Column(name = "salvage_value", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal salvageValue = BigDecimal.ZERO; // Giá trị thu hồi ước tính (Giá trị thanh lý)

    @Column(name = "accumulated_depreciation", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(name = "useful_life_months")
    @Builder.Default
    private Integer usefulLifeMonths = 36;

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}