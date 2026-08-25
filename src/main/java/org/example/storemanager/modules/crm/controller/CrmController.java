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
    public ResponseEntity<ApiResponse<List<LoyaltyPointHistory>>> getAllLoyaltyHistory() {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyPointHistoryRepository.findByIsDeletedFalse()));
    }

    @PostMapping({"/loyalty-history", "/loyalty-histories"})
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
        String customerName = req.get("customerName") != null ? req.get("customerName").toString() : "";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString() : "";

        Customer customer = null;
        if (!customerPhone.isBlank()) {
            customer = customerRepository.findByPhone(customerPhone).orElse(null);
        }
        if (customer == null) {
            List<Customer> all = customerRepository.findAll();
            if (!all.isEmpty()) customer = all.get(0);
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
        String ticketCode = req.get("ticketCode") != null ? req.get("ticketCode").toString() : "ONLINE-" + (System.currentTimeMillis() % 100000);
        String customerName = req.get("customerName") != null ? req.get("customerName").toString() : "";
        String defaultTitle = !customerName.isBlank() ? "[Khách Web Online] " + customerName : "Yêu cầu hỗ trợ";
        String subject = req.get("subject") != null ? req.get("subject").toString() : (req.get("title") != null ? req.get("title").toString() : defaultTitle);
        String priority = req.get("priority") != null ? req.get("priority").toString() : "HIGH";
        String status = req.get("status") != null ? req.get("status").toString() : "OPEN";
        String customerPhone = req.get("customerPhone") != null ? req.get("customerPhone").toString() : "";

        Customer customer = null;
        if (!customerPhone.isBlank()) {
            customer = customerRepository.findByPhone(customerPhone).orElse(null);
        }
        if (customer == null && !customerName.isBlank()) {
            customer = customerRepository.findAll().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()) && customerName.equalsIgnoreCase(c.getName()))
                    .findFirst().orElse(null);
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
