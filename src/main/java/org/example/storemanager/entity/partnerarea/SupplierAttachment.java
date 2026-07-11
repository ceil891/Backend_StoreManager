package org.example.storemanager.entity.partnerarea;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "supplier_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SupplierAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(length = 50)
    private String type; // CONTRACT, CERTIFICATE, etc.
}
