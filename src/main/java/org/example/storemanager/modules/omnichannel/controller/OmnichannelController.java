package org.example.storemanager.modules.omnichannel.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.omnichannel.entity.*;
import org.example.storemanager.modules.omnichannel.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/omnichannel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OmnichannelController {

    private final SalesChannelRepository salesChannelRepository;
    private final ChannelProductMappingRepository channelProductMappingRepository;
    private final WebhookLogRepository webhookLogRepository;

    // --- SALES CHANNELS ---
    @GetMapping("/channels")
    public ResponseEntity<ApiResponse<List<SalesChannel>>> getAllChannels() {
        return ResponseEntity.ok(ApiResponse.ok(salesChannelRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/channels")
    public ResponseEntity<ApiResponse<SalesChannel>> createChannel(@RequestBody SalesChannel req) {
        req.setIsDeleted(false);
        if (req.getChannelCode() == null || req.getChannelCode().trim().isEmpty()) {
            req.setChannelCode("CH-" + System.currentTimeMillis());
        }
        if (req.getStatus() == null) {
            req.setStatus("CONNECTED");
        }
        if (req.getIsActive() == null) {
            req.setIsActive(true);
        }
        return ResponseEntity.status(201).body(ApiResponse.created(salesChannelRepository.save(req)));
    }

    @PutMapping("/channels/{id}")
    public ResponseEntity<ApiResponse<SalesChannel>> updateChannel(@PathVariable Long id, @RequestBody SalesChannel req) {
        SalesChannel existing = salesChannelRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("SalesChannel", "id", id));
        if (req.getChannelName() != null) existing.setChannelName(req.getChannelName());
        if (req.getPlatform() != null) existing.setPlatform(req.getPlatform());
        if (req.getApiKey() != null) existing.setApiKey(req.getApiKey());
        if (req.getIsActive() != null) existing.setIsActive(req.getIsActive());
        if (req.getShopId() != null) existing.setShopId(req.getShopId());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getLastSyncedAt() != null) existing.setLastSyncedAt(req.getLastSyncedAt());
        if (req.getProductCount() != null) existing.setProductCount(req.getProductCount());
        return ResponseEntity.ok(ApiResponse.ok(salesChannelRepository.save(existing)));
    }

    @DeleteMapping("/channels/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(@PathVariable Long id) {
        SalesChannel existing = salesChannelRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("SalesChannel", "id", id));
        existing.setIsDeleted(true);
        salesChannelRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PRODUCT MAPPINGS ---
    @GetMapping("/mappings")
    public ResponseEntity<ApiResponse<List<ChannelProductMapping>>> getAllMappings() {
        return ResponseEntity.ok(ApiResponse.ok(channelProductMappingRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/mappings")
    public ResponseEntity<ApiResponse<ChannelProductMapping>> createMapping(@RequestBody ChannelProductMapping req) {
        req.setIsDeleted(false);
        if (req.getChannelProductId() == null || req.getChannelProductId().trim().isEmpty()) {
            req.setChannelProductId("ITEM-" + System.currentTimeMillis());
        }
        if (req.getSyncStatus() == null) {
            req.setSyncStatus("SYNCED");
        }
        return ResponseEntity.status(201).body(ApiResponse.created(channelProductMappingRepository.save(req)));
    }

    @PutMapping("/mappings/{id}")
    public ResponseEntity<ApiResponse<ChannelProductMapping>> updateMapping(@PathVariable Long id, @RequestBody ChannelProductMapping req) {
        ChannelProductMapping existing = channelProductMappingRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("ChannelProductMapping", "id", id));
        if (req.getInternalSku() != null) existing.setInternalSku(req.getInternalSku());
        if (req.getProductName() != null) existing.setProductName(req.getProductName());
        if (req.getChannelName() != null) existing.setChannelName(req.getChannelName());
        if (req.getChannelSku() != null) existing.setChannelSku(req.getChannelSku());
        if (req.getChannelPrice() != null) existing.setChannelPrice(req.getChannelPrice());
        if (req.getSyncStatus() != null) existing.setSyncStatus(req.getSyncStatus());
        if (req.getLastSyncedAt() != null) existing.setLastSyncedAt(req.getLastSyncedAt());
        return ResponseEntity.ok(ApiResponse.ok(channelProductMappingRepository.save(existing)));
    }

    @DeleteMapping("/mappings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(@PathVariable Long id) {
        ChannelProductMapping existing = channelProductMappingRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("ChannelProductMapping", "id", id));
        existing.setIsDeleted(true);
        channelProductMappingRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- WEBHOOK LOGS ---
    @GetMapping("/webhook-logs")
    public ResponseEntity<ApiResponse<List<WebhookLog>>> getAllWebhookLogs() {
        return ResponseEntity.ok(ApiResponse.ok(webhookLogRepository.findByIsDeletedFalse()));
    }
}
