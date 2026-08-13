package org.example.storemanager.modules.crm.service;

import org.example.storemanager.modules.crm.dto.LoyaltyCalculateRequest;
import org.example.storemanager.modules.crm.dto.LoyaltyCalculateResponse;
import org.example.storemanager.modules.crm.dto.LoyaltyTransactionResponse;
import org.example.storemanager.modules.crm.entity.LoyaltyPointHistory;
import org.example.storemanager.modules.sales.entity.SaleOrder;

import java.math.BigDecimal;
import java.util.List;

public interface LoyaltyService {
    LoyaltyCalculateResponse calculateExpectedPoints(LoyaltyCalculateRequest request);
    LoyaltyPointHistory processOrderLoyaltyEarn(Long customerId, String orderCode, BigDecimal netPaidAmount, SaleOrder order);
    LoyaltyPointHistory processOrderRedeem(Long customerId, String orderCode, int pointsToRedeem, SaleOrder order);
    LoyaltyPointHistory processOrderRefund(Long customerId, String returnCode, String originalOrderCode, BigDecimal refundAmount, BigDecimal originalOrderAmount);
    List<LoyaltyTransactionResponse> getCustomerLoyaltyHistory(Long customerId);
}
