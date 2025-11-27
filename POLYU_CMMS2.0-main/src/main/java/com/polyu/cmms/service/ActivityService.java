package com.polyu.cmms.service;

import com.polyu.cmms.model.Activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Activity service class, provides data access functions related to activities
 */
public class ActivityService extends BaseService {

    // 1. Private static member variable: stores the unique instance of the class (core of singleton pattern)
    private static ActivityService instance;

    // 2. Private constructor: prevents external instance creation via new
    private ActivityService() {
        // Can add initialization logic (such as loading configuration, initializing connection pool, etc.)
    }

    // 3. Public static method: provides a global unique instance access point
    public static synchronized ActivityService getInstance() {
        if (instance == null) { // Lazy initialization: create instance only when first called
            instance = new ActivityService();
        }
        return instance;
    }

    /**
     * Add activity
     * @param activity Activity object
     * @return Whether the addition was successful
     * @throws SQLException SQL exception
     */
    public boolean addActivity(Activity activity) throws SQLException {
        String sql = "INSERT INTO activity (activity_type, title, description, status, priority, hazard_level, activity_datetime, expected_unavailable_duration, created_by_staff_id, weather_id, area_id, building_id, facility_type, active_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int result = executeUpdate(sql,
                activity.getActivityType(), activity.getTitle(), activity.getDescription(),
                activity.getStatus(), "medium", // Default priority
                activity.getHazardLevel(), activity.getDate(),
                activity.getExpectedDowntime(), activity.getCreatedByStaffId(),
                activity.getWeatherId(), activity.getAreaId(), activity.getBuildingId(),
                "none", // Default facility type
                "Y");

        return result > 0;
    }

    /**
     * Update activity status
     * @param activityId Activity ID
     * @param status New status
     * @return Whether the update was successful
     * @throws SQLException SQL exception
     */
    public boolean updateActivityStatus(int activityId, String status) throws SQLException {
        String sql;
        List<Object> params = new ArrayList<>();

        if ("completed".equals(status)) {
            sql = "UPDATE activity SET status = ?, actual_completion_datetime = NOW() WHERE activity_id = ?";
            params.add(status);
            params.add(activityId);
        } else {
            sql = "UPDATE activity SET status = ? WHERE activity_id = ?";
            params.add(status);
            params.add(activityId);
        }

        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }

    /**
     * Query activities by conditions
     * @param conditions Query conditions
     * @return Activity list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryActivities(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            // Special field mapping
            String key = entry.getKey();
            String dbColumn;

            // Manual mapping for special fields
            switch (key) {
                case "date":
                    dbColumn = "activity_datetime";
                    break;
                case "expectedDowntime":
                    dbColumn = "expected_unavailable_duration";
                    break;
                case "createdByStaffId":
                    dbColumn = "created_by_staff_id";
                    break;
                case "actualCompletionDatetime":
                    dbColumn = "actual_completion_datetime";
                    break;
                case "activityId":
                    dbColumn = "activity_id";
                    break;
                case "activityType":
                    dbColumn = "activity_type";
                    break;
                case "buildingId":
                    dbColumn = "building_id";
                    break;
                case "weatherId":
                    dbColumn = "weather_id";
                    break;
                case "areaId":
                    dbColumn = "area_id";
                    break;
                case "roomId":
                    dbColumn = "room_id";
                    break;
                case "levelId":
                    dbColumn = "level_id";
                    break;
                case "squareId":
                    dbColumn = "square_id";
                    break;
                case "gateId":
                    dbColumn = "gate_id";
                    break;
                case "canteenId":
                    dbColumn = "canteen_id";
                    break;
                case "hazardLevel":
                    dbColumn = "hazard_level";
                    break;
                case "facilityType":
                    dbColumn = "facility_type";
                    break;
                case "activeFlag":
                    dbColumn = "active_flag";
                    break;
                default:
                    // Regular camel case to snake case conversion
                    dbColumn = key.replaceAll("([A-Z])", "_$1").toLowerCase();
                    break;
            }

            sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
            params.add(entry.getValue());
        }

        sqlBuilder.append(" ORDER BY activity_datetime DESC");

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * Query activities by page
     * @param page Page number (starting from 1)
     * @param pageSize Number of items per page
     * @param conditions Query conditions (can be null)
     * @return Paginated query result (activity list)
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryActivitiesByPage(int page, int pageSize, Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        // Handle possibly null conditions
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                // Special field mapping
                String key = entry.getKey();
                String dbColumn;

                // Manual mapping for special fields
                switch (key) {
                    case "date":
                        dbColumn = "activity_datetime";
                        break;
                    case "expectedDowntime":
                        dbColumn = "expected_unavailable_duration";
                        break;
                    case "createdByStaffId":
                        dbColumn = "created_by_staff_id";
                        break;
                    case "actualCompletionDatetime":
                        dbColumn = "actual_completion_datetime";
                        break;
                    case "activityId":
                        dbColumn = "activity_id";
                        break;
                    case "activityType":
                        dbColumn = "activity_type";
                        break;
                    case "buildingId":
                        dbColumn = "building_id";
                        break;
                    case "weatherId":
                        dbColumn = "weather_id";
                        break;
                    case "areaId":
                        dbColumn = "area_id";
                        break;
                    case "roomId":
                        dbColumn = "room_id";
                        break;
                    case "levelId":
                        dbColumn = "level_id";
                        break;
                    case "squareId":
                        dbColumn = "square_id";
                        break;
                    case "gateId":
                        dbColumn = "gate_id";
                        break;
                    case "canteenId":
                        dbColumn = "canteen_id";
                        break;
                    case "hazardLevel":
                        dbColumn = "hazard_level";
                        break;
                    case "facilityType":
                        dbColumn = "facility_type";
                        break;
                    case "activeFlag":
                        dbColumn = "active_flag";
                        break;
                    default:
                        // Regular camel case to snake case conversion
                        dbColumn = key.replaceAll("([A-Z])", "_$1").toLowerCase();
                        break;
                }

                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }

        // Pagination logic (LIMIT pageSize OFFSET startIndex)
        int offset = (page - 1) * pageSize;
        sqlBuilder.append(" ORDER BY activity_datetime DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * Query cleaning activities by time period and building
     * @param startTime Start time (can be null)
     * @param endTime End time (can be null)
     * @param buildingId Building ID (can be null)
     * @param activityTypes Activity type list (e.g., ["cleaning", "deep_cleaning"])
     * @return Cleaning activity list
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryCleaningActivities(Date startTime, Date endTime, Integer buildingId, List<String> activityTypes) throws SQLException {
        if (activityTypes == null || activityTypes.isEmpty()) {
            throw new IllegalArgumentException("Activity type list cannot be empty");
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y' AND activity_type IN (");
        // Dynamically generate IN condition placeholders (e.g., ?, ?, ?)
        sqlBuilder.append(String.join(",", java.util.Collections.nCopies(activityTypes.size(), "?")));
        sqlBuilder.append(")");

        List<Object> params = new java.util.ArrayList<>(activityTypes);

        // Handle time period conditions
        if (startTime != null) {
            sqlBuilder.append(" AND activity_datetime >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sqlBuilder.append(" AND activity_datetime <= ?");
            params.add(endTime);
        }

        // Handle building ID condition
        if (buildingId != null) {
            sqlBuilder.append(" AND building_id = ?");
            params.add(buildingId);
        }

        sqlBuilder.append(" ORDER BY activity_datetime");

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * Count the number of workers participating in various activities by area
     * @param startTime Start time (can be null)
     * @param endTime End time (can be null)
     * @return Statistical results (including activity_type, area_name, worker_count)
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> countWorkersByAreaAndActivity(Date startTime, Date endTime) throws SQLException {
        String sql = """
            SELECT 
                a.activity_type,
                CASE 
                    WHEN a.facility_type = 'building' THEN b.building_code
                    WHEN a.facility_type = 'room' THEN r.name
                    WHEN a.facility_type = 'level' THEN CONCAT(b.building_code, '-', l.level_number)
                    WHEN a.facility_type = 'square' THEN s.name
                    WHEN a.facility_type = 'gate' THEN g.name
                    WHEN a.facility_type = 'canteen' THEN c.name
                    ELSE 'Other'
                END as area_name,
                COUNT(wfs.staff_id) as worker_count
            FROM 
                activity a
            LEFT JOIN 
                works_for wfs ON a.activity_id = wfs.activity_id AND wfs.active_flag = 'Y'
            LEFT JOIN 
                buildings b ON a.building_id = b.building_id
            LEFT JOIN 
                rooms r ON a.room_id = r.room_id
            LEFT JOIN 
                levels l ON a.level_id = l.level_id
            LEFT JOIN 
                squares s ON a.square_id = s.square_id
            LEFT JOIN 
                gates g ON a.gate_id = g.gate_id
            LEFT JOIN 
                canteen c ON a.canteen_id = c.canteen_id
            WHERE 
                a.active_flag = 'Y' AND a.status = 'completed'
        """;

        List<Object> params = new java.util.ArrayList<>();

        // Handle time period conditions
        if (startTime != null) {
            sql += " AND a.activity_datetime >= ?";
            params.add(startTime);
        }
        if (endTime != null) {
            sql += " AND a.activity_datetime <= ?";
            params.add(endTime);
        }

        sql += " GROUP BY a.activity_type, area_name ORDER BY worker_count DESC";

        return executeQuery(sql, params.toArray());
    }
}