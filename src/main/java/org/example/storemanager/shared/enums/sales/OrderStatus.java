package org.example.storemanager.shared.enums.sales;

public enum OrderStatus {
    DRAFT,      // Nháp
    PENDING,    // Chờ xử lý
    CONFIRMED,  // Đã xác nhận
    DELIVERING, // Đang giao
    COMPLETED,  // Hoàn tất
    CANCELLED,  // Đã hủy
    RETURNED    // Trả hàng
}
