package org.example.storemanager.controller.hrm;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.hrm.contract.*;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.hrm.contract.*;
import org.example.storemanager.service.hrm.EmployeeContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hrm/contracts")
public class EmployeeContractController {

    private final EmployeeContractService contractService;

    @Autowired
    public EmployeeContractController(EmployeeContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:create')")
    public ResponseEntity<ApiResponse<CreateEmployeeContractResponse>> create(@Valid @RequestBody CreateEmployeeContractRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(contractService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update')")
    public ResponseEntity<ApiResponse<UpdateEmployeeContractResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateEmployeeContractRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hợp đồng thành công", contractService.update(id, request)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update-status')")
    public ResponseEntity<ApiResponse<UpdateEmployeeContractResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", contractService.updateStatus(id, isActive)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:delete')")
    public ResponseEntity<ApiResponse<DeleteEmployeeContractResponse>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa hợp đồng thành công", contractService.delete(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    contractService.getPaginated(search, isActive, userId, status, page, size, sort, includeDeleted)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                contractService.getAll(search, isActive, userId, status, sort, includeDeleted)));
    }

    // ---- User-specific contract endpoints ----

    @GetMapping("/users/{userId}/contracts")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<java.util.List<EmployeeContractResponse>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getByUserId(userId)));
    }

    @GetMapping("/users/{userId}/contracts/current")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> getCurrentContract(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getCurrentContract(userId)));
    }

    @GetMapping("/users/{userId}/contracts/history")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<java.util.List<ContractHistoryResponse>>> getContractHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getContractHistory(userId)));
    }

    // ---- Contract operation endpoints ----

    @PostMapping("/{id}/renew")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update')")
    public ResponseEntity<ApiResponse<RenewContractResponse>> renewContract(
            @PathVariable Long id, @Valid @RequestBody RenewContractRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Gia hạn hợp đồng thành công", contractService.renewContract(id, request)));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update')")
    public ResponseEntity<ApiResponse<TerminateContractResponse>> terminateContract(
            @PathVariable Long id, @Valid @RequestBody TerminateContractRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Chấm dứt hợp đồng thành công", contractService.terminateContract(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update-status')")
    public ResponseEntity<ApiResponse<UpdateEmployeeContractResponse>> updateStatusPatch(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", contractService.updateStatus(id, isActive)));
    }

    @PatchMapping("/{id}/upload-file")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:update')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> uploadContractFile(
            @PathVariable Long id, @Valid @RequestBody UploadContractFileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Upload file hợp đồng thành công", contractService.uploadContractFile(id, request)));
    }

    @GetMapping("/expiring")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:contract:view')")
    public ResponseEntity<ApiResponse<java.util.List<ExpiringContractResponse>>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(contractService.getExpiringContracts(days)));
    }
}
