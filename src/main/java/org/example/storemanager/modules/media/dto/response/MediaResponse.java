package org.example.storemanager.modules.media.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {
    private Long id;
    private String publicId;
    private String url;
    private String format;
    private String resourceType;
}
