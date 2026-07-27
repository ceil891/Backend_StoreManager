package org.example.storemanager.modules.advancedsecurity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DeviceSession extends BaseEntity {

    @Column(nullable = false, unique = true, length = 500)
    private String token; // Access token hoặc Refresh token

    @Column(name = "device_info", length = 255)
    private String deviceInfo; // Thông tin trình duyệt, hệ điều hành (VD: Chrome on Windows 11)

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}