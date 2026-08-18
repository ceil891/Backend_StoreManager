package org.example.storemanager.modules.marketing.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.marketing.entity.Banner;
import org.example.storemanager.modules.marketing.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/banners", "/api/v1/system/banners"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false) Boolean isActive
    ) {
        if (bannerRepository.count() == 0) {
            initDefaultBanners();
        }
        boolean filterActive = activeOnly || Boolean.TRUE.equals(isActive);
        if (filterActive) {
            return ResponseEntity.ok(bannerRepository.findByIsActiveTrueOrderBySortOrderAsc());
        }
        return ResponseEntity.ok(bannerRepository.findAllByOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        return bannerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void initDefaultBanners() {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            List<Banner> defaultBanners = List.of(
                Banner.builder()
                    .title("Lễ Hội Công Nghệ AuraMart 2026 - Giảm tới 50%")
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(1)
                    .isActive(true)
                    .validFrom(now.minusDays(1))
                    .validUntil(now.plusMonths(12))
                    .build(),
                Banner.builder()
                    .title("Bộ Sưu Tập Giày Sneaker & Thời Trang Chính Hãng")
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(2)
                    .isActive(true)
                    .validFrom(now.minusDays(1))
                    .validUntil(now.plusMonths(12))
                    .build(),
                Banner.builder()
                    .title("Đồng Hồ & Thiết Bị Đeo Thông Minh Thế Hệ Mới")
                    .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1600&auto=format&fit=crop&q=80")
                    .linkUrl("/listing")
                    .sortOrder(3)
                    .isActive(true)
                    .validFrom(now.minusDays(1))
                    .validUntil(now.plusMonths(12))
                    .build()
            );
            bannerRepository.saveAll(defaultBanners);
        } catch (Exception ignored) { }
    }

    @PostMapping
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        if (banner.getIsActive() == null) {
            banner.setIsActive(true);
        }
        Banner saved = bannerRepository.save(banner);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner bannerDetails) {
        return bannerRepository.findById(id)
                .map(existing -> {
                    if (bannerDetails.getTitle() != null) existing.setTitle(bannerDetails.getTitle());
                    if (bannerDetails.getImageUrl() != null) existing.setImageUrl(bannerDetails.getImageUrl());
                    if (bannerDetails.getLinkUrl() != null) existing.setLinkUrl(bannerDetails.getLinkUrl());
                    if (bannerDetails.getSortOrder() != null) existing.setSortOrder(bannerDetails.getSortOrder());
                    if (bannerDetails.getIsActive() != null) existing.setIsActive(bannerDetails.getIsActive());
                    if (bannerDetails.getValidFrom() != null) existing.setValidFrom(bannerDetails.getValidFrom());
                    if (bannerDetails.getValidUntil() != null) existing.setValidUntil(bannerDetails.getValidUntil());
                    return ResponseEntity.ok(bannerRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Banner> patchBanner(@PathVariable Long id, @RequestBody Banner bannerDetails) {
        return updateBanner(id, bannerDetails);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Banner> toggleBannerStatus(@PathVariable Long id, @RequestParam(required = false) Boolean isActive) {
        return bannerRepository.findById(id)
                .map(existing -> {
                    if (isActive != null) {
                        existing.setIsActive(isActive);
                    } else {
                        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
                    }
                    return ResponseEntity.ok(bannerRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        if (bannerRepository.existsById(id)) {
            bannerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
