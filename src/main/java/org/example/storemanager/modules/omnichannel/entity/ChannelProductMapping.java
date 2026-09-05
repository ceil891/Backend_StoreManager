package org.example.storemanager.modules.omnichannel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.catalog.entity.Product;

@Entity
@Table(name = "channel_product_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ChannelProductMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = true)
    private SalesChannel salesChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product; // Sản phẩm gốc trong hệ thống (từ module Catalog)

    @Column(name = "channel_product_id", length = 100)
    private String channelProductId; // ID thực tế của sản phẩm trên nền tảng (VD: item_id trên Shopee)

    @Column(name = "channel_sku", length = 100)
    private String channelSku; // Mã SKU hiển thị trên sàn

    @Column(name = "sync_status", length = 50)
    private String syncStatus; // Trạng thái đồng bộ: SUCCESS, FAILED, PENDING, SYNCED, OUT_OF_SYNC

    @Column(name = "internal_sku", length = 100)
    private String internalSku;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "channel_name", length = 150)
    private String channelName;

    @Column(name = "channel_price", precision = 18, scale = 2)
    private java.math.BigDecimal channelPrice;

    @Column(name = "last_synced_at", length = 50)
    private String lastSyncedAt;
}