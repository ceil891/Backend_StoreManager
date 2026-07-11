package org.example.storemanager.service.media.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.media.MediaResponse;
import org.example.storemanager.entity.media.MediaAsset;
import org.example.storemanager.repository.media.MediaAssetRepository;
import org.example.storemanager.service.media.MediaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    @Transactional
    public MediaResponse uploadMedia(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File không được để trống");
        }

        // Validate file type (e.g., only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ cho phép tải lên tệp tin hình ảnh");
        }

        // Validate size (e.g., max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kích thước tệp tin không được vượt quá 5MB");
        }

        String username = getCurrentUsername();
        String uploadFolder = folder != null && !folder.isBlank() ? folder : "retailhub_media";

        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", uploadFolder,
                "resource_type", "auto"
        ));

        // Save to Database
        MediaAsset asset = MediaAsset.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .resourceType((String) uploadResult.get("resource_type"))
                .originalFilename(file.getOriginalFilename())
                .bytes(file.getSize())
                .build();
                
        asset.setCreatedBy(username);
        asset.setIsDeleted(false);

        MediaAsset saved = mediaAssetRepository.save(asset);

        return MediaResponse.builder()
                .id(saved.getId())
                .publicId(saved.getPublicId())
                .url(saved.getUrl())
                .format(saved.getFormat())
                .resourceType(saved.getResourceType())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "system";
    }
}
