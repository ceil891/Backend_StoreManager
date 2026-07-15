package org.example.storemanager.service.hrm.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.hrm.leave.ApproveLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.CreateLeaveRequest;
import org.example.storemanager.dto.request.hrm.leave.UpdateLeaveRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.leave.CreateLeaveResponse;
import org.example.storemanager.dto.response.hrm.leave.DeleteLeaveResponse;
import org.example.storemanager.dto.response.hrm.leave.LeaveBalanceResponse;
import org.example.storemanager.dto.response.hrm.leave.LeaveRequestResponse;
import org.example.storemanager.dto.response.hrm.leave.UpdateLeaveResponse;
import org.example.storemanager.entity.BaseEntity;
import org.example.storemanager.entity.hrm.LeaveRequest;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.hrm.LeaveStatus;
import org.example.storemanager.enums.hrm.LeaveTypeEnum;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.hrm.LeaveRequestRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.hrm.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public CreateLeaveResponse create(CreateLeaveRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu và ngày kết thúc không được để trống");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = request.getStartDate();
        if (startDate.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được trong quá khứ");
        }
        if (startDate.isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải >= ngày bắt đầu");
        }

        Integer numberOfDays = calculateLeaveDate(startDate, request.getEndDate());

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(startDate)
                .endDate(request.getEndDate())
                .leaveType(requireEnumName(request.getLeaveType(), LeaveTypeEnum.class, "Loại nghỉ phép"))
                .reason(request.getReason())
                .numberOfDays(numberOfDays)
                .attachmentPath(request.getAttachmentPath())
                .status(LeaveStatus.PENDING.name())
                .user(resolveUser(request.getUserId()))
                .build();

        leaveRequest.setIsLocked(Boolean.FALSE.equals(request.getIsActive()));
        leaveRequest.setIsDeleted(false);
        leaveRequest.setCreatedBy(getCurrentUsername());

        return mapToCreateResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public UpdateLeaveResponse update(Long id, UpdateLeaveRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (!LeaveStatus.PENDING.name().equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chỉ có thể chỉnh sửa đơn ở trạng thái PENDING");
        }

        LocalDate today = LocalDate.now();
        LocalDate finalStartDate = leaveRequest.getStartDate();
        LocalDate finalEndDate = leaveRequest.getEndDate();

        if (request.getStartDate() != null) {
            if (request.getStartDate().isBefore(today)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu không được trong quá khứ");
            }
            finalStartDate = request.getStartDate();
            leaveRequest.setStartDate(finalStartDate);
        }
        if (request.getEndDate() != null) {
            finalEndDate = request.getEndDate();
            leaveRequest.setEndDate(finalEndDate);
        }
        if (request.getStartDate() != null || request.getEndDate() != null) {
            if (finalStartDate != null && finalEndDate != null && finalStartDate.isAfter(finalEndDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải >= ngày bắt đầu");
            }
            leaveRequest.setNumberOfDays(calculateLeaveDate(finalStartDate, finalEndDate));
        }
        if (request.getLeaveType() != null) {
            leaveRequest.setLeaveType(requireEnumName(request.getLeaveType(), LeaveTypeEnum.class, "Loại nghỉ phép"));
        }
        if (request.getReason() != null) {
            leaveRequest.setReason(request.getReason());
        }
        if (request.getAttachmentPath() != null) {
            leaveRequest.setAttachmentPath(request.getAttachmentPath());
        }
        if (request.getUserId() != null) {
            leaveRequest.setUser(resolveUser(request.getUserId()));
        }
        if (request.getIsActive() != null) {
            leaveRequest.setIsLocked(!request.getIsActive());
        }
        leaveRequest.setUpdatedBy(getCurrentUsername());

        return mapToUpdateResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public DeleteLeaveResponse delete(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        requireInactiveBeforeDelete(leaveRequest, "leave-" + id);
        applySoftDelete(leaveRequest);
        LeaveRequest deleted = leaveRequestRepository.save(leaveRequest);

        return DeleteLeaveResponse.builder()
                .id(deleted.getId())
                .userId(deleted.getUser().getId())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public UpdateLeaveResponse updateStatus(Long id, Boolean isActive) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        leaveRequest.setIsLocked(!isActive);
        leaveRequest.setUpdatedBy(getCurrentUsername());
        return mapToUpdateResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveRequestResponse getById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return mapToResponse(leaveRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getAll(String search, Boolean isActive, Long userId, String status, String sort, boolean includeDeleted) {
        String normalizedStatus = parseOptionalEnumName(status, LeaveStatus.class, "Trạng thái đơn nghỉ phép");
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, parseSort(sort, "startDate"));
        return leaveRequestRepository.findAllFiltered(search, isActive, userId, normalizedStatus, includeDeleted, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getPaginated(String search, Boolean isActive, Long userId, String status, int page, int size, String sort, boolean includeDeleted) {
        String normalizedStatus = parseOptionalEnumName(status, LeaveStatus.class, "Trạng thái đơn nghỉ phép");
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, "startDate"));
        Page<LeaveRequest> pageResult = leaveRequestRepository.findAllFiltered(search, isActive, userId, normalizedStatus, includeDeleted, pageable);
        List<LeaveRequestResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PageResponse.<LeaveRequestResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @LogActivity(actionType = "APPROVE", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public LeaveRequestResponse approve(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (!LeaveStatus.PENDING.name().equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn nghỉ phép đã được xử lý");
        }

        User approver = requireNonSelfApprover(leaveRequest);
        leaveRequest.setStatus(LeaveStatus.APPROVED.name());
        leaveRequest.setApprovalDate(LocalDateTime.now());
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setRejectionReason(null);
        leaveRequest.setUpdatedBy(getCurrentUsername());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @LogActivity(actionType = "REJECT", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public LeaveRequestResponse reject(Long id, ApproveLeaveRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (!LeaveStatus.PENDING.name().equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn nghỉ phép đã được xử lý");
        }

        User approver = requireNonSelfApprover(leaveRequest);
        leaveRequest.setStatus(LeaveStatus.REJECTED.name());
        leaveRequest.setApprovalDate(LocalDateTime.now());
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setRejectionReason(request != null ? request.getRejectionReason() : null);
        leaveRequest.setUpdatedBy(getCurrentUsername());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @LogActivity(actionType = "CANCEL", entityName = "LeaveRequest", entityClass = LeaveRequest.class)
    public LeaveRequestResponse cancel(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (!LeaveStatus.PENDING.name().equals(leaveRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chỉ có thể hủy đơn ở trạng thái PENDING");
        }

        User approver = requireNonSelfApprover(leaveRequest);
        leaveRequest.setStatus(LeaveStatus.CANCELLED.name());
        leaveRequest.setApprovalDate(LocalDateTime.now());
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setRejectionReason(null);
        leaveRequest.setUpdatedBy(getCurrentUsername());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getUserLeaveHistory(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return leaveRequestRepository.findUserLeaveHistory(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getUserLeaveBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<LeaveRequest> allLeaves = leaveRequestRepository.findUserLeaveHistory(userId);
        
        int totalLeaveAllowed = 12;
        int usedLeaves = 0;
        int pendingLeaves = 0;

        for (LeaveRequest leave : allLeaves) {
            if (LeaveStatus.APPROVED.name().equals(leave.getStatus())) {
                usedLeaves += leave.getNumberOfDays() != null ? leave.getNumberOfDays() : 0;
            } else if (LeaveStatus.PENDING.name().equals(leave.getStatus())) {
                pendingLeaves += leave.getNumberOfDays() != null ? leave.getNumberOfDays() : 0;
            }
        }

        return LeaveBalanceResponse.builder()
                .userId(userId)
                .userName(user.getFullName())
                .totalLeaveAllowed(totalLeaveAllowed)
                .usedLeaveDays(usedLeaves)
                .remainingLeaveDays(totalLeaveAllowed - usedLeaves)
                .pendingLeaveDays(pendingLeaves)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingLeaves() {
        return leaveRequestRepository.findPendingLeaves().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Integer calculateLeaveDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải >= ngày bắt đầu");
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private User requireNonSelfApprover(LeaveRequest leaveRequest) {
        User approver = userRepository.findByUsername(getCurrentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người duyệt"));
        Long requesterId = leaveRequest.getUser() != null ? leaveRequest.getUser().getId() : null;
        if (requesterId != null && requesterId.equals(approver.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nhân viên không được tự duyệt/từ chối/hủy đơn của chính mình");
        }
        return approver;
    }

    private LeaveRequestResponse mapToResponse(LeaveRequest leaveRequest) {
        return LeaveRequestResponse.builder()
                .id(leaveRequest.getId())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .leaveType(leaveRequest.getLeaveType())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .numberOfDays(leaveRequest.getNumberOfDays())
                .approvalDate(leaveRequest.getApprovalDate())
                .rejectionReason(leaveRequest.getRejectionReason())
                .attachmentPath(leaveRequest.getAttachmentPath())
                .userId(leaveRequest.getUser().getId())
                .userName(leaveRequest.getUser().getFullName())
                .approvedById(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getId() : null)
                .approvedByName(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getFullName() : null)
                .isActive(isActive(leaveRequest.getIsLocked()))
                .isDeleted(leaveRequest.getIsDeleted())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    private CreateLeaveResponse mapToCreateResponse(LeaveRequest leaveRequest) {
        return CreateLeaveResponse.builder()
                .id(leaveRequest.getId())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .leaveType(leaveRequest.getLeaveType())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .numberOfDays(leaveRequest.getNumberOfDays())
                .attachmentPath(leaveRequest.getAttachmentPath())
                .userId(leaveRequest.getUser().getId())
                .isActive(isActive(leaveRequest.getIsLocked()))
                .createdAt(leaveRequest.getCreatedAt())
                .createdBy(leaveRequest.getCreatedBy())
                .build();
    }

    private UpdateLeaveResponse mapToUpdateResponse(LeaveRequest leaveRequest) {
        return UpdateLeaveResponse.builder()
                .id(leaveRequest.getId())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .leaveType(leaveRequest.getLeaveType())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .numberOfDays(leaveRequest.getNumberOfDays())
                .approvalDate(leaveRequest.getApprovalDate())
                .rejectionReason(leaveRequest.getRejectionReason())
                .attachmentPath(leaveRequest.getAttachmentPath())
                .userId(leaveRequest.getUser().getId())
                .isActive(isActive(leaveRequest.getIsLocked()))
                .updatedAt(leaveRequest.getUpdatedAt())
                .updatedBy(leaveRequest.getUpdatedBy())
                .build();
    }

    // ---- Inlined Hrm support methods ----
    private static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Người dùng chưa đăng nhập hoặc token không hợp lệ");
    }

    private static Sort parseSort(String sortParam, String defaultProperty) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(defaultProperty).descending();
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    private static boolean isActive(Boolean isLocked) {
        return !Boolean.TRUE.equals(isLocked);
    }

    private static void applySoftDelete(BaseEntity entity) {
        String username = getCurrentUsername();
        entity.setIsDeleted(true);
        entity.setIsLocked(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(username);
        entity.setUpdatedBy(username);
    }

    private static void requireInactiveBeforeDelete(BaseEntity entity, String label) {
        if (isActive(entity.getIsLocked())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa '" + label + "' vì bản ghi vẫn đang HOẠT ĐỘNG. Vui lòng tắt hoạt động trước."
            );
        }
    }

    private static <E extends Enum<E>> String requireEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldLabel + " không được để trống");
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldLabel + " không hợp lệ. Giá trị cho phép: " + formatAllowedEnumValues(enumClass)
            );
        }
    }

    private static <E extends Enum<E>> String parseOptionalEnumName(String value, Class<E> enumClass, String fieldLabel) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireEnumName(value, enumClass, fieldLabel);
    }

    private static <E extends Enum<E>> String formatAllowedEnumValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
