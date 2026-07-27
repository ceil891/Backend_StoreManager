package org.example.storemanager.modules.purchase.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateSupplierEvaluationRequest {
    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    @NotNull(message = "Ngày đánh giá không được để trống")
    private LocalDate evalDate;

    private String remarks;

    private String evaluationType;
    private Integer qualityScore;
    private Integer deliveryScore;
    private Integer serviceScore;
    private Integer priceScore;
    private String result;
    private String improvement;
    private String note;
}
