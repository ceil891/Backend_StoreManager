package org.example.storemanager.modules.finance.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.finance.entity.Payroll;
import org.example.storemanager.modules.finance.repository.PayrollRepository;
import org.example.storemanager.modules.finance.entity.PaymentVoucher;
import org.example.storemanager.modules.finance.repository.PaymentVoucherRepository;
import org.example.storemanager.modules.hrm.entity.Attendance;
import org.example.storemanager.modules.hrm.entity.LeaveRequest;
import org.example.storemanager.modules.hrm.entity.ShiftSwapRequest;
import org.example.storemanager.modules.hrm.repository.AttendanceRepository;
import org.example.storemanager.modules.hrm.repository.LeaveRequestRepository;
import org.example.storemanager.modules.hrm.repository.ShiftSwapRequestRepository;
import org.example.storemanager.shared.service.DocumentSequenceService;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/payrolls")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PayrollController {

    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ShiftSwapRequestRepository shiftSwapRequestRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final DocumentSequenceService documentSequenceService;

    private static final BigDecimal STANDARD_WORK_DAYS = BigDecimal.valueOf(26);
    private static final BigDecimal SHIFT_COVER_ALLOWANCE = BigDecimal.valueOf(200_000);

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        List<Payroll> payrolls = payrollRepository.findByIsDeletedFalse();
        payrolls.forEach(this::calculatePayroll);
        return ResponseEntity.ok(ApiResponse.ok(payrolls));
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
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

        // Resolve user and persistent employee name
        if (req.getUser() == null && req.getUserId() != null) {
            userRepository.findById(req.getUserId()).ifPresent(u -> {
                req.setUser(u);
                if (req.getEmployeeName() == null || req.getEmployeeName().isBlank()) {
                    req.setEmployeeName(u.getFullName() != null ? u.getFullName() : u.getUsername());
                }
            });
        }
        if (req.getUser() == null && req.getEmployeeName() != null && !req.getEmployeeName().isBlank()) {
            userRepository.findAll().stream()
                    .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) &&
                            (req.getEmployeeName().equalsIgnoreCase(u.getFullName()) ||
                             req.getEmployeeName().equalsIgnoreCase(u.getUsername()) ||
                             req.getEmployeeName().equalsIgnoreCase(u.getEmail())))
                    .findFirst().ifPresent(u -> {
                        req.setUser(u);
                        req.setEmployeeName(u.getFullName() != null ? u.getFullName() : req.getEmployeeName());
                    });
        }
        if (req.getEmployeeName() == null || req.getEmployeeName().isBlank()) {
            req.setEmployeeName("Nhân viên");
        }
        if (req.getDepartment() == null || req.getDepartment().isBlank()) {
            req.setDepartment("Nhân sự / Kinh doanh");
        }

        calculatePayroll(req);

        return ResponseEntity.status(201).body(ApiResponse.created(payrollRepository.save(req)));
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
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
        if (req.getEmployeeName() != null) existing.setEmployeeName(req.getEmployeeName());
        if (req.getDepartment() != null) existing.setDepartment(req.getDepartment());
        if (req.getBaseSalary() != null) existing.setBaseSalary(req.getBaseSalary());
        if (req.getAllowance() != null) existing.setAllowance(req.getAllowance());
        if (req.getKpiBonus() != null) existing.setKpiBonus(req.getKpiBonus());
        if (req.getDeduction() != null) existing.setDeduction(req.getDeduction());
        calculatePayroll(existing);
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getPaymentDate() != null) existing.setPaymentDate(req.getPaymentDate());
        if ("PAID".equalsIgnoreCase(existing.getStatus()) && existing.getPaymentDate() == null) {
            existing.setPaymentDate(LocalDateTime.now());
        }
        Payroll saved = payrollRepository.save(existing);
        createPaymentVoucherIfPaid(saved);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bảng lương thành công", saved));
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable Long id) {
        Payroll existing = payrollRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        existing.setIsDeleted(true);
        payrollRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa bảng lương thành công", null));
    }

    /** Tính lại lương từ chấm công, nghỉ không lương và ca trực thay đã duyệt. */
    private void calculatePayroll(Payroll payroll) {
        BigDecimal base = payroll.getBaseSalary() != null ? payroll.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal fixedAllowance = payroll.getAllowance() != null ? payroll.getAllowance() : BigDecimal.ZERO;
        BigDecimal kpi = payroll.getKpiBonus() != null ? payroll.getKpiBonus() : BigDecimal.ZERO;
        BigDecimal manualDeduction = payroll.getDeduction() != null ? payroll.getDeduction() : BigDecimal.ZERO;

        if (payroll.getUser() == null || payroll.getPeriodYear() == null || payroll.getPeriodMonth() == null) {
            payroll.setNetSalary(base.add(fixedAllowance).add(kpi).subtract(manualDeduction));
            return;
        }

        LocalDate from = LocalDate.of(payroll.getPeriodYear(), payroll.getPeriodMonth(), 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        // 1. Lấy số ngày công thực tế từ chấm công
        BigDecimal workDays = attendanceRepository.findByUserAndDateRange(payroll.getUser().getId(), from, to).stream()
                .map(this::attendanceWorkDay).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Lấy số ngày nghỉ không lương
        BigDecimal unpaidLeaveDays = leaveRequestRepository.findByIsDeletedFalse().stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()) && "UNPAID".equalsIgnoreCase(l.getLeaveType()))
                .filter(l -> payroll.getUser().getId().equals(l.getUserId()))
                .map(l -> overlapDays(l, from, to)).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal leaveDays = leaveRequestRepository.findByIsDeletedFalse().stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                .filter(l -> payroll.getUser().getId().equals(l.getUserId()))
                .map(l -> overlapDays(l, from, to)).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Phụ cấp ca trực thay
        BigDecimal coverAllowance = shiftSwapRequestRepository.findByIsDeletedFalse().stream()
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus()) && s.getSwapDate() != null)
                .filter(s -> !s.getSwapDate().isBefore(from) && !s.getSwapDate().isAfter(to))
                .filter(s -> payroll.getEmployeeName().equalsIgnoreCase(s.getTargetUserName()))
                .map(s -> SHIFT_COVER_ALLOWANCE).reduce(BigDecimal.ZERO, BigDecimal::add);

        // FIX: Nếu baseSalary < 1.000.000 thì hiểu đó là "Lương theo ngày công", không chia cho 26
        BigDecimal dailySalary = base.compareTo(new BigDecimal("1000000")) < 0
                ? base
                : base.divide(STANDARD_WORK_DAYS, 2, RoundingMode.HALF_UP);

        BigDecimal attendanceSalary = dailySalary.multiply(workDays);
        BigDecimal unpaidDeduction = dailySalary.multiply(unpaidLeaveDays);



        payroll.setAllowance(fixedAllowance);
        payroll.setDeduction(manualDeduction);
        payroll.setWorkingDays(workDays);
        payroll.setLeaveDays(leaveDays);

        // Thực lĩnh = Lương công + Phụ cấp + KPI + Trực thay - Khấu trừ - Trừ nghỉ không lương
        payroll.setNetSalary(attendanceSalary.add(fixedAllowance).add(kpi).add(coverAllowance)
                .subtract(manualDeduction).subtract(unpaidDeduction).max(BigDecimal.ZERO));
    }
    private BigDecimal attendanceWorkDay(Attendance attendance) {
        if (attendance == null || attendance.getStatus() == null) return BigDecimal.ZERO;
        return switch (attendance.getStatus().toUpperCase()) {
            case "PRESENT", "ON_TIME" -> BigDecimal.ONE;
            case "LATE", "EARLY_LEAVE", "HALF_DAY" -> BigDecimal.valueOf(0.5);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal overlapDays(LeaveRequest leave, LocalDate from, LocalDate to) {
        if (leave.getStartDate() == null || leave.getEndDate() == null) return BigDecimal.ZERO;
        LocalDate start = leave.getStartDate().isBefore(from) ? from : leave.getStartDate();
        LocalDate end = leave.getEndDate().isAfter(to) ? to : leave.getEndDate();
        return end.isBefore(start) ? BigDecimal.ZERO : BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1);
    }

    private void createPaymentVoucherIfPaid(Payroll payroll) {
        if (!"PAID".equalsIgnoreCase(payroll.getStatus()) || payroll.getId() == null || payroll.getNetSalary() == null || payroll.getNetSalary().signum() <= 0) return;
        String reference = "PAYROLL-" + payroll.getId();
        if (paymentVoucherRepository.existsByInvoiceCodeAndIsDeletedFalse(reference)) return;
        PaymentVoucher voucher = PaymentVoucher.builder()
                .voucherCode(documentSequenceService.generatePaymentCode()).voucherDate(payroll.getPaymentDate() != null ? payroll.getPaymentDate() : LocalDateTime.now())
                .amount(payroll.getNetSalary()).receiverName(payroll.getEmployeeName()).invoiceCode(reference).status("COMPLETED")
                .paymentMethod("CHUYEN_KHOAN").fundAccountName("Tài khoản chi lương").handler("Hệ thống")
                .notes("Chi lương " + payroll.getPayrollMonth() + " cho " + payroll.getEmployeeName()).build();
        voucher.setIsDeleted(false);
        voucher.setCreatedBy("System");
        paymentVoucherRepository.save(voucher);
    }
}
