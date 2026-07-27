package org.example.storemanager.modules.warranty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;
import org.example.storemanager.modules.partnerarea.entity.Supplier;
import org.example.storemanager.modules.system.entity.User;

import java.time.LocalDate;

@Entity
@Table(name = "supplier_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierEvaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "eval_date", nullable = false)
    private LocalDate evalDate; // Ngày đánh giá định kỳ

    @Column(nullable = false)
    private Integer score; // Điểm đánh giá chất lượng (VD thang điểm 10 hoặc 100)

    @Column(columnDefinition = "TEXT")
    private String remarks; // Ghi chú chi tiết

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy; // Nhân viên thực hiện đánh giá

    @Column(name = "evaluation_type", length = 50)
    private String evaluationType; // Monthly, Quarterly

    @Column(name = "quality_score")
    private Integer qualityScore; // Điểm chất lượng

    @Column(name = "delivery_score")
    private Integer deliveryScore; // Điểm giao hàng

    @Column(name = "service_score")
    private Integer serviceScore; // Điểm hỗ trợ

    @Column(name = "price_score")
    private Integer priceScore; // Điểm giá

    @Column(name = "overall_score")
    private Integer overallScore; // Điểm tổng

    @Column(length = 30)
    private String result; // EXCELLENT, GOOD, FAIR, POOR

    @Column(columnDefinition = "TEXT")
    private String improvement; // Kiến nghị cải thiện
}