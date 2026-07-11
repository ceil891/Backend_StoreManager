package org.example.storemanager.entity.warranty;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "warranty_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WarrantyAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_claim_id", nullable = false)
    private WarrantyClaim warrantyClaim;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_type", length = 50)
    private String fileType; // IMAGE, VIDEO, PDF
}
