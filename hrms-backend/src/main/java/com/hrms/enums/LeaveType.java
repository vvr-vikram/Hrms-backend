package com.hrms.enums;

public enum LeaveType {
    ANNUAL("Annual Leave"),
    SICK("Sick Leave"),
    CASUAL("Casual Leave"),
    UNPAID("Unpaid Leave"),
    MATERNITY("Maternity Leave"),
    PATERNITY("Paternity Leave"),
    BEREAVEMENT("Bereavement Leave");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}