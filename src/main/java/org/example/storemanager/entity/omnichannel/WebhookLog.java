package org.example.storemanager.entity.omnichannel;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WebhookLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private SalesChannel salesChannel; // Thông báo này đến từ sàn nào

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // Loại sự kiện: ORDER_CREATED, ORDER_STATUS_CHANGED, STOCK_UPDATED

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; // Chuỗi JSON thô chứa toàn bộ dữ liệu sàn đẩy về

    @Column(name = "processed_status", nullable = false, length = 50)
    private String processedStatus; // Trạng thái xử lý trong hệ thống: PENDING, SUCCESS, ERROR, IGNORED

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt; // Thời điểm nhận được request Webhook
}