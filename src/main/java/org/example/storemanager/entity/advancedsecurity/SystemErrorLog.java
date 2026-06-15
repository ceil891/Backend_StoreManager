package org.example.storemanager.entity.advancedsecurity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.system.User;

@Entity
@Table(name = "system_error_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SystemErrorLog extends BaseEntity {

    @Column(name = "error_code", length = 50)
    private String errorCode; // VD: ERR_500, NULL_POINTER

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(length = 255)
    private String endpoint; // API hoặc URL gây ra lỗi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Gắn ID user nếu lỗi xảy ra khi đã đăng nhập (để null nếu lỗi ở màn login/khách)
}