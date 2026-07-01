package org.example.storemanager.service.hrm.impl;

import org.example.storemanager.config.LogActivity;
import org.example.storemanager.dto.request.hrm.attendance.CheckInRequest;
import org.example.storemanager.dto.request.hrm.attendance.CheckOutRequest;
import org.example.storemanager.dto.request.hrm.attendance.CreateAttendanceRequest;
import org.example.storemanager.dto.request.hrm.attendance.UpdateAttendanceRequest;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.attendance.*;
import org.example.storemanager.entity.hrm.Attendance;
import org.example.storemanager.entity.system.User;
import org.example.storemanager.enums.hrm.AttendanceStatus;
import org.example.storemanager.exception.ResourceNotFoundException;
import org.example.storemanager.repository.hrm.AttendanceRepository;
import org.example.storemanager.repository.system.UserRepository;
import org.example.storemanager.service.hrm.AttendanceService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.example.storemanager.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime STANDARD_START = LocalTime.of(8, 0);

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    @Override
    @LogActivity(actionType = "CREATE", entityName = "Attendance", entityClass = Attendance.class)
    public CreateAttendanceResponse create(CreateAttendanceRequest request) {
        Attendance attendance = Attendance.builder()
                .user(resolveUser(request.getUserId()))
                .workDate(request.getWorkDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .gpsLocation(request.getGpsLocation())
                .status(request.getStatus())
                .build();

        attendance.setIsLocked(Boolean.FALSE.equals(request.getIsActive()));
        attendance.setIsDeleted(false);
        attendance.setCreatedBy(getCurrentUsername());

        return mapToCreateResponse(attendanceRepository.save(attendance));
    }

    @Override
    @LogActivity(actionType = "UPDATE", entityName = "Attendance", entityClass = Attendance.class)
    public UpdateAttendanceResponse update(Long id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        attendance.setUser(resolveUser(request.getUserId()));
        attendance.setWorkDate(request.getWorkDate());
        attendance.setCheckInTime(request.getCheckInTime());
        attendance.setCheckOutTime(request.getCheckOutTime());
        attendance.setGpsLocation(request.getGpsLocation());
        attendance.setStatus(request.getStatus());
        if (request.getIsActive() != null) {
            attendance.setIsLocked(!request.getIsActive());
        }
        attendance.setUpdatedBy(getCurrentUsername());

        return mapToUpdateResponse(attendanceRepository.save(attendance));
    }

    @Override
    @LogActivity(actionType = "DELETE", entityName = "Attendance", entityClass = Attendance.class)
    public DeleteAttendanceResponse delete(Long id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        requireInactiveBeforeDelete(attendance, "attendance-" + id);
        applySoftDelete(attendance);
        Attendance deleted = attendanceRepository.save(attendance);

        return DeleteAttendanceResponse.builder()
                .id(deleted.getId())
                .userId(deleted.getUser().getId())
                .isDeleted(deleted.getIsDeleted())
                .deletedAt(deleted.getDeletedAt())
                .deletedBy(deleted.getDeletedBy())
                .build();
    }

    @Override
    @LogActivity(actionType = "UPDATE_STATUS", entityName = "Attendance", entityClass = Attendance.class)
    public UpdateAttendanceResponse updateStatus(Long id, Boolean isActive) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        attendance.setIsLocked(!isActive);
        attendance.setUpdatedBy(getCurrentUsername());
        return mapToUpdateResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
        return mapToResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAll(String search, Boolean isActive, Long userId, String status,
                                           LocalDate workDateFrom, LocalDate workDateTo, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, parseSort(sort, "workDate"));
        return attendanceRepository.findAllFiltered(search, isActive, userId, status, workDateFrom, workDateTo, includeDeleted, pageable)
                .getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getPaginated(String search, Boolean isActive, Long userId, String status,
                                                         LocalDate workDateFrom, LocalDate workDateTo,
                                                         int page, int size, String sort, boolean includeDeleted) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, "workDate"));
        Page<Attendance> pageResult = attendanceRepository.findAllFiltered(search, isActive, userId, status, workDateFrom, workDateTo, includeDeleted, pageable);
        List<AttendanceResponse> content = pageResult.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PageResponse.<AttendanceResponse>builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @LogActivity(actionType = "CHECK_IN", entityName = "Attendance", entityClass = Attendance.class)
    public AttendanceResponse checkIn(CheckInRequest request) {
        User user = resolveUser(request.getUserId());
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findByUserIdAndWorkDateAndIsDeletedFalse(user.getId(), today)
                .orElseGet(() -> Attendance.builder()
                        .user(user)
                        .workDate(today)
                        .status(AttendanceStatus.ABSENT.name())
                        .build());

        if (attendance.getCheckInTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nhân viên đã check-in hôm nay");
        }

        attendance.setCheckInTime(now);
        String gps = request.getGpsLocation();
        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            gps = (gps != null ? gps + ";" : "") + "device:" + request.getDeviceId();
        }
        attendance.setGpsLocation(gps);
        attendance.setStatus(now.toLocalTime().isAfter(STANDARD_START)
                ? AttendanceStatus.LATE.name()
                : AttendanceStatus.PRESENT.name());

        if (attendance.getId() == null) {
            attendance.setIsDeleted(false);
            attendance.setCreatedBy(getCurrentUsername());
        } else {
            attendance.setUpdatedBy(getCurrentUsername());
        }

        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    @LogActivity(actionType = "CHECK_OUT", entityName = "Attendance", entityClass = Attendance.class)
    public AttendanceResponse checkOut(CheckOutRequest request) {
        User user = resolveUser(request.getUserId());
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByUserIdAndWorkDateAndIsDeletedFalse(user.getId(), today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chưa có bản ghi chấm công hôm nay"));

        if (attendance.getCheckInTime() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nhân viên chưa check-in");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nhân viên đã check-out hôm nay");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setUpdatedBy(getCurrentUsername());
        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceSheetResponse getMonthlySheet(Integer month, Integer year, Long userId, Long departmentId) {
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

        List<Attendance> records;
        if (userId != null) {
            records = attendanceRepository.findByUserAndDateRange(userId, fromDate, toDate);
        } else if (departmentId != null) {
            records = attendanceRepository.findByDepartmentAndDateRange(departmentId, fromDate, toDate);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần cung cấp userId hoặc departmentId");
        }

        int workDays = 0;
        int lateCount = 0;
        int earlyLeaveCount = 0;
        BigDecimal overtimeHours = BigDecimal.ZERO;

        for (Attendance attendance : records) {
            if (AttendanceStatus.PRESENT.name().equals(attendance.getStatus())
                    || AttendanceStatus.LATE.name().equals(attendance.getStatus())) {
                workDays++;
            }
            if (AttendanceStatus.LATE.name().equals(attendance.getStatus())) {
                lateCount++;
            }
            if (AttendanceStatus.HALF_DAY.name().equals(attendance.getStatus())) {
                earlyLeaveCount++;
            }
            if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
                long minutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                BigDecimal overtime = hours.subtract(BigDecimal.valueOf(8));
                if (overtime.compareTo(BigDecimal.ZERO) > 0) {
                    overtimeHours = overtimeHours.add(overtime);
                }
            }
        }

        List<AttendanceResponse> details = records.stream().map(this::mapToResponse).collect(Collectors.toList());

        return MonthlyAttendanceSheetResponse.builder()
                .month(month)
                .year(year)
                .userId(userId)
                .departmentId(departmentId)
                .workDays(workDays)
                .lateCount(lateCount)
                .earlyLeaveCount(earlyLeaveCount)
                .overtimeHours(overtimeHours)
                .details(details)
                .build();
    }

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .userName(attendance.getUser().getFullName())
                .workDate(attendance.getWorkDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .gpsLocation(attendance.getGpsLocation())
                .status(attendance.getStatus())
                .isActive(isActive(attendance.getIsLocked()))
                .isDeleted(attendance.getIsDeleted())
                .note(attendance.getNote())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }

    private CreateAttendanceResponse mapToCreateResponse(Attendance attendance) {
        return CreateAttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .workDate(attendance.getWorkDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .gpsLocation(attendance.getGpsLocation())
                .status(attendance.getStatus())
                .isActive(isActive(attendance.getIsLocked()))
                .createdAt(attendance.getCreatedAt())
                .createdBy(attendance.getCreatedBy())
                .build();
    }

    private UpdateAttendanceResponse mapToUpdateResponse(Attendance attendance) {
        return UpdateAttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUser().getId())
                .workDate(attendance.getWorkDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .gpsLocation(attendance.getGpsLocation())
                .status(attendance.getStatus())
                .isActive(isActive(attendance.getIsLocked()))
                .updatedAt(attendance.getUpdatedAt())
                .updatedBy(attendance.getUpdatedBy())
                .build();
    }

    // ---- Hrm support methods (inlined) ----
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

