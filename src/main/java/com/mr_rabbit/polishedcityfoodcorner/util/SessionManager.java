package com.mr_rabbit.polishedcityfoodcorner.util;


public class SessionManager {

    private static String employeeCode;
    private static String employeeName;
    private static String role;

    private SessionManager() {
    }

    public static void login(String employeeCode, String employeeName, String role) {
        SessionManager.employeeCode = employeeCode;
        SessionManager.employeeName = employeeName;
        SessionManager.role = role;
    }

    public static void clear() {
        employeeCode = null;
        employeeName = null;
        role = null;
    }

    public static String getEmployeeCode() { return employeeCode; }
    public static String getEmployeeName() { return employeeName; }
    public static String getRole() { return role; }
}
