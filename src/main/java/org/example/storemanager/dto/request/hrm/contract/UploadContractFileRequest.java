package org.example.storemanager.dto.request.hrm.contract;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadContractFileRequest {

    @NotBlank(message = "URL file hợp đồng không được để trống")
    private String contractUrl;

    private String notes;
}

