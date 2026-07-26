package org.example.storemanager.mapper.inventory;

import org.example.storemanager.dto.inventory.CancelIssueDTO;
import org.example.storemanager.dto.inventory.CancelIssueDetailDTO;
import org.example.storemanager.entity.inventory.CancelIssue;
import org.example.storemanager.entity.inventory.CancelIssueDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CancelIssueMapper {
    
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "cancelLines", ignore = true)
    CancelIssueDTO toDto(CancelIssue entity);
    
    @Mapping(target = "productVariantId", source = "product.id")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "productName", source = "product.name")
    CancelIssueDetailDTO toDetailDto(CancelIssueDetail entity);
    
    List<CancelIssueDTO> toDtoList(List<CancelIssue> entities);
    List<CancelIssueDetailDTO> toDetailDtoList(List<CancelIssueDetail> entities);
}
