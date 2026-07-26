package org.example.storemanager.dto.request.purchase;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateSupplierEvaluationRequest {
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
