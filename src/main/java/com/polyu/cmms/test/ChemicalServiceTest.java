package com.polyu.cmms.test;

import com.polyu.cmms.service.ChemicalService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChemicalService测试类，用于验证化学品相关功能
 */
public class ChemicalServiceTest {

    public static void main(String[] args) {
        System.out.println("============= 开始执行 ChemicalService 测试 =============");
        // 调用各个测试方法
        testGetAllActiveChemicals();
        testGetChemicalsByPage();
        testQueryChemicalsWithConditions();
        testGetChemicalById();
        testGetChemicalsByHazardCategory();
        testGetChemicalsByType();
        System.out.println("============= ChemicalService 测试执行完毕 =============");
    }

    /**
     * 测试获取所有活跃的化学品
     */
    public static void testGetAllActiveChemicals() {
        System.out.println("\n[测试1] 获取所有活跃的化学品...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            List<Map<String, Object>> activeChemicals = chemicalService.getAllActiveChemicals();
            System.out.println("活跃化学品总数: " + activeChemicals.size());
            printChemicals(activeChemicals, 5); // 打印前5条记录

        } catch (SQLException e) {
            System.err.println("获取活跃化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试分页查询化学品
     */
    public static void testGetChemicalsByPage() {
        System.out.println("\n[测试2] 分页查询化学品...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            // 分页参数：第1页，每页5条，按名称升序排序
            int page = 1;
            int pageSize = 5;
            Map<String, Object> conditions = new HashMap<>(); // 无额外条件
            String sortField = "name"; // 排序字段（小驼峰命名）
            String sortOrder = "ASC"; // 升序

            Map<String, Object> pageResult = chemicalService.getChemicalsByPage(page, pageSize, conditions, sortField, sortOrder);

            // 解析分页结果
            List<Map<String, Object>> chemicals = (List<Map<String, Object>>) pageResult.get("data");
            int total = (int) pageResult.get("total");
            int totalPages = (int) pageResult.get("totalPages");

            System.out.println("分页查询结果:");
            System.out.println("总记录数: " + total + ", 总页数: " + totalPages + ", 当前页: " + page);
            System.out.println("当前页化学品数量: " + chemicals.size());
            printChemicals(chemicals, pageSize);

        } catch (SQLException e) {
            System.err.println("分页查询化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试带条件查询化学品（使用小驼峰命名作为查询Key）
     */
    public static void testQueryChemicalsWithConditions() {
        System.out.println("\n[测试3] 根据条件查询化学品...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            // 构建查询条件：例如查询危险类别为"flammable"且类型为"liquid"的化学品
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("hazardCategory", "flammable"); // 小驼峰命名（对应数据库的 hazard_category）
            conditions.put("type", "liquid"); // 小驼峰命名（对应数据库的 type）

            List<Map<String, Object>> filteredChemicals = chemicalService.queryChemicals(conditions);
            System.out.println("条件查询（hazardCategory=flammable, type=liquid）结果数量: " + filteredChemicals.size());
            printChemicals(filteredChemicals, filteredChemicals.size());

        } catch (SQLException e) {
            System.err.println("条件查询化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试根据ID获取化学品详情
     */
    public static void testGetChemicalById() {
        System.out.println("\n[测试4] 根据ID获取化学品详情...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            // 假设存在ID为1的化学品
            int chemicalId = 1;
            Map<String, Object> chemical = chemicalService.getChemicalById(chemicalId);

            if (chemical != null) {
                System.out.println("化学品ID=" + chemicalId + " 的详情:");
                printChemicalDetail(chemical);
            } else {
                System.out.println("未查询到ID=" + chemicalId + " 的化学品");
            }

        } catch (SQLException e) {
            System.err.println("根据ID查询化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试根据危险类别查询化学品
     */
    public static void testGetChemicalsByHazardCategory() {
        System.out.println("\n[测试5] 根据危险类别查询化学品...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            // 假设存在危险类别为"corrosive"的化学品
            String hazardCategory = "corrosive";
            List<Map<String, Object>> chemicals = chemicalService.getChemicalsByHazardCategory(hazardCategory);

            System.out.println("危险类别为 '" + hazardCategory + "' 的化学品数量: " + chemicals.size());
            printChemicals(chemicals, chemicals.size());

        } catch (SQLException e) {
            System.err.println("根据危险类别查询化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试根据类型查询化学品
     */
    public static void testGetChemicalsByType() {
        System.out.println("\n[测试6] 根据类型查询化学品...");
        ChemicalService chemicalService = ChemicalService.getInstance();

        try {
            // 假设存在类型为"solid"的化学品
            String type = "solid";
            List<Map<String, Object>> chemicals = chemicalService.getChemicalsByType(type);

            System.out.println("类型为 '" + type + "' 的化学品数量: " + chemicals.size());
            printChemicals(chemicals, chemicals.size());

        } catch (SQLException e) {
            System.err.println("根据类型查询化学品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 辅助方法：打印化学品列表（简化版，显示核心字段）
     * @param chemicals 化学品列表
     * @param maxCount 最大打印数量
     */
    private static void printChemicals(List<Map<String, Object>> chemicals, int maxCount) {
        if (chemicals == null || chemicals.isEmpty()) {
            System.out.println(" -> 未查询到任何化学品");
            return;
        }

        int printCount = Math.min(chemicals.size(), maxCount);
        System.out.println(" -> 前" + printCount + "条化学品信息:");
        for (int i = 0; i < printCount; i++) {
            Map<String, Object> chemical = chemicals.get(i);
            System.out.printf("    [ID: %d, 产品代码: %s, 名称: %s, 类型: %s, 危险类别: %s, 状态: %s]%n",
                    chemical.get("chemicalId"),
                    chemical.get("productCode"),
                    chemical.get("name"),
                    chemical.get("type"),
                    chemical.get("hazardCategory"),
                    chemical.get("activeFlag")
            );
        }

        if (chemicals.size() > maxCount) {
            System.out.println("    ...以及其他" + (chemicals.size() - maxCount) + "条记录");
        }
    }

    /**
     * 辅助方法：打印化学品详细信息（显示所有字段）
     * @param chemical 化学品详情Map
     */
    private static void printChemicalDetail(Map<String, Object> chemical) {
        for (Map.Entry<String, Object> entry : chemical.entrySet()) {
            System.out.printf("    %s: %s%n", entry.getKey(), entry.getValue());
        }
    }
}