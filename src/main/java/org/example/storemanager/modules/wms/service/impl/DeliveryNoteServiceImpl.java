package org.example.storemanager.modules.wms.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.DeliveryNoteDTO;
import org.example.storemanager.modules.catalog.entity.Product;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.catalog.entity.SerialNumber;
import org.example.storemanager.modules.inventory.entity.InventoryBalance;
import org.example.storemanager.modules.inventory.entity.InventoryTransaction;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.wms.entity.DeliveryNote;
import org.example.storemanager.modules.wms.entity.PackingList;
import org.example.storemanager.modules.wms.entity.PackingListItem;
import org.example.storemanager.modules.wms.entity.ProductLocation;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.enums.inventory.InventoryTransactionType;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.catalog.repository.SerialNumberRepository;
import org.example.storemanager.modules.inventory.repository.InventoryBalanceRepository;
import org.example.storemanager.modules.inventory.repository.InventoryTransactionRepository;
import org.example.storemanager.modules.wms.repository.DeliveryNoteRepository;
import org.example.storemanager.modules.wms.repository.PackingListItemRepository;
import org.example.storemanager.modules.wms.repository.PackingListRepository;
import org.example.storemanager.modules.wms.repository.ProductLocationRepository;
import org.example.storemanager.modules.wms.service.DeliveryNoteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryNoteServiceImpl implements DeliveryNoteService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final PackingListRepository packingListRepository;
    private final PackingListItemRepository packingListItemRepository;
    private final ProductLocationRepository productLocationRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final SerialNumberRepository serialNumberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryNoteDTO> getAll() {
        return deliveryNoteRepository.findByIsDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryNoteDTO getById(Long id) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));
        return toDTO(dn);
    }

    @Override
    public DeliveryNoteDTO create(DeliveryNoteDTO dto) {
        PackingList pl = null;
        if (dto.getPackingListId() != null) {
            pl = packingListRepository.findByIdAndIsDeletedFalse(dto.getPackingListId()).orElse(null);
        }

        String noteCode = dto.getNoteCode();
        if (noteCode == null || noteCode.trim().isEmpty()) {
            noteCode = "BB-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%04d", deliveryNoteRepository.count() + 1);
        }

        DeliveryNote dn = DeliveryNote.builder()
                .noteCode(noteCode)
                .waybillCode(dto.getWaybillCode())
                .customerName(dto.getCustomerName() != null ? dto.getCustomerName() : dto.getRecipientName())
                .deliveryStaff(dto.getDeliveryStaff())
                .totalWeight(dto.getTotalWeight())
                .packageCount(dto.getPackageCount())
                .productCount(dto.getProductCount())
                .deliveryDate(dto.getDeliveryDate() != null ? dto.getDeliveryDate() : LocalDateTime.now())
                .recipientName(dto.getRecipientName() != null ? dto.getRecipientName() : dto.getCustomerName())
                .status(dto.getStatus() != null ? dto.getStatus() : "CHO_BAN_GIAO")
                .signerName(dto.getSignerName())
                .signedAt(dto.getSignedAt())
                .conditionNotes(dto.getConditionNotes())
                .attachments(dto.getAttachments())
                .rejectionReasonType(dto.getRejectionReasonType())
                .rejectionReasonDetail(dto.getRejectionReasonDetail())
                .carrierName(dto.getCarrierName())
                .trackingNumber(dto.getTrackingNumber())
                .packingList(pl)
                .build();
        dn.setIsDeleted(false);
        dn.setCreatedBy(getCurrentUsername());

        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO update(Long id, DeliveryNoteDTO dto) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (dto.getNoteCode() != null) dn.setNoteCode(dto.getNoteCode());
        if (dto.getWaybillCode() != null) dn.setWaybillCode(dto.getWaybillCode());
        if (dto.getCustomerName() != null) dn.setCustomerName(dto.getCustomerName());
        if (dto.getRecipientName() != null) dn.setRecipientName(dto.getRecipientName());
        if (dto.getDeliveryStaff() != null) dn.setDeliveryStaff(dto.getDeliveryStaff());
        if (dto.getTotalWeight() != null) dn.setTotalWeight(dto.getTotalWeight());
        if (dto.getPackageCount() != null) dn.setPackageCount(dto.getPackageCount());
        if (dto.getProductCount() != null) dn.setProductCount(dto.getProductCount());
        if (dto.getStatus() != null) dn.setStatus(dto.getStatus());
        if (dto.getSignerName() != null) dn.setSignerName(dto.getSignerName());
        if (dto.getSignedAt() != null) dn.setSignedAt(dto.getSignedAt());
        if (dto.getConditionNotes() != null) dn.setConditionNotes(dto.getConditionNotes());
        if (dto.getAttachments() != null) dn.setAttachments(dto.getAttachments());
        if (dto.getRejectionReasonType() != null) dn.setRejectionReasonType(dto.getRejectionReasonType());
        if (dto.getRejectionReasonDetail() != null) dn.setRejectionReasonDetail(dto.getRejectionReasonDetail());
        if (dto.getCarrierName() != null) dn.setCarrierName(dto.getCarrierName());
        if (dto.getTrackingNumber() != null) dn.setTrackingNumber(dto.getTrackingNumber());

        dn.setUpdatedBy(getCurrentUsername());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public void delete(Long id) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (!"DRAFT".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể xóa phiếu ở trạng thái DRAFT");
        }

        dn.setIsDeleted(true);
        dn.setDeletedAt(LocalDateTime.now());
        dn.setDeletedBy(getCurrentUsername());
        deliveryNoteRepository.save(dn);
    }

    @Override
    public DeliveryNoteDTO assignCarrier(Long id, String carrierName, String trackingNumber) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));
        dn.setCarrierName(carrierName);
        dn.setTrackingNumber(trackingNumber);
        dn.setUpdatedBy(getCurrentUsername());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO dispatch(Long id) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (!"DRAFT".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể xuất đi từ trạng thái DRAFT");
        }

        PackingList pl = dn.getPackingList();
        if (!"PACKED".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể xuất đi khi kiện hàng đã được đóng gói xong (PACKED)");
        }

        Branch branch = pl.getOrder() != null ? pl.getOrder().getBranch() : null;
        if (branch == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Kiện hàng không liên kết với chi nhánh/kho cụ thể");
        }

        List<PackingListItem> items = packingListItemRepository.findByPackingListIdAndIsDeletedFalse(pl.getId());
        String username = getCurrentUsername();

        for (PackingListItem item : items) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();
            BigDecimal quantity = item.getQuantity();

            // 1. Subtract ProductLocation (bin-level quantity)
            List<ProductLocation> locations = productLocationRepository.findByProductIdAndIsDeletedFalse(product.getId());
            BigDecimal remainingToDeduct = quantity;
            for (ProductLocation loc : locations) {
                if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal available = loc.getQuantity();
                if (available.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal deduct = available.min(remainingToDeduct);
                loc.setQuantity(available.subtract(deduct));
                productLocationRepository.save(loc);
                remainingToDeduct = remainingToDeduct.subtract(deduct);
            }

            if (remainingToDeduct.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Số lượng mặt hàng trong các ô kệ không đủ để xuất");
            }

            // 2. Subtract InventoryBalance
            InventoryBalance balance = inventoryBalanceRepository.findByProductVariantIdAndBranchId(variant.getId(), branch.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Không tìm thấy số dư tồn kho của sản phẩm tại chi nhánh này"));

            BigDecimal beforeQty = balance.getAvailableQuantity();
            if (beforeQty.compareTo(quantity) < 0) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Số lượng tồn kho khả dụng không đủ");
            }

            BigDecimal afterQty = beforeQty.subtract(quantity);
            balance.setAvailableQuantity(afterQty);
            balance.setLastUpdated(LocalDateTime.now());
            inventoryBalanceRepository.save(balance);

            // 3. Create InventoryTransaction = SALE
            String txCode = "TX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%06d", inventoryTransactionRepository.count() + 1);

            InventoryTransaction tx = InventoryTransaction.builder()
                    .transactionCode(txCode)
                    .productVariant(variant)
                    .sourceBranch(branch)
                    .transactionType(InventoryTransactionType.SALE)
                    .quantity(quantity)
                    .beforeQuantity(beforeQty)
                    .afterQuantity(afterQty)
                    .build();
            tx.setIsDeleted(false);
            tx.setCreatedBy(username);
            inventoryTransactionRepository.save(tx);

            // 4. Update Serial Number status to SOLD
            List<SerialNumber> serials = serialNumberRepository.findByProductIdAndStatusAndIsDeletedFalse(product.getId(), "AVAILABLE");
            int limit = quantity.intValue();
            for (int i = 0; i < Math.min(limit, serials.size()); i++) {
                SerialNumber sn = serials.get(i);
                sn.setStatus("SOLD");
                serialNumberRepository.save(sn);
            }
        }

        dn.setStatus("DISPATCHED");
        dn.setDispatchedBy(username);
        dn.setDispatchedAt(LocalDateTime.now());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO inTransit(Long id) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (!"DISPATCHED".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể chuyển sang trạng thái đang vận chuyển từ DISPATCHED");
        }

        dn.setStatus("IN_TRANSIT");
        dn.setInTransitBy(getCurrentUsername());
        dn.setInTransitAt(LocalDateTime.now());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO deliver(Long id, String recipientName) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (!"IN_TRANSIT".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể chuyển sang trạng thái đã giao từ IN_TRANSIT");
        }

        dn.setStatus("DELIVERED");
        dn.setRecipientName(recipientName);
        dn.setDeliveredBy(getCurrentUsername());
        dn.setDeliveredAt(LocalDateTime.now());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO failed(Long id, String failureReason) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if (!"IN_TRANSIT".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể chuyển sang trạng thái giao lỗi từ IN_TRANSIT");
        }

        dn.setStatus("FAILED");
        dn.setFailureReason(failureReason);
        dn.setFailedBy(getCurrentUsername());
        dn.setFailedAt(LocalDateTime.now());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    @Override
    public DeliveryNoteDTO cancel(Long id, String cancelReason) {
        DeliveryNote dn = deliveryNoteRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryNote", "id", id));

        if ("DELIVERED".equals(dn.getStatus()) || "CANCELLED".equals(dn.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Không thể hủy đơn khi đã hoàn tất giao hàng hoặc đã hủy");
        }

        dn.setStatus("CANCELLED");
        dn.setCancelReason(cancelReason);
        dn.setCancelledBy(getCurrentUsername());
        dn.setCancelledAt(LocalDateTime.now());
        return toDTO(deliveryNoteRepository.save(dn));
    }

    private DeliveryNoteDTO toDTO(DeliveryNote dn) {
        return DeliveryNoteDTO.builder()
                .id(dn.getId())
                .noteCode(dn.getNoteCode())
                .deliveryDate(dn.getDeliveryDate())
                .recipientName(dn.getRecipientName())
                .status(dn.getStatus())
                .packingListId(dn.getPackingList() != null ? dn.getPackingList().getId() : null)
                .packingListCode(dn.getPackingList() != null ? dn.getPackingList().getPackCode() : null)
                .waybillCode(dn.getWaybillCode())
                .customerName(dn.getCustomerName())
                .deliveryStaff(dn.getDeliveryStaff())
                .totalWeight(dn.getTotalWeight())
                .packageCount(dn.getPackageCount())
                .productCount(dn.getProductCount())
                .signerName(dn.getSignerName())
                .signedAt(dn.getSignedAt())
                .conditionNotes(dn.getConditionNotes())
                .attachments(dn.getAttachments())
                .rejectionReasonType(dn.getRejectionReasonType())
                .rejectionReasonDetail(dn.getRejectionReasonDetail())
                .carrierName(dn.getCarrierName())
                .trackingNumber(dn.getTrackingNumber())
                .dispatchedBy(dn.getDispatchedBy())
                .dispatchedAt(dn.getDispatchedAt())
                .inTransitBy(dn.getInTransitBy())
                .inTransitAt(dn.getInTransitAt())
                .deliveredBy(dn.getDeliveredBy())
                .deliveredAt(dn.getDeliveredAt())
                .failedBy(dn.getFailedBy())
                .failedAt(dn.getFailedAt())
                .failureReason(dn.getFailureReason())
                .cancelledBy(dn.getCancelledBy())
                .cancelledAt(dn.getCancelledAt())
                .cancelReason(dn.getCancelReason())
                .build();
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "system";
    }
}
