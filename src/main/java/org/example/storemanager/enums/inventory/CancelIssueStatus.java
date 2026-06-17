package org.example.storemanager.enums.inventory;

public enum CancelIssueStatus {
    PENDING_APPROVAL,   // Chờ phê duyệt
    APPROVED,           // Đã phê duyệt – kích hoạt trừ tồn kho
    REJECTED,           // Từ chối
    PROCESSED           // Đã xử lý xong
}
