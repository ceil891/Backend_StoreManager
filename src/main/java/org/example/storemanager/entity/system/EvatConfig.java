package org.example.storemanager.entity.system;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "evat_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class EvatConfig extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String provider; // VNPT, Viettel, Softkeys...

    @Column(name = "tax_code", nullable = false, length = 20)
    private String taxCode;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(length = 100)
    private String username;

    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String symbol; // Ký hiệu hóa đơn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}