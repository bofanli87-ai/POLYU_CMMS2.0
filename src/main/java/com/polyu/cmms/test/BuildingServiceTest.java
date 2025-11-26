package com.polyu.cmms.test;

import com.polyu.cmms.service.BuildingService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BuildingService测试类，用于验证建筑物相关功能
 * 遵循规则：查询条件使用小驼峰命名法（如 buildingCode），而非数据库的下划线命名（building_code）
 */
public class BuildingServiceTest {

    public static void main(String[] args) {
        System.out.println("============= 开始执行 BuildingService 测试 =============");
        // 调用各个测试方法
        testGetAllActiveBuildings();
        testGetBuildingsByPage();
        testQueryBuildings();
        testGetBuildingById();
        testGetBuildingsBySupervisor();
        System.out.println("============= BuildingService 测试执行完毕 =============");
    }

    /**
     * 测试获取所有活跃的建筑物
     */
    public static void testGetAllActiveBuildings() {
        System.out.println("\n[测试1] 获取所有活跃的建筑物...");
        BuildingService buildingService = BuildingService.getInstance();

        try {
            List<Map<String, Object>> activeBuildings = buildingService.getAllActiveBuildings();
            System.out.println("活跃建筑物总数: " + activeBuildings.size());
            printBuildings(activeBuildings, 5); // 打印前5条记录

        } catch (SQLException e) {
            System.err.println("获取活跃建筑物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试分页查询建筑物（带排序，无额外条件）
     */
    public static void testGetBuildingsByPage() {
        System.out.println("\n[测试2] 分页查询建筑物...");
        BuildingService buildingService = BuildingService.getInstance();

        try {
            // 分页参数：第1页，每页5条
            int page = 1;
            int pageSize = 5;
            Map<String, Object> conditions = new HashMap<>(); // 无额外条件
            String sortField = "buildingCode"; // 排序字段（小驼峰命名）
            String sortOrder = "ASC"; // 升序

            Map<String, Object> pageResult = buildingService.getBuildingsByPage(page, pageSize, conditions, sortField, sortOrder);

            // 解析分页结果
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) pageResult.get("data");
            int total = (int) pageResult.get("total");
            int totalPages = (int) pageResult.get("totalPages");

            System.out.println("分页查询结果:");
            System.out.println("总记录数: " + total + ", 总页数: " + totalPages + ", 当前页: " + page);
            System.out.println("当前页建筑物数量: " + buildings.size());
            printBuildings(buildings, pageSize);

        } catch (SQLException e) {
            System.err.println("分页查询建筑物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试带条件查询建筑物（使用小驼峰命名作为查询Key）
     */
    public static void testQueryBuildings() {
        System.out.println("\n[测试3] 根据条件查询建筑物...");
        BuildingService buildingService = BuildingService.getInstance();

        try {
            // 正确：查询条件的Key使用小驼峰命名（buildingCode 对应数据库的 building_code）
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("buildingCode", "POLYU-B1"); // 小驼峰命名
            conditions.put("numFloors", 10); // 小驼峰命名（对应数据库的 num_floors）

            List<Map<String, Object>> filteredBuildings = buildingService.queryBuildings(conditions);
            System.out.println("条件查询（buildingCode=POLYU-B1, numFloors=10）结果数量: " + filteredBuildings.size());
            printBuildings(filteredBuildings, filteredBuildings.size());

        } catch (SQLException e) {
            System.err.println("条件查询建筑物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试根据ID获取建筑物详情
     */
    public static void testGetBuildingById() {
        System.out.println("\n[测试4] 根据ID获取建筑物详情...");
        BuildingService buildingService = BuildingService.getInstance();

        try {
            int buildingId = 1; // 假设存在ID为1的建筑物
            Map<String, Object> building = buildingService.getBuildingById(buildingId);

            if (building != null) {
                System.out.println("建筑物ID=" + buildingId + " 的详情:");
                printBuildingDetail(building);
            } else {
                System.out.println("未查询到ID=" + buildingId + " 的建筑物");
            }

        } catch (SQLException e) {
            System.err.println("根据ID查询建筑物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试根据负责经理ID查询建筑物（使用小驼峰命名作为查询Key）
     */
    public static void testGetBuildingsBySupervisor() {
        System.out.println("\n[测试5] 根据负责经理ID查询建筑物...");
        BuildingService buildingService = BuildingService.getInstance();

        try {
            // 正确：查询条件的Key使用小驼峰命名（supervisorStaffId 对应数据库的 supervisor_staff_id）
            int supervisorStaffId = 1001; // 假设存在负责经理ID为1001的员工
            List<Map<String, Object>> buildings = buildingService.getBuildingsBySupervisor(supervisorStaffId);

            System.out.println("负责经理ID=" + supervisorStaffId + " 管理的建筑物数量: " + buildings.size());
            printBuildings(buildings, buildings.size());

        } catch (SQLException e) {
            System.err.println("根据负责经理ID查询建筑物失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 辅助方法：打印建筑物列表（简化版）
     */
    private static void printBuildings(List<Map<String, Object>> buildings, int maxCount) {
        if (buildings == null || buildings.isEmpty()) {
            System.out.println(" -> 未查询到任何建筑物");
            return;
        }

        int printCount = Math.min(buildings.size(), maxCount);
        System.out.println(" -> 前" + printCount + "条建筑物信息:");
        for (int i = 0; i < printCount; i++) {
            Map<String, Object> building = buildings.get(i);
            System.out.printf("    [ID: %d, 建筑编号: %s, 建造日期: %s, 楼层数: %d, 状态: %s]%n",
                    building.get("buildingId"),
                    building.get("buildingCode"),
                    building.get("constructionDate"),
                    building.get("numFloors"),
                    building.get("activeFlag")
            );
        }

        if (buildings.size() > maxCount) {
            System.out.println("    ...以及其他" + (buildings.size() - maxCount) + "条记录");
        }
    }

    /**
     * 辅助方法：打印建筑物详细信息
     */
    private static void printBuildingDetail(Map<String, Object> building) {
        for (Map.Entry<String, Object> entry : building.entrySet()) {
            System.out.printf("    %s: %s%n", entry.getKey(), entry.getValue());
        }
    }
}