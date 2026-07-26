package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;

import java.time.LocalDate;

@Entity
@Table(name = "product_batches", indexes = {
        @Index(name = "idx_product_batch_product", columnList = "product_id"),
        @Index(name = "idx_product_batch_expiry", columnList = "expiry_date"),
        @Index(name = "idx_product_batch_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductBatch extends BaseEntity {

    @Column(name = "batch_number", nullable = false, length = 100)
    private String batchNumber;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(length = 30)
    private String status; // ACTIVE, EXPIRED, ALMOST_EXPIRED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "initial_units", precision = 18, scale = 3)
    private java.math.BigDecimal initialUnits;

    @Column(name = "remaining_units", precision = 18, scale = 3)
    private java.math.BigDecimal remainingUnits;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private java.math.BigDecimal unitCost;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "quality_status", length = 50)
    private String qualityStatus; // PASSED_QA, QUARANTINED, EXPIRED, RECALLED

    @Column(name = "inspector", length = 100)
    private String inspector;
}