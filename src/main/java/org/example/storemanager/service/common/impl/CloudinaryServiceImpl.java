package org.example.storemanager.service.common.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.dto.response.common.UploadResponse;
import org.example.storemanager.service.common.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResponse uploadFile(MultipartFile file, String folder) throws IOException {
        Map params = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto"
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        
        String secureUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        return UploadResponse.builder()
                .imageUrl(secureUrl)
                .publicId(publicId)
                .build();
    }

    @Override
    public List<UploadResponse> uploadMultipleFiles(MultipartFile[] files, String folder) throws IOException {
        List<UploadResponse> responses = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    responses.add(uploadFile(file, folder));
                }
            }
        }
        return responses;
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Kết quả xóa ảnh từ Cloudinary (publicId={}): {}", publicId, result);
    }

    @Override
    public void deleteMultipleFiles(List<String> publicIds) throws IOException {
        if (publicIds != null) {
            for (String publicId : publicIds) {
                if (publicId != null && !publicId.trim().isEmpty()) {
                    deleteFile(publicId.trim());
                }
            }
        }
    }

    @Override
    public void deleteFileByUrl(String url) {
        if (url == null || url.trim().isEmpty() || !url.contains("cloudinary.com")) {
            return;
        }
        try {
            // Trích xuất publicId từ URL
            int lastDotIdx = url.lastIndexOf(".");
            String urlWithoutExtension = lastDotIdx != -1 ? url.substring(0, lastDotIdx) : url;

            String marker = "image/upload/";
            int markerIdx = urlWithoutExtension.indexOf(marker);
            if (markerIdx == -1) {
                log.warn("Không xác định được marker '{}' trong URL Cloudinary: {}", marker, url);
                return;
            }

            String afterMarker = urlWithoutExtension.substring(markerIdx + marker.length());
            String[] segments = afterMarker.split("/");
            if (segments.length == 0) {
                return;
            }

            // Bỏ qua phần version (ví dụ: v1719700000)
            int startIndex = 0;
            if (segments[0].matches("v\\d+")) {
                startIndex = 1;
            }

            String publicId = Arrays.stream(segments)
                    .skip(startIndex)
                    .collect(Collectors.joining("/"));

            log.info("Tự động dọn dẹp -> Trích xuất publicId '{}' từ URL. Yêu cầu xóa trên Cloudinary.", publicId);
            deleteFile(publicId);
        } catch (Exception e) {
            log.error("Không thể tự động xóa tệp tin Cloudinary của URL {}: {}", url, e.getMessage());
        }
    }

    @Override
    @org.springframework.scheduling.annotation.Async("cloudinaryExecutor")
    @org.springframework.retry.annotation.Retryable(maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 2000))
    public void deleteFileByUrlAsync(String url) {
        log.info("Bắt đầu xóa ảnh bất đồng bộ cho URL: {}", url);
        deleteFileByUrl(url);
    }
}
