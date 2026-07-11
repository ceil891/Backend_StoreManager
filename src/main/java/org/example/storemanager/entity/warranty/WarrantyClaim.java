package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.User;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "warranty_claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarrantyClaim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_id", nullable = false)
    private ProductWarranty warranty;

    @Column(name = "claim_date", nullable = false)
    private LocalDateTime claimDate; // Ngày khách hàng mang máy đến bảo hành

    @Column(name = "issue_description", columnDefinition = "TEXT", nullable = false)
    private String issueDescription; // Mô tả tình trạng lỗi do khách báo

    @Column(columnDefinition = "TEXT")
    private String resolution; // Hướng xử lý: Thay main, Đổi máy mới, Sửa chữa...

    @Column(nullable = false, length = 30)
    private String status; // RECEIVED (Đã tiếp nhận), REPAIRING (Đang sửa), DONE (Hoàn thành), RETURNED (Đã trả khách)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy; // Nhân viên kỹ thuật phụ trách

    @Column(name = "claim_code", unique = true, length = 50)
    private String claimCode; // Mã phiếu bảo hành

    @Column(name = "received_condition", columnDefinition = "TEXT")
    private String receivedCondition; // Tình trạng khi tiếp nhận

    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate; // Ngày hẹn trả

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate; // Ngày trả thực tế

    @Column(name = "repair_cost", precision = 18, scale = 2)
    private BigDecimal repairCost; // Chi phí sửa chữa

    @Column(name = "warranty_cost", precision = 18, scale = 2)
    private BigDecimal warrantyCost; // Chi phí bảo hành

    @Column(length = 500)
    private String attachment; // Hình ảnh lỗi / File đính kèm

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason; // Lý do từ chối bảo hành

    @Column(name = "customer_signature", length = 500)
    private String customerSignature; // Chữ ký khách hàng
}