package org.example.storemanager.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "cancel_issue_details", indexes = {
        @Index(name = "idx_cancel_detail_issue", columnList = "cancel_id"),
        @Index(name = "idx_cancel_detail_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CancelIssueDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancel_id", nullable = false)
    private CancelIssue cancelIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;
}