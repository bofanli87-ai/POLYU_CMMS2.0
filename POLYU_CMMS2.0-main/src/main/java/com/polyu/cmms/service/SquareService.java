

package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * Square Service class, provides data access functionality related to squares.
 */
public class SquareService extends BaseService {
    private static SquareService instance;

    // Singleton pattern
    private SquareService() {}

    public static synchronized SquareService getInstance() {
        if (instance == null) {
            instance = new SquareService();
        }
        return instance;
    }

    /**
     * Add a square record.
     * @param squareData Square data.
     * @return Whether the insertion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean addSquare(Map<String, Object> squareData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO squares (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        // Add necessary fields
        if (squareData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("name"));
        }
        if (squareData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("addressId"));
        }
        if (squareData.containsKey("capacity")) {
            sqlBuilder.append("capacity, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("capacity"));
        }

        // Add default active status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");

        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Update square information.
     * @param squareId Square ID.
     * @param updates Fields and values to update.
     * @return Whether the update was successful.
     * @throws SQLException SQL exception.
     */
    public boolean updateSquare(int squareId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE squares SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove the last comma and space
        sqlBuilder.append(" WHERE square_id = ?");
        params.add(squareId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete a square (soft delete).
     * @param squareId Square ID.
     * @return Whether the deletion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean deleteSquare(int squareId) throws SQLException {
        String sql = "UPDATE squares SET active_flag = 'N' WHERE square_id = ?";
        int result = executeUpdate(sql, squareId);
        return result > 0;
    }

    /**
     * Query squares based on conditions.
     * @param conditions Query conditions.
     * @return List of squares.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> querySquares(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM squares WHERE 1=1");
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
     * Paginated query for squares.
     * @param page Page number.
     * @param pageSize Page size.
     * @param conditions Query conditions.
     * @param sortField Sort field.
     * @param sortOrder Sort order.
     * @return Paginated query results.
     * @throws SQLException SQL exception.
     */
    public Map<String, Object> getSquaresByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM squares WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY square_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());

        // Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM squares WHERE 1=1");
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
     * Get square information by ID.
     * @param squareId Square ID.
     * @return Square information.
     * @throws SQLException SQL exception.
     */
    public Map<String, Object> getSquareById(int squareId) throws SQLException {
        String sql = "SELECT * FROM squares WHERE square_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, squareId);
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }

    /**
     * Get all active squares.
     * @return List of squares.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> getAllActiveSquares() throws SQLException {
        String sql = "SELECT * FROM squares WHERE active_flag = 'Y' ORDER BY square_id ASC";
        return executeQuery(sql);
    }

    /**
     * Convert Java camelCase naming to database underscore naming.
     * @param javaName Java property name (camelCase).
     * @return Database column name (underscore naming).
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