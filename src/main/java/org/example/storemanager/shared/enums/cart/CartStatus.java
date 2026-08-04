package org.example.storemanager.shared.enums.cart;

public enum CartStatus {
    ACTIVE,    // Đang hoạt động
    MERGED,    // Đã gộp vào User cart sau khi đăng nhập
    ORDERED,   // Đã checkout → tạo SaleOrder
    EXPIRED    // Scheduler mark sau TTL hết hạn; xóa thật sau 30 ngày
}
