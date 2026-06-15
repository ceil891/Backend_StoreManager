package org.example.storemanager.entity.advancedaccounting;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "cost_centers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CostCenter extends BaseEntity {

    @Column(name = "center_code", nullable = false, unique = true, length = 50)
    private String centerCode;

    @Column(name = "center_name", nullable = false, length = 150)
    private String centerName;

    @Column(columnDefinition = "TEXT")
    private String description;
}