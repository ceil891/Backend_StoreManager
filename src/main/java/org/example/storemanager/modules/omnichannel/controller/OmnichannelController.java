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
        return ResponseEntity.status(201).body(ApiResponse.created(salesChannelRepository.save(req)));
    }

    // --- PRODUCT MAPPINGS ---
    @GetMapping("/mappings")
    public ResponseEntity<ApiResponse<List<ChannelProductMapping>>> getAllMappings() {
        return ResponseEntity.ok(ApiResponse.ok(channelProductMappingRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/mappings")
    public ResponseEntity<ApiResponse<ChannelProductMapping>> createMapping(@RequestBody ChannelProductMapping req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(channelProductMappingRepository.save(req)));
    }

    // --- WEBHOOK LOGS ---
    @GetMapping("/webhook-logs")
    public ResponseEntity<ApiResponse<List<WebhookLog>>> getAllWebhookLogs() {
        return ResponseEntity.ok(ApiResponse.ok(webhookLogRepository.findByIsDeletedFalse()));
    }
}
