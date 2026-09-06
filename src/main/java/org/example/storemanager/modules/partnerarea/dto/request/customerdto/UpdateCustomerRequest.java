package org.example.storemanager.modules.partnerarea.dto.request.customerdto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Builder
public class UpdateCustomerRequest {
    private String customerCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private Long groupId;
    private Long areaId;
    private Boolean isActive;
    private MultipartFile avatar;
    private String avatarUrl;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;
    private String gender;
    private String membershipRank;
    private Double points;
    private Double totalSpend;
    private String note;
    private Double debtLimit;
}