package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "serial_numbers", indexes = {
    @Index(name = "idx_serials_serial_number", columnList = "serial_number"),
    @Index(name = "idx_serials_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SerialNumber extends BaseEntity {

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber; // Mã IMEI hoặc số mã định danh sản phẩm độc bản

    @Column(length = 30)
    private String status; // Trạng thái: AVAILABLE, SOLD, WARRANTY, RETURNED...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "import_receipt_id")
    private Long importReceiptId; // Khóa ngoại kết nối sang phân hệ Kho (ImportReceipt) sau này

    @Column(name = "mac_address", length = 100)
    private String macAddress;

    @Column(name = "imei1", length = 100)
    private String imei1;

    @Column(name = "imei2", length = 100)
    private String imei2;

    @Column(name = "warranty_expiry")
    private java.time.LocalDateTime warrantyExpiry;
}