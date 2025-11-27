
package com.polyu.cmms.service;

import com.polyu.cmms.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base service class, provides common database operation methods
 */
public abstract class BaseService {

    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException SQL exception
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    /**
     * Convert database underscore naming to Java camel case naming
     * @param dbName Database column name (underscore naming)
     * @return Java property name (camel case naming)
     */
    protected String convertToJavaName(String dbName) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < dbName.length(); i++) {
            char c = dbName.charAt(i);
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    /**
     * Execute SQL query, return result set
     * @param sql SQL query statement
     * @param params Parameter list
     * @return Query result list
     * @throws SQLException SQL exception
     */
    protected List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    // Convert database underscore naming to Java camel case naming
                    String javaName = convertToJavaName(columnName);
                    Object value = rs.getObject(i);
                    row.put(javaName, value);
                }
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Execute update operation (INSERT, UPDATE, DELETE)
     * @param sql SQL statement
     * @param params Parameter list
     * @return Number of affected rows
     * @throws SQLException SQL exception
     */
    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params)) {
            return stmt.executeUpdate();
        }
    }

    /**
     * Execute batch update operation
     * @param sql SQL statement
     * @param paramsList List of parameter lists
     * @return Array of affected rows for each update operation
     * @throws SQLException SQL exception
     */
    protected int[] executeBatch(String sql, List<Object[]> paramsList) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] params : paramsList) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.addBatch();
            }

            return stmt.executeBatch();
        }
    }

    /**
     * Execute transaction
     * @param operations Transaction operation list
     * @throws SQLException SQL exception
     */
    protected void executeTransaction(List<TransactionOperation> operations) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (TransactionOperation operation : operations) {
                try (PreparedStatement stmt = prepareStatement(conn, operation.getSql(), operation.getParams())) {
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Log rollback exception, but don't override original exception
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prepare PreparedStatement with parameters
     * @param conn Database connection
     * @param sql SQL statement
     * @param params Parameter list
     * @return PreparedStatement object
     * @throws SQLException SQL exception
     */
    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }

    /**
     * Transaction operation interface
     */
    public static class TransactionOperation {
        private final String sql;
        private final Object[] params;

        public TransactionOperation(String sql, Object... params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getParams() {
            return params;
        }
    }
}