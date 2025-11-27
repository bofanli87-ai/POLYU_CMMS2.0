
package com.polyu.cmms.service;
import java.sql.SQLException;
import java.util.*;
/**
 Gate service class, provides data access functions related to gates
 */
public class GateService extends BaseService {
    private static GateService instance;
    // Singleton pattern
    private GateService() {}
    public static synchronized GateService getInstance() {
        if (instance == null) {
            instance = new GateService();
        }
        return instance;
    }
    /**
     Add a gate record
     @param gateData Gate data
     @return Whether the insertion was successful
     @throws SQLException SQL exception
     */
    public boolean addGate(Map<String, Object> gateData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO gates (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
// Add necessary fields
        if (gateData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(gateData.get("name"));
        }
        if (gateData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(gateData.get("addressId"));
        }
        if (gateData.containsKey("flowCapacity")) {
            sqlBuilder.append("flow_capacity, ");
            valuesBuilder.append("?, ");
            params.add(gateData.get("flowCapacity"));
        }
// Add default active status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");
        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }
    /**
     Update gate information
     @param gateId Gate ID
     @param updates Fields and values to update
     @return Whether the update was successful
     @throws SQLException SQL exception
     */
    public boolean updateGate(int gateId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }
        StringBuilder sqlBuilder = new StringBuilder("UPDATE gates SET ");
        List<Object> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }
        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove the last comma and space
        sqlBuilder.append(" WHERE gate_id = ?");
        params.add(gateId);
        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }
    /**
     Delete a gate (soft delete)
     @param gateId Gate ID
     @return Whether the deletion was successful
     @throws SQLException SQL exception
     */
    public boolean deleteGate(int gateId) throws SQLException {
        String sql = "UPDATE gates SET active_flag = 'N' WHERE gate_id = ?";
        int result = executeUpdate(sql, gateId);
        return result > 0;
    }
    /**
     Query gates based on conditions
     @param conditions Query conditions
     @return List of gates
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryGates(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM gates WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (conditions != null) {
// Convert Java property names to database column names
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
        return executeQuery(sqlBuilder.toString(), params.toArray());
    }
    /**
     Paginated query for gates
     @param page Page number
     @param pageSize Page size
     @param conditions Query conditions
     @param sortField Sort field
     @param sortOrder Sort order
     @return Paginated query results
     @throws SQLException SQL exception
     */
    public Map<String, Object> getGatesByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
// Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM gates WHERE 1=1");
        List<Object> params = new ArrayList<>();
// Add filter conditions
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
// Add sorting
        if (sortField != null && !sortField.trim().isEmpty()) {
            String dbColumn = convertToDbColumn(sortField);
            String order = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
            sqlBuilder.append(" ORDER BY ").append(dbColumn).append(" ").append(order);
        } else {
// Default sort by ID
            sqlBuilder.append(" ORDER BY gate_id ASC");
        }
// Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
// Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());
// Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM gates WHERE 1=1");
        List<Object> countParams = new ArrayList<>();
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                countSqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                countParams.add(entry.getValue());
            }
        }
        List<Map<String, Object>> countResult = executeQuery(countSqlBuilder.toString(), countParams.toArray());
        int total = 0;
// Safely get total record count, avoid null pointer exceptions
        if (countResult != null && !countResult.isEmpty()) {
            Map<String, Object> firstRow = countResult.get(0);
            if (firstRow != null) {
                Object countObj = firstRow.get("count");
                if (countObj instanceof Number) {
                    total = ((Number) countObj).intValue();
                } else if (countObj != null) {
                    try {
                        total = Integer.parseInt(countObj.toString());
                    } catch (NumberFormatException e) {
                        total = 0;
                    }
                }
            }
        }
// Calculate total pages
        int totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
// Return result
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", totalPages);
        return result;
    }
    /**
     Get gate information by ID
     @param gateId Gate ID
     @return Gate information
     @throws SQLException SQL exception
     */
    public Map<String, Object> getGateById(int gateId) throws SQLException {
        String sql = "SELECT * FROM gates WHERE gate_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, gateId);
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }
    /**
     Get all enabled gates
     @return List of gates
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getAllActiveGates() throws SQLException {
        String sql = "SELECT * FROM gates WHERE active_flag = 'Y' ORDER BY gate_id ASC";
        return executeQuery(sql);
    }
    /**
     Query gates by traffic capacity
     @param minCapacity Minimum traffic capacity
     @param maxCapacity Maximum traffic capacity
     @return List of gates meeting the criteria
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getGatesByCapacityRange(int minCapacity, int maxCapacity) throws SQLException {
        String sql = "SELECT * FROM gates WHERE flow_capacity BETWEEN ? AND ? AND active_flag = 'Y'";
        return executeQuery(sql, minCapacity, maxCapacity);
    }
    /**
     Convert Java camel case naming to database underscore naming
     @param javaName Java property name (camel case)
     @return Database column name (underscore naming)
     */
    private String convertToDbColumn(String javaName) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < javaName.length(); i++) {
            char c = javaName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}