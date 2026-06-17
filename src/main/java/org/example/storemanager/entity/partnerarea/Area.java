package org.example.storemanager.entity.partnerarea;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Area extends BaseEntity {

    @Column(name = "area_code", nullable = false, unique = true, length = 50)
    private String areaCode;

    @Column(name = "area_name", nullable = false, length = 150)
    private String areaName;

    @Column(name = "area_level")
    private Integer level; // Cấp bậc: 1 (Quốc gia), 2 (Tỉnh), 3 (Huyện)...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Area parent; // Khóa ngoại trỏ đến khu vực cha
}