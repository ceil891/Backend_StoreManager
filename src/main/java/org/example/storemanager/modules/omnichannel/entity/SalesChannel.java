package org.example.storemanager.modules.omnichannel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity
@Table(name = "sales_channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SalesChannel extends BaseEntity {

    @Column(name = "channel_code", nullable = false, unique = true, length = 50)
    private String channelCode; // VD: SHOPEE_01, TIKTOK_MAIN

    @Column(name = "channel_name", nullable = false, length = 150)
    private String channelName;

    @Column(length = 50)
    private String platform; // Nền tảng: SHOPEE, TIKTOK, LAZADA, WOOCOMMERCE...

    @Column(name = "api_key", length = 500)
    private String apiKey; // Access Token hoặc Secret Key để gọi API sàn

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "shop_id", length = 100)
    private String shopId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "last_synced_at", length = 50)
    private String lastSyncedAt;

    @Column(name = "product_count")
    private Integer productCount = 0;
}