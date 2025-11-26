package com.polyu.cmms.service;

import com.polyu.cmms.model.Activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 活动服务类，提供活动相关的数据访问功能
 */
public class ActivityService extends BaseService {

    // 1. 私有静态成员变量：存储类的唯一实例（单例模式核心）
    private static ActivityService instance;

    // 2. 私有构造函数：防止外部通过 new 创建实例
    private ActivityService() {
        // 可添加初始化逻辑（如加载配置、初始化连接池等）
    }

    // 3. 公共静态方法：提供全局唯一的实例访问点
    public static synchronized ActivityService getInstance() {
        if (instance == null) { // 懒汉式初始化：首次调用时才创建实例
            instance = new ActivityService();
        }
        return instance;
    }

    /**
     * 添加活动
     * @param activity 活动对象
     * @return 添加是否成功
     * @throws SQLException SQL异常
     */
    public boolean addActivity(Activity activity) throws SQLException {
        String sql = "INSERT INTO activity (activity_type, title, description, status, priority, hazard_level, activity_datetime, expected_unavailable_duration, created_by_staff_id, weather_id, area_id, building_id, facility_type, active_flag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        int result = executeUpdate(sql, 
            activity.getActivityType(), activity.getTitle(), activity.getDescription(),
            activity.getStatus(), "medium", // 默认优先级
            activity.getHazardLevel(), activity.getDate(),
            activity.getExpectedDowntime(), activity.getCreatedByStaffId(),
            activity.getWeatherId(), activity.getAreaId(), activity.getBuildingId(),
            "none", // 默认设施类型
            "Y");
        
        return result > 0;
    }

    /**
     * 更新活动状态
     * @param activityId 活动ID
     * @param status 新状态
     * @return 更新是否成功
     * @throws SQLException SQL异常
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
     * 按条件查询活动
     * @param conditions 查询条件
     * @return 活动列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> queryActivities(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            // 特殊字段映射
            String key = entry.getKey();
            String dbColumn;
            
            // 手动映射特殊字段
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
                    // 常规驼峰命名转换为下划线命名
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
     * 分页查询活动
     * @param page 页码（从1开始）
     * @param pageSize 每页显示数量
     * @param conditions 查询条件（可为null）
     * @return 分页查询结果（活动列表）
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> queryActivitiesByPage(int page, int pageSize, Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y'");
        List<Object> params = new java.util.ArrayList<>();
        
        // 处理可能为null的conditions
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                // 特殊字段映射
                    String key = entry.getKey();
                    String dbColumn;
                    
                    // 手动映射特殊字段
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
                            // 常规驼峰命名转换为下划线命名
                            dbColumn = key.replaceAll("([A-Z])", "_$1").toLowerCase();
                            break;
                    }
                
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }

        // 分页逻辑（LIMIT 每页数量 OFFSET 起始索引）
        int offset = (page - 1) * pageSize;
        sqlBuilder.append(" ORDER BY activity_datetime DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * 按时间段和建筑物查询清洁活动
     * @param startTime 开始时间（可为null）
     * @param endTime 结束时间（可为null）
     * @param buildingId 建筑物ID（可为null）
     * @param activityTypes 活动类型列表（如 ["cleaning", "deep_cleaning"]）
     * @return 清洁活动列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> queryCleaningActivities(Date startTime, Date endTime, Integer buildingId, List<String> activityTypes) throws SQLException {
        if (activityTypes == null || activityTypes.isEmpty()) {
            throw new IllegalArgumentException("活动类型列表不能为空");
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity WHERE active_flag = 'Y' AND activity_type IN (");
        // 动态生成 IN 条件的占位符（如 ?, ?, ?）
        sqlBuilder.append(String.join(",", java.util.Collections.nCopies(activityTypes.size(), "?")));
        sqlBuilder.append(")");

        List<Object> params = new java.util.ArrayList<>(activityTypes);

        // 处理时间段条件
        if (startTime != null) {
            sqlBuilder.append(" AND activity_datetime >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sqlBuilder.append(" AND activity_datetime <= ?");
            params.add(endTime);
        }

        // 处理建筑物ID条件
        if (buildingId != null) {
            sqlBuilder.append(" AND building_id = ?");
            params.add(buildingId);
        }

        sqlBuilder.append(" ORDER BY activity_datetime");

        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * 统计各区域各类活动的工人参与数量
     * @param startTime 开始时间（可为null）
     * @param endTime 结束时间（可为null）
     * @return 统计结果（包含 activity_type, area_name, worker_count）
     * @throws SQLException SQL异常
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

        // 处理时间段条件
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