package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.finance.entity.Payroll;
import org.example.storemanager.modules.finance.repository.PayrollRepository;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payrolls")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PayrollController {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        return ResponseEntity.ok(ApiResponse.ok(payrollRepository.findByIsDeletedFalse()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Payroll>> createPayroll(@RequestBody Payroll req) {
        req.setIsDeleted(false);
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            req.setStatus("DRAFT");
        }
        if (req.getPayrollCode() == null || req.getPayrollCode().isBlank()) {
            req.setPayrollCode("PR-" + System.currentTimeMillis());
        }

        // Parse payrollMonth (e.g. "2026-06")
        if ((req.getPeriodYear() == null || req.getPeriodMonth() == null) && req.getPayrollMonth() != null && req.getPayrollMonth().contains("-")) {
            String[] parts = req.getPayrollMonth().split("-");
            try {
                req.setPeriodYear(Integer.parseInt(parts[0]));
                req.setPeriodMonth(Integer.parseInt(parts[1]));
            } catch (Exception ignored) {
            }
        }
        if (req.getPeriodYear() == null) req.setPeriodYear(java.time.LocalDate.now().getYear());
        if (req.getPeriodMonth() == null) req.setPeriodMonth(java.time.LocalDate.now().getMonthValue());

        // Resolve user
        if (req.getUser() == null && req.getUserId() != null) {
            userRepository.findById(req.getUserId()).ifPresent(req::setUser);
        }
        if (req.getUser() == null && req.getEmployeeName() != null && !req.getEmployeeName().isBlank()) {
            userRepository.findAll().stream()
                    .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) && req.getEmployeeName().equalsIgnoreCase(u.getFullName()))
                    .findFirst().ifPresent(req::setUser);
        }
        if (req.getUser() == null) {
            userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getIsDeleted())).findFirst().ifPresent(req::setUser);
        }

        BigDecimal base = req.getBaseSalary() != null ? req.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal allow = req.getAllowance() != null ? req.getAllowance() : BigDecimal.ZERO;
        BigDecimal bonus = req.getKpiBonus() != null ? req.getKpiBonus() : BigDecimal.ZERO;
        BigDecimal ded = req.getDeduction() != null ? req.getDeduction() : BigDecimal.ZERO;

        if (req.getNetSalary() == null || req.getNetSalary().compareTo(BigDecimal.ZERO) == 0) {
            req.setNetSalary(base.add(allow).add(bonus).subtract(ded));
        }

        return ResponseEntity.status(201).body(ApiResponse.created(payrollRepository.save(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Payroll>> updatePayroll(@PathVariable Long id, @RequestBody Payroll req) {
        Payroll existing = payrollRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        if (req.getPayrollCode() != null) existing.setPayrollCode(req.getPayrollCode());
        if (req.getPeriodMonth() != null) existing.setPeriodMonth(req.getPeriodMonth());
        if (req.getPeriodYear() != null) existing.setPeriodYear(req.getPeriodYear());
        if (req.getPayrollMonth() != null && req.getPayrollMonth().contains("-")) {
            String[] parts = req.getPayrollMonth().split("-");
            try {
                existing.setPeriodYear(Integer.parseInt(parts[0]));
                existing.setPeriodMonth(Integer.parseInt(parts[1]));
            } catch (Exception ignored) {
            }
        }
        if (req.getBaseSalary() != null) existing.setBaseSalary(req.getBaseSalary());
        if (req.getAllowance() != null) existing.setAllowance(req.getAllowance());
        if (req.getKpiBonus() != null) existing.setKpiBonus(req.getKpiBonus());
        if (req.getDeduction() != null) existing.setDeduction(req.getDeduction());
        if (req.getNetSalary() != null) existing.setNetSalary(req.getNetSalary());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getPaymentDate() != null) existing.setPaymentDate(req.getPaymentDate());

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
