package org.example.storemanager.modules.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_request_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierRequestDetail extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_request_id", nullable = false)
    private SupplierRequest supplierRequest;

    @Column(length = 100)
    private String sku;

    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String specifications;
}
