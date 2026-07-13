package org.example.storemanager.entity.system;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.enums.system.PosSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_sessions", indexes = {
        @Index(name = "idx_pos_session_branch", columnList = "branch_id"),
        @Index(name = "idx_pos_session_user", columnList = "user_id"),
        @Index(name = "idx_pos_session_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class PosSession extends BaseEntity {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(precision = 19, scale = 4)
    private BigDecimal openingCash;

    @Column(precision = 19, scale = 4)
    private BigDecimal expectedClosingCash;

    @Column(precision = 19, scale = 4)
    private BigDecimal actualClosingCash;

    @Enumerated(EnumType.STRING)
    private PosSessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
}