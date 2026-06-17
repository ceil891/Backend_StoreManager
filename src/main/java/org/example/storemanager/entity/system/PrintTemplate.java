package org.example.storemanager.entity.system;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "print_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PrintTemplate extends BaseEntity {

    @Column(name = "template_code", nullable = false, unique = true, length = 50)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 150)
    private String templateName;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent; // Lưu trữ cấu trúc mã HTML template hóa đơn/phiếu in

    @Column(name = "paper_size", length = 20)
    private String paperSize; // K80, A4, A5...
}