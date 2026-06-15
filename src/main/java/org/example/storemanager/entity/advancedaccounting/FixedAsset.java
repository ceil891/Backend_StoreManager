package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

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

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate; // Ngày mua/Ghi nhận tài sản

    @Column(name = "purchase_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal purchasePrice; // Nguyên giá

    @Column(name = "salvage_value", precision = 18, scale = 2)
    private BigDecimal salvageValue; // Giá trị thu hồi ước tính (Giá trị thanh lý)
}