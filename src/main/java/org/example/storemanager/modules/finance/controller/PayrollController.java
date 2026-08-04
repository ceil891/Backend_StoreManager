package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.finance.entity.Payroll;
import org.example.storemanager.modules.finance.repository.PayrollRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payrolls")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PayrollController {

    private final PayrollRepository payrollRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.ok(payrollRepository.findByIsDeletedFalse()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Payroll>> createPayroll(@RequestBody Payroll req) {
        req.setIsDeleted(false);
        if (req.getStatus() == null) {
            req.setStatus("DRAFT");
        }
        return ResponseEntity.status(201).body(ApiResponse.created(payrollRepository.save(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Payroll>> updatePayroll(@PathVariable Long id, @RequestBody Payroll req) {
        Payroll existing = payrollRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        
        if (req.getPeriodMonth() != null) existing.setPeriodMonth(req.getPeriodMonth());
        if (req.getPeriodYear() != null) existing.setPeriodYear(req.getPeriodYear());
        if (req.getBaseSalary() != null) existing.setBaseSalary(req.getBaseSalary());
        if (req.getAllowance() != null) existing.setAllowance(req.getAllowance());
        if (req.getDeduction() != null) existing.setDeduction(req.getDeduction());
        if (req.getNetSalary() != null) existing.setNetSalary(req.getNetSalary());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bảng lương thành công", payrollRepository.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable Long id) {
        Payroll existing = payrollRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        existing.setIsDeleted(true);
        payrollRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bảng lương thành công", null));
    }
}
