package org.example.storemanager.mapper.inventory;

import org.example.storemanager.dto.inventory.ProductBatchDTO;
import org.example.storemanager.entity.inventory.ProductBatch;
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
