
package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * Canteen service class, provides canteen-related data access functions
 */
public class CanteenService extends BaseService {
    private static CanteenService instance;

    // Singleton pattern
    private CanteenService() {}

    public static synchronized CanteenService getInstance() {
        if (instance == null) {
            instance = new CanteenService();
        }
        return instance;
    }

    /**
     * Add canteen record
     * @param canteenData Canteen data
     * @return Whether insertion was successful
     * @throws SQLException SQL exception
     */
    public boolean addCanteen(Map<String, Object> canteenData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO canteen (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        // Add necessary fields
        if (canteenData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("name"));
        }
        if (canteenData.containsKey("constructionDate")) {
            sqlBuilder.append("construction_date, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("constructionDate"));
        }
        if (canteenData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("addressId"));
        }
        if (canteenData.containsKey("foodType")) {
            sqlBuilder.append("food_type, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("foodType"));
        }

        // Add default business status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");

        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Update canteen information
     * @param canteenId Canteen ID
     * @param updates Fields and values to update
     * @return Whether update was successful
     * @throws SQLException SQL exception
     */
    public boolean updateCanteen(int canteenId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE canteen SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove last comma and space
        sqlBuilder.append(" WHERE canteen_id = ?");
        params.add(canteenId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete canteen (soft delete)
     * @param canteenId Canteen ID
     * @return Whether deletion was successful
     * @throws SQLException SQL exception
     */
    public boolean deleteCanteen(int canteenId) throws SQLException {
        String sql = "UPDATE canteen SET active_flag = 'N' WHERE canteen_id = ?";
        int result = executeUpdate(sql, canteenId);
        return result > 0;
    }

    /**
     * Query canteens based on conditions
     * @param conditions Query conditions
     * @return Canteen list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryCanteens(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM canteen WHERE 1=1");
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
     * Paginated query of canteens
     * @param page Page number
     * @param pageSize Page size
     * @param conditions Query conditions
     * @param sortField Sort field
     * @param sortOrder Sort order
     * @return Paginated query result
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getCanteensByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM canteen WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY canteen_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());

        // Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM canteen WHERE 1=1");
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
     * Get canteen information by ID
     * @param canteenId Canteen ID
     * @return Canteen information
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getCanteenById(int canteenId) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE canteen_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, canteenId);
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }

    /**
     * Get all operating canteens
     * @return Canteen list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getAllActiveCanteens() throws SQLException {
        String sql = "SELECT * FROM canteen WHERE active_flag = 'Y' ORDER BY canteen_id ASC";
        return executeQuery(sql);
    }

    /**
     * Query canteens by food type
     * @param foodType Food type
     * @return List of canteens that meet the criteria
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getCanteensByFoodType(String foodType) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE food_type = ? AND active_flag = 'Y'";
        return executeQuery(sql, foodType);
    }

    /**
     * Query canteens by construction date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of canteens that meet the criteria
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getCanteensByConstructionDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE construction_date BETWEEN ? AND ? AND active_flag = 'Y'";
        return executeQuery(sql, startDate, endDate);
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
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}