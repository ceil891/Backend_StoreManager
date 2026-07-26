package org.example.storemanager.mapper.inventory;

import org.example.storemanager.dto.inventory.StockTransferDTO;
import org.example.storemanager.dto.inventory.StockTransferDetailDTO;
import org.example.storemanager.entity.inventory.StockTransfer;
import org.example.storemanager.entity.inventory.StockTransferDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockTransferMapper {
    
    @Mapping(target = "fromBranchId", source = "fromBranch.id")
    @Mapping(target = "fromBranchName", source = "fromBranch.branchName")
    @Mapping(target = "toBranchId", source = "toBranch.id")
    @Mapping(target = "toBranchName", source = "toBranch.branchName")
    @Mapping(target = "transferLines", ignore = true)
    StockTransferDTO toDto(StockTransfer entity);
    
    @Mapping(target = "productVariantId", source = "product.id")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "transferQuantity", source = "quantityShipped")
    StockTransferDetailDTO toDetailDto(StockTransferDetail entity);
    
    List<StockTransferDTO> toDtoList(List<StockTransfer> entities);
    List<StockTransferDetailDTO> toDetailDtoList(List<StockTransferDetail> entities);
}
