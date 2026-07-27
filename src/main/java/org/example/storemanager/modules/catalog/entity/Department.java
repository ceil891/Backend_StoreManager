package org.example.storemanager.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.shared.base.BaseEntity;

@Entity(name = "CatalogDepartment")
@Table(name = "catalog_departments", indexes = {
        @Index(name = "idx_departments_dept_code", columnList = "dept_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {

    @Column(name = "dept_code", nullable = false, unique = true, length = 50)
    private String deptCode;

    @Column(name = "dept_name", nullable = false, length = 150)
    private String deptName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;
}