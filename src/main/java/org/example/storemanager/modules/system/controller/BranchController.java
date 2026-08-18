package org.example.storemanager.modules.system.controller;

import jakarta.validation.Valid;
import org.example.storemanager.modules.system.dto.request.branch.CreateBranchRequest;
import org.example.storemanager.modules.system.dto.request.branch.UpdateBranchRequest;
import org.example.storemanager.modules.system.dto.response.branch.CreateBranchResponse;
import org.example.storemanager.modules.system.dto.response.branch.DeleteBranchResponse;
import org.example.storemanager.modules.system.dto.response.branch.UpdateBranchResponse;
import org.example.storemanager.modules.system.dto.response.branch.BranchResponse;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.system.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('system:branch:create')")
    public ResponseEntity<ApiResponse<CreateBranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        CreateBranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(201).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:branch:update')")
    public ResponseEntity<ApiResponse<UpdateBranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        UpdateBranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chi nhánh thành công", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('system:branch:update-status')")
    public ResponseEntity<ApiResponse<UpdateBranchResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        UpdateBranchResponse response = branchService.updateStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('system:branch:delete')")
    public ResponseEntity<ApiResponse<DeleteBranchResponse>> deleteBranch(@PathVariable Long id) {
        DeleteBranchResponse response = branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa chi nhánh thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> getBranches(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "branchName,asc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                branchService.getBranchesPaginated(search, isActive, page, size, sort, includeDeleted)));
        } else {
            return ResponseEntity.ok(ApiResponse.ok(
                branchService.getAllBranches(search, isActive, sort, includeDeleted)));
        }
    }
}
