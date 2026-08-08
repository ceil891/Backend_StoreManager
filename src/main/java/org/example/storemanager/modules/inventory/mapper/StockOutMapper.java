package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.StockOutDTO;
import org.example.storemanager.modules.inventory.dto.StockOutDetailDTO;
import org.example.storemanager.modules.inventory.entity.StockOut;
import org.example.storemanager.modules.inventory.entity.StockOutDetail;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockOutMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StockOutDTO toDTO(StockOut entity) {
        if (entity == null) return null;

        List<StockOutDetailDTO> itemDTOs = new ArrayList<>();
        if (entity.getDetails() != null) {
            itemDTOs = entity.getDetails().stream()
                    .map(this::toDetailDTO)
                    .collect(Collectors.toList());
        }

        String issuedDateStr = entity.getIssuedDate() != null
                ? entity.getIssuedDate().format(FORMATTER)
                : (entity.getCreatedAt() != null ? entity.getCreatedAt().format(FORMATTER) : "");

        return StockOutDTO.builder()
                .id(entity.getId())
                .stockOutCode(entity.getStockOutCode())
                .outType(entity.getOutType())
                .warehouseName(entity.getWarehouseName())
                .issuedDate(issuedDateStr)
                .totalVariants(entity.getTotalVariants() != null ? entity.getTotalVariants() : itemDTOs.size())
                .totalItems(entity.getTotalItems())
                .totalValue(entity.getTotalValue())
                .creator(entity.getCreator())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .items(itemDTOs)
                .build();
    }

    public StockOutDetailDTO toDetailDTO(StockOutDetail detail) {
        if (detail == null) return null;
        return StockOutDetailDTO.builder()
                .id(detail.getId())
                .productName(detail.getProductName())
                .variant(detail.getVariant())
                .sku(detail.getSku())
                .barcode(detail.getBarcode())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .amount(detail.getAmount())
                .build();
    }

    public StockOut toEntity(StockOutDTO dto) {
        if (dto == null) return null;

        LocalDateTime issuedDate = null;
        if (dto.getIssuedDate() != null && !dto.getIssuedDate().trim().isEmpty()) {
            try {
                if (dto.getIssuedDate().contains("T")) {
                    issuedDate = LocalDateTime.parse(dto.getIssuedDate().substring(0, 19));
                } else if (dto.getIssuedDate().length() >= 16) {
                    issuedDate = LocalDateTime.parse(dto.getIssuedDate().substring(0, 16), FORMATTER);
                } else if (dto.getIssuedDate().length() >= 10) {
                    issuedDate = LocalDateTime.parse(dto.getIssuedDate().substring(0, 10) + " 00:00", FORMATTER);
                }
            } catch (Exception e) {
                issuedDate = LocalDateTime.now();
            }
        } else {
            issuedDate = LocalDateTime.now();
        }

        StockOut stockOut = StockOut.builder()
                .stockOutCode(dto.getStockOutCode())
                .outType(dto.getOutType() != null ? dto.getOutType() : "BAN_HANG")
                .warehouseName(dto.getWarehouseName())
                .issuedDate(issuedDate)
                .totalVariants(dto.getTotalVariants())
                .totalItems(dto.getTotalItems())
                .totalValue(dto.getTotalValue())
                .creator(dto.getCreator())
                .status(dto.getStatus() != null ? dto.getStatus() : "CHO_XU_LY")
                .notes(dto.getNotes())
                .details(new ArrayList<>())
                .build();

        if (dto.getId() != null) {
            stockOut.setId(dto.getId());
        }

        if (dto.getItems() != null) {
            List<StockOutDetail> details = dto.getItems().stream().map(i -> {
                StockOutDetail detail = StockOutDetail.builder()
                        .stockOut(stockOut)
                        .productName(i.getProductName())
                        .variant(i.getVariant())
                        .sku(i.getSku())
                        .barcode(i.getBarcode())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .amount(i.getAmount())
                        .build();
                if (i.getId() != null) {
                    detail.setId(i.getId());
                }
                return detail;
            }).collect(Collectors.toList());
            stockOut.setDetails(details);
        }

        return stockOut;
    }
}
