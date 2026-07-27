package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.InventoryCheckDTO;
import org.example.storemanager.modules.inventory.dto.InventoryCheckDetailDTO;
import org.example.storemanager.modules.inventory.entity.InventoryCheck;
import org.example.storemanager.modules.inventory.entity.InventoryCheckDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryCheckMapper {
    
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "warehouseZoneId", source = "warehouseZone.id")
    @Mapping(target = "warehouseZoneName", source = "warehouseZone.zoneName")
    @Mapping(target = "checkLines", ignore = true)
    InventoryCheckDTO toDto(InventoryCheck entity);
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "product.productCode")
    InventoryCheckDetailDTO toDetailDto(InventoryCheckDetail entity);
    
    List<InventoryCheckDTO> toDtoList(List<InventoryCheck> entities);
    List<InventoryCheckDetailDTO> toDetailDtoList(List<InventoryCheckDetail> entities);
}
