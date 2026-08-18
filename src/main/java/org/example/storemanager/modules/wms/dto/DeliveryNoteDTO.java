package org.example.storemanager.modules.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteDTO {
    private Long id;
    private String noteCode;
    private LocalDateTime deliveryDate;
    private String recipientName;
    private String status;
    private Long packingListId;
    private String packingListCode;

    private String waybillCode;
    private String customerName;
    private String deliveryStaff;
    private Double totalWeight;
    private Integer packageCount;
    private Integer productCount;
    private String signerName;
    private String signedAt;
    private String conditionNotes;
    private String attachments;
    private String rejectionReasonType;
    private String rejectionReasonDetail;

    private String carrierName;
    private String trackingNumber;

    private String dispatchedBy;
    private LocalDateTime dispatchedAt;
    private String inTransitBy;
    private LocalDateTime inTransitAt;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private String failedBy;
    private LocalDateTime failedAt;
    private String failureReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancelReason;
}
