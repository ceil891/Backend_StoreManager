package org.example.storemanager.controller.common;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.request.common.DeleteImagesRequest;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.common.UploadResponse;
import org.example.storemanager.service.common.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadsController {

    private final CloudinaryService cloudinaryService;

    // 1. Upload 1 ảnh
    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "File không được để trống"));
            }
            UploadResponse response = cloudinaryService.uploadFile(file, folder);
            return ResponseEntity.ok(ApiResponse.ok("Tải ảnh lên thành công", response));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(500, "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage())
            );
        }
    }

    // 2. Upload nhiều ảnh
    @PostMapping("/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UploadResponse>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "products") String folder) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "Danh sách file không được để trống"));
            }
            List<UploadResponse> responses = cloudinaryService.uploadMultipleFiles(files, folder);
            return ResponseEntity.ok(ApiResponse.ok("Tải danh sách ảnh lên thành công", responses));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(500, "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage())
            );
        }
    }

    // 3. Xóa 1 ảnh (Dùng wildcard {*publicId} để bắt toàn bộ ký tự bao gồm cả dấu /)
    @DeleteMapping("/{*publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable String publicId) {
        try {
            if (publicId == null || publicId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "publicId không hợp lệ"));
            }
            // Loại bỏ dấu gạch chéo ở đầu nếu có do wildcard của Spring
            String cleanedPublicId = publicId.startsWith("/") ? publicId.substring(1) : publicId;
            cloudinaryService.deleteFile(cleanedPublicId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa ảnh thành công"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(500, "Lỗi khi xóa ảnh trên Cloudinary: " + e.getMessage())
            );
        }
    }

    // 4. Xóa nhiều ảnh
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteImages(@RequestBody DeleteImagesRequest request) {
        try {
            if (request == null || request.getPublicIds() == null || request.getPublicIds().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "Danh sách publicId không được để trống"));
            }
            cloudinaryService.deleteMultipleFiles(request.getPublicIds());
            return ResponseEntity.ok(ApiResponse.ok("Xóa danh sách ảnh thành công"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error(500, "Lỗi khi xóa ảnh trên Cloudinary: " + e.getMessage())
            );
        }
    }
}
