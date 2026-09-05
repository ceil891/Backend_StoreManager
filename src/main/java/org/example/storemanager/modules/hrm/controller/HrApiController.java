package org.example.storemanager.modules.hrm.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.hrm.entity.*;
import org.example.storemanager.modules.hrm.repository.*;
import org.example.storemanager.modules.system.entity.User;
import org.example.storemanager.modules.system.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HrApiController {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeContractRepository employeeContractRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final KpiRecordRepository kpiRecordRepository;
    private final ShiftSwapRequestRepository shiftSwapRequestRepository;
    private final UserRepository userRepository;

    // --- DEPARTMENTS ---
    @GetMapping("/departments")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(departmentRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Department req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(departmentRepository.save(req)));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id, @RequestBody Department req) {
        Department d = departmentRepository.findById(id).orElseThrow();
        if (req.getDeptCode() != null) d.setDeptCode(req.getDeptCode());
        if (req.getDeptName() != null) d.setDeptName(req.getDeptName());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getManager() != null) d.setManager(req.getManager());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật phòng ban thành công", departmentRepository.save(d)));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        Department d = departmentRepository.findById(id).orElseThrow();
        d.setIsDeleted(true);
        departmentRepository.save(d);
        return ResponseEntity.ok(ApiResponse.ok("Xóa phòng ban thành công", null));
    }

    // --- POSITIONS ---
    @GetMapping("/positions")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PositionDTO>>> getAllPositions() {
        List<PositionDTO> list = positionRepository.findByIsDeletedFalse().stream()
                .map(p -> PositionDTO.builder()
                        .id(p.getId())
                        .positionCode(p.getPositionCode())
                        .positionName(p.getPositionName())
                        .positionTitle(p.getPositionName())
                        .baseSalary(p.getBaseSalary())
                        .departmentId(p.getDepartment() != null ? p.getDepartment().getId() : null)
                        .departmentName(p.getDepartment() != null ? p.getDepartment().getDeptName() : "")
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/positions")
    public ResponseEntity<ApiResponse<PositionDTO>> createPosition(@RequestBody PositionDTO req) {
        Department dept = null;
        if (req.getDepartmentId() != null) {
            dept = departmentRepository.findById(req.getDepartmentId()).orElse(null);
        }
        Position p = Position.builder()
                .positionCode(req.getPositionCode())
                .positionName(req.getPositionName() != null ? req.getPositionName() : req.getPositionTitle())
                .baseSalary(req.getBaseSalary())
                .department(dept)
                .build();
        p.setIsDeleted(false);
        Position saved = positionRepository.save(p);
        PositionDTO resp = PositionDTO.builder()
                .id(saved.getId())
                .positionCode(saved.getPositionCode())
                .positionName(saved.getPositionName())
                .positionTitle(saved.getPositionName())
                .baseSalary(saved.getBaseSalary())
                .departmentId(saved.getDepartment() != null ? saved.getDepartment().getId() : null)
                .departmentName(saved.getDepartment() != null ? saved.getDepartment().getDeptName() : "")
                .build();
        return ResponseEntity.status(201).body(ApiResponse.created(resp));
    }

    @PutMapping("/positions/{id}")
    public ResponseEntity<ApiResponse<PositionDTO>> updatePosition(@PathVariable Long id, @RequestBody PositionDTO req) {
        Position p = positionRepository.findById(id).orElseThrow();
        if (req.getPositionCode() != null) p.setPositionCode(req.getPositionCode());
        if (req.getPositionName() != null) p.setPositionName(req.getPositionName());
        if (req.getPositionTitle() != null) p.setPositionName(req.getPositionTitle());
        if (req.getBaseSalary() != null) p.setBaseSalary(req.getBaseSalary());
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId()).orElse(null);
            p.setDepartment(dept);
        }
        Position saved = positionRepository.save(p);
        PositionDTO resp = PositionDTO.builder()
                .id(saved.getId())
                .positionCode(saved.getPositionCode())
                .positionName(saved.getPositionName())
                .positionTitle(saved.getPositionName())
                .baseSalary(saved.getBaseSalary())
                .departmentId(saved.getDepartment() != null ? saved.getDepartment().getId() : null)
                .departmentName(saved.getDepartment() != null ? saved.getDepartment().getDeptName() : "")
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chức vụ thành công", resp));
    }

    @DeleteMapping("/positions/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable Long id) {
        Position p = positionRepository.findById(id).orElseThrow();
        p.setIsDeleted(true);
        positionRepository.save(p);
        return ResponseEntity.ok(ApiResponse.ok("Xóa chức vụ thành công", null));
    }

    // --- LABOR CONTRACTS ---
    @GetMapping("/contracts")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<EmployeeContractDTO>>> getAllContracts() {
        List<EmployeeContractDTO> list = employeeContractRepository.findByIsDeletedFalse().stream()
                .map(c -> EmployeeContractDTO.builder()
                        .id(c.getId())
                        .contractNumber(c.getContractNumber())
                        .contractCode(c.getContractCode())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .contractType(c.getContractType())
                        .status(c.getStatus())
                        .userId(c.getUser() != null ? c.getUser().getId() : null)
                        .employeeName(c.getEmployeeName())
                        .employeePhone(c.getEmployeePhone())
                        .positionId(c.getPosition() != null ? c.getPosition().getId() : null)
                        .positionName(c.getPosition() != null ? c.getPosition().getPositionName() : "Nhân viên chính thức")
                        .baseSalary(c.getBaseSalary() != null ? c.getBaseSalary() : BigDecimal.valueOf(10000000))
                        .notes(c.getNotes())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/contracts")
    public ResponseEntity<ApiResponse<EmployeeContract>> createContract(@RequestBody EmployeeContract req) {
        req.setIsDeleted(false);
        if (req.getContractNumber() == null || req.getContractNumber().isBlank()) {
            req.setContractNumber(req.getContractCode() != null && !req.getContractCode().isBlank()
                    ? req.getContractCode()
                    : "HD-" + System.currentTimeMillis());
        }
        if (req.getStartDate() == null) {
            req.setStartDate(LocalDate.now());
        }
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            req.setStatus("ACTIVE");
        }
        if (req.getUser() == null) {
            if (req.getEmployeeName() != null && !req.getEmployeeName().isBlank()) {
                userRepository.findAll().stream()
                        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) && req.getEmployeeName().equalsIgnoreCase(u.getFullName()))
                        .findFirst().ifPresent(req::setUser);
            }
            if (req.getUser() == null) {
                userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getIsDeleted())).findFirst().ifPresent(req::setUser);
            }
        }
        if (req.getPosition() == null) {
            positionRepository.findByIsDeletedFalse().stream().findFirst().ifPresent(req::setPosition);
        }

        return ResponseEntity.status(201).body(ApiResponse.created(employeeContractRepository.save(req)));
    }

    @PutMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<EmployeeContract>> updateContract(@PathVariable Long id, @RequestBody EmployeeContract req) {
        EmployeeContract existing = employeeContractRepository.findById(id).orElseThrow();
        if (req.getContractNumber() != null) existing.setContractNumber(req.getContractNumber());
        if (req.getContractCode() != null) existing.setContractNumber(req.getContractCode());
        if (req.getStartDate() != null) existing.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) existing.setEndDate(req.getEndDate());
        if (req.getContractType() != null) existing.setContractType(req.getContractType());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getBaseSalary() != null) existing.setBaseSalary(req.getBaseSalary());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hợp đồng thành công", employeeContractRepository.save(existing)));
    }

    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long id) {
        EmployeeContract existing = employeeContractRepository.findById(id).orElseThrow();
        existing.setIsDeleted(true);
        employeeContractRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa hợp đồng thành công", null));
    }

    // --- LEAVE REQUESTS ---
    @GetMapping("/leave-requests")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getAllLeaveRequests() {
        return ResponseEntity.ok(ApiResponse.ok(leaveRequestRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeaveRequest(@RequestBody LeaveRequest req) {
        req.setIsDeleted(false);
        if (req.getRequestCode() == null || req.getRequestCode().isBlank()) {
            req.setRequestCode("NP-" + (System.currentTimeMillis() % 10000));
        }
        if (req.getStartDate() == null) req.setStartDate(LocalDate.now());
        if (req.getEndDate() == null) req.setEndDate(req.getStartDate());
        if (req.getStatus() == null || req.getStatus().isBlank()) req.setStatus("PENDING");
        if (req.getLeaveType() == null || req.getLeaveType().isBlank()) req.setLeaveType("ANNUAL");

        if (req.getUser() == null) {
            if (req.getEmployeeName() != null && !req.getEmployeeName().isBlank()) {
                userRepository.findAll().stream()
                        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) && req.getEmployeeName().equalsIgnoreCase(u.getFullName()))
                        .findFirst().ifPresent(req::setUser);
            }
            if (req.getUser() == null) {
                userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getIsDeleted())).findFirst().ifPresent(req::setUser);
            }
        }

        return ResponseEntity.status(201).body(ApiResponse.created(leaveRequestRepository.save(req)));
    }

    @PutMapping("/leave-requests/{id}")
    public ResponseEntity<ApiResponse<LeaveRequest>> updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequest req) {
        LeaveRequest existing = leaveRequestRepository.findById(id).orElseThrow();
        if (req.getStartDate() != null) existing.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) existing.setEndDate(req.getEndDate());
        if (req.getLeaveType() != null) existing.setLeaveType(req.getLeaveType());
        if (req.getReason() != null) existing.setReason(req.getReason());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getApproverName() != null && !req.getApproverName().isBlank()) {
            existing.setApproverName(req.getApproverName());
        }
        if ("APPROVED".equalsIgnoreCase(existing.getStatus()) && (existing.getApproverName() == null || existing.getApproverName().isBlank())) {
            existing.setApproverName("Admin quản lý");
        }
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đơn nghỉ phép thành công", leaveRequestRepository.save(existing)));
    }

    @DeleteMapping("/leave-requests/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Long id) {
        LeaveRequest existing = leaveRequestRepository.findById(id).orElseThrow();
        existing.setIsDeleted(true);
        leaveRequestRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đơn nghỉ phép thành công", null));
    }

    // --- KPI EVALUATIONS ---
    @GetMapping("/kpis")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<KpiRecord>>> getAllKpis() {
        return ResponseEntity.ok(ApiResponse.ok(kpiRecordRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/kpis")
    public ResponseEntity<ApiResponse<KpiRecord>> createKpi(@RequestBody KpiRecord req) {
        req.setIsDeleted(false);
        if ((req.getPeriodYear() == null || req.getPeriodMonth() == null) && req.getKpiMonth() != null && req.getKpiMonth().contains("-")) {
            String[] parts = req.getKpiMonth().split("-");
            try {
                req.setPeriodYear(Integer.parseInt(parts[0]));
                req.setPeriodMonth(Integer.parseInt(parts[1]));
            } catch (Exception ignored) {
            }
        }
        if (req.getPeriodYear() == null) req.setPeriodYear(LocalDate.now().getYear());
        if (req.getPeriodMonth() == null) req.setPeriodMonth(LocalDate.now().getMonthValue());
        if (req.getTargetScore() == null) req.setTargetScore(BigDecimal.valueOf(100));
        if (req.getAchievedScore() == null) req.setAchievedScore(BigDecimal.ZERO);
        if (req.getStatus() == null) req.setStatus("PENDING");
        if (req.getRatingGrade() == null) req.setRatingGrade("C_AVERAGE");

        if (req.getUser() == null) {
            if (req.getEmployeeName() != null && !req.getEmployeeName().isBlank()) {
                userRepository.findAll().stream()
                        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()) && req.getEmployeeName().equalsIgnoreCase(u.getFullName()))
                        .findFirst().ifPresent(req::setUser);
            }
            if (req.getUser() == null) {
                userRepository.findAll().stream().filter(u -> !Boolean.TRUE.equals(u.getIsDeleted())).findFirst().ifPresent(req::setUser);
            }
        }

        return ResponseEntity.status(201).body(ApiResponse.created(kpiRecordRepository.save(req)));
    }

    @PutMapping("/kpis/{id}")
    public ResponseEntity<ApiResponse<KpiRecord>> updateKpi(@PathVariable Long id, @RequestBody KpiRecord req) {
        KpiRecord existing = kpiRecordRepository.findById(id).orElseThrow();
        if (req.getPeriodMonth() != null) existing.setPeriodMonth(req.getPeriodMonth());
        if (req.getPeriodYear() != null) existing.setPeriodYear(req.getPeriodYear());
        if (req.getKpiMonth() != null && req.getKpiMonth().contains("-")) {
            String[] parts = req.getKpiMonth().split("-");
            try {
                existing.setPeriodYear(Integer.parseInt(parts[0]));
                existing.setPeriodMonth(Integer.parseInt(parts[1]));
            } catch (Exception ignored) {
            }
        }
        if (req.getTargetScore() != null) existing.setTargetScore(req.getTargetScore());
        if (req.getAchievedScore() != null) existing.setAchievedScore(req.getAchievedScore());
        if (req.getDepartmentName() != null) existing.setDepartmentName(req.getDepartmentName());
        if (req.getRatingGrade() != null) existing.setRatingGrade(req.getRatingGrade());
        if (req.getBonusAmount() != null) existing.setBonusAmount(req.getBonusAmount());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá KPI thành công", kpiRecordRepository.save(existing)));
    }

    @DeleteMapping("/kpis/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKpi(@PathVariable Long id) {
        KpiRecord existing = kpiRecordRepository.findById(id).orElseThrow();
        existing.setIsDeleted(true);
        kpiRecordRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa đánh giá KPI thành công", null));
    }

    // --- SHIFT SWAP REQUESTS ---
    @GetMapping("/shift-swaps")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ShiftSwapRequest>>> getAllShiftSwaps() {
        return ResponseEntity.ok(ApiResponse.ok(shiftSwapRequestRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/shift-swaps")
    public ResponseEntity<ApiResponse<ShiftSwapRequest>> createShiftSwap(@RequestBody ShiftSwapRequest req) {
        req.setIsDeleted(false);
        if (req.getRequestCode() == null || req.getRequestCode().isBlank()) {
            req.setRequestCode("DC-2026-" + (int)(100 + Math.random() * 900));
        }
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            req.setStatus("PENDING");
        }
        if (req.getSwapDate() == null) {
            req.setSwapDate(LocalDate.now());
        }
        return ResponseEntity.status(201).body(ApiResponse.created(shiftSwapRequestRepository.save(req)));
    }

    @PutMapping("/shift-swaps/{id}")
    public ResponseEntity<ApiResponse<ShiftSwapRequest>> updateShiftSwap(@PathVariable Long id, @RequestBody ShiftSwapRequest req) {
        ShiftSwapRequest existing = shiftSwapRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Shift swap request not found with id: " + id));

        if (req.getRequestCode() != null) existing.setRequestCode(req.getRequestCode());
        if (req.getRequesterName() != null) existing.setRequesterName(req.getRequesterName());
        if (req.getRequesterShift() != null) existing.setRequesterShift(req.getRequesterShift());
        if (req.getTargetUserName() != null) existing.setTargetUserName(req.getTargetUserName());
        if (req.getTargetUserShift() != null) existing.setTargetUserShift(req.getTargetUserShift());
        if (req.getSwapDate() != null) existing.setSwapDate(req.getSwapDate());
        if (req.getReason() != null) existing.setReason(req.getReason());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getApprovedBy() != null) existing.setApprovedBy(req.getApprovedBy());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật yêu cầu đổi ca thành công", shiftSwapRequestRepository.save(existing)));
    }

    @DeleteMapping("/shift-swaps/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShiftSwap(@PathVariable Long id) {
        ShiftSwapRequest existing = shiftSwapRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Shift swap request not found with id: " + id));
        existing.setIsDeleted(true);
        shiftSwapRequestRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa yêu cầu đổi ca thành công", null));
    }

    // --- DTO DEFINITIONS ---
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDTO {
        private Long id;
        private String positionCode;
        private String positionName;
        private String positionTitle;
        private BigDecimal baseSalary;
        private Long departmentId;
        private String departmentName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeContractDTO {
        private Long id;
        private String contractNumber;
        private String contractCode;
        private LocalDate startDate;
        private LocalDate endDate;
        private String contractType;
        private String status;
        private Long userId;
        private String employeeName;
        private String employeePhone;
        private Long positionId;
        private String positionName;
        private BigDecimal baseSalary;
        private String notes;
    }
}
