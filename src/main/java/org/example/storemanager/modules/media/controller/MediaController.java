package org.example.storemanager.modules.media.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.media.dto.response.MediaResponse;
import org.example.storemanager.modules.media.service.MediaService;
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
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) throws IOException {
        
        MediaResponse response = mediaService.uploadMedia(file, folder);
        return ResponseEntity.ok(ApiResponse.ok("Tải ảnh lên thành công", response));
    }
}
