package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.ImportReceiptDTO;
import org.example.storemanager.modules.inventory.dto.ImportReceiptDetailDTO;
import org.example.storemanager.modules.inventory.entity.ImportReceipt;
import org.example.storemanager.modules.inventory.entity.ImportReceiptDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImportReceiptMapper {
    
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "purchaseOrderCode", source = "purchaseOrder.poCode")
    @Mapping(target = "receiptLines", ignore = true)
    ImportReceiptDTO toDto(ImportReceipt entity);
    
    @Mapping(target = "productVariantId", source = "productVariant.id")
    @Mapping(target = "productName", source = "productVariant.product.name")
    @Mapping(target = "sku", source = "productVariant.sku")
    @Mapping(target = "barcode", source = "productVariant.barcode")
    @Mapping(target = "unitCost", source = "unitCostSnapshot")
    @Mapping(target = "targetBinId", source = "targetBin.id")
    @Mapping(target = "targetBinCode", source = "targetBin.binCode")
    ImportReceiptDetailDTO toDetailDto(ImportReceiptDetail entity);
    
    List<ImportReceiptDTO> toDtoList(List<ImportReceipt> entities);
    List<ImportReceiptDetailDTO> toDetailDtoList(List<ImportReceiptDetail> entities);
}
