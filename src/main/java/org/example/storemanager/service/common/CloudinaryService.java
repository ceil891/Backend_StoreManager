package org.example.storemanager.service.common;

import org.example.storemanager.dto.response.common.UploadResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface CloudinaryService {
    UploadResponse uploadFile(MultipartFile file, String folder) throws IOException;
    List<UploadResponse> uploadMultipleFiles(MultipartFile[] files, String folder) throws IOException;
    void deleteFile(String publicId) throws IOException;
    void deleteMultipleFiles(List<String> publicIds) throws IOException;
    void deleteFileByUrl(String url);
    void deleteFileByUrlAsync(String url);
}
