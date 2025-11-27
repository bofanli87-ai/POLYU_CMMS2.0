package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Authentication service class, provides user authentication and authorization related functions
 */
public class AuthService extends BaseService {
    private static AuthService instance;
    private static String currentUsername = null;
    private static Integer currentUserId = null;
    private static String currentRole = null;

    // Static initialization block - used for default initialization of administrator identity
    static {
        // Default set administrator identity
        currentUserId = 1;
        currentRole = "管理员";
        currentUsername = "admin";
    }

    // Singleton pattern
    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * User login verification - simplified version
     * @param username Username (staff number)
     * @param password Password (fixed as 123)
     * @return Whether verification is successful
     * @throws SQLException SQL exception
     */
    public boolean login(String username, String password) throws SQLException {
        // Simplified login: username is staff number, password fixed as 123
        if (!"123".equals(password)) {
            return false;
        }

        // Query staff table to check if employee exists
        String sql = "SELECT staff_id, role_id FROM staff WHERE staff_number = ? AND active_flag = 'Y'";
        List<Map<String, Object>> results = executeQuery(sql, username);
        if (!results.isEmpty()) {
            currentUsername = username;
            // Safely get staff_id and role_id, avoid null pointer exception
            Map<String, Object> result = results.get(0);
            if (result != null) {
                currentUserId = result.get("staff_id") instanceof Integer ? (Integer) result.get("staff_id") : null;
                // Simply set role name based on role_id
                int roleId = getIntValue(result.get("role_id"), 0);
                switch (roleId) {
                    case 1:
                        currentRole = "管理员";
                        break;
                    case 2:
                        currentRole = "主管";
                        break;
                    default:
                        currentRole = "普通员工";
                        break;
                }
            } else {
                // If result is null, set default values
                currentUserId = null;
                currentRole = "普通员工";
            }
            return true;
        }
        return false;
    }

    /**
     * Get user role - simplified version
     * @param username Username (staff number)
     * @return User role
     * @throws SQLException SQL exception
     */
    public String getUserRole(String username) throws SQLException {
        // If already logged in, directly return current role
        if (currentUsername != null && currentUsername.equals(username)) {
            return currentRole;
        }

        // Query staff table to get role information
        String sql = "SELECT role_id FROM staff WHERE staff_number = ? AND active_flag = 'Y'";
        List<Map<String, Object>> results = executeQuery(sql, username);
        if (results.isEmpty()) {
            return "普通员工";
        }

        // Ensure using getIntValue method for safe type conversion
        Map<String, Object> result = results.get(0);
        int roleId = getIntValue(result != null ? result.get("role_id") : null, 0);
        switch (roleId) {
            case 1:
                return "管理员";
            case 2:
                return "主管";
            default:
                return "普通员工";
        }
    }

    /**
     * Check if user has specific permission - simplified version
     * @param username Username
     * @param permission Permission code
     * @return Whether has permission
     * @throws SQLException SQL exception
     */
    public boolean hasPermission(String username, String permission) throws SQLException {
        // Get user role
        String role = getUserRole(username);

        // Simply judge permission based on role
        // Administrator has all permissions
        if ("管理员".equals(role)) {
            return true;
        }
        // Supervisor has partial management permissions
        else if ("主管".equals(role)) {
            // Supervisor can manage activities and view reports, but cannot manage staff
            return !"MANAGE_STAFF".equals(permission);
        }
        // Regular employees only have view permissions
        else {
            return permission.startsWith("VIEW_");
        }
    }


    // Helper method for safely getting integer value
    private int getIntValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean hasPermission(String permission) throws SQLException {
        if (currentUsername == null) {
            return false;
        }

        // Directly judge permission based on current role
        if ("管理员".equals(currentRole)) {
            return true;
        } else if ("主管".equals(currentRole)) {
            return !"MANAGE_STAFF".equals(permission);
        } else {
            return permission.startsWith("VIEW_");
        }
    }

    /**
     * Get current logged-in user ID
     * @return Current user ID
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get current logged-in user role
     * @return Current user role
     */
    public String getCurrentRole() {
        return currentRole;
    }

    /**
     * User logout
     */
    public void logout() {
        currentUsername = null;
        currentUserId = null;
        currentRole = null;
    }
}