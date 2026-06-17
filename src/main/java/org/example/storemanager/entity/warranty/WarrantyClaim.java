package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.User;

import java.time.LocalDateTime;

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
}