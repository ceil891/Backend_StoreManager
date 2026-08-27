package org.example.storemanager.modules.hrm.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.hrm.entity.*;
import org.example.storemanager.modules.hrm.repository.*;
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
                        .contractCode(c.getContractNumber())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .contractType(c.getContractType())
                        .status(c.getStatus())
                        .userId(c.getUser() != null ? c.getUser().getId() : null)
                        .employeeName(c.getUser() != null ? c.getUser().getFullName() : "")
                        .employeePhone(c.getUser() != null ? c.getUser().getPhone() : "")
                        .positionId(c.getPosition() != null ? c.getPosition().getId() : null)
                        .positionName(c.getPosition() != null ? c.getPosition().getPositionName() : "")
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/contracts")
    public ResponseEntity<ApiResponse<EmployeeContract>> createContract(@RequestBody EmployeeContract req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(employeeContractRepository.save(req)));
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
        return ResponseEntity.status(201).body(ApiResponse.created(leaveRequestRepository.save(req)));
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
        return ResponseEntity.status(201).body(ApiResponse.created(kpiRecordRepository.save(req)));
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
    }
}
