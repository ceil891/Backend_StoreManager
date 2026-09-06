package org.example.storemanager.modules.crm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.crm.dto.LoyaltyCalculateRequest;
import org.example.storemanager.modules.crm.dto.LoyaltyCalculateResponse;
import org.example.storemanager.modules.crm.dto.LoyaltyTransactionResponse;
import org.example.storemanager.modules.crm.entity.LoyaltyPointHistory;
import org.example.storemanager.modules.crm.entity.LoyaltyTier;
import org.example.storemanager.modules.crm.repository.LoyaltyPointHistoryRepository;
import org.example.storemanager.modules.crm.repository.LoyaltyTierRepository;
import org.example.storemanager.modules.crm.service.LoyaltyService;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    private final CustomerRepository customerRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final LoyaltyPointHistoryRepository loyaltyPointHistoryRepository;

    private static final BigDecimal DEFAULT_AMOUNT_PER_POINT = new BigDecimal("1000");

    @Override
    @Transactional(readOnly = true)
    public LoyaltyCalculateResponse calculateExpectedPoints(LoyaltyCalculateRequest request) {
        if (request.getCustomerId() == null) {
            return LoyaltyCalculateResponse.builder()
                    .customerId(null)
                    .customerName("Khách vãng lai")
                    .netPaidAmount(request.getNetPaidAmount())
                    .amountPerPoint(DEFAULT_AMOUNT_PER_POINT)
                    .tierCode("GUEST")
                    .tierName("Khách vãng lai")
                    .tierMultiplier(BigDecimal.ONE)
                    .expectedPointsEarned(0)
                    .currentPoints(0)
                    .expectedBalanceAfter(0)
                    .build();
        }

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại: " + request.getCustomerId()));

        LoyaltyTier tier = resolveCustomerTier(customer);
        BigDecimal multiplier = tier != null && tier.getPointMultiplier() != null ? tier.getPointMultiplier() : BigDecimal.ONE;

        int currentPoints = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        int pointsEarned = calculateEarnedPoints(request.getNetPaidAmount(), DEFAULT_AMOUNT_PER_POINT, multiplier);

        return LoyaltyCalculateResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .netPaidAmount(request.getNetPaidAmount())
                .amountPerPoint(DEFAULT_AMOUNT_PER_POINT)
                .tierCode(tier != null ? tier.getTierCode() : (customer.getMembershipRank() != null ? customer.getMembershipRank() : "BRONZE"))
                .tierName(tier != null ? tier.getTierName() : (customer.getMembershipRank() != null ? customer.getMembershipRank() : "Thành viên Đồng"))
                .tierMultiplier(multiplier)
                .expectedPointsEarned(pointsEarned)
                .currentPoints(currentPoints)
                .expectedBalanceAfter(currentPoints + pointsEarned)
                .build();
    }

    @Override
    @Transactional
    public LoyaltyPointHistory processOrderLoyaltyEarn(Long customerId, String orderCode, BigDecimal netPaidAmount, SaleOrder order) {
        if (customerId == null) return null; // Khách vãng lai -> Không tích điểm

        // 1. Chống duplicate cho luồng EARN
        if (loyaltyPointHistoryRepository.existsByRefCodeAndTransactionTypeAndIsDeletedFalse(orderCode, "EARN")) {
            log.warn("[Loyalty] Duplicate EARN request for orderCode={}. Returning existing transaction.", orderCode);
            return loyaltyPointHistoryRepository.findByRefCodeAndTransactionTypeAndIsDeletedFalse(orderCode, "EARN").orElse(null);
        }

        // 2. Load Customer
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId).orElse(null);
        if (customer == null) return null;

        // 3. Lấy hệ số Tier
        LoyaltyTier tier = resolveCustomerTier(customer);
        BigDecimal multiplier = tier != null && tier.getPointMultiplier() != null ? tier.getPointMultiplier() : BigDecimal.ONE;

        // 4. Tính điểm nhận
        int pointsEarned = calculateEarnedPoints(netPaidAmount, DEFAULT_AMOUNT_PER_POINT, multiplier);

        // 5. Cập nhật số dư điểm & tự động nâng hạng
        int balanceBefore = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        int balanceAfter = balanceBefore + pointsEarned;

        customer.setPoints((double) balanceAfter);
        double currentSpend = customer.getTotalSpend() != null ? customer.getTotalSpend() : 0.0;
        double addedSpend = netPaidAmount != null ? netPaidAmount.doubleValue() : 0.0;
        double newTotalSpend = currentSpend + addedSpend;
        customer.setTotalSpend(newTotalSpend);

        // Auto Upgrade Tier nếu đạt mốc mới
        String newRank = resolveMembershipRank(balanceAfter, newTotalSpend);
        if (newRank != null && !newRank.isBlank()) {
            customer.setMembershipRank(newRank);
        }

        customerRepository.save(customer);

        if (pointsEarned <= 0) return null;

        // 6. Ghi Sổ cái Lịch sử
        LoyaltyPointHistory history = LoyaltyPointHistory.builder()
                .customer(customer)
                .order(order)
                .pointsChange(pointsEarned)
                .currentPoints(balanceAfter)
                .transactionType("EARN")
                .refCode(orderCode)
                .description("Tích điểm đơn hàng " + orderCode + " (+" + pointsEarned + " pt)")
                .build();
        history.setIsDeleted(false);

        log.info("[Loyalty] Earned {} points for orderCode={}. Customer new balance={}, newRank={}", pointsEarned, orderCode, balanceAfter, newRank);
        return loyaltyPointHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public LoyaltyPointHistory processOrderRedeem(Long customerId, String orderCode, int pointsToRedeem, SaleOrder order) {
        if (customerId == null || pointsToRedeem <= 0) return null;

        // 1. Chống duplicate cho luồng REDEEM
        if (loyaltyPointHistoryRepository.existsByRefCodeAndTransactionTypeAndIsDeletedFalse(orderCode, "REDEEM")) {
            log.warn("[Loyalty] Duplicate REDEEM request for orderCode={}. Returning existing transaction.", orderCode);
            return loyaltyPointHistoryRepository.findByRefCodeAndTransactionTypeAndIsDeletedFalse(orderCode, "REDEEM").orElse(null);
        }

        // 2. Load Customer
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId).orElse(null);
        if (customer == null) return null;

        int currentPoints = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        if (currentPoints <= 0) {
            log.warn("[Loyalty] Khách hàng {} không có điểm để tiêu.", customerId);
            return null;
        }

        int actualRedeem = Math.min(currentPoints, pointsToRedeem);

        // 3. Trừ điểm & ghi sổ cái
        int balanceAfter = Math.max(0, currentPoints - actualRedeem);
        customer.setPoints((double) balanceAfter);
        customerRepository.save(customer);

        LoyaltyPointHistory history = LoyaltyPointHistory.builder()
                .customer(customer)
                .order(order)
                .pointsChange(-actualRedeem)
                .currentPoints(balanceAfter)
                .transactionType("REDEEM")
                .refCode(orderCode)
                .description("Sử dụng điểm cho đơn hàng " + orderCode + " (-" + actualRedeem + " pt)")
                .build();
        history.setIsDeleted(false);

        log.info("[Loyalty] Redeemed {} points for orderCode={}. Customer new balance={}", actualRedeem, orderCode, balanceAfter);
        return loyaltyPointHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public LoyaltyPointHistory processOrderRefund(Long customerId, String returnCode, String originalOrderCode, BigDecimal refundAmount, BigDecimal originalOrderAmount) {
        if (customerId == null) return null;

        // 1. Chống duplicate cho luồng REFUND
        if (loyaltyPointHistoryRepository.existsByRefCodeAndTransactionTypeAndIsDeletedFalse(returnCode, "REFUND")) {
            log.warn("[Loyalty] Duplicate REFUND request for returnCode={}. Returning existing transaction.", returnCode);
            return loyaltyPointHistoryRepository.findByRefCodeAndTransactionTypeAndIsDeletedFalse(returnCode, "REFUND").orElse(null);
        }

        // 2. Tìm EARN transaction của đơn hàng gốc
        LoyaltyPointHistory originalEarnTx = loyaltyPointHistoryRepository
                .findByRefCodeAndTransactionTypeAndIsDeletedFalse(originalOrderCode, "EARN")
                .orElse(null);

        if (originalEarnTx == null || originalEarnTx.getPointsChange() <= 0) return null;

        // 3. Tính points cần thu hồi = FLOOR(Original Points * (Refund Amount / Original Order Amount))
        BigDecimal ratio = refundAmount.divide(originalOrderAmount, 4, RoundingMode.FLOOR);
        int pointsToRefund = BigDecimal.valueOf(originalEarnTx.getPointsChange()).multiply(ratio).setScale(0, RoundingMode.FLOOR).intValue();

        if (pointsToRefund <= 0) return null;

        // 4. Load Customer & trừ điểm
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(customerId).orElse(null);
        if (customer == null) return null;

        int balanceBefore = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        int balanceAfter = balanceBefore - pointsToRefund;

        customer.setPoints((double) balanceAfter);
        customerRepository.save(customer);

        LoyaltyPointHistory history = LoyaltyPointHistory.builder()
                .customer(customer)
                .pointsChange(-pointsToRefund)
                .currentPoints(balanceAfter)
                .transactionType("REFUND")
                .refCode(returnCode)
                .description("Thu hồi điểm do hoàn đơn " + originalOrderCode + " (-" + pointsToRefund + " pt)")
                .build();
        history.setIsDeleted(false);

        log.info("[Loyalty] Refunded {} points for returnCode={}. Customer new balance={}", pointsToRefund, returnCode, balanceAfter);
        return loyaltyPointHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getCustomerLoyaltyHistory(Long customerId) {
        return loyaltyPointHistoryRepository.findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(customerId)
                .stream()
                .map(h -> LoyaltyTransactionResponse.builder()
                        .id(h.getId())
                        .refCode(h.getRefCode())
                        .transactionType(h.getTransactionType())
                        .pointsChange(h.getPointsChange())
                        .currentPoints(h.getCurrentPoints())
                        .description(h.getDescription())
                        .createdAt(h.getCreatedAt() != null ? h.getCreatedAt() : LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    private int calculateEarnedPoints(BigDecimal netPaidAmount, BigDecimal amountPerPoint, BigDecimal tierMultiplier) {
        if (netPaidAmount == null || netPaidAmount.compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (amountPerPoint == null || amountPerPoint.compareTo(BigDecimal.ZERO) <= 0) return 0;

        BigDecimal basePoints = netPaidAmount.divide(amountPerPoint, 4, RoundingMode.FLOOR);
        BigDecimal multiplier = tierMultiplier != null ? tierMultiplier : BigDecimal.ONE;
        return basePoints.multiply(multiplier).setScale(0, RoundingMode.FLOOR).intValue();
    }

    public String resolveMembershipRank(double points, double totalSpend) {
        List<LoyaltyTier> tiers = loyaltyTierRepository.findByIsDeletedFalse();
        if (!tiers.isEmpty()) {
            LoyaltyTier matched = tiers.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                    .filter(t -> (t.getMinPoints() != null && points >= t.getMinPoints()) ||
                            (t.getMinSpend() != null && BigDecimal.valueOf(totalSpend).compareTo(t.getMinSpend()) >= 0))
                    .max(Comparator.comparingInt(t -> t.getMinPoints() != null ? t.getMinPoints() : 0))
                    .orElse(null);
            if (matched != null) {
                return matched.getTierName();
            }
        }
        // Chuẩn fallback thống nhất toàn hệ thống
        if (points >= 6000 || totalSpend >= 50000000.0) return "DIAMOND";
        if (points >= 3000 || totalSpend >= 25000000.0) return "ELITE_CLUB";
        if (points >= 1500 || totalSpend >= 10000000.0) return "GOLD";
        if (points >= 500 || totalSpend >= 3000000.0) return "SILVER";
        return "BRONZE";
    }

    private LoyaltyTier resolveCustomerTier(Customer customer) {
        List<LoyaltyTier> tiers = loyaltyTierRepository.findByIsDeletedFalse();
        if (tiers.isEmpty()) return null;

        if (customer.getMembershipRank() != null) {
            for (LoyaltyTier t : tiers) {
                if (customer.getMembershipRank().equalsIgnoreCase(t.getTierName()) || customer.getMembershipRank().equalsIgnoreCase(t.getTierCode())) {
                    return t;
                }
            }
        }

        int currentPoints = customer.getPoints() != null ? customer.getPoints().intValue() : 0;
        return resolveTierByPoints(currentPoints);
    }

    private LoyaltyTier resolveTierByPoints(int points) {
        List<LoyaltyTier> tiers = loyaltyTierRepository.findByIsDeletedFalse();
        return tiers.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                .filter(t -> t.getMinPoints() != null && points >= t.getMinPoints())
                .max(Comparator.comparingInt(LoyaltyTier::getMinPoints))
                .orElse(null);
    }
}
