package org.example.storemanager.modules.partnerarea.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

import java.util.List;

@Entity(name = "PartnerArea")
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

    //Thêm type để lấy địa chỉ chi tiết
    private String type;

    //sửa lazy thành eager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Area parent; // Khóa ngoại trỏ đến khu vực cha

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY) // Tối ưu: chỉ load con khi cần
    private List<Area> children;

    //Thêm trạng thái của khu vực
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // isDeleted is inherited from BaseEntity
}