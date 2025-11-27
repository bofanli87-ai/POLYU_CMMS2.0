
package com.polyu.cmms.service;

import com.polyu.cmms.model.Staff;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Staff Service class, provides data access functionality related to staff.
 */
public class StaffService extends BaseService {
    private static StaffService instance;

    // Singleton pattern
    private StaffService() {}

    public static synchronized StaffService getInstance() {
        if (instance == null) {
            instance = new StaffService();
        }
        return instance;
    }

    /**
     * Add a single staff record.
     * @param staff Staff object.
     * @return Whether the insertion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean addStaff(Staff staff) throws SQLException {
        String sql = "INSERT INTO staff (staff_number, first_name, last_name, age, gender, role, email, phone, hire_date, responsibility, active_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                staff.getStaffNumber(), staff.getFirstName(), staff.getLastName(),
                staff.getAge(), staff.getGender(), staff.getRole(),
                staff.getEmail(), staff.getPhone(), staff.getHireDate(),
                staff.getResponsibility(), "Y");
        return result > 0;
    }

    /**
     * Batch add staff records.
     * @param staffList List of staff.
     * @return Whether the insertion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean batchAddStaff(List<Staff> staffList) throws SQLException {
        String sql = "INSERT INTO staff (staff_number, first_name, last_name, age, gender, role, email, phone, hire_date, responsibility, active_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> paramsList = new java.util.ArrayList<>();
        for (Staff staff : staffList) {
            Object[] params = {
                    staff.getStaffNumber(), staff.getFirstName(), staff.getLastName(),
                    staff.getAge(), staff.getGender(), staff.getRole(),
                    staff.getEmail(), staff.getPhone(), staff.getHireDate(),
                    staff.getResponsibility(), "Y"
            };
            paramsList.add(params);
        }

        int[] results = executeBatch(sql, paramsList);
        return results.length == staffList.size();
    }

    /**
     * Update staff information.
     * @param staffId Staff ID.
     * @param updates Fields and values to update.
     * @return Whether the update was successful.
     * @throws SQLException SQL exception.
     */
    public boolean updateStaff(int staffId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE staff SET ");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            sqlBuilder.append(entry.getKey()).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove the last comma and space
        sqlBuilder.append(" WHERE staff_id = ?");
        params.add(staffId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete staff.
     * @param staffId Staff ID.
     * @return Whether the deletion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean deleteStaff(int staffId) throws SQLException {
        String sql = "UPDATE staff SET active_flag = 'N' WHERE staff_id = ?";
        int result = executeUpdate(sql, staffId);
        return result > 0;
    }

    /**
     * Query staff by conditions.
     * @param conditions Query conditions.
     * @return List of staff.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> queryStaff(Map<String, Object> conditions) throws SQLException {
        // Directly build SQL to avoid dynamic column name issues
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM staff WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        if (conditions.containsKey("activeFlag")) {
            // active_flag = 'Y' is already set in the base SQL
        }

        if (conditions.containsKey("roleId") && conditions.get("roleId") instanceof List) {
            List<?> roleIdList = (List<?>) conditions.get("roleId");
            if (!roleIdList.isEmpty()) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < roleIdList.size(); i++) {
                    if (i > 0) {
                        placeholders.append(", ");
                    }
                    placeholders.append("?");
                    params.add(roleIdList.get(i));
                }
                sqlBuilder.append(" AND role_id IN (").append(placeholders).append(")");
            }
        }

        // Handle other possible conditions
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            if ("activeFlag".equals(key) || "roleId".equals(key)) {
                continue; // Conditions already handled
            }
            // Other conditions are temporarily not handled to avoid introducing new issues
        }

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }


    // Convert Java camelCase naming to database underscore naming
    private String convertToDbColumn(String javaName) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < javaName.length(); i++) {
            char c = javaName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    public Map<String, Object> getStaffByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Get total count
        int total = getStaffCount(conditions);
        int totalPages = (total + pageSize - 1) / pageSize; // Calculate total pages

        // Query data
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM staff WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            // Convert Java camelCase naming to database underscore naming
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            params.add(entry.getValue());
        }

        // Add sorting
        if (sortField != null && !sortField.isEmpty()) {
            // Also convert sort field column name
            String dbSortField = convertToDbColumn(sortField);
            sqlBuilder.append(" ORDER BY ").append(dbSortField);
            if (sortOrder != null && "desc".equalsIgnoreCase(sortOrder)) {
                sqlBuilder.append(" DESC");
            } else {
                sqlBuilder.append(" ASC");
            }
        }

        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Map<String, Object>> staffList = executeQuery(sqlBuilder.toString(), params.toArray());

        // Build return result
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("data", staffList);
        result.put("totalPages", totalPages);
        result.put("total", total);

        return result;
    }

    public List<Map<String, Object>> queryStaffByPage(int page, int pageSize, Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM staff WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            // Convert Java camelCase naming to database underscore naming
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            params.add(entry.getValue());
        }

        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * Get total staff count.
     * @param conditions Query conditions.
     * @return Total staff count.
     * @throws SQLException SQL exception.
     */
    public int getStaffCount(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) as count FROM staff WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            // Convert Java camelCase naming to database underscore naming
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            params.add(entry.getValue());
        }

        List<Map<String, Object>> results = executeQuery(sqlBuilder.toString(), params.toArray());
        if (results == null || results.isEmpty()) {
            return 0;
        }

        Map<String, Object> firstRow = results.get(0);
        if (firstRow == null) {
            return 0;
        }

        Object countObj = firstRow.get("count");
        if (countObj instanceof Number) {
            return ((Number) countObj).intValue();
        } else if (countObj != null) {
            try {
                return Integer.parseInt(countObj.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Count staff by role.
     * @return Role statistics data, key is role ID, value is count.
     * @throws SQLException SQL exception.
     */
    public Map<Integer, Integer> getStaffCountByRole() throws SQLException {
        String sql = "SELECT role_id, COUNT(*) as count FROM staff WHERE active_flag = 'Y' GROUP BY role_id";
        List<Map<String, Object>> results = executeQuery(sql);

        Map<Integer, Integer> roleCountMap = new java.util.HashMap<>();
        for (Map<String, Object> result : results) {
            // Safely get integer values, avoid null pointer exception
            Integer roleId = null;
            Integer count = null;

            // Try to get roleId from different key names (considering database column name might be role_id)
            Object roleIdObj = result.get("roleId");
            if (roleIdObj == null) {
                roleIdObj = result.get("role_id");
            }

            // Try to get count from different key names
            Object countObj = result.get("count");

            // Safely convert to Integer
            if (roleIdObj instanceof Number) {
                roleId = ((Number) roleIdObj).intValue();
            } else if (roleIdObj != null) {
                try {
                    roleId = Integer.parseInt(roleIdObj.toString());
                } catch (NumberFormatException e) {
                    roleId = null;
                }
            }

            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            } else if (countObj != null) {
                try {
                    count = Integer.parseInt(countObj.toString());
                } catch (NumberFormatException e) {
                    count = null;
                }
            }

            // Only add to map if both values are valid
            if (roleId != null && count != null) {
                roleCountMap.put(roleId, count);
            }
        }
        return roleCountMap;
    }

    /**
     * Count staff by gender.
     * @return Gender statistics data, key is gender, value is count.
     * @throws SQLException SQL exception.
     */
    public Map<String, Integer> getStaffCountByGender() throws SQLException {
        String sql = "SELECT gender, COUNT(*) as count FROM staff WHERE active_flag = 'Y' GROUP BY gender";
        List<Map<String, Object>> results = executeQuery(sql);

        Map<String, Integer> genderCountMap = new java.util.HashMap<>();
        for (Map<String, Object> result : results) {
            // Safely get gender value
            String gender = null;
            Object genderObj = result.get("gender");
            if (genderObj instanceof String) {
                gender = (String) genderObj;
            } else if (genderObj != null) {
                gender = genderObj.toString();
            }

            // Safely get count value
            Integer count = null;
            Object countObj = result.get("count");
            if (countObj instanceof Number) {
                count = ((Number) countObj).intValue();
            } else if (countObj != null) {
                try {
                    count = Integer.parseInt(countObj.toString());
                } catch (NumberFormatException e) {
                    count = null;
                }
            }

            // Only add to map if both values are valid
            if (gender != null && count != null) {
                genderCountMap.put(gender, count);
            }
        }
        return genderCountMap;
    }

    /**
     * Get total staff count.
     * @return Total staff count.
     * @throws SQLException SQL exception.
     */
    public int getTotalStaffCount() throws SQLException {
        return getStaffCount(new java.util.HashMap<>());
    }

    /**
     * Get staff information by ID.
     * @param staffId Staff ID.
     * @return Staff information.
     * @throws SQLException SQL exception.
     */
    public Map<String, Object> getStaffById(int staffId) throws SQLException {
        String sql = "SELECT * FROM staff WHERE staff_id = ? AND active_flag = 'Y'";
        List<Map<String, Object>> results = executeQuery(sql, staffId);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     * Create staff (matches the call in StaffManagementPanel).
     * @return Whether the creation was successful.
     * @throws SQLException SQL exception.
     */
    public boolean createStaff(String staffNumber, String firstName, String lastName, String gender, java.util.Date dateOfBirth,
                               String phone, String email, java.util.Date hireDate, int roleId,
                               String emergencyContact, String emergencyPhone) throws SQLException {
        // Build SQL statement, only including fields that exist in the staff table
        String sql = "INSERT INTO staff (staff_number, first_name, last_name, gender, date_of_birth, phone, email, " +
                "hire_date, role_id, emergency_contact, emergency_phone, active_flag) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int result = executeUpdate(sql,
                staffNumber, firstName, lastName, gender, dateOfBirth,
                phone, email, hireDate, roleId, emergencyContact, emergencyPhone, "Y"); // Default set to active status

        return result > 0;
    }


    public boolean updateStaff(int staffId, String staffNumber, String firstName, String lastName, String gender,
                               java.util.Date dateOfBirth, String phone, String email, java.util.Date hireDate,
                               int roleId, String emergencyContact, String emergencyPhone, String activeFlag) throws SQLException {
        // Build SQL statement, only including fields that exist in the staff table
        String sql = "UPDATE staff SET staff_number = ?, first_name = ?, last_name = ?, gender = ?, " +
                "date_of_birth = ?, phone = ?, email = ?, hire_date = ?, " +
                "role_id = ?, emergency_contact = ?, emergency_phone = ?, active_flag = ? " +
                "WHERE staff_id = ?";

        int result = executeUpdate(sql,
                staffNumber, firstName, lastName, gender, dateOfBirth,
                phone, email, hireDate, roleId, emergencyContact, emergencyPhone, activeFlag, staffId);

        return result > 0;
    }
}