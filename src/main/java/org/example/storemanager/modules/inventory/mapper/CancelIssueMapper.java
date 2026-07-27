package org.example.storemanager.modules.inventory.mapper;

import org.example.storemanager.modules.inventory.dto.CancelIssueDTO;
import org.example.storemanager.modules.inventory.dto.CancelIssueDetailDTO;
import org.example.storemanager.modules.inventory.entity.CancelIssue;
import org.example.storemanager.modules.inventory.entity.CancelIssueDetail;
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
