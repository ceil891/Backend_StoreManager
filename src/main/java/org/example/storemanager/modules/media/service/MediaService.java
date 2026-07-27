package org.example.storemanager.modules.media.service;

import org.example.storemanager.modules.media.dto.response.MediaResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MediaService {
    MediaResponse uploadMedia(MultipartFile file, String folder) throws IOException;
}
