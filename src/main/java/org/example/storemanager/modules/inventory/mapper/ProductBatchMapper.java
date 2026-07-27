package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.ProductBatchDTO;
import org.example.storemanager.modules.inventory.entity.ProductBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductBatchMapper {
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "product.productCode")
    ProductBatchDTO toDto(ProductBatch entity);
    
    List<ProductBatchDTO> toDtoList(List<ProductBatch> entities);
}
