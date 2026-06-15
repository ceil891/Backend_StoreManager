package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.partnerarea.Supplier;
import org.example.storemanager.entity.system.User;

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
}