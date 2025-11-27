
package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * Building service class, provides building-related data access functions
 */
public class BuildingService extends BaseService {
    private static BuildingService instance;

    // Singleton pattern
    private BuildingService() {}

    public static synchronized BuildingService getInstance() {
        if (instance == null) {
            instance = new BuildingService();
        }
        return instance;
    }

    /**
     * Add building record
     * @param buildingData Building data
     * @return Whether insertion was successful
     * @throws SQLException SQL exception
     */
    public boolean addBuilding(Map<String, Object> buildingData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO buildings (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        // Add necessary fields
        if (buildingData.containsKey("buildingCode")) {
            sqlBuilder.append("building_code, ");
            valuesBuilder.append("?, ");
            params.add(buildingData.get("buildingCode"));
        }
        if (buildingData.containsKey("constructionDate")) {
            sqlBuilder.append("construction_date, ");
            valuesBuilder.append("?, ");
            params.add(buildingData.get("constructionDate"));
        }
        if (buildingData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(buildingData.get("addressId"));
        }
        if (buildingData.containsKey("numFloors")) {
            sqlBuilder.append("num_floors, ");
            valuesBuilder.append("?, ");
            params.add(buildingData.get("numFloors"));
        }
        if (buildingData.containsKey("supervisorStaffId")) {
            sqlBuilder.append("supervisor_staff_id, ");
            valuesBuilder.append("?, ");
            params.add(buildingData.get("supervisorStaffId"));
        }

        // Add default enabled status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");

        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Update building information
     * @param buildingId Building ID
     * @param updates Fields and values to update
     * @return Whether update was successful
     * @throws SQLException SQL exception
     */
    public boolean updateBuilding(int buildingId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE buildings SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove last comma and space
        sqlBuilder.append(" WHERE building_id = ?");
        params.add(buildingId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete building (soft delete)
     * @param buildingId Building ID
     * @return Whether deletion was successful
     * @throws SQLException SQL exception
     */
    public boolean deleteBuilding(int buildingId) throws SQLException {
        String sql = "UPDATE buildings SET active_flag = 'N' WHERE building_id = ?";
        int result = executeUpdate(sql, buildingId);
        return result > 0;
    }

    /**
     * Query buildings based on conditions
     * @param conditions Query conditions
     * @return Building list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryBuildings(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM buildings WHERE 1=1");
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
     * Paginated query of buildings
     * @param page Page number
     * @param pageSize Page size
     * @param conditions Query conditions
     * @param sortField Sort field
     * @param sortOrder Sort order
     * @return Paginated query result
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getBuildingsByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM buildings WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY building_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());

        // Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM buildings WHERE 1=1");
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

        // Safely get total record count, avoid null pointer exception
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

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));

        return result;
    }

    /**
     * Get building details by ID
     * @param buildingId Building ID
     * @return Building details
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getBuildingById(int buildingId) throws SQLException {
        String sql = "SELECT * FROM buildings WHERE building_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, buildingId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get all active buildings
     * @return Active building list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getAllActiveBuildings() throws SQLException {
        String sql = "SELECT * FROM buildings WHERE active_flag = 'Y' ORDER BY building_code";
        return executeQuery(sql);
    }

    /**
     * Query buildings by responsible manager ID
     * @param supervisorStaffId Responsible manager ID
     * @return Building list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getBuildingsBySupervisor(int supervisorStaffId) throws SQLException {
        String sql = "SELECT * FROM buildings WHERE supervisor_staff_id = ? AND active_flag = 'Y'";
        return executeQuery(sql, supervisorStaffId);
    }

    /**
     * Convert Java camel case naming to database underscore naming
     * @param javaName Java property name (camel case naming)
     * @return Database column name (underscore naming)
     */
    private String convertToDbColumn(String javaName) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < javaName.length(); i++) {
            char c = javaName.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}