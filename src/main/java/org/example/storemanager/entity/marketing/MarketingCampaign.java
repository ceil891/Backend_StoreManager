package org.example.storemanager.entity.marketing;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MarketingCampaign extends BaseEntity {

    @Column(name = "campaign_code", nullable = false, unique = true, length = 50)
    private String campaignCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(precision = 18, scale = 2)
    private BigDecimal budget;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false, length = 30)
    private String status; // PLANNING, ACTIVE, COMPLETED, CANCELLED
}