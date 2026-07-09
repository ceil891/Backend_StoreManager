package org.example.storemanager.enums.hrm;

public enum ManagementStatus {
    NON_MANAGER("Không quản lý"),
    MANAGE_STAFF("Quản lý nhân viên"),
    MANAGE_TEAM("Quản lý nhóm"),
    MANAGE_DEPARTMENT("Quản lý phòng ban"),
    MANAGE_DIVISION("Quản lý bộ phận");

    private final String displayName;

    ManagementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
