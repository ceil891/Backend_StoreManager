package org.example.storemanager.service.media;

import org.example.storemanager.dto.response.media.MediaResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MediaService {
    MediaResponse uploadMedia(MultipartFile file, String folder) throws IOException;
}
