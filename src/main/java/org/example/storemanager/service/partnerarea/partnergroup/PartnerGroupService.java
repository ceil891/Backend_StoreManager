package org.example.storemanager.service.partnerarea.partnergroup;

import org.example.storemanager.dto.request.partnerarea.partnergroup.PartnerGroupRequest;
import org.example.storemanager.dto.response.partnerarea.partnergroup.CreatePartnerGroupResponse;
import org.example.storemanager.dto.response.partnerarea.partnergroup.DeletePartnerGroupResponse;
import org.example.storemanager.dto.response.partnerarea.partnergroup.PartnerGroupDetailResponse;
import org.example.storemanager.dto.response.partnerarea.partnergroup.PartnerGroupListResponse;
import org.example.storemanager.dto.response.partnerarea.partnergroup.UpdatePartnerGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartnerGroupService {

    // Tạo mới trả về CreateResponse
    CreatePartnerGroupResponse create(PartnerGroupRequest req);

    // Cập nhật trả về UpdateResponse
    UpdatePartnerGroupResponse update(Long id, PartnerGroupRequest req);

    // Xóa trả về DeleteResponse (thông tin người xóa, thời gian xóa)
    DeletePartnerGroupResponse delete(Long id);

    // Xem chi tiết (đầy đủ Audit)
    PartnerGroupDetailResponse getById(Long id);

    // Toggle trạng thái
    UpdatePartnerGroupResponse toggleStatus(Long id);

    // Tìm kiếm có điều kiện (Dùng ListResponse cho nhẹ)
    Page<PartnerGroupListResponse> findWithFilter(Pageable pageable, String groupCode, String type, String groupName, Boolean isActive);

    // Danh sách mặc định
    Page<PartnerGroupListResponse> getAll(Pageable pageable, String search, String type);

    void addMemberToGroup(Long groupId, Long memberId, String type);
}