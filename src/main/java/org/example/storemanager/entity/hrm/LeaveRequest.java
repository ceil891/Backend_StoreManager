package org.example.storemanager.entity.hrm;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class LeaveRequest extends BaseEntity {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "leave_type", nullable = false, length = 50)
    private String leaveType; // Nghỉ ốm, Nghỉ phép năm, Nghỉ không lương...

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người làm đơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Người duyệt đơn

    @Column(name = "number_of_days")
    private Integer numberOfDays; // Số ngày nghỉ

    @Column(name = "approval_date")
    private LocalDateTime approvalDate; // Thời gian duyệt

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // Lý do từ chối

    @Column(name = "attachment_path", length = 255)
    private String attachmentPath; // Đường dẫn file đính kèm (pdf, ...)

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}