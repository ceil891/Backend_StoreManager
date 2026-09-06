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
import org.springframework.transaction.annotation.Transactional;
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
    private final org.example.storemanager.modules.catalog.repository.SerialNumberRepository serialNumberRepository;
    private final org.example.storemanager.modules.catalog.repository.ProductRepository productRepository;

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
        if (req.getMinPoints() == null) {
            req.setMinPoints(req.getMinSpend() != null ? req.getMinSpend().intValue() : 0);
        }
        if (req.getMinSpend() == null && req.getMinPoints() != null) {
            req.setMinSpend(java.math.BigDecimal.valueOf(req.getMinPoints()));
        }
        if (req.getPointMultiplier() == null) {
            req.setPointMultiplier(java.math.BigDecimal.ONE);
        }
        if (req.getDiscountPercent() == null) {
            req.setDiscountPercent(java.math.BigDecimal.ZERO);
        }
        if (req.getTierCode() == null || req.getTierCode().isBlank()) {
            req.setTierCode("TIER_" + System.currentTimeMillis());
        }
        if (req.getTierName() == null || req.getTierName().isBlank()) {
            req.setTierName("Hạng " + req.getTierCode());
        }
        return ResponseEntity.status(201).body(ApiResponse.created(loyaltyTierRepository.save(req)));
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<LoyaltyTier>> updateTier(@PathVariable String id, @RequestBody LoyaltyTier req) {
        LoyaltyTier target = null;
        if (id.matches("\\d+")) {
            target = loyaltyTierRepository.findByIdAndIsDeletedFalse(Long.parseLong(id)).orElse(null);
        }
        if (target == null) {
            target = loyaltyTierRepository.findByTierCodeAndIsDeletedFalse(id).orElse(null);
        }
        if (target == null) {
            target = loyaltyTierRepository.findByTierNameAndIsDeletedFalse(id).orElse(null);
        }
        if (target != null) {
            req.setId(target.getId());
            if (req.getTierCode() == null || req.getTierCode().isBlank()) req.setTierCode(target.getTierCode());
            if (req.getTierName() == null || req.getTierName().isBlank()) req.setTierName(target.getTierName());
        }
        if (req.getMinPoints() == null) {
            req.setMinPoints(req.getMinSpend() != null ? req.getMinSpend().intValue() : (target != null && target.getMinPoints() != null ? target.getMinPoints() : 0));
        }
        if (req.getMinSpend() == null && req.getMinPoints() != null) {
            req.setMinSpend(java.math.BigDecimal.valueOf(req.getMinPoints()));
        }
        if (req.getPointMultiplier() == null) {
            req.setPointMultiplier(target != null && target.getPointMultiplier() != null ? target.getPointMultiplier() : java.math.BigDecimal.ONE);
        }
        if (req.getDiscountPercent() == null) {
            req.setDiscountPercent(target != null && target.getDiscountPercent() != null ? target.getDiscountPercent() : java.math.BigDecimal.ZERO);
        }
        req.setIsDeleted(false);
        return ResponseEntity.ok(ApiResponse.ok(loyaltyTierRepository.save(req)));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable String id) {
        if (id.matches("\\d+")) {
            loyaltyTierRepository.findById(Long.parseLong(id)).ifPresent(entity -> {
                entity.setIsDeleted(true);
                loyaltyTierRepository.save(entity);
            });
        }
        loyaltyTierRepository.findByTierCodeAndIsDeletedFalse(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            loyaltyTierRepository.save(entity);
        });
        loyaltyTierRepository.findByTierNameAndIsDeletedFalse(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            loyaltyTierRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- LOYALTY POINT HISTORY ---
    @GetMapping({"/loyalty-history", "/loyalty-histories"})
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllLoyaltyHistory() {
        List<LoyaltyPointHistory> list = loyaltyPointHistoryRepository.findByIsDeletedFalse();
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L, a.getId() != null ? a.getId() : 0L));
        List<java.util.Map<String, Object>> res = list.stream().map(h -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", h.getId().toString());
            m.put("code", h.getRefCode() != null ? h.getRefCode() : ("TX-" + h.getId()));
            m.put("pointsChange", h.getPointsChange() != null ? h.getPointsChange() : 0);
            m.put("pointChange", h.getPointsChange() != null ? h.getPointsChange() : 0);
            m.put("transactionType", h.getTransactionType() != null ? h.getTransactionType() : "TÍCH ĐIỂM BÁN HÀNG");
            m.put("refDocument", h.getRefCode() != null ? h.getRefCode() : "");
            m.put("balanceAfter", h.getCurrentPoints() != null ? h.getCurrentPoints() : 0);
            m.put("date", h.getCreatedAt() != null ? h.getCreatedAt().toLocalDate().toString() : java.time.LocalDate.now().toString());
            m.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
            m.put("notes", h.getDescription() != null ? h.getDescription() : "");
            if (h.getCustomer() != null) {
                m.put("customerId", h.getCustomer().getId().toString());
                m.put("customerName", h.getCustomer().getName() != null ? h.getCustomer().getName() : "Khách hàng");
                m.put("customerPhone", h.getCustomer().getPhone() != null ? h.getCustomer().getPhone() : "");
            } else {
                m.put("customerName", "Khách hàng");
                m.put("customerPhone", "");
            }
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping({"/loyalty-history", "/loyalty-histories"})
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createLoyaltyHistory(@RequestBody java.util.Map<String, Object> req) {
        Long customerId = null;
        if (req.get("customerId") != null) {
            try { customerId = Long.valueOf(req.get("customerId").toString()); } catch (Exception ignored) {}
        }
        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId).orElse(null);
        }
        if (customer == null && req.get("phone") != null) {
            String ph = req.get("phone").toString().trim();
            customer = customerRepository.findByPhone(ph).orElse(null);
        }
        if (customer == null && req.get("customerPhone") != null) {
            String ph = req.get("customerPhone").toString().trim();
            customer = customerRepository.findByPhone(ph).orElse(null);
        }

        Integer pointsChange = req.get("pointsChange") != null ? Integer.valueOf(req.get("pointsChange").toString()) : 
                              (req.get("pointChange") != null ? Integer.valueOf(req.get("pointChange").toString()) : 0);
        String txType = req.get("transactionType") != null ? req.get("transactionType").toString() : (pointsChange >= 0 ? "EARN" : "REDEEM");
        String refCode = req.get("refDocument") != null ? req.get("refDocument").toString() : 
                        (req.get("code") != null ? req.get("code").toString() : "TX-POS-" + System.currentTimeMillis());
        Integer balanceAfter = req.get("balanceAfter") != null ? Integer.valueOf(req.get("balanceAfter").toString()) : pointsChange;
        String notes = req.get("notes") != null ? req.get("notes").toString() : (req.get("description") != null ? req.get("description").toString() : "");

        // Đồng bộ trực tiếp điểm, chi tiêu và hạng vào bảng customers
        if (customer != null) {
            int curPts = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
            int newBalance = Math.max(0, curPts + pointsChange);
            customer.setPoints((double) newBalance);

            if (pointsChange > 0 && req.get("amount") != null) {
                try {
                    double amt = Double.parseDouble(req.get("amount").toString());
                    double curSpend = customer.getTotalSpend() != null ? customer.getTotalSpend() : 0.0;
                    customer.setTotalSpend(curSpend + amt);
                } catch (Exception ignored) {}
            }

            double spendVal = customer.getTotalSpend() != null ? customer.getTotalSpend() : 0.0;
            if (newBalance >= 6000 || spendVal >= 50000000.0) customer.setMembershipRank("DIAMOND");
            else if (newBalance >= 3000 || spendVal >= 25000000.0) customer.setMembershipRank("ELITE_CLUB");
            else if (newBalance >= 1500 || spendVal >= 10000000.0) customer.setMembershipRank("GOLD");
            else if (newBalance >= 500  || spendVal >= 3000000.0)  customer.setMembershipRank("SILVER");
            else customer.setMembershipRank("BRONZE");

            customerRepository.save(customer);
            balanceAfter = newBalance;
        }

        LoyaltyPointHistory history = LoyaltyPointHistory.builder()
                .customer(customer)
                .pointsChange(pointsChange)
                .transactionType(txType)
                .refCode(refCode)
                .currentPoints(balanceAfter)
                .description(notes)
                .build();
        history.setIsDeleted(false);
        LoyaltyPointHistory saved = loyaltyPointHistoryRepository.save(history);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("code", saved.getRefCode());
        resp.put("pointsChange", saved.getPointsChange());
        resp.put("transactionType", saved.getTransactionType());
        resp.put("customerName", customer != null ? customer.getName() : "Khách hàng");
        resp.put("customerPhone", customer != null ? customer.getPhone() : "");
        resp.put("balanceAfter", saved.getCurrentPoints());
        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PostMapping("/customers/{id}/adjust-points")
    @Transactional
    public ResponseEntity<ApiResponse<Customer>> adjustCustomerPoints(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> req) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Customer", "id", id));

        int pointsChange = req.get("pointsChange") != null ? Integer.parseInt(req.get("pointsChange").toString()) : 0;
        String reason = req.get("reason") != null ? req.get("reason").toString() : "Điều chỉnh điểm thủ công";
        String txType = pointsChange >= 0 ? "EARN" : "REDEEM";

        int curPts = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        int balanceAfter = Math.max(0, curPts + pointsChange);
        customer.setPoints((double) balanceAfter);

        double spendVal = customer.getTotalSpend() != null ? customer.getTotalSpend() : 0.0;
        if (balanceAfter >= 6000 || spendVal >= 50000000.0) customer.setMembershipRank("DIAMOND");
        else if (balanceAfter >= 3000 || spendVal >= 25000000.0) customer.setMembershipRank("ELITE_CLUB");
        else if (balanceAfter >= 1500 || spendVal >= 10000000.0) customer.setMembershipRank("GOLD");
        else if (balanceAfter >= 500  || spendVal >= 3000000.0)  customer.setMembershipRank("SILVER");
        else customer.setMembershipRank("BRONZE");

        Customer saved = customerRepository.save(customer);

        LoyaltyPointHistory history = LoyaltyPointHistory.builder()
                .customer(saved)
                .pointsChange(pointsChange)
                .transactionType(txType)
                .refCode("ADJ-" + System.currentTimeMillis())
                .currentPoints(balanceAfter)
                .description(reason)
                .build();
        history.setIsDeleted(false);
        loyaltyPointHistoryRepository.save(history);

        return ResponseEntity.ok(ApiResponse.ok("Điều chỉnh điểm khách hàng thành công", saved));
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
        if (req.getStartDate() != null && req.getEndDate() != null && req.getEndDate().isBefore(req.getStartDate())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Hạn sử dụng không được nhỏ hơn Ngày bắt đầu"
            );
        }
        req.setIsDeleted(false);
        if (req.getIsActive() == null) req.setIsActive(true);
        if (req.getIsPublic() == null) req.setIsPublic(true);
        if (req.getCurrentUsage() == null) req.setCurrentUsage(0);
        if (req.getStatus() == null) req.setStatus("ACTIVE");
        return ResponseEntity.status(201).body(ApiResponse.created(voucherRepository.save(req)));
    }

    @PutMapping("/vouchers/{id}")
    public ResponseEntity<ApiResponse<Voucher>> updateVoucher(@PathVariable Long id, @RequestBody Voucher req) {
        if (req.getStartDate() != null && req.getEndDate() != null && req.getEndDate().isBefore(req.getStartDate())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Hạn sử dụng không được nhỏ hơn Ngày bắt đầu"
            );
        }
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
    @GetMapping({"/feedback", "/feedbacks"})
    @Transactional
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllFeedback() {
        List<CustomerFeedback> list = customerFeedbackRepository.findByIsDeletedFalse();
        List<java.util.Map<String, Object>> res = list.stream().map(fb -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", fb.getId().toString());
            m.put("rating", fb.getRating() != null ? fb.getRating() : 5);
            m.put("content", fb.getComment() != null ? fb.getComment() : "");
            m.put("comment", fb.getComment() != null ? fb.getComment() : "");
            m.put("title", fb.getTitle() != null ? fb.getTitle() : "");
            m.put("category", "SERVICE");
            m.put("status", fb.getStatus() != null ? fb.getStatus() : "APPROVED");
            m.put("createdAt", fb.getCreatedAt() != null ? fb.getCreatedAt().toString() : "");
            m.put("resolutionNote", fb.getReply() != null ? fb.getReply() : "");
            m.put("reply", fb.getReply() != null ? fb.getReply() : "");
            if (fb.getCustomer() != null && fb.getCustomer().getName() != null && !fb.getCustomer().getName().isBlank()) {
                m.put("customerId", fb.getCustomer().getId().toString());
                m.put("customerName", fb.getCustomer().getName());
                m.put("customerPhone", fb.getCustomer().getPhone() != null ? fb.getCustomer().getPhone() : "");
            } else {
                m.put("customerName", "Nguyễn Văn An (Web Online)");
                m.put("customerPhone", "0901234567");
            }
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping({"/feedback", "/feedbacks"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createFeedback(@RequestBody java.util.Map<String, Object> req) {
        String content = req.get("content") != null ? req.get("content").toString() : (req.get("comment") != null ? req.get("comment").toString() : "");
        Integer rating = req.get("rating") != null ? Integer.valueOf(req.get("rating").toString()) : 5;
        String status = req.get("status") != null ? req.get("status").toString() : "PENDING";
        String customerName = req.get("customerName") != null ? req.get("customerName").toString().trim() : "";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString().trim() : "";
        String customerEmail = req.get("customerEmail") != null ? req.get("customerEmail").toString().trim() : "";

        Customer customer = null;
        if (!customerPhone.isBlank()) {
            customer = customerRepository.findByPhoneAndIsDeletedFalse(customerPhone).orElse(null);
            if (customer == null) {
                customer = customerRepository.findByPhone(customerPhone).orElse(null);
            }
        }
        if (customer == null && !customerName.isBlank()) {
            customer = customerRepository.findByNameIgnoreCaseAndIsDeletedFalse(customerName).orElse(null);
        }
        if (customer == null && !customerName.isBlank()) {
            Customer newCust = Customer.builder()
                    .name(customerName)
                    .phone(!customerPhone.isBlank() ? customerPhone : "090" + (System.currentTimeMillis() % 10000000))
                    .email(!customerEmail.isBlank() ? customerEmail : "customer@store.vn")
                    .customerCode("KH-FB-" + (System.currentTimeMillis() % 100000))
                    .membershipRank("BRONZE")
                    .points(0.0)
                    .totalSpend(0.0)
                    .isActive(true)
                    .build();
            newCust.setIsDeleted(false);
            customer = customerRepository.save(newCust);
        }

        CustomerFeedback fb = CustomerFeedback.builder()
                .customer(customer)
                .rating(rating)
                .comment(content)
                .title(req.get("title") != null ? req.get("title").toString() : "Đánh giá")
                .status(status)
                .build();
        fb.setIsDeleted(false);
        CustomerFeedback saved = customerFeedbackRepository.save(fb);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("rating", saved.getRating());
        resp.put("content", saved.getComment());
        resp.put("status", saved.getStatus());
        resp.put("customerName", customer != null ? customer.getName() : customerName);
        resp.put("customerPhone", customer != null ? customer.getPhone() : customerPhone);
        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping({"/feedback/{id}", "/feedbacks/{id}"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateFeedback(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        CustomerFeedback fb = customerFeedbackRepository.findById(id).orElse(null);
        if (fb != null) {
            if (req.get("status") != null) fb.setStatus(req.get("status").toString());
            if (req.get("resolutionNote") != null) fb.setReply(req.get("resolutionNote").toString());
            if (req.get("reply") != null) fb.setReply(req.get("reply").toString());
            if (req.get("rating") != null) fb.setRating(Integer.valueOf(req.get("rating").toString()));
            if (req.get("content") != null) fb.setComment(req.get("content").toString());
            customerFeedbackRepository.save(fb);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @DeleteMapping({"/feedback/{id}", "/feedbacks/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        customerFeedbackRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            customerFeedbackRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- SUPPORT TICKETS ---
    @GetMapping({"/tickets", "/support-tickets"})
    @Transactional
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllTickets() {
        List<SupportTicket> list = supportTicketRepository.findByIsDeletedFalse();
        // Sort descending by ID so newly created tickets appear first
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L, a.getId() != null ? a.getId() : 0L));

        List<java.util.Map<String, Object>> res = list.stream().map(st -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", st.getId().toString());
            m.put("ticketCode", st.getTicketCode() != null ? st.getTicketCode() : "TK-" + st.getId());
            m.put("subject", st.getTitle() != null ? st.getTitle() : "");
            m.put("title", st.getTitle() != null ? st.getTitle() : "");
            m.put("priority", st.getPriority() != null ? st.getPriority() : "MEDIUM");
            m.put("status", st.getStatus() != null ? st.getStatus() : "OPEN");
            m.put("category", "GENERAL");
            m.put("createdDate", st.getCreatedAt() != null ? st.getCreatedAt().toLocalDate().toString() : java.time.LocalDate.now().toString());
            m.put("assignedTo", st.getAssignedTo() != null ? st.getAssignedTo().getFullName() : "Nhân viên CSKH");
            if (st.getCustomer() != null && st.getCustomer().getName() != null && !st.getCustomer().getName().isBlank()) {
                m.put("customerId", st.getCustomer().getId().toString());
                m.put("customerName", st.getCustomer().getName());
                m.put("customerPhone", st.getCustomer().getPhone() != null ? st.getCustomer().getPhone() : "");
            } else {
                String cName = "Khách hàng Web Online";
                if (st.getTitle() != null && st.getTitle().contains(": ")) {
                    cName = st.getTitle().substring(st.getTitle().lastIndexOf(": ") + 2).trim();
                } else if (st.getTitle() != null && st.getTitle().contains("] ")) {
                    cName = st.getTitle().substring(st.getTitle().lastIndexOf("] ") + 2).trim();
                }
                m.put("customerName", cName);
                m.put("customerPhone", "0988123456");
            }
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping({"/tickets", "/support-tickets"})
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createTicket(@RequestBody java.util.Map<String, Object> req) {
        String ticketCode = req.get("ticketCode") != null ? req.get("ticketCode").toString() :
                (req.get("ticketNumber") != null ? req.get("ticketNumber").toString() : "ONLINE-" + (System.currentTimeMillis() % 100000));
        String customerName = req.get("customerName") != null ? req.get("customerName").toString() : "";
        String defaultTitle = !customerName.isBlank() ? "[Khách Web Online] " + customerName : "Yêu cầu hỗ trợ";
        String subject = req.get("subject") != null ? req.get("subject").toString() : (req.get("title") != null ? req.get("title").toString() : defaultTitle);
        String priority = req.get("priority") != null ? req.get("priority").toString() : "HIGH";
        String status = req.get("status") != null ? req.get("status").toString() : "OPEN";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString() : "";

        Customer customer = null;
        if (!customerPhone.isBlank()) {
            customer = customerRepository.findByPhoneAndIsDeletedFalse(customerPhone.trim()).orElse(null);
            if (customer == null) {
                customer = customerRepository.findByPhone(customerPhone.trim()).orElse(null);
            }
        }
        if (customer == null && !customerName.isBlank()) {
            customer = customerRepository.findByNameIgnoreCaseAndIsDeletedFalse(customerName.trim()).orElse(null);
        }
        if (customer == null && !customerName.isBlank()) {
            Customer newCust = Customer.builder()
                    .name(customerName)
                    .phone(!customerPhone.isBlank() ? customerPhone : "0988123456")
                    .email("customer@store.vn")
                    .address("Việt Nam")
                    .customerCode("KH-ONLINE-" + (System.currentTimeMillis() % 100000))
                    .membershipRank("BRONZE")
                    .points(0.0)
                    .totalSpend(0.0)
                    .isActive(true)
                    .build();
            newCust.setIsDeleted(false);
            customer = customerRepository.save(newCust);
        }

        SupportTicket ticket = SupportTicket.builder()
                .ticketCode(ticketCode)
                .title(subject)
                .priority(priority)
                .status(status)
                .customer(customer)
                .build();
        ticket.setIsDeleted(false);
        SupportTicket saved = supportTicketRepository.save(ticket);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("ticketCode", saved.getTicketCode());
        resp.put("subject", saved.getTitle());
        resp.put("title", saved.getTitle());
        resp.put("priority", saved.getPriority());
        resp.put("status", saved.getStatus());
        resp.put("customerName", customer != null ? customer.getName() : customerName);
        resp.put("customerPhone", customer != null ? customer.getPhone() : customerPhone);
        resp.put("createdDate", java.time.LocalDate.now().toString());
        resp.put("assignedTo", "Nhân viên CSKH");
        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping({"/tickets/{id}", "/support-tickets/{id}"})
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateTicket(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        SupportTicket st = supportTicketRepository.findById(id).orElse(null);
        if (st != null) {
            if (req.get("status") != null) st.setStatus(req.get("status").toString());
            if (req.get("priority") != null) st.setPriority(req.get("priority").toString());
            if (req.get("subject") != null) st.setTitle(req.get("subject").toString());
            if (req.get("title") != null) st.setTitle(req.get("title").toString());
            supportTicketRepository.save(st);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @DeleteMapping({"/tickets/{id}", "/support-tickets/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long id) {
        supportTicketRepository.findById(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            supportTicketRepository.save(entity);
        });
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- TICKET MESSAGES ---
    @GetMapping({"/ticket-messages", "/support-tickets/{ticketId}/messages"})
    @Transactional
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllTicketMessages(
            @PathVariable(required = false) Long ticketId,
            @RequestParam(value = "ticketId", required = false) Long queryTicketId) {
        Long effectiveTicketId = ticketId != null ? ticketId : queryTicketId;
        List<TicketMessage> list = ticketMessageRepository.findByIsDeletedFalse();
        if (effectiveTicketId != null) {
            list = list.stream().filter(tm -> tm.getTicket() != null && effectiveTicketId.equals(tm.getTicket().getId())).collect(java.util.stream.Collectors.toList());
        }
        // Sort chronologically ascending
        list.sort((a, b) -> Long.compare(a.getId() != null ? a.getId() : 0L, b.getId() != null ? b.getId() : 0L));

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<java.util.Map<String, Object>> res = list.stream().map(tm -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", tm.getId().toString());
            m.put("ticketId", tm.getTicket() != null ? tm.getTicket().getId().toString() : (effectiveTicketId != null ? effectiveTicketId.toString() : "1"));
            m.put("message", tm.getMessage() != null ? tm.getMessage() : "");
            m.put("isStaff", tm.getIsFromCustomer() != null ? !tm.getIsFromCustomer() : true);
            String custName = "Khách hàng Web Online";
            if (tm.getTicket() != null && tm.getTicket().getCustomer() != null && tm.getTicket().getCustomer().getName() != null) {
                custName = tm.getTicket().getCustomer().getName();
            } else if (tm.getTicket() != null && tm.getTicket().getTitle() != null && tm.getTicket().getTitle().contains(": ")) {
                custName = tm.getTicket().getTitle().substring(tm.getTicket().getTitle().lastIndexOf(": ") + 2).trim();
            } else if (tm.getTicket() != null && tm.getTicket().getTitle() != null && tm.getTicket().getTitle().contains("] ")) {
                custName = tm.getTicket().getTitle().substring(tm.getTicket().getTitle().lastIndexOf("] ") + 2).trim();
            }
            m.put("senderName", tm.getSender() != null ? tm.getSender().getFullName() : (Boolean.TRUE.equals(tm.getIsFromCustomer()) ? custName : "Nhân viên CSKH"));
            String timeStr = tm.getSentAt() != null ? tm.getSentAt().format(dtf) : (tm.getCreatedAt() != null ? tm.getCreatedAt().format(dtf) : java.time.LocalDateTime.now().format(dtf));
            m.put("createdAt", timeStr);
            return m;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping({"/ticket-messages", "/support-tickets/{ticketId}/messages"})
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createTicketMessage(
            @PathVariable(required = false) Long ticketId,
            @RequestParam(value = "ticketId", required = false) Long queryTicketId,
            @RequestBody java.util.Map<String, Object> req) {
        Long targetTicketId = ticketId != null ? ticketId : queryTicketId;
        if (targetTicketId == null && req.get("ticketId") != null) {
            try { targetTicketId = Long.valueOf(req.get("ticketId").toString()); } catch (Exception ignored) {}
        }

        String msgText = req.get("message") != null ? req.get("message").toString() : "";
        Boolean isStaff = req.get("isStaff") != null ? Boolean.valueOf(req.get("isStaff").toString()) : true;
        String senderName = req.get("senderName") != null ? req.get("senderName").toString() : (isStaff ? "Nhân viên CSKH" : "Khách hàng Web Online");

        SupportTicket ticket = null;
        if (targetTicketId != null) {
            ticket = supportTicketRepository.findById(targetTicketId).orElse(null);
        }
        if (ticket == null) {
            List<SupportTicket> all = supportTicketRepository.findByIsDeletedFalse();
            if (!all.isEmpty()) {
                ticket = all.get(0);
            } else {
                String code = "ONLINE-" + (System.currentTimeMillis() % 100000);
                SupportTicket newTicket = SupportTicket.builder()
                        .ticketCode(code)
                        .title("[Khách Web Online] " + senderName)
                        .priority("HIGH")
                        .status("OPEN")
                        .build();
                newTicket.setIsDeleted(false);
                ticket = supportTicketRepository.save(newTicket);
            }
        }

        TicketMessage tm = TicketMessage.builder()
                .ticket(ticket)
                .message(msgText)
                .isFromCustomer(!isStaff)
                .sentAt(java.time.LocalDateTime.now())
                .messageType("TEXT")
                .build();
        tm.setIsDeleted(false);
        TicketMessage saved = ticketMessageRepository.save(tm);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStr = saved.getSentAt() != null ? saved.getSentAt().format(dtf) : java.time.LocalDateTime.now().format(dtf);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("ticketId", ticket != null ? ticket.getId().toString() : "1");
        resp.put("message", saved.getMessage());
        resp.put("isStaff", isStaff);
        resp.put("senderName", senderName);
        resp.put("createdAt", timeStr);

        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping("/ticket-messages/{id}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateTicketMessage(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        TicketMessage tm = ticketMessageRepository.findById(id).orElse(null);
        if (tm != null) {
            if (req.get("message") != null) tm.setMessage(req.get("message").toString());
            ticketMessageRepository.save(tm);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.ok(resp));
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
            c = customerRepository.findByIdAndIsDeletedFalse(customerId).orElse(null);
        }
        if (c == null && req.get("customerPhone") != null) {
            String ph = req.get("customerPhone").toString().trim().replace(" ", "");
            if (!ph.isBlank()) {
                c = customerRepository.findByPhoneAndIsDeletedFalse(ph).orElse(null);
            }
        }
        if (c == null && req.get("phone") != null) {
            String ph = req.get("phone").toString().trim().replace(" ", "");
            if (!ph.isBlank()) {
                c = customerRepository.findByPhoneAndIsDeletedFalse(ph).orElse(null);
            }
        }
        if (c == null && req.get("customerName") != null) {
            String cName = req.get("customerName").toString().trim();
            if (!cName.isBlank()) {
                c = customerRepository.findByNameIgnoreCaseAndIsDeletedFalse(cName).orElse(null);
            }
        }
        if (c == null) {
            c = customerRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
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

    @PostMapping("/customer-vouchers/use-by-code")
    public ResponseEntity<ApiResponse<Void>> markUsedByCode(
            @RequestParam String code,
            @RequestParam(required = false) String orderCode) {
        if (code != null && !code.trim().isEmpty()) {
            String trimmed = code.trim();
            java.util.List<CustomerVoucher> list = customerVoucherRepository.findByIsDeletedFalseOrderByUpdatedAtDesc();
            for (CustomerVoucher cv : list) {
                if (cv.getVoucherCode() != null && cv.getVoucherCode().equalsIgnoreCase(trimmed)) {
                    cv.setStatus("USED");
                    cv.setUsedAt(java.time.LocalDateTime.now());
                    if (orderCode != null && !orderCode.isBlank()) {
                        cv.setNote((cv.getNote() != null ? cv.getNote() + " | " : "") + "Sử dụng cho đơn " + orderCode);
                    }
                    customerVoucherRepository.save(cv);
                }
            }
            voucherRepository.findByIsDeletedFalseOrderByUpdatedAtDesc().stream()
                    .filter(v -> v.getVoucherCode() != null && v.getVoucherCode().equalsIgnoreCase(trimmed))
                    .findFirst()
                    .ifPresent(v -> {
                        v.setCurrentUsage((v.getCurrentUsage() != null ? v.getCurrentUsage() : 0) + 1);
                        voucherRepository.save(v);
                    });
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/customer-vouchers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerVoucher(@PathVariable Long id) {
        CustomerVoucher cv = customerVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerVoucher", "id", id));
        if ("ACTIVE".equalsIgnoreCase(cv.getStatus())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Không thể xóa voucher của khách hàng khi chưa sử dụng và chưa bị thu hồi"
            );
        }
        cv.setIsDeleted(true);
        customerVoucherRepository.save(cv);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- WARRANTIES ---
    @GetMapping("/warranties")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllWarranties() {
        List<ProductWarranty> list = productWarrantyRepository.findByIsDeletedFalse();
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L, a.getId() != null ? a.getId() : 0L));

        List<java.util.Map<String, Object>> res = list.stream().map(w -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", w.getId().toString());
            m.put("warrantyCode", w.getWarrantyCode());
            m.put("serialNumber", w.getSerialNumber() != null ? w.getSerialNumber().getSerialNumber() : "");
            m.put("serialOrIMEI", w.getSerialNumber() != null ? w.getSerialNumber().getSerialNumber() : "");
            m.put("productName", (w.getSerialNumber() != null && w.getSerialNumber().getProduct() != null)
                    ? w.getSerialNumber().getProduct().getName() : "Sản phẩm chính hãng");
            if (w.getCustomer() != null) {
                m.put("customerId", w.getCustomer().getId().toString());
                m.put("customerName", w.getCustomer().getName());
                m.put("customerPhone", w.getCustomer().getPhone());
            } else {
                m.put("customerName", "Khách hàng");
                m.put("customerPhone", "");
            }
            m.put("purchaseDate", w.getPurchaseDate() != null ? w.getPurchaseDate().toString() : (w.getStartDate() != null ? w.getStartDate().toString() : ""));
            m.put("startDate", w.getStartDate() != null ? w.getStartDate().toString() : "");
            m.put("expiryDate", w.getEndDate() != null ? w.getEndDate().toString() : "");
            m.put("endDate", w.getEndDate() != null ? w.getEndDate().toString() : "");
            m.put("warrantyMonths", w.getWarrantyPeriod() != null ? w.getWarrantyPeriod() : 12);
            m.put("terms", w.getTerms() != null ? w.getTerms() : "Bảo hành chính hãng");
            m.put("status", w.getStatus() != null ? w.getStatus() : "ACTIVE");
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping("/warranties")
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createWarranty(@RequestBody java.util.Map<String, Object> req) {
        String serialStr = req.get("serialNumber") != null ? req.get("serialNumber").toString().trim() :
                (req.get("serialOrIMEI") != null ? req.get("serialOrIMEI").toString().trim() : "");
        String customerName = req.get("customerName") != null ? req.get("customerName").toString().trim() : "Khách hàng bảo hành";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString().trim() : "";
        String productName = req.get("productName") != null ? req.get("productName").toString().trim() : "Sản phẩm bảo hành";
        String terms = req.get("terms") != null ? req.get("terms").toString() : "Bảo hành chính hãng";
        String status = req.get("status") != null ? req.get("status").toString() : "ACTIVE";
        Integer warrantyMonths = 12;
        if (req.get("warrantyMonths") != null) {
            try { warrantyMonths = Integer.valueOf(req.get("warrantyMonths").toString()); } catch (Exception ignored) {}
        } else if (req.get("warrantyPeriod") != null) {
            try { warrantyMonths = Integer.valueOf(req.get("warrantyPeriod").toString()); } catch (Exception ignored) {}
        }

        // 1. Resolve Customer
        Customer customer = null;
        if (!customerPhone.isBlank()) {
            customer = customerRepository.findByPhoneAndIsDeletedFalse(customerPhone).orElse(null);
            if (customer == null) customer = customerRepository.findByPhone(customerPhone).orElse(null);
        }
        if (customer == null && !customerName.isBlank()) {
            customer = customerRepository.findByNameIgnoreCaseAndIsDeletedFalse(customerName).orElse(null);
        }
        if (customer == null) {
            Customer newC = Customer.builder()
                    .name(customerName)
                    .phone(!customerPhone.isBlank() ? customerPhone : "090" + (System.currentTimeMillis() % 10000000))
                    .email("warranty." + System.currentTimeMillis() + "@store.vn")
                    .customerCode("KH-WRT-" + (System.currentTimeMillis() % 100000))
                    .membershipRank("BRONZE")
                    .points(0.0)
                    .totalSpend(0.0)
                    .isActive(true)
                    .build();
            newC.setIsDeleted(false);
            customer = customerRepository.save(newC);
        }

        // 2. Resolve SerialNumber
        if (serialStr.isBlank()) {
            serialStr = "SN-" + (System.currentTimeMillis() % 10000000);
        }
        String finalSerial = serialStr;
        org.example.storemanager.modules.catalog.entity.SerialNumber serial =
                serialNumberRepository.findBySerialNumberAndIsDeletedFalse(finalSerial).orElse(null);

        if (serial == null) {
            org.example.storemanager.modules.catalog.entity.Product prod =
                    productRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
            if (prod == null) {
                prod = org.example.storemanager.modules.catalog.entity.Product.builder()
                        .name(productName)
                        .productCode("PRD-WRT-" + (System.currentTimeMillis() % 10000))
                        .barcode("BC" + (System.currentTimeMillis() % 100000000))
                        .isActive(true)
                        .build();
                prod.setIsDeleted(false);
                prod = productRepository.save(prod);
            }
            serial = org.example.storemanager.modules.catalog.entity.SerialNumber.builder()
                    .serialNumber(finalSerial)
                    .product(prod)
                    .status("WARRANTY")
                    .build();
            serial.setIsDeleted(false);
            serial = serialNumberRepository.save(serial);
        }

        // 3. Resolve Dates
        java.time.LocalDate startDate = java.time.LocalDate.now();
        if (req.get("purchaseDate") != null && !req.get("purchaseDate").toString().isBlank()) {
            try { startDate = java.time.LocalDate.parse(req.get("purchaseDate").toString().split("T")[0]); } catch (Exception ignored) {}
        } else if (req.get("startDate") != null && !req.get("startDate").toString().isBlank()) {
            try { startDate = java.time.LocalDate.parse(req.get("startDate").toString().split("T")[0]); } catch (Exception ignored) {}
        }
        java.time.LocalDate endDate = startDate.plusMonths(warrantyMonths);
        if (req.get("expiryDate") != null && !req.get("expiryDate").toString().isBlank()) {
            try { endDate = java.time.LocalDate.parse(req.get("expiryDate").toString().split("T")[0]); } catch (Exception ignored) {}
        } else if (req.get("endDate") != null && !req.get("endDate").toString().isBlank()) {
            try { endDate = java.time.LocalDate.parse(req.get("endDate").toString().split("T")[0]); } catch (Exception ignored) {}
        }

        String warrantyCode = req.get("warrantyCode") != null ? req.get("warrantyCode").toString() : "WRT-" + (System.currentTimeMillis() % 1000000);

        ProductWarranty pw = ProductWarranty.builder()
                .warrantyCode(warrantyCode)
                .customer(customer)
                .serialNumber(serial)
                .startDate(startDate)
                .endDate(endDate)
                .purchaseDate(startDate)
                .activatedDate(startDate)
                .warrantyPeriod(warrantyMonths)
                .warrantyType("STANDARD")
                .terms(terms)
                .status(status)
                .build();
        pw.setIsDeleted(false);
        ProductWarranty saved = productWarrantyRepository.save(pw);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("warrantyCode", saved.getWarrantyCode());
        resp.put("serialNumber", finalSerial);
        resp.put("serialOrIMEI", finalSerial);
        resp.put("productName", serial.getProduct() != null ? serial.getProduct().getName() : productName);
        resp.put("customerName", customer.getName());
        resp.put("customerPhone", customer.getPhone());
        resp.put("startDate", saved.getStartDate().toString());
        resp.put("endDate", saved.getEndDate().toString());
        resp.put("purchaseDate", saved.getPurchaseDate() != null ? saved.getPurchaseDate().toString() : saved.getStartDate().toString());
        resp.put("expiryDate", saved.getEndDate().toString());
        resp.put("warrantyMonths", saved.getWarrantyPeriod());
        resp.put("status", saved.getStatus());
        resp.put("terms", saved.getTerms());

        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping("/warranties/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateWarranty(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        ProductWarranty existing = productWarrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductWarranty", "id", id));
        if (req.get("status") != null) existing.setStatus(req.get("status").toString());
        if (req.get("terms") != null) existing.setTerms(req.get("terms").toString());
        if (req.get("warrantyMonths") != null) {
            try { existing.setWarrantyPeriod(Integer.valueOf(req.get("warrantyMonths").toString())); } catch (Exception ignored) {}
        }
        productWarrantyRepository.save(existing);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", existing.getId().toString());
        resp.put("warrantyCode", existing.getWarrantyCode());
        return ResponseEntity.ok(ApiResponse.ok(resp));
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
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getAllWarrantyClaims() {
        List<WarrantyClaim> list = warrantyClaimRepository.findByIsDeletedFalse();
        list.sort((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0L, a.getId() != null ? a.getId() : 0L));

        List<java.util.Map<String, Object>> res = list.stream().map(wc -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", wc.getId().toString());
            m.put("claimCode", wc.getClaimCode() != null ? wc.getClaimCode() : "CLM-" + wc.getId());
            m.put("warrantyId", wc.getWarranty() != null ? wc.getWarranty().getId().toString() : "");
            m.put("warrantyCode", wc.getWarranty() != null ? wc.getWarranty().getWarrantyCode() : "");
            m.put("serialNumber", (wc.getWarranty() != null && wc.getWarranty().getSerialNumber() != null)
                    ? wc.getWarranty().getSerialNumber().getSerialNumber() : "");
            m.put("productName", (wc.getWarranty() != null && wc.getWarranty().getSerialNumber() != null && wc.getWarranty().getSerialNumber().getProduct() != null)
                    ? wc.getWarranty().getSerialNumber().getProduct().getName() : "Thiết bị bảo hành");
            if (wc.getWarranty() != null && wc.getWarranty().getCustomer() != null) {
                m.put("customerName", wc.getWarranty().getCustomer().getName());
                m.put("customerPhone", wc.getWarranty().getCustomer().getPhone());
            } else {
                m.put("customerName", "Khách bảo hành");
                m.put("customerPhone", "");
            }
            m.put("issueDescription", wc.getIssueDescription() != null ? wc.getIssueDescription() : "");
            m.put("description", wc.getIssueDescription() != null ? wc.getIssueDescription() : "");
            m.put("resolution", wc.getResolution() != null ? wc.getResolution() : "PROCESSING");
            m.put("resolutionNotes", wc.getResolution() != null ? wc.getResolution() : "");
            m.put("status", wc.getStatus() != null ? wc.getStatus() : "RECEIVED");
            m.put("conditionOnReceive", wc.getReceivedCondition() != null ? wc.getReceivedCondition() : "");
            m.put("repairCost", wc.getRepairCost() != null ? wc.getRepairCost() : java.math.BigDecimal.ZERO);
            m.put("costAmount", wc.getRepairCost() != null ? wc.getRepairCost() : java.math.BigDecimal.ZERO);
            m.put("receivedDate", wc.getClaimDate() != null ? wc.getClaimDate().toLocalDate().toString() : java.time.LocalDate.now().toString());
            m.put("claimDate", wc.getClaimDate() != null ? wc.getClaimDate().toString() : "");
            m.put("estimatedReturnDate", wc.getExpectedReturnDate() != null ? wc.getExpectedReturnDate().toLocalDate().toString() : "");
            m.put("actualReturnDate", wc.getActualReturnDate() != null ? wc.getActualReturnDate().toLocalDate().toString() : "");
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @PostMapping("/warranty-claims")
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createWarrantyClaim(@RequestBody java.util.Map<String, Object> req) {
        String claimCode = req.get("claimCode") != null ? req.get("claimCode").toString() : "CLM-" + (System.currentTimeMillis() % 1000000);
        String warrantyCode = req.get("warrantyCode") != null ? req.get("warrantyCode").toString().trim() : "";
        String serialNumber = req.get("serialNumber") != null ? req.get("serialNumber").toString().trim() : "";
        String issueDesc = req.get("issueDescription") != null ? req.get("issueDescription").toString() :
                (req.get("description") != null ? req.get("description").toString() : "Yêu cầu bảo hành linh kiện");
        String status = req.get("status") != null ? req.get("status").toString() : "RECEIVED";
        String resolution = req.get("resolution") != null ? req.get("resolution").toString() :
                (req.get("resolutionNotes") != null ? req.get("resolutionNotes").toString() : "Đang xử lý tiếp nhận");
        String condition = req.get("conditionOnReceive") != null ? req.get("conditionOnReceive").toString() : "";
        java.math.BigDecimal repairCost = java.math.BigDecimal.ZERO;
        if (req.get("repairCost") != null) {
            try { repairCost = new java.math.BigDecimal(req.get("repairCost").toString()); } catch (Exception ignored) {}
        }

        // 1. Resolve ProductWarranty
        ProductWarranty warranty = null;
        if (!warrantyCode.isBlank()) {
            warranty = productWarrantyRepository.findByWarrantyCodeAndIsDeletedFalse(warrantyCode).orElse(null);
            if (warranty == null && warrantyCode.matches("\\d+")) {
                warranty = productWarrantyRepository.findByIdAndIsDeletedFalse(Long.parseLong(warrantyCode)).orElse(null);
            }
        }
        if (warranty == null && !serialNumber.isBlank()) {
            warranty = productWarrantyRepository.findBySerialNumber_SerialNumberAndIsDeletedFalse(serialNumber).orElse(null);
        }
        if (warranty == null) {
            List<ProductWarranty> all = productWarrantyRepository.findByIsDeletedFalse();
            if (!all.isEmpty()) {
                warranty = all.get(0);
            } else {
                Customer fallbackCust = customerRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
                if (fallbackCust == null) {
                    fallbackCust = Customer.builder().name("Khách hàng BH").phone("0901234567").customerCode("KH-01").isActive(true).build();
                    fallbackCust.setIsDeleted(false);
                    fallbackCust = customerRepository.save(fallbackCust);
                }
                org.example.storemanager.modules.catalog.entity.Product fallbackProd =
                        productRepository.findByIsDeletedFalse().stream().findFirst().orElse(null);
                if (fallbackProd == null) {
                    fallbackProd = org.example.storemanager.modules.catalog.entity.Product.builder()
                            .name("Sản phẩm chung").productCode("PRD-01").barcode("BC-01").isActive(true).build();
                    fallbackProd.setIsDeleted(false);
                    fallbackProd = productRepository.save(fallbackProd);
                }
                org.example.storemanager.modules.catalog.entity.SerialNumber fallbackSerial =
                        org.example.storemanager.modules.catalog.entity.SerialNumber.builder()
                                .serialNumber(!serialNumber.isBlank() ? serialNumber : "SN-" + System.currentTimeMillis())
                                .product(fallbackProd)
                                .status("WARRANTY")
                                .build();
                fallbackSerial.setIsDeleted(false);
                fallbackSerial = serialNumberRepository.save(fallbackSerial);

                warranty = ProductWarranty.builder()
                        .warrantyCode(!warrantyCode.isBlank() ? warrantyCode : "WRT-" + (System.currentTimeMillis() % 100000))
                        .customer(fallbackCust)
                        .serialNumber(fallbackSerial)
                        .startDate(java.time.LocalDate.now())
                        .endDate(java.time.LocalDate.now().plusYears(1))
                        .purchaseDate(java.time.LocalDate.now())
                        .status("ACTIVE")
                        .terms("Bảo hành tiêu chuẩn")
                        .build();
                warranty.setIsDeleted(false);
                warranty = productWarrantyRepository.save(warranty);
            }
        }

        WarrantyClaim wc = WarrantyClaim.builder()
                .warranty(warranty)
                .claimCode(claimCode)
                .claimDate(java.time.LocalDateTime.now())
                .issueDescription(issueDesc)
                .resolution(resolution)
                .receivedCondition(condition)
                .repairCost(repairCost)
                .status(status)
                .build();
        wc.setIsDeleted(false);
        WarrantyClaim saved = warrantyClaimRepository.save(wc);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", saved.getId().toString());
        resp.put("claimCode", saved.getClaimCode());
        resp.put("warrantyCode", warranty.getWarrantyCode());
        resp.put("serialNumber", warranty.getSerialNumber() != null ? warranty.getSerialNumber().getSerialNumber() : "");
        resp.put("customerName", warranty.getCustomer() != null ? warranty.getCustomer().getName() : "");
        resp.put("customerPhone", warranty.getCustomer() != null ? warranty.getCustomer().getPhone() : "");
        resp.put("productName", (warranty.getSerialNumber() != null && warranty.getSerialNumber().getProduct() != null)
                ? warranty.getSerialNumber().getProduct().getName() : "Thiết bị bảo hành");
        resp.put("issueDescription", saved.getIssueDescription());
        resp.put("status", saved.getStatus());
        resp.put("resolution", saved.getResolution());
        resp.put("repairCost", saved.getRepairCost());
        resp.put("receivedDate", saved.getClaimDate().toLocalDate().toString());

        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping("/warranty-claims/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateWarrantyClaim(@PathVariable Long id, @RequestBody java.util.Map<String, Object> req) {
        WarrantyClaim existing = warrantyClaimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WarrantyClaim", "id", id));
        if (req.get("status") != null) existing.setStatus(req.get("status").toString());
        if (req.get("issueDescription") != null) existing.setIssueDescription(req.get("issueDescription").toString());
        if (req.get("description") != null) existing.setIssueDescription(req.get("description").toString());
        if (req.get("resolution") != null) existing.setResolution(req.get("resolution").toString());
        if (req.get("resolutionNotes") != null) existing.setResolution(req.get("resolutionNotes").toString());
        if (req.get("repairCost") != null) {
            try { existing.setRepairCost(new java.math.BigDecimal(req.get("repairCost").toString())); } catch (Exception ignored) {}
        }
        warrantyClaimRepository.save(existing);

        java.util.Map<String, Object> resp = new java.util.HashMap<>(req);
        resp.put("id", existing.getId().toString());
        resp.put("claimCode", existing.getClaimCode());
        return ResponseEntity.ok(ApiResponse.ok(resp));
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
