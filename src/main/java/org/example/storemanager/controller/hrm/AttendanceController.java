package org.example.storemanager.controller.hrm;

import jakarta.validation.Valid;
import org.example.storemanager.dto.request.hrm.attendance.*;
import org.example.storemanager.dto.response.common.ApiResponse;
import org.example.storemanager.dto.response.hrm.attendance.*;
import org.example.storemanager.service.hrm.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/hrm/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Tạo mới một bản ghi chấm công
     *
     * @param request Thông tin chi tiết bản ghi chấm công cần tạo
     * @return Bản ghi chấm công vừa được tạo (HTTP 201 Created)
     */
    @PostMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:create')")
    public ResponseEntity<ApiResponse<CreateAttendanceResponse>> create(@Valid @RequestBody CreateAttendanceRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(attendanceService.create(request)));
    }

    /**
     * Ghi nhận nhân viên check-in (bắt đầu ca làm việc)
     *
     * @param request Thông tin check-in bao gồm ID nhân viên và thời gian
     * @return Bản ghi chấm công với thời gian check-in
     */
    @PostMapping("/check-in")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:check-in')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Check-in thành công", attendanceService.checkIn(request)));
    }

    /**
     * Ghi nhận nhân viên check-out (kết thúc ca làm việc)
     *
     * @param request Thông tin check-out bao gồm ID nhân viên và thời gian
     * @return Bản ghi chấm công với thời gian check-out
     */
    @PostMapping("/check-out")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:check-out')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Check-out thành công", attendanceService.checkOut(request)));
    }

    /**
     * Lấy bảng chấm công hàng tháng
     * Cho phép lọc theo nhân viên hoặc phòng ban
     *
     * @param month Tháng cần lấy (1-12)
     * @param year Năm cần lấy
     * @param userId ID nhân viên (tùy chọn) - nếu không truyền sẽ lấy tất cả
     * @param departmentId ID phòng ban (tùy chọn) - nếu không truyền sẽ lấy tất cả
     * @return Bảng chấm công chi tiết của tháng và năm được chỉ định
     */
    @GetMapping("/monthly-sheet")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:view')")
    public ResponseEntity<ApiResponse<MonthlyAttendanceSheetResponse>> monthlySheet(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getMonthlySheet(month, year, userId, departmentId)));
    }

    /**
     * Gửi yêu cầu chỉnh sửa công (điều chỉnh bản ghi chấm công)
     * Dùng khi cần sửa lại thời gian check-in/check-out hoặc trạng thái công
     *
     * @param request Thông tin yêu cầu chỉnh sửa
     * @return Bản ghi chấm công đã được cập nhật
     *
    @PostMapping("/requests/adjust")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:adjust')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> adjust(@Valid @RequestBody AdjustAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Gửi yêu cầu chỉnh sửa công thành công", attendanceService.adjustAttendance(request)));
    }*/

    /**
     * Cập nhật thông tin bản ghi chấm công
     * Cho phép sửa lại chi tiết bản ghi chấm công theo ID
     *
     * @param id ID của bản ghi chấm công cần cập nhật
     * @param request Thông tin mới của bản ghi
     * @return Bản ghi chấm công đã được cập nhật
     */
    @PutMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:update')")
    public ResponseEntity<ApiResponse<UpdateAttendanceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật chấm công thành công", attendanceService.update(id, request)));
    }

    /**
     * Cập nhật trạng thái hoạt động của bản ghi chấm công
     * (kích hoạt hoặc vô hiệu hóa bản ghi)
     *
     * @param id ID của bản ghi chấm công
     * @param isActive true để kích hoạt, false để vô hiệu hóa
     * @return Bản ghi chấm công với trạng thái đã được cập nhật
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:update-status')")
    public ResponseEntity<ApiResponse<UpdateAttendanceResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", attendanceService.updateStatus(id, isActive)));
    }

    /**
     * Xóa một bản ghi chấm công
     *
     * @param id ID của bản ghi cần xóa
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:delete')")
    public ResponseEntity<ApiResponse<DeleteAttendanceResponse>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa chấm công thành công", attendanceService.delete(id)));
    }

    /**
     * Lấy chi tiết một bản ghi chấm công theo ID
     *
     * @param id ID của bản ghi chấm công cần lấy
     * @return Thông tin chi tiết bản ghi chấm công
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:view')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getById(id)));
    }

    /**
     * Danh sách các bản ghi chấm công với các lọc nâng cao
     * Hỗ trợ tìm kiếm, lọc theo nhiều tiêu chí và phân trang
     *
     * @param search Từ khóa tìm kiếm (tên nhân viên, v.v.)
     * @param isActive Lọc theo trạng thái hoạt động
     * @param userId Lọc theo ID nhân viên
     * @param status Lọc theo trạng thái công (Present, Absent, v.v.)
     * @param workDateFrom Ngày bắt đầu (từ ngày)
     * @param workDateTo Ngày kết thúc (đến ngày)
     * @param includeDeleted Có bao gồm các bản ghi đã xóa hay không
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng bản ghi trên một trang
     * @param sort Sắp xếp theo trường nào (ví dụ: workDate,desc)
     * @return Danh sách bản ghi chấm công hoặc danh sách phân trang
     */
    @GetMapping
    @PreAuthorize("@securityEvaluator.hasPermission('hrm:attendance:view')")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate workDateFrom,
            @RequestParam(required = false) LocalDate workDateTo,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "workDate,desc") String sort) {
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    attendanceService.getPaginated(search, isActive, userId, status, workDateFrom, workDateTo, page, size, sort, includeDeleted)));
        }
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.getAll(search, isActive, userId, status, workDateFrom, workDateTo, sort, includeDeleted)));
    }
}
