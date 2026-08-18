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
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.partnerarea.entity.Customer;

@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CrmController {

    private final CustomerRepository customerRepository;
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
    private final org.example.storemanager.modules.crm.service.LoyaltyService loyaltyService;

    // --- LOYALTY CALCULATION & CUSTOMER HISTORY ---
    @PostMapping("/loyalty/calculate")
    public ResponseEntity<ApiResponse<org.example.storemanager.modules.crm.dto.LoyaltyCalculateResponse>> calculateLoyalty(
            @RequestBody org.example.storemanager.modules.crm.dto.LoyaltyCalculateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.calculateExpectedPoints(req)));
    }

    @GetMapping("/customers/{customerId}/loyalty-history")
    public ResponseEntity<ApiResponse<List<org.example.storemanager.modules.crm.dto.LoyaltyTransactionResponse>>> getCustomerLoyaltyHistory(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getCustomerLoyaltyHistory(customerId)));
    }

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
        return ResponseEntity.ok(ApiResponse.ok(voucherRepository.findByIsDeletedFalseOrderByUpdatedAtDesc()));
    }

    @PostMapping("/vouchers")
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@RequestBody Voucher req) {
        req.setIsDeleted(false);
        if (req.getIsActive() == null) req.setIsActive(true);
        if (req.getIsPublic() == null) req.setIsPublic(true);
        if (req.getCurrentUsage() == null) req.setCurrentUsage(0);
        if (req.getStatus() == null) req.setStatus("ACTIVE");
        return ResponseEntity.status(201).body(ApiResponse.created(voucherRepository.save(req)));
    }

    @PutMapping("/vouchers/{id}")
    public ResponseEntity<ApiResponse<Voucher>> updateVoucher(@PathVariable Long id, @RequestBody Voucher req) {
        Voucher existing = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher ID: " + id));
        if (req.getVoucherCode() != null) existing.setVoucherCode(req.getVoucherCode());
        if (req.getVoucherName() != null) existing.setVoucherName(req.getVoucherName());
        if (req.getType() != null) existing.setType(req.getType());
        if (req.getValue() != null) existing.setValue(req.getValue());
        if (req.getMinOrderAmount() != null) existing.setMinOrderAmount(req.getMinOrderAmount());
        if (req.getMaxDiscountAmount() != null) existing.setMaxDiscountAmount(req.getMaxDiscountAmount());
        if (req.getMaxUsage() != null) existing.setMaxUsage(req.getMaxUsage());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getStartDate() != null) existing.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) existing.setEndDate(req.getEndDate());
        if (req.getIsActive() != null) existing.setIsActive(req.getIsActive());
        if (req.getIsPublic() != null) existing.setIsPublic(req.getIsPublic());
        existing.setIsDeleted(false);
        return ResponseEntity.ok(ApiResponse.ok(voucherRepository.save(existing)));
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> getAllCustomerVouchers() {
        java.util.List<CustomerVoucher> list = customerVoucherRepository.findByIsDeletedFalseOrderByUpdatedAtDesc();
        java.util.List<java.util.Map<String, Object>> res = list.stream().map(cv -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", cv.getId().toString());
            m.put("voucherCode", cv.getVoucherCode());
            m.put("status", cv.getStatus());
            m.put("issueDate", cv.getCollectedAt() != null ? cv.getCollectedAt().toLocalDate().toString() : "");
            m.put("expiryDate", cv.getExpiredAt() != null ? cv.getExpiredAt().toLocalDate().toString() : "");
            m.put("usedDate", cv.getUsedAt() != null ? cv.getUsedAt().toLocalDate().toString() : "");
            m.put("notes", cv.getNote() != null ? cv.getNote() : "");
            if (cv.getUsedOrder() != null) {
                m.put("usedOrderId", cv.getUsedOrder().getId().toString());
            }
            if (cv.getCustomer() != null) {
                m.put("customerId", cv.getCustomer().getId().toString());
                m.put("customerName", cv.getCustomer().getName());
                m.put("customerPhone", cv.getCustomer().getPhone());
                m.put("customerCode", cv.getCustomer().getCustomerCode());
            }
            if (cv.getVoucher() != null) {
                m.put("programId", cv.getVoucher().getId().toString());
                m.put("programName", cv.getVoucher().getVoucherName());
                m.put("voucherName", cv.getVoucher().getVoucherName());
                m.put("discountType", cv.getVoucher().getType());
                m.put("discountValue", cv.getVoucher().getValue());
                m.put("minOrderValue", cv.getVoucher().getMinOrderAmount());
                m.put("maxDiscount", cv.getVoucher().getMaxDiscountAmount());
            }
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping("/customer-vouchers")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createCustomerVoucher(@RequestBody java.util.Map<String, Object> req) {
        String voucherCode = req.get("voucherCode") != null ? req.get("voucherCode").toString() : "VC-" + System.currentTimeMillis();
        String status = req.get("status") != null ? req.get("status").toString() : "ACTIVE";
        String notes = req.get("notes") != null ? req.get("notes").toString() : "";

        Long customerId = null;
        if (req.get("customerId") != null) {
            try { customerId = Long.valueOf(req.get("customerId").toString()); } catch (Exception ignored) {}
        } else if (req.get("customer") instanceof java.util.Map) {
            Object cid = ((java.util.Map<?, ?>) req.get("customer")).get("id");
            if (cid != null) try { customerId = Long.valueOf(cid.toString()); } catch (Exception ignored) {}
        }

        Customer c = null;
        if (customerId != null) {
            c = customerRepository.findById(customerId).orElse(null);
        }
        if (c == null) {
            java.util.List<Customer> customers = customerRepository.findAll();
            if (!customers.isEmpty()) {
                c = customers.get(0);
            }
        }

        Long voucherId = null;
        if (req.get("programId") != null) {
            try { voucherId = Long.valueOf(req.get("programId").toString()); } catch (Exception ignored) {}
        } else if (req.get("voucherId") != null) {
            try { voucherId = Long.valueOf(req.get("voucherId").toString()); } catch (Exception ignored) {}
        } else if (req.get("voucher") instanceof java.util.Map) {
            Object vid = ((java.util.Map<?, ?>) req.get("voucher")).get("id");
            if (vid != null) try { voucherId = Long.valueOf(vid.toString()); } catch (Exception ignored) {}
        }

        Voucher v = null;
        if (voucherId != null) {
            v = voucherRepository.findById(voucherId).orElse(null);
        }
        if (v == null) {
            java.util.List<Voucher> vouchers = voucherRepository.findAll();
            if (!vouchers.isEmpty()) {
                v = vouchers.get(0);
            }
        }

        CustomerVoucher cv = CustomerVoucher.builder()
                .customer(c)
                .voucher(v)
                .voucherCode(voucherCode)
                .collectedAt(java.time.LocalDateTime.now())
                .expiredAt(java.time.LocalDateTime.now().plusDays(30))
                .status(status)
                .build();
        cv.setIsDeleted(false);
        cv.setNote(notes);

        CustomerVoucher saved = customerVoucherRepository.save(cv);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("voucherCode", saved.getVoucherCode());
        resp.put("status", saved.getStatus());
        if (c != null) {
            resp.put("customerId", c.getId().toString());
            resp.put("customerName", c.getName());
            resp.put("customerPhone", c.getPhone());
            resp.put("customerCode", c.getCustomerCode());
        }
        if (v != null) {
            resp.put("programId", v.getId().toString());
            resp.put("programName", v.getVoucherName());
            resp.put("voucherName", v.getVoucherName());
        }

        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping("/customer-vouchers/{id}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateCustomerVoucher(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        CustomerVoucher cv = customerVoucherRepository.findById(id).orElse(null);
        if (cv != null) {
            if (req.get("status") != null) cv.setStatus(req.get("status").toString());
            if (req.get("notes") != null) cv.setNote(req.get("notes").toString());
            customerVoucherRepository.save(cv);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.ok(resp));
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
