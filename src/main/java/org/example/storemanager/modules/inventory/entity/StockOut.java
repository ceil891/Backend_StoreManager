package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_outs", indexes = {
        @Index(name = "idx_stock_out_code", columnList = "stock_out_code"),
        @Index(name = "idx_stock_out_type", columnList = "out_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StockOut extends BaseEntity {

    @Column(name = "stock_out_code", nullable = false, unique = true, length = 50)
    private String stockOutCode;

    @Column(name = "out_type", nullable = false, length = 50)
    private String outType; // BAN_HANG, TRA_NCC, HUY_HANG_HONG, CHUYEN_KHO, NOI_BO

    @Column(name = "warehouse_name", length = 150)
    private String warehouseName;

    @Column(name = "issued_date")
    private LocalDateTime issuedDate;

    @Column(name = "total_variants")
    private Integer totalVariants;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "total_value", precision = 18, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "creator", length = 100)
    private String creator;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // CHO_XU_LY, DA_XUAT, DA_HUY

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "stockOut", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockOutDetail> details = new ArrayList<>();
}
