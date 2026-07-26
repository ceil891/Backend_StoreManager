package org.example.storemanager.controller.hrm;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.entity.hrm.*;
import org.example.storemanager.repository.hrm.*;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<List<Position>>> getAllPositions() {
        return ResponseEntity.ok(ApiResponse.ok(positionRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/positions")
    public ResponseEntity<ApiResponse<Position>> createPosition(@RequestBody Position req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(positionRepository.save(req)));
    }

    // --- LABOR CONTRACTS ---
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<List<EmployeeContract>>> getAllContracts() {
        return ResponseEntity.ok(ApiResponse.ok(employeeContractRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/contracts")
    public ResponseEntity<ApiResponse<EmployeeContract>> createContract(@RequestBody EmployeeContract req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(employeeContractRepository.save(req)));
    }

    // --- LEAVE REQUESTS ---
    @GetMapping("/leave-requests")
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
    public ResponseEntity<ApiResponse<List<KpiRecord>>> getAllKpis() {
        return ResponseEntity.ok(ApiResponse.ok(kpiRecordRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/kpis")
    public ResponseEntity<ApiResponse<KpiRecord>> createKpi(@RequestBody KpiRecord req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(kpiRecordRepository.save(req)));
    }
}
