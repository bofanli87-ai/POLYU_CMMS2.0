
package com.polyu.cmms.service;

import com.polyu.cmms.model.WorksFor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WorksFor service class, provides management functions for employee-activity association relationships
 */
public class WorksForService extends BaseService {
    private static WorksForService instance;

    // Singleton pattern
    private WorksForService() {}

    public static synchronized WorksForService getInstance() {
        if (instance == null) {
            instance = new WorksForService();
        }
        return instance;
    }

    /**
     * Add staff to activity
     * @param worksFor WorksFor object
     * @return Whether the addition was successful
     * @throws SQLException SQL exception
     */
    public boolean addStaffToActivity(WorksFor worksFor) throws SQLException {
        String sql = "INSERT INTO works_for (staff_id, activity_id, activity_responsibility, assigned_datetime, active_flag) " +
                "VALUES (?, ?, ?, ?, ?)";

        Date assignedDatetime = worksFor.getAssignedDatetime() != null ?
                worksFor.getAssignedDatetime() : new Date();
        String activeFlag = worksFor.getActiveFlag() != null ?
                worksFor.getActiveFlag() : "Y";

        int result = executeUpdate(sql,
                worksFor.getStaffId(),
                worksFor.getActivityId(),
                worksFor.getActivityResponsibility(),
                assignedDatetime,
                activeFlag);

        return result > 0;
    }

    /**
     * Remove staff from activity (soft delete)
     * @param worksForId Association ID
     * @return Whether the removal was successful
     * @throws SQLException SQL exception
     */
    public boolean removeStaffFromActivity(Integer worksForId) throws SQLException {
        String sql = "UPDATE works_for SET active_flag = 'N' WHERE works_for_id = ?";
        int result = executeUpdate(sql, worksForId);
        return result > 0;
    }

    /**
     * Remove association by staff ID and activity ID (soft delete)
     * @param staffId Staff ID
     * @param activityId Activity ID
     * @return Whether the removal was successful
     * @throws SQLException SQL exception
     */
    public boolean removeStaffFromActivityByStaffAndActivity(Integer staffId, Integer activityId) throws SQLException {
        String sql = "UPDATE works_for SET active_flag = 'N' WHERE staff_id = ? AND activity_id = ? AND active_flag = 'Y'";
        int result = executeUpdate(sql, staffId, activityId);
        return result > 0;
    }

    /**
     * Update staff responsibility in activity
     * @param worksForId Association ID
     * @param newResponsibility New responsibility
     * @return Whether the update was successful
     * @throws SQLException SQL exception
     */
    public boolean updateStaffResponsibility(Integer worksForId, String newResponsibility) throws SQLException {
        String sql = "UPDATE works_for SET activity_responsibility = ? WHERE works_for_id = ? AND active_flag = 'Y'";
        int result = executeUpdate(sql, newResponsibility, worksForId);
        return result > 0;
    }

    /**
     * Query all participating staff of an activity
     * @param activityId Activity ID
     * @return List of participating staff
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryStaffByActivityId(Integer activityId) throws SQLException {
        String sql = """
            SELECT wf.works_for_id, wf.staff_id, CONCAT(s.first_name, ' ', s.last_name) as staff_name, s.role_id as role, 
                   wf.activity_responsibility, wf.assigned_datetime
            FROM works_for wf
            JOIN staff s ON wf.staff_id = s.staff_id
            WHERE wf.activity_id = ? AND wf.active_flag = 'Y'
            ORDER BY wf.assigned_datetime DESC
        """;
        return executeQuery(sql, activityId);
    }

    /**
     * Query all activities a staff member participates in
     * @param staffId Staff ID
     * @return List of participating activities
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryActivitiesByStaffId(Integer staffId) throws SQLException {
        String sql = """
            SELECT wf.works_for_id, wf.activity_id, a.title as activity_title, a.activity_type, 
                   a.status, wf.activity_responsibility, wf.assigned_datetime, a.activity_datetime
            FROM works_for wf
            JOIN activity a ON wf.activity_id = a.activity_id
            WHERE wf.staff_id = ? AND wf.active_flag = 'Y' AND a.active_flag = 'Y'
            ORDER BY a.activity_datetime DESC
        """;
        return executeQuery(sql, staffId);
    }

    /**
     * Query staff-activity associations based on conditions
     * @param conditions Query conditions
     * @return List of associations
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryWorksFor(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT wf.*, CONCAT(s.first_name, ' ', s.last_name) as staff_name, a.title as activity_title
            FROM works_for wf
            LEFT JOIN staff s ON wf.staff_id = s.staff_id
            LEFT JOIN activity a ON wf.activity_id = a.activity_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (conditions != null) {
            if (conditions.containsKey("worksForId")) {
                sqlBuilder.append(" AND wf.works_for_id = ?");
                params.add(conditions.get("worksForId"));
            }
            if (conditions.containsKey("staffId")) {
                sqlBuilder.append(" AND wf.staff_id = ?");
                params.add(conditions.get("staffId"));
            }
            if (conditions.containsKey("activityId")) {
                sqlBuilder.append(" AND wf.activity_id = ?");
                params.add(conditions.get("activityId"));
            }
            if (conditions.containsKey("activeFlag")) {
                sqlBuilder.append(" AND wf.active_flag = ?");
                params.add(conditions.get("activeFlag"));
            }
        }

        sqlBuilder.append(" ORDER BY wf.assigned_datetime DESC");

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * Batch add staff to activity
     * @param staffIds List of staff IDs
     * @param activityId Activity ID
     * @param responsibility Responsibility description
     * @return Number of successful additions
     * @throws SQLException SQL exception
     */
    public int batchAddStaffToActivity(List<Integer> staffIds, Integer activityId, String responsibility) throws SQLException {
        List<Object[]> paramsList = new ArrayList<>();
        Date now = new Date();

        for (Integer staffId : staffIds) {
            paramsList.add(new Object[]{
                    staffId, activityId, responsibility, now, "Y"
            });
        }

        String sql = "INSERT INTO works_for (staff_id, activity_id, activity_responsibility, assigned_datetime, active_flag) " +
                "VALUES (?, ?, ?, ?, ?)";

        int[] results = executeBatch(sql, paramsList);

        // Calculate the number of successful additions
        int successCount = 0;
        for (int result : results) {
            if (result > 0) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * Check if staff is already in activity
     * @param staffId Staff ID
     * @param activityId Activity ID
     * @return Whether already participating
     * @throws SQLException SQL exception
     */
    public boolean isStaffInActivity(Integer staffId, Integer activityId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM works_for WHERE staff_id = ? AND activity_id = ? AND active_flag = 'Y'";
        List<Map<String, Object>> results = executeQuery(sql, staffId, activityId);

        if (!results.isEmpty() && results.get(0).containsKey("count")) {
            return ((Number)results.get(0).get("count")).intValue() > 0;
        }

        return false;
    }

    /**
     * Get activity participant count statistics
     * @param activityId Activity ID
     * @return Number of participants
     * @throws SQLException SQL exception
     */
    public int getActivityParticipantCount(Integer activityId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM works_for WHERE activity_id = ? AND active_flag = 'Y'";
        List<Map<String, Object>> results = executeQuery(sql, activityId);

        if (!results.isEmpty() && results.get(0).containsKey("count")) {
            return ((Number)results.get(0).get("count")).intValue();
        }

        return 0;
    }
}