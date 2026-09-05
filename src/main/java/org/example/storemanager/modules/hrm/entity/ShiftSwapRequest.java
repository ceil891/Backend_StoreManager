package org.example.storemanager.modules.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "shift_swap_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ShiftSwapRequest extends BaseEntity {

    @Column(name = "request_code", nullable = false, length = 50)
    private String requestCode;

    @Column(name = "requester_name", nullable = false, length = 100)
    private String requesterName;

    @Column(name = "requester_shift", nullable = false, length = 100)
    private String requesterShift;

    @Column(name = "target_user_name", nullable = false, length = 100)
    private String targetUserName;

    @Column(name = "target_user_shift", nullable = false, length = 100)
    private String targetUserShift;

    @Column(name = "swap_date")
    private LocalDate swapDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
