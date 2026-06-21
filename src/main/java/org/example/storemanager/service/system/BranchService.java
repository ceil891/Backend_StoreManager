package org.example.storemanager.service.system;

import org.example.storemanager.dto.request.system.branch.CreateBranchRequest;
import org.example.storemanager.dto.request.system.branch.UpdateBranchRequest;
import org.example.storemanager.dto.response.system.branch.*;
import org.example.storemanager.dto.response.common.PageResponse;

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
