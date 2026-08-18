package org.example.storemanager.modules.sales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReturnResponse {
    private Long id;
    private String returnCode;
    private Long returnRequestId;
    private String returnRequestCode;
    private LocalDateTime returnDate;
    private BigDecimal totalRefund;
    private String reason;
    private String status;
    private Long customerId;
    private String customerName;
    private Long invoiceId;
    private String invoiceCode;
    private Long branchId;
    private String branchName;
    private String note;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<CustomerReturnDetailResponse> details;

    public static CustomerReturnResponseBuilder builder() {
        return new CustomerReturnResponseBuilder();
    }

    public static class CustomerReturnResponseBuilder {
        private Long id;
        private String returnCode;
        private Long returnRequestId;
        private String returnRequestCode;
        private LocalDateTime returnDate;
        private BigDecimal totalRefund;
        private String reason;
        private String status;
        private Long customerId;
        private String customerName;
        private Long invoiceId;
        private String invoiceCode;
        private Long branchId;
        private String branchName;
        private String note;
        private LocalDateTime createdAt;
        private String createdBy;
        private List<CustomerReturnDetailResponse> details;

        public CustomerReturnResponseBuilder id(Long id) { this.id = id; return this; }
        public CustomerReturnResponseBuilder returnCode(String returnCode) { this.returnCode = returnCode; return this; }
        public CustomerReturnResponseBuilder returnRequestId(Long returnRequestId) { this.returnRequestId = returnRequestId; return this; }
        public CustomerReturnResponseBuilder returnRequestCode(String returnRequestCode) { this.returnRequestCode = returnRequestCode; return this; }
        public CustomerReturnResponseBuilder returnDate(LocalDateTime returnDate) { this.returnDate = returnDate; return this; }
        public CustomerReturnResponseBuilder totalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; return this; }
        public CustomerReturnResponseBuilder reason(String reason) { this.reason = reason; return this; }
        public CustomerReturnResponseBuilder status(String status) { this.status = status; return this; }
        public CustomerReturnResponseBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public CustomerReturnResponseBuilder customerName(String customerName) { this.customerName = customerName; return this; }
        public CustomerReturnResponseBuilder invoiceId(Long invoiceId) { this.invoiceId = invoiceId; return this; }
        public CustomerReturnResponseBuilder invoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; return this; }
        public CustomerReturnResponseBuilder branchId(Long branchId) { this.branchId = branchId; return this; }
        public CustomerReturnResponseBuilder branchName(String branchName) { this.branchName = branchName; return this; }
        public CustomerReturnResponseBuilder note(String note) { this.note = note; return this; }
        public CustomerReturnResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CustomerReturnResponseBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public CustomerReturnResponseBuilder details(List<CustomerReturnDetailResponse> details) { this.details = details; return this; }

        public CustomerReturnResponse build() {
            return new CustomerReturnResponse(id, returnCode, returnRequestId, returnRequestCode, returnDate, totalRefund, reason, status, customerId, customerName, invoiceId, invoiceCode, branchId, branchName, note, createdAt, createdBy, details);
        }
    }
}
