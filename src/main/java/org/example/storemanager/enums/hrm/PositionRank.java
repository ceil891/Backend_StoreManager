package org.example.storemanager.enums.hrm;

public enum PositionRank {
    INTERN("Thực tập sinh"),
    STAFF("Nhân viên"),
    SENIOR_STAFF("Nhân viên cấp cao"),
    TEAM_LEAD("Trưởng nhóm"),
    SUPERVISOR("Giám sát viên"),
    MANAGER("Quản lý"),
    SENIOR_MANAGER("Quản lý cấp cao"),
    DIRECTOR("Giám đốc"),
    EXECUTIVE("Giám đốc điều hành");

    private final String displayName;

    PositionRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
