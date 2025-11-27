
package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * Room Service class, provides data access functionality related to rooms.
 */
public class RoomService extends BaseService {
    private static RoomService instance;

    // Singleton pattern
    private RoomService() {}

    public static synchronized RoomService getInstance() {
        if (instance == null) {
            instance = new RoomService();
        }
        return instance;
    }

    /**
     * Add a room record.
     * @param roomData Room data.
     * @return Whether the insertion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean addRoom(Map<String, Object> roomData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO rooms (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();

        // Add necessary fields
        if (roomData.containsKey("buildingId")) {
            sqlBuilder.append("building_id, ");
            valuesBuilder.append("?, ");
            params.add(roomData.get("buildingId"));
        }
        if (roomData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(roomData.get("name"));
        }
        if (roomData.containsKey("roomType")) {
            sqlBuilder.append("room_type, ");
            valuesBuilder.append("?, ");
            params.add(roomData.get("roomType"));
        }
        if (roomData.containsKey("capacity")) {
            sqlBuilder.append("capacity, ");
            valuesBuilder.append("?, ");
            params.add(roomData.get("capacity"));
        }
        if (roomData.containsKey("roomFeatures")) {
            sqlBuilder.append("room_features, ");
            valuesBuilder.append("?, ");
            params.add(roomData.get("roomFeatures"));
        }

        // Add default active status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");

        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Update room information.
     * @param roomId Room ID.
     * @param updates Fields and values to update.
     * @return Whether the update was successful.
     * @throws SQLException SQL exception.
     */
    public boolean updateRoom(int roomId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }

        StringBuilder sqlBuilder = new StringBuilder("UPDATE rooms SET ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }

        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove the last comma and space
        sqlBuilder.append(" WHERE room_id = ?");
        params.add(roomId);

        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }

    /**
     * Delete a room (soft delete).
     * @param roomId Room ID.
     * @return Whether the deletion was successful.
     * @throws SQLException SQL exception.
     */
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "UPDATE rooms SET active_flag = 'N' WHERE room_id = ?";
        int result = executeUpdate(sql, roomId);
        return result > 0;
    }

    /**
     * Query rooms based on conditions.
     * @param conditions Query conditions.
     * @return List of rooms.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> queryRooms(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
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
     * Paginated query for rooms.
     * @param page Page number.
     * @param pageSize Page size.
     * @param conditions Query conditions.
     * @param sortField Sort field.
     * @param sortOrder Sort order.
     * @return Paginated query results.
     * @throws SQLException SQL exception.
     */
    public Map<String, Object> getRoomsByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY room_id ASC");
        }

        // Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        // Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());

        // Get total count
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM rooms WHERE 1=1");
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
     * Get room details by ID.
     * @param roomId Room ID.
     * @return Room details.
     * @throws SQLException SQL exception.
     */
    public Map<String, Object> getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, roomId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get list of rooms by building ID.
     * @param buildingId Building ID.
     * @return List of rooms.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> getRoomsByBuilding(int buildingId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE building_id = ? AND active_flag = 'Y' ORDER BY name";
        return executeQuery(sql, buildingId);
    }

    /**
     * Get list of rooms by room type.
     * @param roomType Room type.
     * @return List of rooms.
     * @throws SQLException SQL exception.
     */
    public List<Map<String, Object>> getRoomsByType(String roomType) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_type = ? AND active_flag = 'Y' ORDER BY building_id, name";
        return executeQuery(sql, roomType);
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
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}