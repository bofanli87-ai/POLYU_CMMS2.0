package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * 外包公司服务类，提供外包公司相关的数据访问功能
 */
public class CompanyService extends BaseService {
    private static CompanyService instance;
    
    // 单例模式
    private CompanyService() {}
    
    public static synchronized CompanyService getInstance() {
        if (instance == null) {
            instance = new CompanyService();
        }
        return instance;
    }
    
    /**
     * 添加外包公司记录
     * @param companyData 公司数据
     * @return 插入是否成功
     * @throws SQLException SQL异常
     */
    public boolean addCompany(Map<String, Object> companyData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO company (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
        
        // 添加必要字段
        if (companyData.containsKey("contractorCode")) {
            sqlBuilder.append("contractor_code, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("contractorCode"));
        }
        if (companyData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("name"));
        }
        if (companyData.containsKey("contactName")) {
            sqlBuilder.append("contact_name, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("contactName"));
        }
        if (companyData.containsKey("contractQuote")) {
            sqlBuilder.append("contract_quote, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("contractQuote"));
        }
        if (companyData.containsKey("email")) {
            sqlBuilder.append("email, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("email"));
        }
        if (companyData.containsKey("phone")) {
            sqlBuilder.append("phone, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("phone"));
        }
        if (companyData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("addressId"));
        }
        if (companyData.containsKey("expertise")) {
            sqlBuilder.append("expertise, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("expertise"));
        }
        if (companyData.containsKey("taxId")) {
            sqlBuilder.append("tax_id, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("taxId"));
        }
        if (companyData.containsKey("bankAccount")) {
            sqlBuilder.append("bank_account, ");
            valuesBuilder.append("?, ");
            params.add(companyData.get("bankAccount"));
        }
        
        // 添加默认启用状态
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");
        
        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }
    
    /**
     * 更新外包公司信息
     * @param contractorId 公司ID
     * @param updates 更新的字段和值
     * @return 更新是否成功
     * @throws SQLException SQL异常
     */
    public boolean updateCompany(int contractorId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }
        
        StringBuilder sqlBuilder = new StringBuilder("UPDATE company SET ");
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }
        
        sqlBuilder.setLength(sqlBuilder.length() - 2); // 移除最后一个逗号和空格
        sqlBuilder.append(" WHERE contractor_id = ?");
        params.add(contractorId);
        
        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }
    
    /**
     * 删除外包公司（软删除）
     * @param contractorId 公司ID
     * @return 删除是否成功
     * @throws SQLException SQL异常
     */
    public boolean deleteCompany(int contractorId) throws SQLException {
        String sql = "UPDATE company SET active_flag = 'N' WHERE contractor_id = ?";
        int result = executeUpdate(sql, contractorId);
        return result > 0;
    }
    
    /**
     * 根据条件查询外包公司
     * @param conditions 查询条件
     * @return 公司列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> queryCompanies(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (conditions != null) {
            // 转换Java属性名为数据库列名
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
        
        return executeQuery(sqlBuilder.toString(), params.toArray());
    }

    /**
     * 分页查询外包公司
     * @param page 页码
     * @param pageSize 每页大小
     * @param conditions 查询条件
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return 分页查询结果
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getCompaniesByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // 参数验证
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加过滤条件
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
        
        // 添加排序
        if (sortField != null && !sortField.trim().isEmpty()) {
            String dbColumn = convertToDbColumn(sortField);
            String order = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
            sqlBuilder.append(" ORDER BY ").append(dbColumn).append(" ").append(order);
        } else {
            // 默认按ID排序
            sqlBuilder.append(" ORDER BY contractor_id ASC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        // 获取数据
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());
        
        // 获取总数 - 简化获取方式
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM company WHERE 1=1");
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
        
        // 直接使用第一列获取总数
        if (countResult != null && !countResult.isEmpty() && countResult.get(0) != null && !countResult.get(0).isEmpty()) {
            // 获取结果集第一列的值
            Object firstValue = countResult.get(0).values().iterator().next();
            if (firstValue instanceof Number) {
                total = ((Number) firstValue).intValue();
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total); // 使用"total"键名以保持与测试类一致
        result.put("currentPage", page);
        result.put("pageSize", pageSize);
        // 安全计算总页数，避免类型转换问题
        int totalPages = 0;
        if (pageSize > 0) {
            try {
                totalPages = (int) Math.ceil((double) total / pageSize);
            } catch (Exception e) {
                totalPages = 0;
            }
        }
        result.put("totalPages", totalPages);
        
        return result;
    }
    
    /**
     * 根据ID获取外包公司详情
     * @param contractorId 公司ID
     * @return 公司详情
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getCompanyById(int contractorId) throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE contractor_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, contractorId);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * 获取所有活跃的外包公司
     * @return 活跃公司列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getAllActiveCompanies() throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE active_flag = 'Y' ORDER BY name";
        return executeQuery(sql);
    }
    
    /**
     * 根据专业领域查询外包公司
     * @param expertise 专业领域
     * @return 公司列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getCompaniesByExpertise(String expertise) throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE expertise LIKE ? AND active_flag = 'Y' ORDER BY name";
        return executeQuery(sql, "%" + expertise + "%");
    }
    
    /**
     * 将Java驼峰命名转换为数据库下划线命名
     * @param javaName Java属性名（驼峰命名）
     * @return 数据库列名（下划线命名）
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