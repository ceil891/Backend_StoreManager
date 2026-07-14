package org.example.storemanager.dto.response.partnerarea.supplier; // Thay bằng package của cậu

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierInfo {
    private Long id;
    private String name;
    private String phone;
}