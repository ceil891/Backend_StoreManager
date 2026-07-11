package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Customer;
import org.example.storemanager.entity.catalog.SerialNumber;

import java.time.LocalDate;
import org.example.storemanager.entity.sales.ExportInvoice;

@Entity
@Table(name = "product_warranties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductWarranty extends BaseEntity {

    @Column(name = "warranty_code", nullable = false, unique = true, length = 50)
    private String warrantyCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String terms; // Điều khoản hoặc mô tả gói bảo hành (VD: 1 đổi 1 trong 30 ngày)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_id", nullable = false)
    private SerialNumber serialNumber; // Gắn liền với 1 thiết bị/sản phẩm cụ thể qua mã Serial/IMEI

    @Column(name = "warranty_type", length = 30)
    private String warrantyType; // STANDARD, EXTENDED, VIP

    @Column(name = "warranty_period")
    private Integer warrantyPeriod; // Số tháng bảo hành

    @Column(name = "purchase_date")
    private LocalDate purchaseDate; // Ngày mua

    @Column(name = "activated_date")
    private LocalDate activatedDate; // Ngày kích hoạt bảo hành

    @Column(length = 30)
    private String status; // ACTIVE, EXPIRED, VOID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private ExportInvoice invoice; // Hóa đơn bán hàng

    @Column(length = 500)
    private String attachment; // File/phiếu bảo hành
}