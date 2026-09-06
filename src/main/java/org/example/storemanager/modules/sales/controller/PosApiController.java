package org.example.storemanager.modules.sales.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.entity.PosSession;
import org.example.storemanager.modules.system.repository.PosSessionRepository;
import org.example.storemanager.modules.finance.entity.PaymentMethod;
import org.example.storemanager.modules.finance.repository.PaymentMethodRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PosApiController {

    private final PosSessionRepository posSessionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final org.example.storemanager.modules.system.repository.UserRepository userRepository;
    private final org.example.storemanager.modules.system.repository.BranchRepository branchRepository;
    private final org.example.storemanager.modules.sales.repository.ExportInvoiceRepository exportInvoiceRepository;
    private final org.example.storemanager.modules.sales.repository.SaleOrderRepository saleOrderRepository;

    // --- POS SESSIONS ---
    @GetMapping("/sessions")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<org.example.storemanager.modules.sales.dto.response.PosSessionResponse>>> getAllSessions() {
        List<PosSession> sessions = posSessionRepository.findByIsDeletedFalse();
        List<org.example.storemanager.modules.sales.dto.response.PosSessionResponse> responses = sessions.stream()
                .map(this::mapToSessionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/sessions")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<org.example.storemanager.modules.sales.dto.response.PosSessionResponse>> createSession(
            @RequestBody org.example.storemanager.modules.sales.dto.request.CreatePosSessionRequest req) {
        
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
                ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        org.example.storemanager.modules.system.entity.User user = null;
        if (req.getUserId() != null) {
            user = userRepository.findById(req.getUserId()).orElse(null);
        }
        if (user == null && username != null && !"anonymousUser".equals(username)) {
            user = userRepository.findByUsername(username).or(() -> userRepository.findByEmail(username)).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getIsDeleted())).findFirst().orElse(null);
        }

        org.example.storemanager.modules.system.entity.Branch branch = null;
        if (req.getBranchId() != null) {
            branch = branchRepository.findById(req.getBranchId()).orElse(null);
        }
        if (branch == null && user != null && user.getBranch() != null) {
            branch = user.getBranch();
        }
        if (branch == null) {
            branch = branchRepository.findAll().stream().filter(b -> !Boolean.TRUE.equals(b.getIsDeleted())).findFirst().orElse(null);
        }

        String shiftName = req.getShiftName();
        if (shiftName == null || shiftName.isBlank()) {
            int hour = LocalDateTime.now().getHour();
            if (hour < 12) shiftName = "CA_SANG";
            else if (hour < 18) shiftName = "CA_CHIEU";
            else shiftName = "CA_TOI";
        }

        PosSession session = PosSession.builder()
                .sessionCode(req.getSessionCode() != null ? req.getSessionCode() : "POS-SES-" + System.currentTimeMillis())
                .terminalCode(req.getTerminalCode() != null ? req.getTerminalCode() : "POS-001")
                .startTime(LocalDateTime.now())
                .openingCash(req.getOpeningCash() != null ? req.getOpeningCash() : java.math.BigDecimal.ZERO)
                .expectedClosingCash(req.getOpeningCash() != null ? req.getOpeningCash() : java.math.BigDecimal.ZERO)
                .shiftName(shiftName)
                .status("OPEN")
                .user(user)
                .branch(branch)
                .build();
        session.setIsDeleted(false);
        session.setCreatedBy(username != null ? username : "SYSTEM");

        PosSession saved = posSessionRepository.save(session);
        return ResponseEntity.status(201).body(ApiResponse.created(mapToSessionResponse(saved)));
    }

    @PutMapping("/sessions/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<org.example.storemanager.modules.sales.dto.response.PosSessionResponse>> updateSession(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        PosSession existing = posSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));

        if (req.containsKey("terminalCode") && req.get("terminalCode") != null) {
            existing.setTerminalCode(String.valueOf(req.get("terminalCode")));
        }
        if (req.containsKey("sessionCode") && req.get("sessionCode") != null) {
            existing.setSessionCode(String.valueOf(req.get("sessionCode")));
        }
        if (req.containsKey("openingCash") && req.get("openingCash") != null) {
            if ("CLOSED".equalsIgnoreCase(existing.getStatus())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Không thể thay đổi tiền đầu ca của phiên làm việc đã đóng/chốt sổ!");
            }
            existing.setOpeningCash(new java.math.BigDecimal(String.valueOf(req.get("openingCash"))));
        }
        if (req.containsKey("actualClosingCash") && req.get("actualClosingCash") != null) {
            existing.setActualClosingCash(new java.math.BigDecimal(String.valueOf(req.get("actualClosingCash"))));
        } else if (req.containsKey("actualCash") && req.get("actualCash") != null) {
            existing.setActualClosingCash(new java.math.BigDecimal(String.valueOf(req.get("actualCash"))));
        }
        if (req.containsKey("expectedClosingCash") && req.get("expectedClosingCash") != null) {
            existing.setExpectedClosingCash(new java.math.BigDecimal(String.valueOf(req.get("expectedClosingCash"))));
        } else if (req.containsKey("expectedCash") && req.get("expectedCash") != null) {
            existing.setExpectedClosingCash(new java.math.BigDecimal(String.valueOf(req.get("expectedCash"))));
        }
        if (req.containsKey("status") && req.get("status") != null) {
            String newStatus = String.valueOf(req.get("status")).toUpperCase();
            existing.setStatus(newStatus);
            if ("CLOSED".equals(newStatus) && existing.getEndTime() == null) {
                existing.setEndTime(LocalDateTime.now());
            }
        }
        if (req.containsKey("userId") && req.get("userId") != null) {
            try {
                Long uid = Long.parseLong(String.valueOf(req.get("userId")));
                userRepository.findById(uid).ifPresent(existing::setUser);
            } catch (Exception ignored) {}
        }
        if (req.containsKey("branchId") && req.get("branchId") != null) {
            try {
                Long bid = Long.parseLong(String.valueOf(req.get("branchId")));
                branchRepository.findById(bid).ifPresent(existing::setBranch);
            } catch (Exception ignored) {}
        }

        if (req.containsKey("shiftName") && req.get("shiftName") != null) {
            existing.setShiftName(String.valueOf(req.get("shiftName")));
        }

        PosSession saved = posSessionRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật ca làm việc thành công", mapToSessionResponse(saved)));
    }

    @PutMapping("/sessions/{id}/close")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<org.example.storemanager.modules.sales.dto.response.PosSessionResponse>> closeSession(
            @PathVariable Long id,
            @RequestBody(required = false) org.example.storemanager.modules.sales.dto.request.ClosePosSessionRequest req,
            @RequestParam(required = false) java.math.BigDecimal actualClosingCash) {
        PosSession existing = posSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));
        existing.setEndTime(LocalDateTime.now());

        java.math.BigDecimal actualCash = actualClosingCash;
        if (actualCash == null && req != null) {
            actualCash = req.getActualClosingCash();
        }
        if (actualCash != null) {
            existing.setActualClosingCash(actualCash);
        }

        // Tính doanh thu tiền mặt thực tế từ các đơn bán hàng và hóa đơn trong ca (loại trừ chuyển khoản/thẻ/ví)
        java.math.BigDecimal sessionCashSales = java.math.BigDecimal.ZERO;
        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = getAndSyncOrdersForSession(existing);
        if (orders != null) {
            for (org.example.storemanager.modules.sales.entity.SaleOrder order : orders) {
                if (Boolean.TRUE.equals(order.getIsDeleted()) || "CANCELLED".equalsIgnoreCase(order.getStatus())) continue;
                java.math.BigDecimal amt = order.getFinalAmount() != null ? order.getFinalAmount() : (order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
                String pm = order.getPaymentMethodCode();
                if (pm != null && (pm.toLowerCase().contains("tiền mặt") || pm.toLowerCase().contains("tien mat") || pm.toUpperCase().contains("CASH") || pm.toUpperCase().contains("COD"))) {
                    sessionCashSales = sessionCashSales.add(amt);
                }
            }
        }

        List<org.example.storemanager.modules.sales.entity.ExportInvoice> invoices = 
                exportInvoiceRepository.findByPosSessionId(id);
        if (invoices != null && !invoices.isEmpty()) {
            for (org.example.storemanager.modules.sales.entity.ExportInvoice inv : invoices) {
                if (Boolean.TRUE.equals(inv.getIsDeleted()) || "CANCELLED".equalsIgnoreCase(inv.getStatus())) continue;
                String terms = (inv.getPaymentTerms() != null ? inv.getPaymentTerms() : "") + " " + (inv.getNote() != null ? inv.getNote() : "");
                if (terms.toLowerCase().contains("tiền mặt") || terms.toLowerCase().contains("tien mat") || terms.toUpperCase().contains("CASH") || terms.toUpperCase().contains("COD")) {
                    if (inv.getTotalAmount() != null) {
                        sessionCashSales = sessionCashSales.add(inv.getTotalAmount());
                    }
                }
            }
        }
        java.math.BigDecimal opening = existing.getOpeningCash() != null ? existing.getOpeningCash() : java.math.BigDecimal.ZERO;
        existing.setExpectedClosingCash(opening.add(sessionCashSales));

        existing.setStatus("CLOSED");
        PosSession saved = posSessionRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Đóng phiên bán hàng thành công", mapToSessionResponse(saved)));
    }

    @GetMapping("/sessions/{id}/orders")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<List<org.example.storemanager.modules.sales.entity.SaleOrder>>> getSessionOrders(@PathVariable Long id) {
        PosSession session = posSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));
        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = getAndSyncOrdersForSession(session);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long id) {
        PosSession existing = posSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PosSession", "id", id));

        // 1. Tuyệt đối không cho phép xóa ca đã đóng/chốt sổ
        if ("CLOSED".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Không thể xóa ca làm việc đã kết thúc (đã chốt ca)! Dữ liệu phải được lưu giữ để phục vụ kiểm toán và đối soát sổ quỹ."
            );
        }

        // 2. Chặn xóa nếu ca đã phát sinh đơn hàng bán lẻ
        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = getAndSyncOrdersForSession(existing);
        if (orders != null && !orders.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_ERROR,
                    "Không thể xóa ca làm việc đã phát sinh " + orders.size() + " đơn hàng bán lẻ! Chỉ có thể đóng ca để lưu vết kiểm toán."
            );
        }

        // 3. Chặn xóa nếu ca đã phát sinh hóa đơn bán lẻ
        if (exportInvoiceRepository != null) {
            List<org.example.storemanager.modules.sales.entity.ExportInvoice> invoices = 
                    exportInvoiceRepository.findByPosSessionId(id);
            if (invoices != null && !invoices.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_ERROR,
                        "Không thể xóa ca làm việc đã phát sinh hóa đơn bán lẻ! Chỉ có thể đóng ca để lưu vết kiểm toán."
                );
            }
        }

        existing.setIsDeleted(true);
        posSessionRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Hủy ca làm việc mở nhầm thành công", null));
    }

    private List<org.example.storemanager.modules.sales.entity.SaleOrder> getAndSyncOrdersForSession(PosSession session) {
        if (saleOrderRepository == null) return new ArrayList<>();
        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = 
                new ArrayList<>(saleOrderRepository.findByPosSessionIdAndIsDeletedFalse(session.getId()));

        LocalDateTime start = session.getStartTime();
        LocalDateTime end = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();

        // Tự động quét và liên kết các đơn POS mồ côi (chưa có posSessionId) phát sinh trong thời gian ca
        if (start != null) {
            List<org.example.storemanager.modules.sales.entity.SaleOrder> allOrders = saleOrderRepository.findByIsDeletedFalse();
            for (org.example.storemanager.modules.sales.entity.SaleOrder o : allOrders) {
                if (Boolean.TRUE.equals(o.getIsDeleted()) || "CANCELLED".equalsIgnoreCase(o.getStatus())) continue;
                if (o.getPosSessionId() == null && ("POS".equalsIgnoreCase(o.getOrderOrigin()) || (o.getOrderCode() != null && o.getOrderCode().startsWith("ORD-POS-")))) {
                    LocalDateTime od = o.getOrderDate() != null ? o.getOrderDate() : o.getCreatedAt();
                    if (od != null && !od.isBefore(start.minusMinutes(15)) && !od.isAfter(end.plusMinutes(15))) {
                        o.setPosSessionId(session.getId());
                        saleOrderRepository.save(o);
                        if (!orders.contains(o)) {
                            orders.add(o);
                        }
                    }
                }
            }
        }
        return orders;
    }

    private org.example.storemanager.modules.sales.dto.response.PosSessionResponse mapToSessionResponse(PosSession session) {
        int orderCount = 0;
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal cashRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal nonCashRevenue = java.math.BigDecimal.ZERO;

        List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = getAndSyncOrdersForSession(session);
        if (orders != null) {
            for (org.example.storemanager.modules.sales.entity.SaleOrder order : orders) {
                if (Boolean.TRUE.equals(order.getIsDeleted()) || "CANCELLED".equalsIgnoreCase(order.getStatus())) continue;
                orderCount++;
                java.math.BigDecimal amt = order.getFinalAmount() != null ? order.getFinalAmount() : (order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
                totalRevenue = totalRevenue.add(amt);
                String pm = order.getPaymentMethodCode();
                if (pm != null && (pm.toLowerCase().contains("tiền mặt") || pm.toLowerCase().contains("tien mat") || pm.toUpperCase().contains("CASH") || pm.toUpperCase().contains("COD"))) {
                    cashRevenue = cashRevenue.add(amt);
                } else {
                    nonCashRevenue = nonCashRevenue.add(amt);
                }
            }
        }

        List<org.example.storemanager.modules.sales.entity.ExportInvoice> invoices = 
                exportInvoiceRepository.findByPosSessionId(session.getId());
        if (invoices != null && !invoices.isEmpty()) {
            for (org.example.storemanager.modules.sales.entity.ExportInvoice inv : invoices) {
                if (Boolean.TRUE.equals(inv.getIsDeleted()) || "CANCELLED".equalsIgnoreCase(inv.getStatus())) continue;
                orderCount++;
                if (inv.getTotalAmount() != null) {
                    totalRevenue = totalRevenue.add(inv.getTotalAmount());
                    String terms = (inv.getPaymentTerms() != null ? inv.getPaymentTerms() : "") + " " + (inv.getNote() != null ? inv.getNote() : "");
                    if (terms.toLowerCase().contains("tiền mặt") || terms.toLowerCase().contains("tien mat") || terms.toUpperCase().contains("CASH") || terms.toUpperCase().contains("COD")) {
                        cashRevenue = cashRevenue.add(inv.getTotalAmount());
                    } else {
                        nonCashRevenue = nonCashRevenue.add(inv.getTotalAmount());
                    }
                }
            }
        }

        java.math.BigDecimal opening = session.getOpeningCash() != null ? session.getOpeningCash() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal expected = session.getExpectedClosingCash();
        if (expected == null || (expected.compareTo(opening) == 0 && cashRevenue.compareTo(java.math.BigDecimal.ZERO) > 0)) {
            expected = opening.add(cashRevenue);
        }

        return org.example.storemanager.modules.sales.dto.response.PosSessionResponse.builder()
                .id(session.getId())
                .sessionCode(session.getSessionCode())
                .terminalCode(session.getTerminalCode())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .openingCash(session.getOpeningCash())
                .expectedClosingCash(expected)
                .actualClosingCash(session.getActualClosingCash())
                .status(session.getStatus())
                .shiftName(session.getShiftName())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .cashierName(session.getUser() != null ? session.getUser().getFullName() : "Thu ngân")
                .branchId(session.getBranch() != null ? session.getBranch().getId() : null)
                .branchName(session.getBranch() != null ? session.getBranch().getBranchName() : "Chi nhánh")
                .totalOrders(orderCount)
                .totalRevenue(totalRevenue)
                .cashRevenue(cashRevenue)
                .nonCashRevenue(nonCashRevenue)
                .build();
    }

    // --- PAYMENT METHODS ---
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPosPaymentMethods(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        if (branchId != null) {
            return ResponseEntity.ok(ApiResponse.ok(paymentMethodRepository.findActiveByBranchId(branchId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(paymentMethodRepository.findByIsDeletedFalse()));
    }

    // --- MOCKED / PLACEHOLDER ENDPOINTS FOR POS TERMINALS ---
    @GetMapping("/terminals")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPosTerminals() {
        List<Map<String, Object>> mock = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("id", 1L);
        m.put("terminalCode", "POS-001");
        m.put("terminalName", "Máy bán hàng POS 01");
        m.put("isActive", true);
        mock.add(m);
        return ResponseEntity.ok(ApiResponse.ok(mock));
    }

    // --- POS CASHIERS (Nhân viên thu ngân hệ thống) ---
    @GetMapping("/cashiers")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPosCashiers() {
        List<org.example.storemanager.modules.system.entity.User> activeUsers = userRepository.findByIsDeletedFalse();
        java.util.Set<String> customerRoles = java.util.Set.of("CUSTOMER", "KHÁCH HÀNG", "KHACH HANG", "USER", "NGƯỜI DÙNG", "NGUOI DUNG");
        List<Map<String, Object>> cashiers = activeUsers.stream()
                .filter(u -> !"LOCKED".equalsIgnoreCase(u.getStatus()) && !"SUSPENDED".equalsIgnoreCase(u.getStatus()))
                .filter(u -> {
                    if (u.getRole() == null || u.getRole().getRoleName() == null) return true;
                    return !customerRoles.contains(u.getRole().getRoleName().trim().toUpperCase());
                })
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    map.put("fullName", u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
                    map.put("roleName", u.getRole() != null ? u.getRole().getRoleName() : "Nhân viên");
                    map.put("branchId", u.getBranch() != null ? u.getBranch().getId() : null);
                    map.put("branchName", u.getBranch() != null ? u.getBranch().getBranchName() : null);
                    map.put("avatar", u.getAvatar());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(cashiers));
    }
}
