package org.example.storemanager.enums.inventory;

public enum ReturnToSupplierStatus {
    PENDING_SUPPLIER_APPROVAL,  // Chờ nhà cung cấp xác nhận
    APPROVED_CREDIT_NOTE,       // NCC đã duyệt – kích hoạt trừ tồn kho
    REJECTED,                   // Từ chối
    COMPLETED                   // Hoàn tất
}
