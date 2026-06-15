package org.example.storemanager.entity.omnichannel;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.catalog.Product;

@Entity
@Table(name = "channel_product_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ChannelProductMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private SalesChannel salesChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Sản phẩm gốc trong hệ thống (từ module Catalog)

    @Column(name = "channel_product_id", nullable = false, length = 100)
    private String channelProductId; // ID thực tế của sản phẩm trên nền tảng (VD: item_id trên Shopee)

    @Column(name = "channel_sku", length = 100)
    private String channelSku; // Mã SKU hiển thị trên sàn

    @Column(name = "sync_status", length = 50)
    private String syncStatus; // Trạng thái đồng bộ: SUCCESS, FAILED, PENDING
}