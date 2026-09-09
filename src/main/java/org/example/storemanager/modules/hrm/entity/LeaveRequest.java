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
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user; // Người làm đơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User approvedByUser; // Người duyệt đơn

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Column(name = "total_days")
    private Integer totalDays;

    public String getRequestCode() {
        return requestCode != null ? requestCode : (getId() != null ? "NP-" + getId() : "NP-NEW");
    }

    @Transient
    @lombok.Getter(lombok.AccessLevel.NONE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Long requestUserId;

    @com.fasterxml.jackson.annotation.JsonProperty("userId")
    @com.fasterxml.jackson.annotation.JsonGetter("userId")
    public Long getUserId() {
        if (requestUserId != null) return requestUserId;
        try {
            if (user != null) return user.getId();
        } catch (Exception ignored) {}
        return null;
    }

    public void setUserId(Long userId) {
        this.requestUserId = userId;
    }

    public String getUserCode() {
        try {
            if (user != null && org.hibernate.Hibernate.isInitialized(user) && user.getUsername() != null) {
                return user.getUsername();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public String getEmployeeName() {
        if (employeeName != null && !employeeName.isBlank()) return employeeName;
        try {
            if (user != null && org.hibernate.Hibernate.isInitialized(user) && user.getFullName() != null) {
                return user.getFullName();
            }
        } catch (Exception ignored) {}
        return employeeName != null ? employeeName : "Nhân viên";
    }

    public String getApprovedBy() {
        if (approverName != null && !approverName.isBlank()) return approverName;
        try {
            if (approvedByUser != null && org.hibernate.Hibernate.isInitialized(approvedByUser) && approvedByUser.getFullName() != null) {
                return approvedByUser.getFullName();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public Integer getTotalDays() {
        if (totalDays != null && totalDays > 0) return totalDays;
        if (startDate != null && endDate != null) {
            return (int) Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);
        }
        return 1;
    }
}
