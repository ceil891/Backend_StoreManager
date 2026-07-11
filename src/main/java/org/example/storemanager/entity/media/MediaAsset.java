package org.example.storemanager.entity.media;

import jakarta.persistence.*;
import lombok.*;
import org.example.storemanager.entity.BaseEntity;

@Entity
@Table(name = "media_assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MediaAsset extends BaseEntity {

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "url", nullable = false, length = 2000)
    private String url;

    @Column(name = "format", length = 50)
    private String format;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "original_filename", length = 500)
    private String originalFilename;

    @Column(name = "bytes")
    private Long bytes;
}
