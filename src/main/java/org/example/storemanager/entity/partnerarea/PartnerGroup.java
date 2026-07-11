package org.example.storemanager.entity.partnerarea;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "partner_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PartnerGroup extends BaseEntity {

    @Column(name = "group_code", nullable = false, unique = true, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 150)
    private String groupName;

    @Column(length = 50)
    private String type;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "initial_member_count")
    private Integer initialMemberCount = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}