package org.example.storemanager.modules.marketing.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.marketing.entity.Banner;
import org.example.storemanager.modules.marketing.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return ResponseEntity.ok(bannerRepository.findByIsActiveTrueOrderBySortOrderAsc());
        }
        return ResponseEntity.ok(bannerRepository.findAllByOrderBySortOrderAsc());
    }

    @PostMapping
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        if (bannerRepository.existsById(id)) {
            bannerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
