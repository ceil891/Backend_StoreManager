package org.example.storemanager.modules.hrm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user", "approvedByUser"})
@ToString(callSuper = true, exclude = {"user", "approvedByUser"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeaveRequest extends BaseEntity {

    @Column(name = "request_code", length = 50)
    @JsonAlias({"requestCode"})
    private String requestCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "leave_type", length = 50)
    private String leaveType; // ANNUAL, SICK, MATERNITY, UNPAID...

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approver_name", length = 100)
    private String approverName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User user; // Người làm đơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User approvedByUser; // Người duyệt đơn

    @Transient
    private String employeeName;

    @Transient
    private Integer totalDays;

    public String getRequestCode() {
        return requestCode != null ? requestCode : (getId() != null ? "NP-" + getId() : "NP-NEW");
    }

    public String getEmployeeName() {
        if (user != null && user.getFullName() != null) return user.getFullName();
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getApprovedBy() {
        if (approverName != null && !approverName.isBlank()) return approverName;
        if (approvedByUser != null && approvedByUser.getFullName() != null) return approvedByUser.getFullName();
        return null;
    }

    public int getTotalDays() {
        if (totalDays != null && totalDays > 0) return totalDays;
        if (startDate != null && endDate != null) {
            return (int) Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);
        }
        return 1;
    }
}