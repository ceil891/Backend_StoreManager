package org.example.storemanager.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.partnerarea.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CustomerReturn extends BaseEntity {

    @Column(name = "return_code", nullable = false, unique = true, length = 50)
    private String returnCode;

    @Column(name = "return_request_id")
    private Long returnRequestId;

    @Column(name = "return_request_code", length = 50)
    private String returnRequestCode; // Nullable - NULL if direct walk-in return

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "total_refund", precision = 18, scale = 2)
    private BigDecimal totalRefund;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private ExportInvoice invoice; // Phiếu trả có thể tham chiếu từ hóa đơn hoặc đơn bán lẻ trực tiếp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private SaleOrder order; // Phiếu trả tham chiếu trực tiếp đơn bán hàng POS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}