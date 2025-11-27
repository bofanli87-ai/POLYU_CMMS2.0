
package com.polyu.cmms.service;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
/**
 General query service class, provides SQL execution and result processing functions
 */
public class QueryService extends BaseService {
    /**
     * Execute custom SQL query
     *
     * @param sql    SQL query statement
     * @param params Parameter list
     * @return Query results
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> executeCustomQuery(String sql, Object... params) throws SQLException {
    // Security check: prevent dangerous SQL injection
        if (sql.toUpperCase().contains("DELETE") || sql.toUpperCase().contains("DROP") ||
                sql.toUpperCase().contains("ALTER") || sql.toUpperCase().contains("TRUNCATE")) {
            throw new SQLException("Execution of dangerous SQL statements is not allowed");
        }
        return executeQuery(sql, params);
    }

    /**
     * Execute paginated query
     *
     * @param sql      SQL query statement (without LIMIT clause)
     * @param page     Page number
     * @param pageSize Page size
     * @param params   Parameter list
     * @return Paginated query results
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> executePagedQuery(String sql, int page, int pageSize, Object... params) throws SQLException {
    // Add pagination clause
        String pagedSql = sql + " LIMIT ? OFFSET ?";
    // Extend parameter array
        Object[] pagedParams = new Object[params.length + 2];
        System.arraycopy(params, 0, pagedParams, 0, params.length);
        pagedParams[params.length] = pageSize;
        pagedParams[params.length + 1] = (page - 1) * pageSize;
        return executeQuery(pagedSql, pagedParams);
    }

    /**
     * Get the total count of query results
     *
     * @param sql    SQL query statement (without LIMIT clause)
     * @param params Parameter list
     * @return Total result count
     * @throws SQLException SQL exception
     */
    public int getQueryCount(String sql, Object... params) throws SQLException {
    // Construct COUNT query
        String countSql = "SELECT COUNT(*) as count FROM (" + sql + ") as temp";
        List<Map<String, Object>> results = executeQuery(countSql, params);
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
}