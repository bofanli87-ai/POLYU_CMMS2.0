package com.polyu.cmms.service;

import com.polyu.cmms.util.DatabaseUtil;
import com.polyu.cmms.util.DateUtils;
import com.polyu.cmms.util.HtmlLogger;
import com.polyu.cmms.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReportService extends BaseService {

// ==================== Worker Activity Distribution Report ====================
    public String generateWorkerActivityReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Worker Activity Distribution Report (By Assignment) ===\n");
        report.append("Generated Date: ").append(new Date()).append("\n");
        report.append("Note: 1. Data is based on valid assignment records in the works_for table (active_flag=Y)\n");
        report.append("      2. Progress bar indicates the 'proportion of a single task to the employee's total tasks' (most employees have 2 tasks, so half is displayed)\n");
        report.append("      3. Working hours are only counted for 'completed' status; hours for other statuses are 0\n\n");

        // SQL remains unchanged (uses the previously fixed version)
        String sql = "SELECT " +
                "s.staff_id, " +
                "s.first_name, " +
                "s.last_name, " +
                "COALESCE(a.activity_type, 'No Assigned Activity') AS activity_type, " +
                "COALESCE(w.activity_responsibility, 'No Defined Responsibility') AS activity_responsibility, " +
                "COALESCE(a.status, 'No Status') AS activity_status, " +
                "COALESCE(w.activity_id, 'No Related Activity ID') AS related_activity_id, " +
                "COUNT(a.activity_id) AS task_count, " +
                "SUM( " +
                "   CASE " +
                "       WHEN (a.status = 'completed' OR a.status = '已完成') " +
                "            AND a.actual_completion_datetime IS NOT NULL " +
                "            AND a.actual_completion_datetime > a.activity_datetime " +
                "       THEN TIMESTAMPDIFF(MINUTE, a.activity_datetime, a.actual_completion_datetime) " +
                "       ELSE 0 " +
                "   END " +
                ") AS total_minutes " +
                "FROM staff s " +
                "LEFT JOIN works_for w " +
                "   ON s.staff_id = w.staff_id " +
                "   AND w.active_flag = 'Y' " +
                "LEFT JOIN activity a " +
                "   ON w.activity_id = a.activity_id " +
                "   AND a.active_flag = 'Y' " +
                "GROUP BY s.staff_id, s.first_name, s.last_name, a.activity_type, w.activity_responsibility, a.status, w.activity_id " +
                "ORDER BY s.first_name ASC, s.last_name ASC, task_count DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            Map<String, List<Map<String, Object>>> workerMap = new HashMap<>();
            Map<String, Integer> workerTotalCompletedTasks = new HashMap<>(); // Stores total completed tasks for each employee

            // Step 1: First count total tasks and completed tasks for each employee
            while (rs.next()) {
                String workerName = rs.getString("first_name") + " " + rs.getString("last_name");
                String activityType = rs.getString("activity_type");
                String responsibility = rs.getString("activity_responsibility");
                String activityStatus = rs.getString("activity_status");
                String relatedActivityId = rs.getString("related_activity_id");
                int taskCount = rs.getInt("task_count");
                int totalMinutes = rs.getInt("total_minutes");

                // Count completed tasks for this employee
                int completedCount = ("completed".equals(activityStatus) || "已完成".equals(activityStatus)) ? taskCount : 0;
                workerTotalCompletedTasks.merge(workerName, completedCount, Integer::sum);

                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("type", activityType);
                taskInfo.put("responsibility", responsibility);
                taskInfo.put("status", activityStatus);
                taskInfo.put("count", taskCount);
                taskInfo.put("minutes", totalMinutes);
                taskInfo.put("completedCount", completedCount); // Record if the task is completed

                workerMap.computeIfAbsent(workerName, k -> new ArrayList<>()).add(taskInfo);
            }

            // Step 2: Generate report
            for (Map.Entry<String, List<Map<String, Object>>> entry : workerMap.entrySet()) {
                String workerName = entry.getKey();
                List<Map<String, Object>> tasks = entry.getValue();

                int totalTasks = tasks.stream().mapToInt(t -> (int) t.get("count")).sum();
                int totalCompletedTasks = workerTotalCompletedTasks.getOrDefault(workerName, 0);
                int totalMinutes = tasks.stream().mapToInt(t -> (int) t.get("minutes")).sum();
                double totalHours = totalMinutes / 60.0;

                report.append("========================================\n");
                report.append("👷 Employee: ").append(workerName).append("\n");
                report.append("========================================\n");
                // New: Display employee's overall completion rate
                report.append(String.format("📊 Total Tasks: %d | Completed Tasks: %d | Completion Rate: %.0f%% | Total Working Hours: %.1f hours%n",
                        totalTasks, totalCompletedTasks, (totalTasks > 0 ? (double) totalCompletedTasks / totalTasks * 100 : 0), totalHours));
                report.append("----------------------------------------\n");

                for (Map<String, Object> task : tasks) {
                    String type = (String) task.get("type");
                    String responsibility = (String) task.get("responsibility");
                    String status = (String) task.get("status");
                    int count = (int) task.get("count");
                    int minutes = (int) task.get("minutes");

                    // Keep original logic: Proportion of single task to total tasks
                    double ratio = totalTasks > 0 ? (double) count / totalTasks : 0.0;
                    String progressBar = StringUtils.getProgressBar(ratio, 15);
                    double hours = minutes / 60.0;

                    report.append(String.format("  ▶ Responsibility: %-8s | Type: %-8s | Status: %-6s | Count: %2d (%-15s) | Working Hours: %.1f hours%n",
                            responsibility, type, status, count, progressBar, hours));
                }
                report.append("\n");
            }

            if (workerMap.isEmpty()) {
                report.append("❌ No employee activity assignment records found!\n");
            }

        } catch (SQLException e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!! Error generating Worker Activity Distribution Report (By Assignment) !!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
            HtmlLogger.error("ReportService.generateWorkerActivityReport", "Failed to generate Worker Activity Distribution Report", e);
            return "Report generation failed! Please check logs for details.";
        }

        report.append("=================== End of Report ===================\n");
        return report.toString();
    }


    // ==================== Activity Type Distribution Report ====================
    public String generateActivityTypeReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Activity Type Distribution Report (Detailed Version) ===\n");
        report.append("Generated Date: ").append(new Date()).append("\n");
        report.append("Note: 1. Only valid activities (active_flag=Y) are counted; invalid activities (N) are summarized separately\n");
        report.append("      2. Working hours unit: hours (1 decimal place), only counted for completed and time-valid tasks\n");
        report.append("      3. Incomplete status breakdown: in_progress, planned, and other statuses\n\n");

        // Final optimized SQL:
        // 1. GROUP BY is exactly consistent with the CASE expression for activity_type in SELECT (compatible with ONLY_FULL_GROUP_BY)
        // 2. Simplify redundant logic to ensure compatibility with mainstream databases like MySQL and Oracle
        // 3. Clarify the statistical scope of all fields to avoid ambiguity
        String sql = """
                SELECT
                    -- Activity type grouping key (exactly consistent with GROUP BY)
                    CASE 
                        WHEN active_flag = 'Y' THEN activity_type 
                        ELSE 'Invalid Activities (active_flag=N)' 
                    END AS activity_type,
                    -- 1. Basic Statistics
                    COUNT(*) AS total_tasks,                  -- Total tasks in this group (including valid/invalid)
                    SUM(CASE WHEN active_flag = 'Y' THEN 1 ELSE 0 END) AS valid_tasks,  -- Valid tasks count (Y)
                    -- 2. Completion status statistics (only valid tasks)
                    SUM(CASE WHEN active_flag = 'Y' 
                             AND (status = 'completed' OR status = '已完成') 
                             THEN 1 ELSE 0 END) AS completed_tasks,  -- Completed tasks count
                    SUM(CASE WHEN active_flag = 'Y' 
                             AND (status != 'completed' AND status != '已完成') 
                             THEN 1 ELSE 0 END) AS uncompleted_tasks,-- Incomplete tasks count
                    -- 3. Incomplete status breakdown (only valid tasks)
                    SUM(CASE WHEN active_flag = 'Y' AND status = 'in_progress' THEN 1 ELSE 0 END) AS in_progress_tasks,
                    SUM(CASE WHEN active_flag = 'Y' AND status = 'planned' THEN 1 ELSE 0 END) AS planned_tasks,
                    SUM(CASE WHEN active_flag = 'Y' 
                             AND (status != 'completed' AND status != '已完成') 
                             AND status NOT IN ('in_progress', 'planned') 
                             THEN 1 ELSE 0 END) AS other_uncompleted_tasks,
                    -- 4. Working hours statistics (only valid + completed + reasonable time logic)
                    ROUND(
                        SUM(CASE WHEN active_flag = 'Y' 
                                  AND (status = 'completed' OR status = '已完成') 
                                  AND actual_completion_datetime IS NOT NULL 
                                  AND actual_completion_datetime > activity_datetime 
                                  THEN TIMESTAMPDIFF(MINUTE, activity_datetime, actual_completion_datetime) / 60 
                                  ELSE 0 END),
                        1
                    ) AS total_completed_hours,
                    -- 5. Average time spent (only completed and time-valid tasks)
                    ROUND(
                        CASE 
                            WHEN SUM(CASE WHEN active_flag = 'Y' 
                                        AND (status = 'completed' OR status = '已完成') 
                                        AND actual_completion_datetime IS NOT NULL 
                                        AND actual_completion_datetime > activity_datetime 
                                        THEN 1 ELSE 0 END) > 0 
                            THEN SUM(CASE WHEN active_flag = 'Y' 
                                        AND (status = 'completed' OR status = '已完成') 
                                        AND actual_completion_datetime IS NOT NULL 
                                        AND actual_completion_datetime > activity_datetime 
                                        THEN TIMESTAMPDIFF(MINUTE, activity_datetime, actual_completion_datetime) / 60 
                                        ELSE 0 END) 
                                 / SUM(CASE WHEN active_flag = 'Y' 
                                        AND (status = 'completed' OR status = '已完成') 
                                        AND actual_completion_datetime IS NOT NULL 
                                        AND actual_completion_datetime > activity_datetime 
                                        THEN 1 ELSE 0 END) 
                            ELSE 0 
                        END,
                        1
                    ) AS avg_completed_hours
                FROM activity
                -- GROUP BY core fix: Exactly consistent with the CASE expression for activity_type in SELECT
                GROUP BY 
                    CASE 
                        WHEN active_flag = 'Y' THEN activity_type 
                        ELSE 'Invalid Activities (active_flag=N)' 
                    END
                -- Sort: Valid tasks count descending (priority activities first), invalid activities last
                ORDER BY 
                    SUM(CASE WHEN active_flag = 'Y' THEN 1 ELSE 0 END) DESC,
                    activity_type ASC
                """;

        try (Connection conn = DatabaseUtil.getConnection();  // Replace with your database connection tool
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Overall summary statistics (for users to quickly grasp the overall situation)
            int globalTotalTasks = 0;       // System total tasks
            int globalValidTasks = 0;       // Total valid tasks
            int globalCompletedTasks = 0;   // Completed valid tasks
            double globalTotalHours = 0.0;  // Total completed working hours

            // Traverse query results and splice report
            while (rs.next()) {
                // Get data from ResultSet (one-to-one correspondence with SQL fields)
                String activityType = rs.getString("activity_type");
                int totalTasks = rs.getInt("total_tasks");
                int validTasks = rs.getInt("valid_tasks");
                int completedTasks = rs.getInt("completed_tasks");
                int uncompletedTasks = rs.getInt("uncompleted_tasks");
                int inProgressTasks = rs.getInt("in_progress_tasks");
                int plannedTasks = rs.getInt("planned_tasks");
                int otherUncompletedTasks = rs.getInt("other_uncompleted_tasks");
                double totalCompletedHours = rs.getDouble("total_completed_hours");
                double avgCompletedHours = rs.getDouble("avg_completed_hours");

                // Accumulate overall summary data
                globalTotalTasks += totalTasks;
                globalValidTasks += validTasks;
                globalCompletedTasks += completedTasks;
                globalTotalHours += totalCompletedHours;

                // Calculate core ratios (avoid division by zero exceptions)
                double completionRate = validTasks > 0 ? (double) completedTasks / validTasks * 100 : 0.0;  // Completion rate (%)
                double validRate = totalTasks > 0 ? (double) validTasks / totalTasks * 100 : 0.0;          // Validity rate (%)

                // Status icons (visual distinction to improve readability)
                String completionIcon = completionRate == 100 ? "✅" :  // 100% completed
                        (completionRate >= 80 ? "⚠️" :  // 80%-99% completed
                                (completionRate > 0 ? "🔄" : "❌")); // 0%-79% completed / No valid tasks

                String validIcon = validRate == 100 ? "🟢" :  // 100% valid
                        (validRate >= 80 ? "🟡" : "🔴");  // 80%-99% valid / <80% valid

                // Splice detailed information of current activity type (hierarchical)
                report.append("========================================\n");
                report.append(String.format("📋 Activity Type: %s %s%n", activityType, validIcon));
                report.append("----------------------------------------\n");

                // 1. Basic statistics row
                report.append(String.format("📊 Basic Statistics:%n"));
                report.append(String.format("  - Total Tasks: %d | Valid Tasks: %d | Validity Rate: %.0f%%%n",
                        totalTasks, validTasks, validRate));
                report.append(String.format("  - Completed Tasks: %d | Incomplete Tasks: %d%n",
                        completedTasks, uncompletedTasks));
                report.append(String.format("  - Completion Rate: %s %.0f%% (Only valid tasks counted)%n",
                        completionIcon, completionRate));

                // 2. Incomplete status breakdown (only display if incomplete count > 0)
                if (uncompletedTasks > 0) {
                    report.append(String.format("⏳ Incomplete Status Breakdown:%n"));
                    report.append(String.format("  - In Progress: %d | Planned: %d | Others: %d%n",
                            inProgressTasks, plannedTasks, otherUncompletedTasks));
                }

                // 3. Working hours statistics (only display if completed count > 0)
                if (completedTasks > 0) {
                    report.append(String.format("⏰ Working Hours Statistics (Completed Tasks Only):%n"));
                    report.append(String.format("  - Total Completed Hours: %.1f hours%n", totalCompletedHours));
                    report.append(String.format("  - Average Time Per Task: %.1f hours%n", avgCompletedHours));
                }

                // Line break to separate different activity types
                report.append("\n");
            }

            // Splice overall summary (display at the end for clarity)
            report.append("========================================\n");
            report.append("📊 System Overall Activity Summary:\n");
            report.append("----------------------------------------\n");
            double globalCompletionRate = globalValidTasks > 0 ? (double) globalCompletedTasks / globalValidTasks * 100 : 0.0;
            report.append(String.format("  - System Total Tasks: %d | Total Valid Tasks: %d%n",
                    globalTotalTasks, globalValidTasks));
            report.append(String.format("  - Completed Valid Tasks: %d | Overall Completion Rate: %.0f%%%n",
                    globalCompletedTasks, globalCompletionRate));
            report.append(String.format("  - Total Completed Working Hours: %.1f hours%n", globalTotalHours));

            // Handle no data scenario (avoid blank report)
            if (!rs.isBeforeFirst()) {
                report.append("========================================\n");
                report.append("❌ No activity task data available!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // If using a logging framework, uncomment the following (replace with your logging tool)
            // HtmlLogger.error("ReportService.generateActivityTypeReport", "Failed to generate Activity Type Distribution Report", e);
            return "❌ Report generation failed: " + e.getMessage();
        }

        return report.toString();
    }

    // ==================== Chemical Usage and Consumption Report ====================
    public String generateChemicalConsumptionReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Chemical Usage & Consumption Report ===\n");
        report.append("Generated Date: ").append(new Date()).append("\n\n");

        String sql = "SELECT " +
                "c.chemical_id, " +
                "c.name, " +
                "c.type, " +
                "COALESCE(SUM(ci.quantity), 0) AS current_stock, " + // Sum of inventory (only valid inventory)
                "COALESCE(COUNT(s.safety_check_id), 0) AS monthly_consumption " + // Monthly inspection count (as consumption count)
                "FROM chemical c " +
                // Left join inventory table (filter valid inventory)
                "LEFT JOIN chemical_inventory ci " +
                "ON c.chemical_id = ci.chemical_id " +
                "AND ci.active_flag = 1 " +
                // Left join safety check table (filter valid monthly checks)
                "LEFT JOIN safety_check s " +
                "ON c.chemical_id = s.chemical_id " +
                "AND s.check_datetime >= DATE_FORMAT(NOW() ,'%Y-%m-01') " +
                // Filter valid chemicals
                "WHERE c.active_flag = 1 " +
                "GROUP BY c.chemical_id, c.name, c.type " +
                "ORDER BY current_stock ASC"; // Sort by low stock first

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String name = rs.getString("name");
                String type = rs.getString("type");
                int currentStock = rs.getInt("current_stock");
                int monthlyConsumption = rs.getInt("monthly_consumption");

                // Inventory status (only based on whether it is 0, no threshold)
                String stockStatus = currentStock <= 0 ? "🔴 Stock Empty!" : "🟢 Stock Sufficient";

                report.append(name).append(" (").append(type).append(")\n");
                report.append(String.format("  - Monthly Usage: %d times | Current Stock: %d units%n",
                        monthlyConsumption, currentStock));
                report.append("  - Stock Status: ").append(stockStatus).append("\n\n");
            }

            if (!hasData) {
                report.append("No valid chemical usage & consumption data available!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Report generation failed: " + e.getMessage();
        }
        
        // 添加报表结尾
        report.append("=================== End of Report ===================\n");
        return report.toString();
    }

    // ==================== Worker Productivity Report ====================
    public String generateWorkerEfficiencyReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Worker Efficiency Report ===\n");
        report.append("Generated Date: ").append(new Date()).append("\n\n");

        String sql = "SELECT " +
                "s.staff_id, " +
                "CONCAT(s.first_name, ' ', s.last_name) AS worker_name, " +
                "COUNT(a.activity_id) AS total_tasks, " +
                "SUM(CASE WHEN a.status = 'completed' THEN 1 ELSE 0 END) AS completed_tasks, " +
                "AVG(CASE WHEN a.status = 'completed' THEN " +
                "TIMESTAMPDIFF(MINUTE, a.activity_datetime, a.actual_completion_datetime) " +
                "ELSE NULL END) AS avg_duration " +
                "FROM staff s " +
                "LEFT JOIN activity a ON s.staff_id = a.created_by_staff_id " +
                "GROUP BY s.staff_id, s.first_name, s.last_name " +
                "HAVING total_tasks > 0 " +
                "ORDER BY (completed_tasks / total_tasks) DESC, avg_duration ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Map<String, Object>> workers = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> worker = new HashMap<>();
                worker.put("worker_name", rs.getString("worker_name"));
                worker.put("total_tasks", rs.getInt("total_tasks"));
                worker.put("completed_tasks", rs.getInt("completed_tasks"));

                // --- Fix: Correctly handle NULL values ---
                double avgDuration;
                avgDuration = rs.getDouble("avg_duration");
                if (rs.wasNull()) {
                    avgDuration = 0.0; // Or set to other default values according to business logic
                }
                worker.put("avg_duration", avgDuration);

                workers.add(worker);
            }

            // 计算整体平均指标
            int totalWorkers = workers.size();
            int totalAllTasks = 0;
            int totalAllCompleted = 0;
            double totalAllDuration = 0.0;
            int workersWithCompletedTasks = 0;
            
            for (Map<String, Object> worker : workers) {
                totalAllTasks += (int) worker.get("total_tasks");
                totalAllCompleted += (int) worker.get("completed_tasks");
                double duration = (double) worker.get("avg_duration");
                if (duration > 0) {
                    totalAllDuration += duration;
                    workersWithCompletedTasks++;
                }
            }
            
            double overallCompletionRate = totalAllTasks > 0 ? (double) totalAllCompleted / totalAllTasks : 0.0;
            double overallAvgDuration = workersWithCompletedTasks > 0 ? totalAllDuration / workersWithCompletedTasks : 0.0;
            
            // 添加整体统计信息
            report.append("===== 整体统计 =====\n");
            report.append(String.format("  统计工人：%d名\n", totalWorkers));
            report.append(String.format("  总任务数：%d次\n", totalAllTasks));
            report.append(String.format("  整体完成率：%.1f%%\n", overallCompletionRate * 100));
            report.append(String.format("  平均完成时长：%.1f分钟/任务\n\n", overallAvgDuration));
            
            // 输出个人排名
            report.append("===== 个人效率排名 =====\n\n");
            String[] rankIcons = {"🥇", "🥈", "🥉", "4", "5"}; // 定义排名图标
            for (int i = 0; i < workers.size(); i++) {
                Map<String, Object> worker = workers.get(i);
                String workerName = (String) worker.get("worker_name");
                int totalTasks = (int) worker.get("total_tasks");
                int completedTasks = (int) worker.get("completed_tasks");
                double avgDuration = (double) worker.get("avg_duration");

                double completionRate = (double) completedTasks / totalTasks;

                String rankIcon = i < rankIcons.length ? rankIcons[i] : String.valueOf(i + 1);
                report.append("Rank ").append(rankIcon).append(" ").append(workerName).append("\n");
                report.append(String.format("  - Total Tasks: %d | Completion Rate: %.0f%% | Average Completion Time: %.0f minutes/task%n",
                        totalTasks, completionRate * 100, avgDuration));
                report.append("\n");
            }

            if (workers.isEmpty()) {
                report.append("No employee task data available!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // HtmlLogger.error("ReportService.generateWorkerEfficiencyReport", "Failed to generate Worker Efficiency Report", e);
            return "Report generation failed: " + e.getMessage();
        }

        report.append("=================== End of Report  ===================\n");
        return report.toString();
    }

    // ==================== Weekly Maintenance Trend Report ====================
    public String generateWeeklyTrendReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Weekly Maintenance Trend Report (Past 7 Days) ===\n");
        report.append("Generated Date: ").append(DateUtils.formatWithWeekday(new Date())).append("\n");
        report.append("Note: 1. Statistics Scope: All activity tasks in the past 7 days (including today)\n");
        report.append("      2. Valid Tasks: active_flag=Y; Invalid Tasks: active_flag=N\n");
        report.append("      3. Working hours unit: hours (1 decimal place), only counted for completed and time-valid tasks\n");
        report.append("      4. Trend Icons: ▲ Increase ▼ Decrease ➡️ Stable | Status Icons: 🟢 Valid 🔴 Invalid ✅ Completed 🔄 In Progress 📅 Planned\n\n");

        // Fully fixed SQL:
        // 1. Remove aggregate function nesting (no MAX() inside GROUP_CONCAT)
        // 2. Associate main in type_stats subquery in advance to get daily total tasks, avoiding nesting
        String sql = """
                -- Main query: Aggregate by date and splice type distribution (no aggregate nesting)
                SELECT
                    main.task_date,
                    -- Columns from main subquery are wrapped with MAX() to meet only_full_group_by
                    MAX(main.daily_total_tasks) AS daily_total_tasks,
                    MAX(main.daily_valid_tasks) AS daily_valid_tasks,
                    MAX(main.daily_invalid_tasks) AS daily_invalid_tasks,
                    MAX(main.daily_completed_tasks) AS daily_completed_tasks,
                    MAX(main.daily_uncompleted_tasks) AS daily_uncompleted_tasks,
                    MAX(main.daily_in_progress) AS daily_in_progress,
                    MAX(main.daily_planned) AS daily_planned,
                    MAX(main.daily_other_uncompleted) AS daily_other_uncompleted,
                    MAX(main.daily_completed_hours) AS daily_completed_hours,
                    -- Type distribution: Calculate proportion directly using daily_total from type_stats (non-aggregate column), no nesting
                    GROUP_CONCAT(
                        DISTINCT CONCAT(
                            type_stats.activity_type,
                            '(',
                            type_stats.type_count,
                            ',',
                            ROUND(type_stats.type_count / type_stats.daily_total * 100, 0),
                            '%)'
                        )
                        ORDER BY type_stats.type_count DESC
                        SEPARATOR '、'
                    ) AS daily_type_distribution
                FROM (
                    -- Subquery 1: Aggregate daily core data by date (1 row per task_date)
                    SELECT
                        DATE(activity_datetime) AS task_date,
                        COUNT(activity_id) AS daily_total_tasks,
                        SUM(CASE WHEN active_flag = 'Y' THEN 1 ELSE 0 END) AS daily_valid_tasks,
                        SUM(CASE WHEN active_flag = 'N' THEN 1 ELSE 0 END) AS daily_invalid_tasks,
                        SUM(CASE WHEN active_flag = 'Y' AND (status = 'completed' OR status = '已完成') THEN 1 ELSE 0 END) AS daily_completed_tasks,
                        SUM(CASE WHEN active_flag = 'Y' AND (status != 'completed' AND status != '已完成') THEN 1 ELSE 0 END) AS daily_uncompleted_tasks,
                        SUM(CASE WHEN active_flag = 'Y' AND status = 'in_progress' THEN 1 ELSE 0 END) AS daily_in_progress,
                        SUM(CASE WHEN active_flag = 'Y' AND status = 'planned' THEN 1 ELSE 0 END) AS daily_planned,
                        SUM(CASE WHEN active_flag = 'Y' 
                                 AND (status != 'completed' AND status != '已完成') 
                                 AND status NOT IN ('in_progress', 'planned') 
                                 THEN 1 ELSE 0 END) AS daily_other_uncompleted,
                        ROUND(
                            SUM(CASE WHEN active_flag = 'Y' 
                                      AND (status = 'completed' OR status = '已完成') 
                                      AND actual_completion_datetime IS NOT NULL 
                                      AND actual_completion_datetime > activity_datetime 
                                      THEN TIMESTAMPDIFF(MINUTE, activity_datetime, actual_completion_datetime) / 60 
                                      ELSE 0 END),
                            1
                        ) AS daily_completed_hours
                    FROM activity
                    WHERE activity_datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                    GROUP BY DATE(activity_datetime)
                ) AS main
                -- Join Subquery 2: Statistics by date + type, associate main in advance to get daily total tasks (avoid aggregate nesting)
                LEFT JOIN (
                    SELECT
                        ts.task_date,
                        ts.activity_type,
                        ts.type_count,
                        m.daily_total_tasks AS daily_total  -- Directly get daily total tasks from main, non-aggregate column
                    FROM (
                        -- Subquery 2.1: First count by date + type
                        SELECT
                            DATE(activity_datetime) AS task_date,
                            activity_type,
                            COUNT(activity_id) AS type_count
                        FROM activity
                        WHERE activity_datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                        GROUP BY DATE(activity_datetime), activity_type
                    ) AS ts
                    -- Join main to get total tasks for the corresponding date
                    LEFT JOIN (
                        SELECT DATE(activity_datetime) AS task_date, COUNT(activity_id) AS daily_total_tasks
                        FROM activity
                        WHERE activity_datetime >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                        GROUP BY DATE(activity_datetime)
                    ) AS m ON ts.task_date = m.task_date
                ) AS type_stats ON main.task_date = type_stats.task_date
                -- Group by date to meet only_full_group_by
                GROUP BY main.task_date
                ORDER BY main.task_date ASC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<DailyReportData> dailyDataList = new ArrayList<>();
            WeeklySummary weeklySummary = new WeeklySummary();

            // Parse ResultSet (traverse directly, SQL has been aggregated by date)
            while (rs.next()) {
                Date taskDate = rs.getDate("task_date");
                String taskDateStr = DateUtils.format(taskDate);

                // Get values (aliases remain unchanged, one-to-one correspondence with SQL fields)
                int totalTasks = rs.getInt("daily_total_tasks");
                int validTasks = rs.getInt("daily_valid_tasks");
                int invalidTasks = rs.getInt("daily_invalid_tasks");
                int completedTasks = rs.getInt("daily_completed_tasks");
                int uncompletedTasks = rs.getInt("daily_uncompleted_tasks");
                int inProgressTasks = rs.getInt("daily_in_progress");
                int plannedTasks = rs.getInt("daily_planned");
                int otherUncompletedTasks = rs.getInt("daily_other_uncompleted");
                double completedHours = rs.getDouble("daily_completed_hours");
                String typeDistribution = rs.getString("daily_type_distribution");

                // Build daily data object
                DailyReportData dailyData = new DailyReportData();
                dailyData.setTaskDate(taskDate);
                dailyData.setDateStr(taskDateStr);
                dailyData.setTotalTasks(totalTasks);
                dailyData.setValidTasks(validTasks);
                dailyData.setInvalidTasks(invalidTasks);
                dailyData.setCompletedTasks(completedTasks);
                dailyData.setUncompletedTasks(uncompletedTasks);
                dailyData.setInProgressTasks(inProgressTasks);
                dailyData.setPlannedTasks(plannedTasks);
                dailyData.setOtherUncompletedTasks(otherUncompletedTasks);
                dailyData.setCompletedHours(completedHours);
                dailyData.setTypeDistribution(typeDistribution != null ? typeDistribution : "");

                dailyDataList.add(dailyData);

                // Accumulate overall summary data
                weeklySummary.addTotalTasks(totalTasks);
                weeklySummary.addValidTasks(validTasks);
                weeklySummary.addInvalidTasks(invalidTasks);
                weeklySummary.addCompletedTasks(completedTasks);
                weeklySummary.addUncompletedTasks(uncompletedTasks);
                weeklySummary.addCompletedHours(completedHours);
            }

            // Generate daily trend report
            report.append("========================================\n");
            report.append("📅 Daily Detailed Trends (Past 7 Days):\n");
            report.append("========================================\n");

            DailyReportData previousDay = null;
            for (DailyReportData daily : dailyDataList) {
                double validRate = daily.getTotalTasks() > 0 ? (double) daily.getValidTasks() / daily.getTotalTasks() * 100 : 0.0;
                double completionRate = daily.getValidTasks() > 0 ? (double) daily.getCompletedTasks() / daily.getValidTasks() * 100 : 0.0;
                double avgHoursPerTask = daily.getCompletedTasks() > 0 ? daily.getCompletedHours() / daily.getCompletedTasks() : 0.0;

                TrendInfo trend = calculateTrend(daily, previousDay);
                String dateDisplay = DateUtils.formatWithWeekday(daily.getTaskDate());

                // Splice report content
                report.append(String.format("\n📆 %s:\n", dateDisplay));
                report.append("  ├─ Basic Statistics:\n");
                report.append(String.format("  │  - Total Tasks: %d %s | Valid Tasks: %d (%.0f%%) %s | Invalid Tasks: %d\n",
                        daily.getTotalTasks(), trend.getTotalTaskTrend(),
                        daily.getValidTasks(), validRate, trend.getValidRateTrend(),
                        daily.getInvalidTasks()));
                report.append("  ├─ Completion Status:\n");
                report.append(String.format("  │  - Completed: %d | Incomplete: %d | Completion Rate: %.0f%% %s\n",
                        daily.getCompletedTasks(), daily.getUncompletedTasks(),
                        completionRate, trend.getCompletionRateTrend()));
                if (daily.getUncompletedTasks() > 0) {
                    report.append(String.format("  │  - Incomplete Breakdown: In Progress: %d | Planned: %d | Others: %d\n",
                            daily.getInProgressTasks(), daily.getPlannedTasks(),
                            daily.getOtherUncompletedTasks()));
                }
                report.append("  ├─ Working Hours Statistics:\n");
                report.append(String.format("  │  - Total Completed Hours: %.1f hours %s | Average Time Per Task: %.1f hours\n",
                        daily.getCompletedHours(), trend.getHoursTrend(), avgHoursPerTask));
                report.append("  └─ Type Distribution:\n");
                report.append(String.format("     - %s\n", daily.getTypeDistribution().isEmpty() ? "No Activity Type Data" : daily.getTypeDistribution()));

                previousDay = daily;
            }

            // Splice overall summary (past 7 days)
            report.append("\n========================================\n");
            report.append("📊 Overall Summary (Past 7 Days):\n");
            report.append("========================================\n");
            double weeklyValidRate = weeklySummary.getTotalTasks() > 0 ? (double) weeklySummary.getValidTasks() / weeklySummary.getTotalTasks() * 100 : 0.0;
            double weeklyCompletionRate = weeklySummary.getValidTasks() > 0 ? (double) weeklySummary.getCompletedTasks() / weeklySummary.getValidTasks() * 100 : 0.0;
            double weeklyAvgHours = weeklySummary.getCompletedTasks() > 0 ? weeklySummary.getCompletedHours() / weeklySummary.getCompletedTasks() : 0.0;

            report.append(String.format("  - Total Tasks: %d | Valid Tasks: %d (%.0f%%) | Invalid Tasks: %d\n",
                    weeklySummary.getTotalTasks(), weeklySummary.getValidTasks(), weeklyValidRate, weeklySummary.getInvalidTasks()));
            report.append(String.format("  - Completed Tasks: %d | Incomplete Tasks: %d | Overall Completion Rate: %.0f%%\n",
                    weeklySummary.getCompletedTasks(), weeklySummary.getUncompletedTasks(), weeklyCompletionRate));
            report.append(String.format("  - Total Completed Hours: %.1f hours | Average Time Per Task: %.1f hours\n",
                    weeklySummary.getCompletedHours(), weeklyAvgHours));

            // Handle no data scenario
            if (dailyDataList.isEmpty()) {
                report.append("\n========================================\n");
                report.append("❌ No activity task records in the past 7 days!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Report generation failed: " + e.getMessage();
        }

        return report.toString();
    }

    /**
     * Represents daily report data structure
     */
    private static class DailyReportData {
        private Date taskDate;
        private String dateStr;
        private int totalTasks;
        private int validTasks;
        private int invalidTasks;
        private int completedTasks;
        private int uncompletedTasks;
        private int inProgressTasks;
        private int plannedTasks;
        private int otherUncompletedTasks;
        private double completedHours;
        private String typeDistribution;

        // getter/setter
        public Date getTaskDate() { return taskDate; }
        public void setTaskDate(Date taskDate) { this.taskDate = taskDate; }
        public String getDateStr() { return dateStr; }
        public void setDateStr(String dateStr) { this.dateStr = dateStr; }
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        public int getValidTasks() { return validTasks; }
        public void setValidTasks(int validTasks) { this.validTasks = validTasks; }
        public int getInvalidTasks() { return invalidTasks; }
        public void setInvalidTasks(int invalidTasks) { this.invalidTasks = invalidTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        public int getUncompletedTasks() { return uncompletedTasks; }
        public void setUncompletedTasks(int uncompletedTasks) { this.uncompletedTasks = uncompletedTasks; }
        public int getInProgressTasks() { return inProgressTasks; }
        public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }
        public int getPlannedTasks() { return plannedTasks; }
        public void setPlannedTasks(int plannedTasks) { this.plannedTasks = plannedTasks; }
        public int getOtherUncompletedTasks() { return otherUncompletedTasks; }
        public void setOtherUncompletedTasks(int otherUncompletedTasks) { this.otherUncompletedTasks = otherUncompletedTasks; }
        public double getCompletedHours() { return completedHours; }
        public void setCompletedHours(double completedHours) { this.completedHours = completedHours; }
        public String getTypeDistribution() { return typeDistribution; }
        public void setTypeDistribution(String typeDistribution) { this.typeDistribution = typeDistribution; }
    }

    private static class WeeklySummary {
        private int totalTasks = 0;
        private int validTasks = 0;
        private int invalidTasks = 0;
        private int completedTasks = 0;
        private int uncompletedTasks = 0;
        private double completedHours = 0.0;

        public void addTotalTasks(int num) { this.totalTasks += num; }
        public void addValidTasks(int num) { this.validTasks += num; }
        public void addInvalidTasks(int num) { this.invalidTasks += num; }
        public void addCompletedTasks(int num) { this.completedTasks += num; }
        public void addUncompletedTasks(int num) { this.uncompletedTasks += num; }
        public void addCompletedHours(double hours) { this.completedHours += hours; }

        public int getTotalTasks() { return totalTasks; }
        public int getValidTasks() { return validTasks; }
        public int getInvalidTasks() { return invalidTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public int getUncompletedTasks() { return uncompletedTasks; }
        public double getCompletedHours() { return completedHours; }
    }

    private TrendInfo calculateTrend(DailyReportData current, DailyReportData previous) {
        TrendInfo trend = new TrendInfo();
        if (previous == null) {
            trend.setTotalTaskTrend("(No Previous Data)");
            trend.setValidRateTrend("");
            trend.setCompletionRateTrend("(No Previous Data)");
            trend.setHoursTrend("(No Previous Data)");
            return trend;
        }

        int totalDiff = current.getTotalTasks() - previous.getTotalTasks();
        trend.setTotalTaskTrend(totalDiff > 0 ? String.format("▲(+%d)", totalDiff) :
                (totalDiff < 0 ? String.format("▼(-%d)", -totalDiff) : "➡️(Stable)"));

        double currentValidRate = current.getTotalTasks() > 0 ? (double) current.getValidTasks() / current.getTotalTasks() * 100 : 0.0;
        double prevValidRate = previous.getTotalTasks() > 0 ? (double) previous.getValidTasks() / previous.getTotalTasks() * 100 : 0.0;
        double validRateDiff = currentValidRate - prevValidRate;
        trend.setValidRateTrend(validRateDiff > 0 ? String.format("▲(+%.0f%%)", validRateDiff) :
                (validRateDiff < 0 ? String.format("▼(-%.0f%%)", -validRateDiff) : "➡️(Stable)"));

        double currentCompletionRate = current.getValidTasks() > 0 ? (double) current.getCompletedTasks() / current.getValidTasks() * 100 : 0.0;
        double prevCompletionRate = previous.getValidTasks() > 0 ? (double) previous.getCompletedTasks() / previous.getValidTasks() * 100 : 0.0;
        double completionRateDiff = currentCompletionRate - prevCompletionRate;
        trend.setCompletionRateTrend(completionRateDiff > 0 ? String.format("▲(+%.0f%%)", completionRateDiff) :
                (completionRateDiff < 0 ? String.format("▼(-%.0f%%)", -completionRateDiff) : "➡️(Stable)"));

        double hoursDiff = current.getCompletedHours() - previous.getCompletedHours();
        trend.setHoursTrend(hoursDiff > 0 ? String.format("▲(+%.1f hours)", hoursDiff) :
                (hoursDiff < 0 ? String.format("▼(-%.1f hours)", -hoursDiff) : "➡️(Stable)"));

        return trend;
    }

    private static class TrendInfo {
        private String totalTaskTrend;
        private String validRateTrend;
        private String completionRateTrend;
        private String hoursTrend;

        public String getTotalTaskTrend() { return totalTaskTrend; }
        public void setTotalTaskTrend(String totalTaskTrend) { this.totalTaskTrend = totalTaskTrend; }
        public String getValidRateTrend() { return validRateTrend; }
        public void setValidRateTrend(String validRateTrend) { this.validRateTrend = validRateTrend; }
        public String getCompletionRateTrend() { return completionRateTrend; }
        public void setCompletionRateTrend(String completionRateTrend) { this.completionRateTrend = completionRateTrend; }
        public String getHoursTrend() { return hoursTrend; }
        public void setHoursTrend(String hoursTrend) { this.hoursTrend = hoursTrend; }
    }

    public static void main(String[] args) {
        ReportService service = new ReportService();
        System.out.println(service.generateWeeklyTrendReport());
    }
}