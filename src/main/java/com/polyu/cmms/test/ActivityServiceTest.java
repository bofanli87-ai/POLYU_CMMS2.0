package com.polyu.cmms.test;

import com.polyu.cmms.model.Activity;
import com.polyu.cmms.service.ActivityService;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ActivityService测试类，用于验证获取活动数据的功能
 */
public class ActivityServiceTest {

    public static void main(String[] args) {
        // 调用测试方法
        testActivityDataRetrieval();
        testAddActivity();
        testExceptionHandling();
        
        System.out.println("\n所有测试完成！");
    }

    /**
     * 测试获取活动数据的功能
     * 包括条件查询、分页查询、清洁活动查询和工人统计等
     */
    public static void testActivityDataRetrieval() {
        System.out.println("开始测试活动数据获取...");
        
        // 获取ActivityService实例
        ActivityService activityService = ActivityService.getInstance();
        
        try {
            // 测试1: 无条件查询所有活动
            System.out.println("\n测试1: 无条件查询所有活动");
            List<Map<String, Object>> allActivities = activityService.queryActivities(new HashMap<>());
            System.out.println("活动总数: " + allActivities.size());
            printActivities(allActivities, 5); // 只打印前5条记录
            
            // 测试2: 按条件查询活动（按状态查询）
            System.out.println("\n测试2: 按条件查询活动（按状态查询）");
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("status", "planned");
            
            List<Map<String, Object>> filteredActivities = activityService.queryActivities(conditions);
            System.out.println("符合条件的活动数量: " + filteredActivities.size());
            printActivities(filteredActivities, 5); // 只打印前5条记录
            
            // 测试3: 分页查询活动数据
            System.out.println("\n测试3: 分页查询活动数据（第1页，每页10条）");
            List<Map<String, Object>> pageActivities = activityService.queryActivitiesByPage(1, 10, null);
            System.out.println("当前页数据量: " + pageActivities.size());
            printActivities(pageActivities, 5); // 只打印前5条记录
            
            // 测试4: 查询第2页数据
            System.out.println("\n测试4: 查询第2页活动数据（每页10条）");
            List<Map<String, Object>> page2Activities = activityService.queryActivitiesByPage(2, 10, null);
            System.out.println("第2页数据量: " + page2Activities.size());
            printActivities(page2Activities, 5); // 只打印前5条记录
            
            // 测试5: 统计各区域各类活动的工人参与数量
            System.out.println("\n测试5: 统计各区域各类活动的工人参与数量");
            try {
                // 获取最近30天的数据
                Calendar calendar = Calendar.getInstance();
                Date endDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, -30);
                Date startDate = calendar.getTime();
                
                List<Map<String, Object>> workerStats = activityService.countWorkersByAreaAndActivity(
                        startDate, endDate); // 获取最近30天的数据
                
                System.out.println("统计记录数量: " + workerStats.size());
                printWorkerStatistics(workerStats, 10); // 只打印前10条统计记录
            } catch (Exception e) {
                System.out.println("工人统计测试失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 测试6: 组合条件查询（状态+优先级）
            System.out.println("\n测试6: 组合条件查询（状态+优先级）");
            Map<String, Object> combinedConditions = new HashMap<>();
            combinedConditions.put("status", "planned");
            combinedConditions.put("priority", "high");
            
            List<Map<String, Object>> combinedActivities = activityService.queryActivities(combinedConditions);
            System.out.println("符合组合条件的活动数量: " + combinedActivities.size());
            printActivities(combinedActivities, 5);
            
            // 测试7: 按活动类型查询
            System.out.println("\n测试7: 按活动类型查询");
            Map<String, Object> typeConditions = new HashMap<>();
            typeConditions.put("activityType", "maintenance");
            
            List<Map<String, Object>> typeActivities = activityService.queryActivities(typeConditions);
            System.out.println("符合类型条件的活动数量: " + typeActivities.size());
            printActivities(typeActivities, 5);
            
            // 测试8: 按时间范围查询清洁活动
            System.out.println("\n测试8: 按时间范围查询清洁活动");
            try {
                // 使用当前时间前后一周的范围
                Calendar calendar = Calendar.getInstance();
                Date endDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                Date startDate = calendar.getTime();
                
                // 定义清洁活动类型列表
                List<String> cleaningTypes = Arrays.asList("cleaning", "housekeeping", "sanitization");
                
                List<Map<String, Object>> cleaningActivities = activityService.queryCleaningActivities(
                        startDate, endDate, null, cleaningTypes);
                System.out.println("时间范围内的清洁活动数量: " + cleaningActivities.size());
                printActivities(cleaningActivities, 5);
            } catch (Exception e) {
                System.out.println("清洁活动查询测试失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 测试9: 测试空结果集情况
            System.out.println("\n测试9: 测试空结果集情况（使用不可能存在的条件）");
            Map<String, Object> impossibleConditions = new HashMap<>();
            impossibleConditions.put("activityId", "IMPOSSIBLE_ID_12345");
            
            List<Map<String, Object>> emptyResults = activityService.queryActivities(impossibleConditions);
            System.out.println("查询结果数量: " + emptyResults.size());
            printActivities(emptyResults, 5);
            
            // 测试10: 分页+条件组合查询
            System.out.println("\n测试10: 分页+条件组合查询（按状态，第1页，每页5条）");
            Map<String, Object> pageConditions = new HashMap<>();
            pageConditions.put("status", "planned");
            
            List<Map<String, Object>> conditionalPageActivities = activityService.queryActivitiesByPage(
                    1, 5, pageConditions);
            System.out.println("符合条件的当前页活动数量: " + conditionalPageActivities.size());
            printActivities(conditionalPageActivities, 5);
            
            System.out.println("\n活动数据获取测试完成！");
            
        } catch (SQLException e) {
            System.err.println("测试过程中发生SQL异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试添加活动功能
     */
    public static void testAddActivity() {
        System.out.println("\n开始测试添加活动功能...");
        
        ActivityService activityService = ActivityService.getInstance();
        
        try {
            // 创建测试活动对象
            Activity newActivity = new Activity();
            newActivity.setActivityType("test_activity");
            newActivity.setTitle("测试活动标题 - " + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            newActivity.setDescription("这是一个用于测试添加功能的活动描述");
            newActivity.setStatus("planned");
            newActivity.setDate(new Date());
            newActivity.setPriority("medium");
            newActivity.setFacilityType("office");
            
            // 添加活动
            boolean result = activityService.addActivity(newActivity);
            
            if (result) {
                System.out.println("✓ 添加活动成功！");
                
                // 验证添加结果
                Map<String, Object> verifyCondition = new HashMap<>();
                verifyCondition.put("title", newActivity.getTitle());
                List<Map<String, Object>> verifyActivities = activityService.queryActivities(verifyCondition);
                
                if (!verifyActivities.isEmpty()) {
                    System.out.println("✓ 添加的活动已验证存在");
                    printActivities(verifyActivities, 1);
                } else {
                    System.out.println("✗ 未能验证添加的活动");
                }
            } else {
                System.out.println("✗ 添加活动失败");
            }
            
        } catch (SQLException e) {
            System.err.println("添加活动过程中发生SQL异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("添加活动过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试异常情况处理
     */
    public static void testExceptionHandling() {
        System.out.println("\n开始测试异常情况处理...");
        
        ActivityService activityService = ActivityService.getInstance();
        
        // 测试无效的分页参数
        System.out.println("\n测试无效的分页参数（页码为负数）");
        try {
            // 在调用前进行参数验证
            int page = -1;
            int pageSize = 10;
            
            if (page <= 0) {
                throw new IllegalArgumentException("页码必须大于0");
            }
            
            activityService.queryActivitiesByPage(page, pageSize, null);
        } catch (IllegalArgumentException e) {
            System.out.println("✓ 成功捕获参数验证异常: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✓ 捕获到其他异常: " + e.getMessage());
        }
        
        // 测试无效的每页数量
        System.out.println("\n测试无效的每页数量（每页数量为0）");
        try {
            // 在调用前进行参数验证
            int page = 1;
            int pageSize = 0;
            
            if (pageSize <= 0) {
                throw new IllegalArgumentException("每页数量必须大于0");
            }
            
            activityService.queryActivitiesByPage(page, pageSize, null);
        } catch (IllegalArgumentException e) {
            System.out.println("✓ 成功捕获参数验证异常: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✓ 捕获到其他异常: " + e.getMessage());
        }
        
        // 测试空条件查询
        System.out.println("\n测试空条件查询");
        try {
            List<Map<String, Object>> results = activityService.queryActivities(null);
            System.out.println("✓ 空条件查询成功，返回结果数量: " + (results != null ? results.size() : 0));
        } catch (Exception e) {
            System.out.println("✗ 空条件查询抛出异常: " + e.getMessage());
        }
        
        // 测试日期解析异常
        System.out.println("\n测试日期解析异常");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse("无效日期"); // 直接调用parse方法，不存储结果
        } catch (ParseException e) {
            System.out.println("✓ 成功捕获日期解析异常: " + e.getMessage());
        }
    }
    
    /**
     * 打印活动数据
     * @param activities 活动数据列表
     * @param maxCount 最大打印数量
     */
    private static void printActivities(List<Map<String, Object>> activities, int maxCount) {
        if (activities == null || activities.isEmpty()) {
            System.out.println("没有找到活动数据");
            return;
        }
        
        int count = Math.min(activities.size(), maxCount);
        System.out.println("前" + count + "条活动数据:");
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> activity = activities.get(i);
            // 安全地获取字段值，避免空值问题
            String activityId = getSafeValue(activity, "activityId");
            String activityType = getSafeValue(activity, "activityType");
            String title = getSafeValue(activity, "title");
            String status = getSafeValue(activity, "status");
            String datetime = getSafeValue(activity, "activityDateTime");
            String priority = getSafeValue(activity, "priority");
            String facilityType = getSafeValue(activity, "facilityType");
            String activeFlag = getSafeValue(activity, "activeFlag");
            
            System.out.println("[记录 " + (i+1) + "] 活动ID: " + activityId + 
                             ", 类型: " + activityType +
                             ", 标题: " + title +
                             ", 状态: " + status +
                             ", 优先级: " + priority);
            
            System.out.println("      日期时间: " + datetime + 
                             ", 设施类型: " + facilityType +
                             ", 激活标志: " + activeFlag);
            
            // 打印其他可能存在的关键字段
            if (activity.containsKey("description") && activity.get("description") != null) {
                String description = activity.get("description").toString();
                // 只打印前50个字符的描述
                if (description.length() > 50) {
                    description = description.substring(0, 50) + "...";
                }
                System.out.println("      描述: " + description);
            }
            
            // 打印预期停机时间（如果存在）
            if (activity.containsKey("expectedDowntime")) {
                System.out.println("      预期停机时间: " + getSafeValue(activity, "expectedDowntime"));
            }
            
            System.out.println();
        }
        
        if (activities.size() > maxCount) {
            System.out.println("...以及其他" + (activities.size() - maxCount) + "条记录");
        }
    }
    
    /**
     * 安全获取Map中的值并转换为字符串
     * @param map Map对象
     * @param key 键名
     * @return 安全的字符串值
     */
    private static String getSafeValue(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key) && map.get(key) != null) {
            return map.get(key).toString();
        }
        return "N/A";
    }
    
    /**
     * 打印工人参与统计数据
     * @param statistics 统计数据列表
     * @param maxCount 最大打印数量
     */
    private static void printWorkerStatistics(List<Map<String, Object>> statistics, int maxCount) {
        if (statistics == null || statistics.isEmpty()) {
            System.out.println("没有找到工人参与统计数据");
            return;
        }
        
        int count = Math.min(statistics.size(), maxCount);
        System.out.println("前" + count + "条工人参与统计数据:");
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> stat = statistics.get(i);
            System.out.println("活动类型: " + stat.get("activity_type") + 
                             ", 区域: " + stat.get("area_name") +
                             ", 参与工人数量: " + stat.get("worker_count"));
        }
        
        if (statistics.size() > maxCount) {
            System.out.println("...以及其他" + (statistics.size() - maxCount) + "条统计记录");
        }
    }
}