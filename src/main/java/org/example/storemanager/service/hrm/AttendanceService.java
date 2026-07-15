package org.example.storemanager.service.hrm;

import org.example.storemanager.dto.request.advancedaccounting.ChartOfAccount.AdjustAttendanceRequest;
import org.example.storemanager.dto.request.hrm.attendance.*;
import org.example.storemanager.dto.response.common.PageResponse;
import org.example.storemanager.dto.response.hrm.attendance.*;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    CreateAttendanceResponse create(CreateAttendanceRequest request);

    UpdateAttendanceResponse update(Long id, UpdateAttendanceRequest request);

    DeleteAttendanceResponse delete(Long id);

    UpdateAttendanceResponse updateStatus(Long id, Boolean isActive);

    AttendanceResponse getById(Long id);

    List<AttendanceResponse> getAll(String search, Boolean isActive, Long userId, String status,
                                  LocalDate workDateFrom, LocalDate workDateTo, String sort, boolean includeDeleted);

    PageResponse<AttendanceResponse> getPaginated(String search, Boolean isActive, Long userId, String status,
                                                  LocalDate workDateFrom, LocalDate workDateTo,
                                                  int page, int size, String sort, boolean includeDeleted);

    AttendanceResponse checkIn(CheckInRequest request);

    AttendanceResponse checkOut(CheckOutRequest request);

    MonthlyAttendanceSheetResponse getMonthlySheet(Integer month, Integer year, Long userId, Long departmentId);

    AttendanceResponse adjustAttendance(AdjustAttendanceRequest request);
}
