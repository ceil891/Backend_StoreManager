package org.example.storemanager.service.hrm;

import org.example.storemanager.dto.request.hrm.leave.ApproveLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.CreateLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.UpdateLeaveRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.leave.CreateLeaveResponse;
import org.example.storemanager.dto.response.hrm.leave.DeleteLeaveResponse;
import org.example.storemanager.dto.response.hrm.leave.LeaveBalanceResponse;
import org.example.storemanager.dto.response.hrm.leave.LeaveRequestResponse;
import org.example.storemanager.dto.response.hrm.leave.UpdateLeaveResponse;

import java.util.List;

public interface LeaveRequestService {

    CreateLeaveResponse create(CreateLeaveRequest request);

    UpdateLeaveResponse update(Long id, UpdateLeaveRequest request);

    DeleteLeaveResponse delete(Long id);

    UpdateLeaveResponse updateStatus(Long id, Boolean isActive);

    LeaveRequestResponse getById(Long id);

    List<LeaveRequestResponse> getAll(String search, Boolean isActive, Long userId, String status, String sort, boolean includeDeleted);

    PageResponse<LeaveRequestResponse> getPaginated(String search, Boolean isActive, Long userId, String status, int page, int size, String sort, boolean includeDeleted);

    LeaveRequestResponse approve(Long id);

    LeaveRequestResponse reject(Long id, ApproveLeaveRequest request);

    LeaveRequestResponse cancel(Long id);

    List<LeaveRequestResponse> getUserLeaveHistory(Long userId);

    LeaveBalanceResponse getUserLeaveBalance(Long userId);

    List<LeaveRequestResponse> getPendingLeaves();
}
