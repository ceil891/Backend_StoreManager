package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.ReturnToSupplierDTO;
import org.example.storemanager.modules.inventory.dto.ReturnToSupplierDetailDTO;
import org.example.storemanager.modules.inventory.entity.ReturnToSupplier;
import org.example.storemanager.modules.inventory.entity.ReturnToSupplierDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReturnToSupplierMapper {
    
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "returnLines", ignore = true)
    ReturnToSupplierDTO toDto(ReturnToSupplier entity);
    
    @Mapping(target = "productVariantId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "product.productCode")
    @Mapping(target = "unitCost", source = "unitPrice")
    ReturnToSupplierDetailDTO toDetailDto(ReturnToSupplierDetail entity);
    
    List<ReturnToSupplierDTO> toDtoList(List<ReturnToSupplier> entities);
    List<ReturnToSupplierDetailDTO> toDetailDtoList(List<ReturnToSupplierDetail> entities);
}
