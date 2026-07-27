package org.example.storemanager.modules.wms.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.PackingListDTO;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.sales.entity.SaleOrder;
import org.example.storemanager.modules.wms.entity.PackingList;
import org.example.storemanager.modules.wms.entity.PackingListItem;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.example.storemanager.modules.wms.repository.PackingListItemRepository;
import org.example.storemanager.modules.wms.repository.PackingListRepository;
import org.example.storemanager.modules.wms.service.PackingListService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PackingListServiceImpl implements PackingListService {

    private final PackingListRepository packingListRepository;
    private final PackingListItemRepository packingListItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SaleOrderRepository saleOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PackingListDTO> getAll() {
        return packingListRepository.findByIsDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PackingListDTO getById(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));
        return toDTO(pl);
    }

    @Override
    public PackingListDTO create(PackingListDTO dto) {
        SaleOrder order = dto.getOrderId() != null
                ? saleOrderRepository.findByIdAndIsDeletedFalse(dto.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("SaleOrder", "id", dto.getOrderId()))
                : null;

        String packCode = "PK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", packingListRepository.count() + 1);

        PackingList pl = PackingList.builder()
                .packCode(packCode)
                .packDate(LocalDateTime.now())
                .weight(dto.getWeight())
                .dimensions(dto.getDimensions())
                .status("DRAFT")
                .order(order)
                .build();
        pl.setIsDeleted(false);
        pl.setCreatedBy(getCurrentUsername());

        PackingList saved = packingListRepository.save(pl);

        List<PackingListDTO.Item> savedItems = new ArrayList<>();
        if (dto.getItems() != null) {
            for (PackingListDTO.Item itemDto : dto.getItems()) {
                ProductVariant variant = productVariantRepository.findById(itemDto.getProductVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemDto.getProductVariantId()));
                
                PackingListItem item = PackingListItem.builder()
                        .packingList(saved)
                        .productVariant(variant)
                        .quantity(itemDto.getQuantity())
                        .pickedQuantity(BigDecimal.ZERO)
                        .packedQuantity(BigDecimal.ZERO)
                        .build();
                item.setIsDeleted(false);
                item.setCreatedBy(getCurrentUsername());

                PackingListItem savedItem = packingListItemRepository.save(item);
                savedItems.add(toItemDTO(savedItem));
            }
        }

        PackingListDTO result = toDTO(saved);
        result.setItems(savedItems);
        return result;
    }

    @Override
    public PackingListDTO update(Long id, PackingListDTO dto) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể sửa đổi phiếu ở trạng thái DRAFT");
        }

        if (dto.getWeight() != null) {
            pl.setWeight(dto.getWeight());
        }
        if (dto.getDimensions() != null) {
            pl.setDimensions(dto.getDimensions());
        }
        pl.setUpdatedBy(getCurrentUsername());
        return toDTO(packingListRepository.save(pl));
    }

    @Override
    public void delete(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể xóa phiếu ở trạng thái DRAFT");
        }

        pl.setIsDeleted(true);
        pl.setDeletedAt(LocalDateTime.now());
        pl.setDeletedBy(getCurrentUsername());
        packingListRepository.save(pl);

        List<PackingListItem> items = packingListItemRepository.findByPackingListIdAndIsDeletedFalse(id);
        for (PackingListItem item : items) {
            item.setIsDeleted(true);
            item.setDeletedAt(LocalDateTime.now());
            item.setDeletedBy(getCurrentUsername());
            packingListItemRepository.save(item);
        }
    }

    @Override
    public PackingListDTO.Item addItem(Long packingListId, PackingListDTO.Item itemDto) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(packingListId)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", packingListId));

        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể thêm mặt hàng ở trạng thái DRAFT");
        }

        ProductVariant variant = productVariantRepository.findById(itemDto.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", itemDto.getProductVariantId()));

        PackingListItem item = PackingListItem.builder()
                .packingList(pl)
                .productVariant(variant)
                .quantity(itemDto.getQuantity())
                .pickedQuantity(BigDecimal.ZERO)
                .packedQuantity(BigDecimal.ZERO)
                .build();
        item.setIsDeleted(false);
        item.setCreatedBy(getCurrentUsername());

        PackingListItem saved = packingListItemRepository.save(item);
        return toItemDTO(saved);
    }

    @Override
    public PackingListDTO.Item updateItem(Long id, PackingListDTO.Item itemDto) {
        PackingListItem item = packingListItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingListItem", "id", id));

        PackingList pl = item.getPackingList();
        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể sửa mặt hàng ở trạng thái DRAFT");
        }

        if (itemDto.getQuantity() != null) {
            item.setQuantity(itemDto.getQuantity());
        }
        item.setUpdatedBy(getCurrentUsername());
        PackingListItem saved = packingListItemRepository.save(item);
        return toItemDTO(saved);
    }

    @Override
    public void deleteItem(Long id) {
        PackingListItem item = packingListItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingListItem", "id", id));

        PackingList pl = item.getPackingList();
        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể xóa mặt hàng ở trạng thái DRAFT");
        }

        item.setIsDeleted(true);
        item.setDeletedAt(LocalDateTime.now());
        item.setDeletedBy(getCurrentUsername());
        packingListItemRepository.save(item);
    }

    @Override
    public PackingListDTO startPicking(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"DRAFT".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể bắt đầu nhặt hàng từ trạng thái DRAFT");
        }

        pl.setStatus("PICKING");
        pl.setPickingStartedBy(getCurrentUsername());
        pl.setPickingStartedAt(LocalDateTime.now());
        return toDTO(packingListRepository.save(pl));
    }

    @Override
    public PackingListDTO pick(Long id, List<PackingListDTO.Item> items) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"PICKING".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể xác nhận nhặt hàng ở trạng thái PICKING");
        }

        for (PackingListDTO.Item inputItem : items) {
            PackingListItem item = packingListItemRepository.findByIdAndIsDeletedFalse(inputItem.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("PackingListItem", "id", inputItem.getId()));
            if (inputItem.getPickedQuantity() != null) {
                item.setPickedQuantity(inputItem.getPickedQuantity());
                packingListItemRepository.save(item);
            }
        }

        pl.setStatus("PICKED");
        pl.setPickedBy(getCurrentUsername());
        pl.setPickedAt(LocalDateTime.now());
        return toDTO(packingListRepository.save(pl));
    }

    @Override
    public PackingListDTO startPacking(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"PICKED".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể bắt đầu đóng gói từ trạng thái PICKED");
        }

        pl.setStatus("PACKING");
        pl.setPackingStartedBy(getCurrentUsername());
        pl.setPackingStartedAt(LocalDateTime.now());
        return toDTO(packingListRepository.save(pl));
    }

    @Override
    public PackingListDTO completePacking(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if (!"PACKING".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể đóng gói xong ở trạng thái PACKING");
        }

        List<PackingListItem> items = packingListItemRepository.findByPackingListIdAndIsDeletedFalse(id);
        for (PackingListItem item : items) {
            if (item.getPackedQuantity() == null || item.getPackedQuantity().compareTo(BigDecimal.ZERO) == 0) {
                item.setPackedQuantity(item.getQuantity()); // fallback to complete quantity
                packingListItemRepository.save(item);
            }
        }

        pl.setStatus("PACKED");
        pl.setPackedBy(getCurrentUsername());
        pl.setPackedAt(LocalDateTime.now());
        return toDTO(packingListRepository.save(pl));
    }

    @Override
    public PackingListDTO cancelPacking(Long id) {
        PackingList pl = packingListRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackingList", "id", id));

        if ("PACKED".equals(pl.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Không thể hủy khi kiện hàng đã đóng gói xong (PACKED)");
        }

        pl.setStatus("CANCELLED");
        pl.setDeletedBy(getCurrentUsername());
        pl.setDeletedAt(LocalDateTime.now());
        return toDTO(packingListRepository.save(pl));
    }

    private PackingListDTO toDTO(PackingList pl) {
        List<PackingListDTO.Item> items = packingListItemRepository.findByPackingListIdAndIsDeletedFalse(pl.getId()).stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        return PackingListDTO.builder()
                .id(pl.getId())
                .packCode(pl.getPackCode())
                .packDate(pl.getPackDate())
                .weight(pl.getWeight())
                .dimensions(pl.getDimensions())
                .status(pl.getStatus())
                .orderId(pl.getOrder() != null ? pl.getOrder().getId() : null)
                .orderCode(pl.getOrder() != null ? pl.getOrder().getOrderCode() : null)
                .pickingStartedBy(pl.getPickingStartedBy())
                .pickingStartedAt(pl.getPickingStartedAt())
                .pickedBy(pl.getPickedBy())
                .pickedAt(pl.getPickedAt())
                .packingStartedBy(pl.getPackingStartedBy())
                .packingStartedAt(pl.getPackingStartedAt())
                .packedBy(pl.getPackedBy())
                .packedAt(pl.getPackedAt())
                .items(items)
                .build();
    }

    private PackingListDTO.Item toItemDTO(PackingListItem item) {
        return PackingListDTO.Item.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariant().getId())
                .sku(item.getProductVariant().getSku())
                .productName(item.getProductVariant().getProduct() != null ? item.getProductVariant().getProduct().getName() : "")
                .quantity(item.getQuantity())
                .pickedQuantity(item.getPickedQuantity())
                .packedQuantity(item.getPackedQuantity())
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
