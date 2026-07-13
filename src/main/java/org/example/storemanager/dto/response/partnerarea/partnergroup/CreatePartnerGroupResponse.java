package org.example.storemanager.dto.response.partnerarea.partnergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePartnerGroupResponse {
    // 1. Thông tin PartnerGroup
    private Long id;
    private String groupCode;
    private String groupName;
    private String type;
    private String description;
    private Integer initialMemberCount;
    private Boolean isActive;

    // 2. Thông tin Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // 3. Các trường chuẩn API (đã sửa lỗi trùng lặp 'message')
    private boolean success;
    private int status;
    private String message;
    private LocalDateTime timestamp;
}