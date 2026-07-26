package org.example.storemanager.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.ProductVariant;
import org.example.storemanager.entity.catalog.Unit;
import org.example.storemanager.entity.wms.WarehouseBin;

import java.math.BigDecimal;

@Entity
@Table(name = "import_receipt_details", indexes = {
        @Index(name = "idx_import_rd_receipt", columnList = "receipt_id"),
        @Index(name = "idx_import_rd_variant", columnList = "product_variant_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ImportReceiptDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private ImportReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private org.example.storemanager.entity.catalog.Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    // ---- Snapshot tại thời điểm nhập ----
    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "sku_snapshot", nullable = false, length = 100)
    private String skuSnapshot;

    @Column(name = "barcode_snapshot", length = 100)
    private String barcodeSnapshot;

    /** Ví dụ: "Size: M, Màu: Đen" */
    @Column(name = "variant_description_snapshot", length = 300)
    private String variantDescriptionSnapshot;

    @Column(name = "unit_cost_snapshot", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitCostSnapshot;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;
    // ---- End snapshot ----

    @Column(precision = 18, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "sub_total", precision = 18, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    /**
     * Ô kệ đích — hàng nhập vào bin nào.
     * Nullable: cho phép nhập kho trước, chỉ định vị trí sau (Putaway workflow).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_bin_id")
    private WarehouseBin targetBin;

    /**
     * Liên kết với lô hàng đã tạo sau khi nhập.
     * Nullable: không phải tất cả sản phẩm đều quản lý theo lô.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;
}