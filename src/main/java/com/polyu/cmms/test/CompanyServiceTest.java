package com.polyu.cmms.test;

import com.polyu.cmms.service.CompanyService;
import java.sql.SQLException;
import java.util.*;

/**
 * CompanyService测试类，用于验证获取公司数据的功能
 */
public class CompanyServiceTest {

    public static void main(String[] args) {
        // 调用测试方法
        testCompanyDataRetrieval();
    }

    /**
     * 测试获取公司数据的功能
     * 包括分页获取、总数统计等
     */
    public static void testCompanyDataRetrieval() {
        System.out.println("开始测试公司数据获取...");
        
        // 获取CompanyService实例
        CompanyService companyService = CompanyService.getInstance();
        
        try {
            // 测试1: 获取所有活跃的公司
            System.out.println("\n测试1: 获取所有活跃的公司");
            List<Map<String, Object>> activeCompanies = companyService.getAllActiveCompanies();
            System.out.println("活跃公司数量: " + activeCompanies.size());
            printCompanies(activeCompanies, 5); // 只打印前5条记录
            
            // 测试2: 分页获取公司数据
            System.out.println("\n测试2: 分页获取公司数据（第1页，每页10条）");
            Map<String, Object> pageResult = companyService.getCompaniesByPage(1, 10, null, "name", "ASC");
            
            // 打印分页信息
            int total = (int) pageResult.get("total");
            int pageSize = (int) pageResult.get("pageSize");
            int totalPages = (int) pageResult.get("totalPages");
            System.out.println("总记录数: " + total);
            System.out.println("每页大小: " + pageSize);
            System.out.println("总页数: " + totalPages);
            
            // 打印当前页数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> companies = (List<Map<String, Object>>) pageResult.get("data");
            System.out.println("当前页数据量: " + companies.size());
            printCompanies(companies, 5); // 只打印前5条记录
            
            // 测试3: 根据条件查询（如果有相关数据）
            System.out.println("\n测试3: 根据条件查询（例如查找专业领域包含特定关键词的公司）");
            Map<String, Object> conditions = new HashMap<>();
            // 可以根据实际数据修改条件
            // conditions.put("expertise", "IT");
            
            List<Map<String, Object>> filteredCompanies = companyService.queryCompanies(conditions);
            System.out.println("符合条件的公司数量: " + filteredCompanies.size());
            printCompanies(filteredCompanies, 5); // 只打印前5条记录
            
            System.out.println("\n公司数据获取测试完成！");
            
        } catch (SQLException e) {
            System.err.println("测试过程中发生SQL异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印公司数据
     * @param companies 公司数据列表
     * @param maxCount 最大打印数量
     */
    private static void printCompanies(List<Map<String, Object>> companies, int maxCount) {
        if (companies == null || companies.isEmpty()) {
            System.out.println("没有找到公司数据");
            return;
        }
        
        int count = Math.min(companies.size(), maxCount);
        System.out.println("前" + count + "条公司数据:");
        
        for (int i = 0; i < count; i++) {
            Map<String, Object> company = companies.get(i);
            // 使用Java驼峰命名的字段名，与BaseService中的转换保持一致
            System.out.println("公司ID: " + company.get("contractorId") + 
                             ", 公司名称: " + company.get("name") +
                             ", 联系人: " + company.get("contactName") +
                             ", 状态: " + company.get("activeFlag"));
        }
        
        if (companies.size() > maxCount) {
            System.out.println("...以及其他" + (companies.size() - maxCount) + "条记录");
        }
    }
}