package org.example.storemanager.modules.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PosSession extends BaseEntity {

    @Column(name = "session_code", length = 50)
    private String sessionCode;

    @Column(name = "terminal_code", length = 50)
    private String terminalCode;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "opening_cash", precision = 18, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "expected_closing_cash", precision = 18, scale = 2)
    private BigDecimal expectedClosingCash;

    @Column(name = "actual_closing_cash", precision = 18, scale = 2)
    private BigDecimal actualClosingCash;

    @Column(name = "shift_name", length = 50)
    private String shiftName; // CA_SANG, CA_CHIEU, CA_TOI, CA_NGAY

    @Column(nullable = false, length = 30)
    private String status; // OPEN, CLOSED...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}