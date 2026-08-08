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
    private final EmployeeContractRepository employeeContractRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final KpiRecordRepository kpiRecordRepository;

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
    public ResponseEntity<ApiResponse<Position>> createPosition(@RequestBody Position req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(positionRepository.save(req)));
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
