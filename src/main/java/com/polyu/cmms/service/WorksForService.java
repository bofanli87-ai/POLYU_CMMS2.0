package com.polyu.cmms.service;

import com.polyu.cmms.model.WorksFor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WorksFor服务类，提供员工参与活动关联关系的管理功能
 */
public class WorksForService extends BaseService {

    /**
     * 添加员工到活动
     * @param worksFor WorksFor对象
     * @return 添加是否成功
     * @throws SQLException SQL异常
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
     * 从活动中移除员工（软删除）
     * @param worksForId 关联ID
     * @return 移除是否成功
     * @throws SQLException SQL异常
     */
    public boolean removeStaffFromActivity(Integer worksForId) throws SQLException {
        String sql = "UPDATE works_for SET active_flag = 'N' WHERE works_for_id = ?";
        int result = executeUpdate(sql, worksForId);
        return result > 0;
    }

    /**
     * 通过员工ID和活动ID移除关联（软删除）
     * @param staffId 员工ID
     * @param activityId 活动ID
     * @return 移除是否成功
     * @throws SQLException SQL异常
     */
    public boolean removeStaffFromActivityByStaffAndActivity(Integer staffId, Integer activityId) throws SQLException {
        String sql = "UPDATE works_for SET active_flag = 'N' WHERE staff_id = ? AND activity_id = ? AND active_flag = 'Y'";
        int result = executeUpdate(sql, staffId, activityId);
        return result > 0;
    }

    /**
     * 更新员工在活动中的职责
     * @param worksForId 关联ID
     * @param newResponsibility 新职责
     * @return 更新是否成功
     * @throws SQLException SQL异常
     */
    public boolean updateStaffResponsibility(Integer worksForId, String newResponsibility) throws SQLException {
        String sql = "UPDATE works_for SET activity_responsibility = ? WHERE works_for_id = ? AND active_flag = 'Y'";
        int result = executeUpdate(sql, newResponsibility, worksForId);
        return result > 0;
    }

    /**
     * 查询活动的所有参与员工
     * @param activityId 活动ID
     * @return 参与员工列表
     * @throws SQLException SQL异常
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
     * 查询员工参与的所有活动
     * @param staffId 员工ID
     * @return 参与活动列表
     * @throws SQLException SQL异常
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
     * 根据条件查询员工与活动的关联
     * @param conditions 查询条件
     * @return 关联列表
     * @throws SQLException SQL异常
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
     * 批量添加员工到活动
     * @param staffIds 员工ID列表
     * @param activityId 活动ID
     * @param responsibility 职责描述
     * @return 成功添加的数量
     * @throws SQLException SQL异常
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
        
        // 计算成功添加的数量
        int successCount = 0;
        for (int result : results) {
            if (result > 0) {
                successCount++;
            }
        }
        
        return successCount;
    }

    /**
     * 检查员工是否已参与活动
     * @param staffId 员工ID
     * @param activityId 活动ID
     * @return 是否已参与
     * @throws SQLException SQL异常
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
     * 获取活动参与人数统计
     * @param activityId 活动ID
     * @return 参与人数
     * @throws SQLException SQL异常
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