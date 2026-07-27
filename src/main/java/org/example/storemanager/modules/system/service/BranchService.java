package org.example.storemanager.modules.system.service;

import org.example.storemanager.modules.system.dto.request.branch.CreateBranchRequest;
import org.example.storemanager.modules.system.dto.request.branch.UpdateBranchRequest;
import org.example.storemanager.modules.system.dto.response.branch.*;
import org.example.storemanager.modules.common.dto.response.PageResponse;

import java.util.List;

public interface BranchService {
    CreateBranchResponse createBranch(CreateBranchRequest request);

    UpdateBranchResponse updateBranch(Long id, UpdateBranchRequest request);

    DeleteBranchResponse deleteBranch(Long id);

    UpdateBranchResponse updateStatus(Long id, Boolean isActive);

    BranchResponse getBranchById(Long id);

    List<MapBranchResponse> getAllBranches(
            String search,
            Boolean isActive,
            String sort,
            boolean includeDeleted);

    PageResponse<MapBranchResponse> getBranchesPaginated(
            String search,
            Boolean isActive,
            int page,
            int size,
            String sort,
            boolean includeDeleted);
}
