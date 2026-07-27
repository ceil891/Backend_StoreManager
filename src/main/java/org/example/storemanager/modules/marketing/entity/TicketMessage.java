package org.example.storemanager.modules.marketing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.system.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class TicketMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_from_customer", nullable = false)
    private Boolean isFromCustomer; // true: Khách nhắn, false: Nhân viên nhắn

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; // Nếu là nhân viên nhắn, lưu ID nhân viên

    @Column(length = 500)
    private String attachment; // File đính kèm

    @Builder.Default
    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead = false; // Đã đọc

    @Column(name = "read_at")
    private LocalDateTime readAt; // Thời gian đọc

    @Column(name = "message_type", length = 20)
    private String messageType; // TEXT, IMAGE, FILE
}