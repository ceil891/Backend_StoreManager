package org.example.storemanager.modules.partnerarea.repository;

import org.example.storemanager.modules.partnerarea.entity.Area;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("partnerAreaRepository")
public interface AreaRepository extends JpaRepository<Area, Long> {

    // 1. Kiểm tra tồn tại theo code (dùng để validate đầu vào)
    boolean existsByAreaCode(String areaCode);

    // 2. Tìm theo cấp độ - Có dùng EntityGraph để load danh sách children ngay từ đầu
    @EntityGraph(attributePaths = {"children"})
    List<Area> findByLevel(Integer level);

    // 3. Tìm danh sách con theo ID cha (tối ưu cho API Tree và Children)
    List<Area> findByParentId(Long parentId);

    // 4. Tìm theo loại (ví dụ: tìm tất cả PROVINCE hoặc WARD)
    List<Area> findByType(String type);

    // 5. Tìm theo tên và cấp độ (dùng trong hàm sync để tránh tạo trùng bản ghi)
    List<Area> findByAreaNameAndLevel(String areaName, Integer level);

    // 6. Truy vấn lấy toàn bộ cây từ gốc (Root)
    @Query("SELECT DISTINCT a FROM PartnerArea a LEFT JOIN FETCH a.children WHERE a.level = 1")
    List<Area> findRootAreasWithChildren();
}