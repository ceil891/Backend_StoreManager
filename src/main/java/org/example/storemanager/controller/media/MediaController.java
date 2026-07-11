package org.example.storemanager.controller.media;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.media.MediaResponse;
import org.example.storemanager.service.media.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    // @PreAuthorize("isAuthenticated()") // Tạm thời comment lại để debug 403
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) throws IOException {
        
        MediaResponse response = mediaService.uploadMedia(file, folder);
        return ResponseEntity.ok(ApiResponse.ok("Tải ảnh lên thành công", response));
    }
}
