package org.example.storemanager.enums.account;

public enum AccountType {
    ASSET("Tài sản"),
    LIABILITY("Nợ phải trả"),
    EQUITY("Vốn chủ sở hữu"),
    REVENUE("Doanh thu"),
    EXPENSE("Chi phí");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
