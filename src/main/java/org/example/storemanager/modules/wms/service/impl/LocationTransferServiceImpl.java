package org.example.storemanager.modules.wms.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.wms.dto.LocationTransferDTO;
import org.example.storemanager.modules.catalog.entity.ProductVariant;
import org.example.storemanager.modules.system.entity.Branch;
import org.example.storemanager.modules.wms.entity.LocationTransfer;
import org.example.storemanager.modules.wms.entity.Rack;
import org.example.storemanager.modules.wms.entity.WarehouseBin;
import org.example.storemanager.modules.wms.entity.ProductLocation;
import org.example.storemanager.shared.enums.ErrorCode;
import org.example.storemanager.shared.exception.BusinessException;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.example.storemanager.modules.catalog.repository.ProductVariantRepository;
import org.example.storemanager.modules.system.repository.BranchRepository;
import org.example.storemanager.modules.wms.repository.LocationTransferRepository;
import org.example.storemanager.modules.wms.repository.WarehouseBinRepository;
import org.example.storemanager.modules.wms.repository.ProductLocationRepository;
import org.example.storemanager.modules.wms.service.LocationTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationTransferServiceImpl implements LocationTransferService {

    private final LocationTransferRepository locationTransferRepository;
    private final WarehouseBinRepository warehouseBinRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BranchRepository branchRepository;
    private final ProductLocationRepository productLocationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocationTransferDTO.Response> getAllTransfers() {
        return locationTransferRepository.findAll().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationTransferDTO.Response> getTransfersByBranchId(Long branchId) {
        return locationTransferRepository.findByBranch_IdAndIsDeletedFalseOrderByTransferDateDesc(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationTransferDTO.Response getTransferById(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));
        return toResponse(transfer);
    }

    @Override
    public LocationTransferDTO.Response createTransfer(LocationTransferDTO.Request request) {
        Branch branch = branchRepository.findByIdAndIsDeletedFalse(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        String transferCode = "LT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", locationTransferRepository.count() + 1);

        LocationTransfer transfer = LocationTransfer.builder()
                .transferCode(transferCode)
                .transferDate(LocalDateTime.now())
                .status("DRAFT")
                .reason(request.getReason())
                .branch(branch)
                .build();

        if (request.getProductVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", request.getProductVariantId()));
            transfer.setProductVariant(variant);
        }

        if (request.getFromBinId() != null) {
            WarehouseBin fromBin = warehouseBinRepository.findByIdAndIsDeletedFalse(request.getFromBinId())
                    .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", request.getFromBinId()));
            transfer.setFromBin(fromBin);
        }

        if (request.getToBinId() != null) {
            WarehouseBin toBin = warehouseBinRepository.findByIdAndIsDeletedFalse(request.getToBinId())
                    .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", request.getToBinId()));
            transfer.setToBin(toBin);
        }

        if (request.getQuantity() != null) {
            transfer.setQuantity(request.getQuantity());
        }

        transfer.setIsDeleted(false);
        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response updateTransfer(Long id, LocationTransferDTO.Request request) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể sửa yêu cầu ở trạng thái DRAFT");
        }

        if (request.getReason() != null) {
            transfer.setReason(request.getReason());
        }
        if (request.getExecutedBy() != null) {
            transfer.setExecutedBy(request.getExecutedBy());
        }
        if (request.getQuantity() != null) {
            transfer.setQuantity(request.getQuantity());
        }

        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public void deleteTransfer(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể xóa yêu cầu ở trạng thái DRAFT");
        }

        transfer.setIsDeleted(true);
        locationTransferRepository.save(transfer);
    }

    @Override
    public LocationTransferDTO.Response setItem(Long id, LocationTransferDTO.Request request) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể thay đổi item ở trạng thái DRAFT");
        }

        WarehouseBin fromBin = warehouseBinRepository.findByIdAndIsDeletedFalse(request.getFromBinId())
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", request.getFromBinId()));
        WarehouseBin toBin = warehouseBinRepository.findByIdAndIsDeletedFalse(request.getToBinId())
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseBin", "id", request.getToBinId()));
        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", request.getProductVariantId()));

        if (request.getFromBinId().equals(request.getToBinId())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Ô kệ nguồn và đích không thể giống nhau");
        }

        transfer.setProductVariant(variant);
        transfer.setFromBin(fromBin);
        transfer.setToBin(toBin);
        transfer.setQuantity(request.getQuantity());

        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response clearItem(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Chỉ có thể thay đổi item ở trạng thái DRAFT");
        }

        transfer.setProductVariant(null);
        transfer.setFromBin(null);
        transfer.setToBin(null);
        transfer.setQuantity(null);

        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response submitTransfer(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể gửi duyệt từ trạng thái DRAFT");
        }

        if (transfer.getProductVariant() == null || transfer.getFromBin() == null || transfer.getToBin() == null || transfer.getQuantity() == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Yêu cầu chưa cấu hình đầy đủ mặt hàng và ô kệ nguồn/đích");
        }

        transfer.setStatus("PENDING_APPROVAL");
        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response approveTransfer(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"PENDING_APPROVAL".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể phê duyệt từ trạng thái PENDING_APPROVAL");
        }

        transfer.setStatus("APPROVED");
        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response executeTransfer(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if (!"APPROVED".equals(transfer.getStatus()) && !"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Chỉ có thể thực hiện bốc xếp từ trạng thái APPROVED");
        }

        ProductVariant variant = transfer.getProductVariant();
        WarehouseBin fromBin = transfer.getFromBin();
        WarehouseBin toBin = transfer.getToBin();

        ProductLocation sourceLoc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(variant.getProduct().getId(), fromBin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_ERROR, "Mặt hàng không tồn tại ở ô kệ nguồn"));

        if (sourceLoc.getQuantity().compareTo(transfer.getQuantity()) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "Số lượng mặt hàng ở ô kệ nguồn không đủ");
        }

        sourceLoc.setQuantity(sourceLoc.getQuantity().subtract(transfer.getQuantity()));
        productLocationRepository.save(sourceLoc);

        ProductLocation destLoc = productLocationRepository.findByProductIdAndBinIdAndIsDeletedFalse(variant.getProduct().getId(), toBin.getId())
                .orElseGet(() -> ProductLocation.builder()
                        .product(variant.getProduct())
                        .bin(toBin)
                        .quantity(java.math.BigDecimal.ZERO)
                        .build());
        destLoc.setQuantity(destLoc.getQuantity().add(transfer.getQuantity()));
        destLoc.setIsDeleted(false);
        productLocationRepository.save(destLoc);

        toBin.setStatus("OCCUPIED");
        warehouseBinRepository.save(toBin);

        transfer.setStatus("EXECUTED");
        return toResponse(locationTransferRepository.save(transfer));
    }

    @Override
    public LocationTransferDTO.Response completeTransfer(Long id) {
        return executeTransfer(id);
    }

    @Override
    public LocationTransferDTO.Response cancelTransfer(Long id) {
        LocationTransfer transfer = locationTransferRepository.findById(id)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("LocationTransfer", "id", id));

        if ("EXECUTED".equals(transfer.getStatus()) || "CANCELLED".equals(transfer.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION, "Không thể hủy khi yêu cầu đã thực hiện hoặc đã hủy trước đó");
        }
        transfer.setStatus("CANCELLED");
        return toResponse(locationTransferRepository.save(transfer));
    }

    private String buildBinPath(WarehouseBin bin) {
        if (bin == null) return "";
        Rack rack = bin.getRack();
        if (rack == null) return bin.getBinCode();
        return (rack.getArea() != null ? rack.getArea().getAreaCode() + "-" : "")
                + rack.getRackCode() + "-" + bin.getBinCode();
    }

    private LocationTransferDTO.Response toResponse(LocationTransfer t) {
        return LocationTransferDTO.Response.builder()
                .id(t.getId())
                .transferCode(t.getTransferCode())
                .transferDate(t.getTransferDate())
                .status(t.getStatus())
                .reason(t.getReason())
                .quantity(t.getQuantity())
                .executedBy(t.getExecutedBy())
                .productVariantId(t.getProductVariant() != null ? t.getProductVariant().getId() : null)
                .productName(t.getProductVariant() != null && t.getProductVariant().getProduct() != null
                        ? t.getProductVariant().getProduct().getName() : null)
                .sku(t.getProductVariant() != null ? t.getProductVariant().getSku() : null)
                .fromBinId(t.getFromBin() != null ? t.getFromBin().getId() : null)
                .fromBinCode(t.getFromBin() != null ? t.getFromBin().getBinCode() : null)
                .fromBinLocation(buildBinPath(t.getFromBin()))
                .toBinId(t.getToBin() != null ? t.getToBin().getId() : null)
                .toBinCode(t.getToBin() != null ? t.getToBin().getBinCode() : null)
                .toBinLocation(buildBinPath(t.getToBin()))
                .branchId(t.getBranch() != null ? t.getBranch().getId() : null)
                .branchName(t.getBranch() != null ? t.getBranch().getBranchName() : null)
                .createdAt(t.getCreatedAt())
                .createdBy(t.getCreatedBy())
                .updatedAt(t.getUpdatedAt())
                .updatedBy(t.getUpdatedBy())
                .build();
    }
}
