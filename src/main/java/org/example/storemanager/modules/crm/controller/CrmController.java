package org.example.storemanager.modules.crm.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.crm.entity.*;
import org.example.storemanager.modules.marketing.entity.*;
import org.example.storemanager.modules.warranty.entity.*;
import org.example.storemanager.modules.partnerarea.entity.PartnerGroup;
import org.example.storemanager.modules.crm.repository.*;
import org.example.storemanager.modules.marketing.repository.*;
import org.example.storemanager.modules.warranty.repository.*;
import org.example.storemanager.modules.partnerarea.repository.PartnerGroupRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CrmController {

    private final LoyaltyTierRepository loyaltyTierRepository;
    private final LoyaltyPointHistoryRepository loyaltyPointHistoryRepository;
    private final VoucherRepository voucherRepository;
    private final CustomerFeedbackRepository customerFeedbackRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final CustomerVoucherRepository customerVoucherRepository;
    private final ProductWarrantyRepository productWarrantyRepository;
    private final WarrantyClaimRepository warrantyClaimRepository;
    private final PartnerGroupRepository partnerGroupRepository;
    private final MarketingCampaignRepository marketingCampaignRepository;

    // --- LOYALTY TIERS ---
    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<List<LoyaltyTier>>> getAllTiers() {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyTierRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/tiers")
    public ResponseEntity<ApiResponse<LoyaltyTier>> createTier(@RequestBody LoyaltyTier req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(loyaltyTierRepository.save(req)));
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<LoyaltyTier>> updateTier(@PathVariable Long id, @RequestBody LoyaltyTier req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(loyaltyTierRepository.save(req)));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable Long id) {
        loyaltyTierRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            loyaltyTierRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- LOYALTY POINT HISTORY ---
    @GetMapping("/loyalty-history")
    public ResponseEntity<ApiResponse<List<LoyaltyPointHistory>>> getAllLoyaltyHistory() {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyPointHistoryRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/loyalty-history")
    public ResponseEntity<ApiResponse<LoyaltyPointHistory>> createLoyaltyHistory(@RequestBody LoyaltyPointHistory req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(loyaltyPointHistoryRepository.save(req)));
    }

    @PutMapping("/loyalty-history/{id}")
    public ResponseEntity<ApiResponse<LoyaltyPointHistory>> updateLoyaltyHistory(@PathVariable Long id, @RequestBody LoyaltyPointHistory req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(loyaltyPointHistoryRepository.save(req)));
    }

    @DeleteMapping("/loyalty-history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLoyaltyHistory(@PathVariable Long id) {
        loyaltyPointHistoryRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            loyaltyPointHistoryRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- VOUCHERS ---
    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<Voucher>>> getAllVouchers() {
        return ResponseEntity.ok(ApiResponse.ok(voucherRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/vouchers")
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@RequestBody Voucher req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(voucherRepository.save(req)));
    }

    @PutMapping("/vouchers/{id}")
    public ResponseEntity<ApiResponse<Voucher>> updateVoucher(@PathVariable Long id, @RequestBody Voucher req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(voucherRepository.save(req)));
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            voucherRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- CUSTOMER FEEDBACK ---
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<CustomerFeedback>>> getAllFeedback() {
        return ResponseEntity.ok(ApiResponse.ok(customerFeedbackRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<CustomerFeedback>> createFeedback(@RequestBody CustomerFeedback req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(customerFeedbackRepository.save(req)));
    }

    @PutMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<CustomerFeedback>> updateFeedback(@PathVariable Long id, @RequestBody CustomerFeedback req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(customerFeedbackRepository.save(req)));
    }

    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        customerFeedbackRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            customerFeedbackRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- SUPPORT TICKETS ---
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<List<SupportTicket>>> getAllTickets() {
        return ResponseEntity.ok(ApiResponse.ok(supportTicketRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<SupportTicket>> createTicket(@RequestBody SupportTicket req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(supportTicketRepository.save(req)));
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<SupportTicket>> updateTicket(@PathVariable Long id, @RequestBody SupportTicket req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(supportTicketRepository.save(req)));
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long id) {
        supportTicketRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            supportTicketRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- TICKET MESSAGES ---
    @GetMapping("/ticket-messages")
    public ResponseEntity<ApiResponse<List<TicketMessage>>> getAllTicketMessages() {
        return ResponseEntity.ok(ApiResponse.ok(ticketMessageRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/ticket-messages")
    public ResponseEntity<ApiResponse<TicketMessage>> createTicketMessage(@RequestBody TicketMessage req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(ticketMessageRepository.save(req)));
    }

    @PutMapping("/ticket-messages/{id}")
    public ResponseEntity<ApiResponse<TicketMessage>> updateTicketMessage(@PathVariable Long id, @RequestBody TicketMessage req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(ticketMessageRepository.save(req)));
    }

    @DeleteMapping("/ticket-messages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTicketMessage(@PathVariable Long id) {
        ticketMessageRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            ticketMessageRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- CUSTOMER VOUCHERS ---
    @GetMapping("/customer-vouchers")
    public ResponseEntity<ApiResponse<List<CustomerVoucher>>> getAllCustomerVouchers() {
        return ResponseEntity.ok(ApiResponse.ok(customerVoucherRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/customer-vouchers")
    public ResponseEntity<ApiResponse<CustomerVoucher>> createCustomerVoucher(@RequestBody CustomerVoucher req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(customerVoucherRepository.save(req)));
    }

    @PutMapping("/customer-vouchers/{id}")
    public ResponseEntity<ApiResponse<CustomerVoucher>> updateCustomerVoucher(@PathVariable Long id, @RequestBody CustomerVoucher req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(customerVoucherRepository.save(req)));
    }

    @DeleteMapping("/customer-vouchers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerVoucher(@PathVariable Long id) {
        customerVoucherRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            customerVoucherRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- WARRANTIES ---
    @GetMapping("/warranties")
    public ResponseEntity<ApiResponse<List<ProductWarranty>>> getAllWarranties() {
        return ResponseEntity.ok(ApiResponse.ok(productWarrantyRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/warranties")
    public ResponseEntity<ApiResponse<ProductWarranty>> createWarranty(@RequestBody ProductWarranty req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(productWarrantyRepository.save(req)));
    }

    @PutMapping("/warranties/{id}")
    public ResponseEntity<ApiResponse<ProductWarranty>> updateWarranty(@PathVariable Long id, @RequestBody ProductWarranty req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(productWarrantyRepository.save(req)));
    }

    @DeleteMapping("/warranties/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarranty(@PathVariable Long id) {
        productWarrantyRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            productWarrantyRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- WARRANTY CLAIMS ---
    @GetMapping("/warranty-claims")
    public ResponseEntity<ApiResponse<List<WarrantyClaim>>> getAllWarrantyClaims() {
        return ResponseEntity.ok(ApiResponse.ok(warrantyClaimRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/warranty-claims")
    public ResponseEntity<ApiResponse<WarrantyClaim>> createWarrantyClaim(@RequestBody WarrantyClaim req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(warrantyClaimRepository.save(req)));
    }

    @PutMapping("/warranty-claims/{id}")
    public ResponseEntity<ApiResponse<WarrantyClaim>> updateWarrantyClaim(@PathVariable Long id, @RequestBody WarrantyClaim req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(warrantyClaimRepository.save(req)));
    }

    @DeleteMapping("/warranty-claims/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarrantyClaim(@PathVariable Long id) {
        warrantyClaimRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            warrantyClaimRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PARTNER GROUPS ---
    @GetMapping("/partner-groups")
    public ResponseEntity<ApiResponse<List<PartnerGroup>>> getAllPartnerGroups() {
        return ResponseEntity.ok(ApiResponse.ok(partnerGroupRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/partner-groups")
    public ResponseEntity<ApiResponse<PartnerGroup>> createPartnerGroup(@RequestBody PartnerGroup req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(partnerGroupRepository.save(req)));
    }

    @PutMapping("/partner-groups/{id}")
    public ResponseEntity<ApiResponse<PartnerGroup>> updatePartnerGroup(@PathVariable Long id, @RequestBody PartnerGroup req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(partnerGroupRepository.save(req)));
    }

    @DeleteMapping("/partner-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartnerGroup(@PathVariable Long id) {
        partnerGroupRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            partnerGroupRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- MARKETING CAMPAIGNS ---
    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<List<MarketingCampaign>>> getAllCampaigns() {
        return ResponseEntity.ok(ApiResponse.ok(marketingCampaignRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<MarketingCampaign>> createCampaign(@RequestBody MarketingCampaign req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(marketingCampaignRepository.save(req)));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<MarketingCampaign>> updateCampaign(@PathVariable Long id, @RequestBody MarketingCampaign req) {
        req.setId(id);
        return ResponseEntity.ok(ApiResponse.ok(marketingCampaignRepository.save(req)));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        marketingCampaignRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            marketingCampaignRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
