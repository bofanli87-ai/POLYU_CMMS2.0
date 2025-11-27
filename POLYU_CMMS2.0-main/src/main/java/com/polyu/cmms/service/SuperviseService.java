
package com.polyu.cmms.service;

import com.polyu.cmms.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class SuperviseService extends BaseService {

    /**
     * Convert Java camelCase naming to database underscore naming
     * @param javaName Java property name (camelCase)
     * @return Database column name (underscore naming)
     */
    private String convertToDbColumn(String javaName) {
        return javaName.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
    private static SuperviseService instance;

    private SuperviseService() {}

    public static SuperviseService getInstance() {
        if (instance == null) {
            instance = new SuperviseService();
        }
        return instance;
    }

    /**
     * Get role ID from staff information
     */
    private int getRoleIdFromStaffInfo(Map<String, Object> staffInfo) {
        // Try multiple possible field name formats
        Object roleIdObj = null;
        // Prefer camelCase naming because the executeQuery method of BaseService converts database column names from underscore naming to Java camelCase naming
        if (staffInfo.containsKey("roleId")) {
            roleIdObj = staffInfo.get("roleId");
        } else if (staffInfo.containsKey("role_id")) {
            roleIdObj = staffInfo.get("roleId");
        }

        // Add null check
        if (roleIdObj == null) {
            throw new IllegalArgumentException("Role ID in staff information is null");
        }

        if (roleIdObj instanceof Number) {
            return ((Number) roleIdObj).intValue();
        } else if (roleIdObj instanceof String) {
            // Try to convert string to number
            try {
                return Integer.parseInt((String) roleIdObj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid role ID format in staff information: " + roleIdObj);
            }
        }
        throw new IllegalArgumentException("Unable to get valid role ID from staff information: " + roleIdObj);
    }

    /**
     * Validate if the role hierarchy conforms to the three-tier structure rule
     * Rule:
     * - Executive (role_id=1) can supervise Middle Manager (role_id=2)
     * - Middle Manager (role_id=2) can supervise Base Employee (role_id=3)
     * - Other combinations are not allowed
     */
    private void validateRoleHierarchy(int supervisorRoleId, int subordinateRoleId) {
        if (supervisorRoleId == 1 && subordinateRoleId == 2) {
            // Executive can supervise Middle Manager, conforms to the rule
            return;
        } else if (supervisorRoleId == 2 && subordinateRoleId == 3) {
            // Middle Manager can supervise Base Employee, conforms to the rule
            return;
        }

        // Does not conform to the three-tier structure rule
        String supervisorRoleText = getRoleText(supervisorRoleId);
        String subordinateRoleText = getRoleText(subordinateRoleId);
        throw new IllegalArgumentException(
                "Supervision relationship does not conform to the three-tier structure rule: " +
                        supervisorRoleText + "(role_id=" + supervisorRoleId + ") cannot supervise " +
                        subordinateRoleText + "(role_id=" + subordinateRoleId + ").\n" +
                        "Allowed supervision relationships: Executive(1)→Middle Manager(2), Middle Manager(2)→Base Employee(3)"
        );
    }

    /**
     * Get role text description based on role ID
     */
    private String getRoleText(int roleId) {
        switch (roleId) {
            case 1:
                return "Executive";
            case 2:
                return "Middle Manager";
            case 3:
                return "Base Employee";
            default:
                return "Unknown Role";
        }
    }

    // Create supervision relationship, following the three-tier structure rule
    public boolean createSupervise(int supervisorStaffId, int subordinateStaffId, Date startDate, Date endDate) {
        // Parameter validation
        if (supervisorStaffId <= 0) {
            throw new IllegalArgumentException("Supervisor staff ID must be greater than 0");
        }
        if (subordinateStaffId <= 0) {
            throw new IllegalArgumentException("Subordinate staff ID must be greater than 0");
        }
        if (supervisorStaffId == subordinateStaffId) {
            throw new IllegalArgumentException("Supervisor cannot be the same as subordinate");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate != null && endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }

        // Validate staff existence and check role hierarchy
        StaffService staffService = StaffService.getInstance();
        try {
            Map<String, Object> supervisorInfo = staffService.getStaffById(supervisorStaffId);
            if (supervisorInfo == null) {
                throw new IllegalArgumentException("Specified supervisor staff does not exist");
            }

            Map<String, Object> subordinateInfo = staffService.getStaffById(subordinateStaffId);
            if (subordinateInfo == null) {
                throw new IllegalArgumentException("Specified subordinate staff does not exist");
            }

            // Get role ID, following the three-tier structure rule
            int supervisorRoleId = getRoleIdFromStaffInfo(supervisorInfo);
            int subordinateRoleId = getRoleIdFromStaffInfo(subordinateInfo);

            // Check if the supervision relationship conforms to the three-tier structure rule
            validateRoleHierarchy(supervisorRoleId, subordinateRoleId);

            System.out.println("Create supervision relationship: Supervisor ID=" + supervisorStaffId + "(Role ID=" + supervisorRoleId + "), " +
                    "Subordinate ID=" + subordinateStaffId + "(Role ID=" + subordinateRoleId + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to validate staff information", e);
        }

        // Check if the same supervision relationship already exists (ignoring end_date)
        String checkSql = "SELECT COUNT(*) FROM supervise WHERE supervisor_staff_id = ? AND subordinate_staff_id = ? AND (end_date IS NULL OR end_date >= ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setInt(1, supervisorStaffId);
            checkPstmt.setInt(2, subordinateStaffId);
            checkPstmt.setDate(3, new java.sql.Date(startDate.getTime()));

            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Specified supervision relationship already exists or time overlaps");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to check supervision relationship", e);
        }

        String sql = "INSERT INTO supervise (supervisor_staff_id, subordinate_staff_id, start_date, end_date) VALUES (?, ?, ?, ?)";
        try {
            // Use the parent class's executeUpdate method
            int result = executeUpdate(sql,
                    supervisorStaffId, subordinateStaffId,
                    new java.sql.Date(startDate.getTime()),
                    endDate != null ? new java.sql.Date(endDate.getTime()) : null);

            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create supervision relationship", e);
        }
    }

    // Get supervision relationship by ID

    // Get supervision relationship by ID
    public Map<String, Object> getSuperviseById(int superviseId) {
        if (superviseId <= 0) {
            throw new IllegalArgumentException("Supervision relationship ID must be greater than 0");
        }

        String sql = "SELECT * FROM supervise WHERE supervise_id = ?";
        try {
            List<Map<String, Object>> results = executeQuery(sql, superviseId);
            if (!results.isEmpty()) {
                return mapResultSetToSupervise(results.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get supervision relationship", e);
        }
        return null;
    }

    // Convert from Map result to camelCase named Map
    private Map<String, Object> mapResultSetToSupervise(Map<String, Object> resultMap) {
        Map<String, Object> map = new HashMap<>();
        // Check and add all necessary fields, while converting to camelCase naming
        if (resultMap.containsKey("supervise_id")) {
            map.put(convertToJavaName("supervise_id"), resultMap.get("supervise_id"));
        }
        if (resultMap.containsKey("supervisor_staff_id")) {
            map.put(convertToJavaName("supervisor_staff_id"), resultMap.get("supervisor_staff_id"));
        }
        if (resultMap.containsKey("subordinate_staff_id")) {
            map.put(convertToJavaName("subordinate_staff_id"), resultMap.get("subordinate_staff_id"));
        }
        if (resultMap.containsKey("start_date")) {
            map.put(convertToJavaName("start_date"), resultMap.get("start_date"));
        }
        if (resultMap.containsKey("end_date")) {
            map.put(convertToJavaName("end_date"), resultMap.get("end_date"));
        }
        return map;
    }

    // Update supervision relationship
    public boolean updateSupervise(int superviseId, Date endDate) {
        // Parameter validation
        if (superviseId <= 0) {
            throw new IllegalArgumentException("Supervision relationship ID must be greater than 0");
        }

        // First get the existing record to validate the date
        Map<String, Object> supervise = getSuperviseById(superviseId);
        if (supervise == null) {
            throw new RuntimeException("Specified supervision relationship record not found");
        }

        Date startDate = (Date) supervise.get("start_date");
        if (endDate != null && endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }

        String sql = "UPDATE supervise SET end_date = ? WHERE supervise_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, endDate != null ? new java.sql.Date(endDate.getTime()) : null);
            pstmt.setInt(2, superviseId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Specified supervision relationship record not found");
            }
            System.out.println("Update supervision relationship ID=" + superviseId + ", set end date=" + endDate);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update supervision relationship", e);
        }
    }

    // Delete supervision relationship
    public boolean deleteSupervise(int superviseId) {
        if (superviseId <= 0) {
            throw new IllegalArgumentException("Supervision relationship ID must be greater than 0");
        }

        String sql = "DELETE FROM supervise WHERE supervise_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, superviseId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Specified supervision relationship record not found");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete supervision relationship", e);
        }
    }



    private static void testgetSubordinatesByStaffId() {
        SuperviseService superviseService = SuperviseService.getInstance();
        List<Map<String, Object>> subordinates = superviseService.getSubordinatesByStaffId(1);
        System.out.println(subordinates);
    }
    // Get all subordinates of a staff member
    public List<Map<String, Object>> getSubordinatesByStaffId(int supervisorStaffId) {
        if (supervisorStaffId <= 0) {
            throw new IllegalArgumentException("Staff ID must be greater than 0");
        }

        String sql = "SELECT s.supervisor_staff_id, s.subordinate_staff_id, s.start_date, s.end_date, " +
                "sf.staff_number, sf.first_name, sf.last_name, sf.role_id " +
                "FROM supervise s " +
                "JOIN staff sf ON s.subordinate_staff_id = sf.staff_id " +
                "WHERE s.supervisor_staff_id = ? AND s.end_date IS NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, supervisorStaffId);

            List<Map<String, Object>> subordinates = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> subordinateMap = new HashMap<>();
                    // Explicitly set required fields
                    subordinateMap.put("supervisor_staff_id", rs.getInt("supervisor_staff_id"));
                    subordinateMap.put("subordinate_staff_id", rs.getInt("subordinate_staff_id"));
                    subordinateMap.put("start_date", rs.getDate("start_date"));
                    subordinateMap.put("end_date", rs.getDate("end_date"));
                    subordinateMap.put("staff_number", rs.getString("staff_number"));
                    subordinateMap.put("first_name", rs.getString("first_name"));
                    subordinateMap.put("last_name", rs.getString("last_name"));
                    subordinateMap.put("role_id", rs.getInt("role_id"));
                    subordinates.add(subordinateMap);
                }
                System.out.println("Number of subordinates retrieved: " + subordinates.size());
            }
            return subordinates;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get staff subordinates", e);
        }
    }
    private static void testgetSupervisorsByStaffId() {
        SuperviseService superviseService = SuperviseService.getInstance();
        List<Map<String, Object>> supervisors = superviseService.getSupervisorsByStaffId(5);
        System.out.println(supervisors);
    }

    // Get all supervisors of a staff member
    public List<Map<String, Object>> getSupervisorsByStaffId(int subordinateStaffId) {
        if (subordinateStaffId <= 0) {
            throw new IllegalArgumentException("Staff ID must be greater than 0");
        }

        String sql = "SELECT s.supervisor_staff_id, s.subordinate_staff_id, s.start_date, s.end_date, " +
                "sf.staff_number, sf.first_name, sf.last_name, sf.role_id " +
                "FROM supervise s " +
                "JOIN staff sf ON s.supervisor_staff_id = sf.staff_id " +
                "WHERE s.subordinate_staff_id = ? AND s.end_date IS NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subordinateStaffId);

            List<Map<String, Object>> supervisors = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> supervisorMap = new HashMap<>();
                    // Explicitly set required fields
                    supervisorMap.put("supervisor_staff_id", rs.getInt("supervisor_staff_id"));
                    supervisorMap.put("subordinate_staff_id", rs.getInt("subordinate_staff_id"));
                    supervisorMap.put("start_date", rs.getDate("start_date"));
                    supervisorMap.put("end_date", rs.getDate("end_date"));
                    supervisorMap.put("staff_number", rs.getString("staff_number"));
                    supervisorMap.put("first_name", rs.getString("first_name"));
                    supervisorMap.put("last_name", rs.getString("last_name"));
                    supervisors.add(supervisorMap);
                }
            }
            return supervisors;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get staff supervisors", e);
        }
    }

    // Paginated query of supervision relationships
    public Map<String, Object> getSupervisesByPage(int page, int pageSize, Map<String, Object> filters, String sortBy, String sortOrder) {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM supervise WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Add filter conditions
        if (filters != null) {
            if (filters.containsKey("supervisorStaffId")) {
                String dbColumn = convertToDbColumn("supervisorStaffId");
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(filters.get("supervisorStaffId"));
            }
            if (filters.containsKey("subordinateStaffId")) {
                String dbColumn = convertToDbColumn("subordinateStaffId");
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(filters.get("subordinateStaffId"));
            }
            if (filters.containsKey("activeOnly")) {
                if (Boolean.TRUE.equals(filters.get("activeOnly"))) {
                    sqlBuilder.append(" AND end_date IS NULL");
                }
            }
        }

        // Add sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String validSortFields = "supervise_id,supervisor_staff_id,subordinate_staff_id,start_date,end_date";
            if (validSortFields.contains(sortBy)) {
                sqlBuilder.append(" ORDER BY " + sortBy + " ");
                sqlBuilder.append(sortOrder != null && sortOrder.equalsIgnoreCase("DESC") ? "DESC" : "ASC");
            }
        } else {
            sqlBuilder.append(" ORDER BY supervise_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get total record count
        String countSql = "SELECT COUNT(*) FROM supervise WHERE 1=1";
        StringBuilder countSqlBuilder = new StringBuilder(countSql);
        if (filters != null) {
            if (filters.containsKey("supervisorStaffId")) {
                String dbColumn = convertToDbColumn("supervisorStaffId");
                countSqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            }
            if (filters.containsKey("subordinateStaffId")) {
                String dbColumn = convertToDbColumn("subordinateStaffId");
                countSqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            }
            if (filters.containsKey("activeOnly")) {
                if (Boolean.TRUE.equals(filters.get("activeOnly"))) {
                    countSqlBuilder.append(" AND end_date IS NULL");
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        try {
            // Get total record count
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement countPstmt = conn.prepareStatement(countSqlBuilder.toString())) {
                // Set parameters except pagination
                for (int i = 0; i < params.size() - 2; i++) {
                    countPstmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet countRs = countPstmt.executeQuery()) {
                    if (countRs.next()) {
                        result.put("total", countRs.getInt(1));
                    }
                }
            }

            // Get paginated data
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
                // Set all parameters
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }

                List<Map<String, Object>> supervises = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        supervises.add(mapResultSetToSupervise(rs));
                    }
                }
                result.put("data", supervises);
                result.put("page", page);
                result.put("pageSize", pageSize);
                // Safely get total value and calculate total pages, avoid null pointer exception
                int total = 0;
                Object totalObj = result.get("total");
                if (totalObj instanceof Number) {
                    total = ((Number) totalObj).intValue();
                } else if (totalObj instanceof String) {
                    try {
                        total = Integer.parseInt((String) totalObj);
                    } catch (NumberFormatException e) {
                        total = 0;
                    }
                }
                result.put("totalPages", (int) Math.ceil(total / (double) pageSize));
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to paginate query supervision relationships", e);
        }
    }

    // Convert database underscore naming to Java camelCase naming
    protected String convertToJavaName(String dbName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < dbName.length(); i++) {
            char c = dbName.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    // Map ResultSet to Map
    private Map<String, Object> mapResultSetToSupervise(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        // Convert underscore naming to camelCase naming
        map.put(convertToJavaName("supervise_id"), rs.getInt("supervise_id"));
        map.put(convertToJavaName("supervisor_staff_id"), rs.getInt("supervisor_staff_id"));
        map.put(convertToJavaName("subordinate_staff_id"), rs.getInt("subordinate_staff_id"));
        map.put(convertToJavaName("start_date"), rs.getDate("start_date"));
        map.put(convertToJavaName("end_date"), rs.getDate("end_date"));
        return map;
    }
    public static void main(String[] args) {
        testgetSupervisorsByStaffId();
        testgetSubordinatesByStaffId();
    }
}