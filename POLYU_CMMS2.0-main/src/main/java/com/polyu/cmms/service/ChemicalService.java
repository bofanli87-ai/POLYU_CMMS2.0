
package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * Chemical service class, provides chemical-related data access functions
 */
public class ChemicalService extends BaseService {
    private static ChemicalService instance;

    // Singleton pattern
    private ChemicalService() {}

    public static synchronized ChemicalService getInstance() {
        if (instance == null) {
            instance = new ChemicalService();
        }
        return instance;
    }

    /**
     * Add chemical record
     * @param chemicalData Chemical data
     * @return Whether insertion was successful
     * @throws SQLException SQL exception
     */
    public boolean addChemical(Map<String, Object> chemicalData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO chemical (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        // Add necessary fields
        if (chemicalData.containsKey("productCode")) {
            sqlBuilder.append("product_code, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("productCode"));
        }
        if (chemicalData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("name"));
        }
        if (chemicalData.containsKey("type")) {
            sqlBuilder.append("type, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("type"));
        }
        if (chemicalData.containsKey("manufacturer")) {
            sqlBuilder.append("manufacturer, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("manufacturer"));
        }
        if (chemicalData.containsKey("msdsUrl")) {
            sqlBuilder.append("msds_url, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("msdsUrl"));
        }
        if (chemicalData.containsKey("hazardCategory")) {
            sqlBuilder.append("hazard_category, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("hazardCategory"));
        }
        if (chemicalData.containsKey("storageRequirements")) {
            sqlBuilder.append("storage_requirements, ");
            valuesBuilder.append("?, ");
            params.add(chemicalData.get("storageRequirements"));
        }

        // Add default enabled status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");

        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Update chemical information
     * @param chemicalId Chemical ID
     * @param updates Fields and values to update
     * @return Whether update was successful
     * @throws SQLException SQL exception
     */
    public boolean updateChemical(int chemicalId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE chemical SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove last comma and space
        sqlBuilder.append(" WHERE chemical_id = ?");
        params.add(chemicalId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete chemical (soft delete)
     * @param chemicalId Chemical ID
     * @return Whether deletion was successful
     * @throws SQLException SQL exception
     */
    public boolean deleteChemical(int chemicalId) throws SQLException {
        String sql = "UPDATE chemical SET active_flag = 'N' WHERE chemical_id = ?";
        int result = executeUpdate(sql, chemicalId);
        return result > 0;
    }

    /**
     * Query chemicals based on conditions
     * @param conditions Query conditions
     * @return Chemical list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryChemicals(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM chemical WHERE 1=1");
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
     * Paginated query of chemicals
     * @param page Page number
     * @param pageSize Page size
     * @param conditions Query conditions
     * @param sortField Sort field
     * @param sortOrder Sort order
     * @return Paginated query result
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getChemicalsByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM chemical WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY chemical_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());

        // Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM chemical WHERE 1=1");
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
        // Safely calculate total pages, avoid type conversion issues
        int totalPages = 0;
        if (pageSize > 0) {
            try {
                totalPages = (int) Math.ceil((double) total / pageSize);
            } catch (Exception e) {
                totalPages = 0;
            }
        }
        result.put("totalPages", totalPages);

        return result;
    }

    /**
     * Get chemical details by ID
     * @param chemicalId Chemical ID
     * @return Chemical details
     * @throws SQLException SQL exception
     */
    public Map<String, Object> getChemicalById(int chemicalId) throws SQLException {
        String sql = "SELECT * FROM chemical WHERE chemical_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, chemicalId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get all active chemicals
     * @return Active chemical list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getAllActiveChemicals() throws SQLException {
        String sql = "SELECT * FROM chemical WHERE active_flag = 'Y' ORDER BY name";
        return executeQuery(sql);
    }

    /**
     * Query chemicals by hazard category
     * @param hazardCategory Hazard category
     * @return Chemical list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getChemicalsByHazardCategory(String hazardCategory) throws SQLException {
        String sql = "SELECT * FROM chemical WHERE hazard_category = ? AND active_flag = 'Y' ORDER BY name";
        return executeQuery(sql, hazardCategory);
    }

    /**
     * Query chemicals by type
     * @param type Chemical type
     * @return Chemical list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getChemicalsByType(String type) throws SQLException {
        String sql = "SELECT * FROM chemical WHERE type = ? AND active_flag = 'Y' ORDER BY name";
        return executeQuery(sql, type);
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