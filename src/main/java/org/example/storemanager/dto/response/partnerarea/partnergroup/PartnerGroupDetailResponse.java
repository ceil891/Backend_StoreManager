package org.example.storemanager.dto.response.partnerarea.partnergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.example.storemanager.dto.response.partnerarea.customer.CustomerInfo;
import org.example.storemanager.dto.response.partnerarea.supplier.SupplierInfo;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY) // Dùng NON_EMPTY để tự ẩn list rỗng
public class PartnerGroupDetailResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private String description;
    private Integer initialMemberCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private Boolean isDeleted;
    private Integer totalMember;
    private List<CustomerInfo> customers;
    private List<SupplierInfo> suppliers;
}