package com.hrms.enums;

public enum UserRole {
    ROLE_ADMIN("Administrator"),
    ROLE_HR("HR Manager"),
    ROLE_MANAGER("Department Manager"),
    ROLE_EMPLOYEE("Employee");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role) || 
                userRole.getDisplayName().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        return ROLE_EMPLOYEE;
    }
}