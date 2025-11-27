package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * 食堂服务类，提供食堂相关的数据访问功能
 */
public class CanteenService extends BaseService {
    private static CanteenService instance;
    
    // 单例模式
    private CanteenService() {}
    
    public static synchronized CanteenService getInstance() {
        if (instance == null) {
            instance = new CanteenService();
        }
        return instance;
    }
    
    /**
     * 添加食堂记录
     * @param canteenData 食堂数据
     * @return 插入是否成功
     * @throws SQLException SQL异常
     */
    public boolean addCanteen(Map<String, Object> canteenData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO canteen (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
        
        // 添加必要字段
        if (canteenData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("name"));
        }
        if (canteenData.containsKey("constructionDate")) {
            sqlBuilder.append("construction_date, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("constructionDate"));
        }
        if (canteenData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("addressId"));
        }
        if (canteenData.containsKey("foodType")) {
            sqlBuilder.append("food_type, ");
            valuesBuilder.append("?, ");
            params.add(canteenData.get("foodType"));
        }
        
        // 添加默认营业状态
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");
        
        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }
    
    /**
     * 更新食堂信息
     * @param canteenId 食堂ID
     * @param updates 更新的字段和值
     * @return 更新是否成功
     * @throws SQLException SQL异常
     */
    public boolean updateCanteen(int canteenId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }
        
        StringBuilder sqlBuilder = new StringBuilder("UPDATE canteen SET ");
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }
        
        sqlBuilder.setLength(sqlBuilder.length() - 2); // 移除最后一个逗号和空格
        sqlBuilder.append(" WHERE canteen_id = ?");
        params.add(canteenId);
        
        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }
    
    /**
     * 删除食堂（软删除）
     * @param canteenId 食堂ID
     * @return 删除是否成功
     * @throws SQLException SQL异常
     */
    public boolean deleteCanteen(int canteenId) throws SQLException {
        String sql = "UPDATE canteen SET active_flag = 'N' WHERE canteen_id = ?";
        int result = executeUpdate(sql, canteenId);
        return result > 0;
    }
    
    /**
     * 根据条件查询食堂
     * @param conditions 查询条件
     * @return 食堂列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> queryCanteens(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM canteen WHERE 1=1");
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
     * 分页查询食堂
     * @param page 页码
     * @param pageSize 每页大小
     * @param conditions 查询条件
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return 分页查询结果
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getCanteensByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // 参数验证
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM canteen WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY canteen_id ASC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        // 获取数据
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());
        
        // 获取总数
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM canteen WHERE 1=1");
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
        
        // 安全获取总记录数，避免空指针异常
        if (countResult != null && !countResult.isEmpty()) {
            Map<String, Object> firstRow = countResult.get(0);
            if (firstRow != null) {
                Object countObj = firstRow.get("count");
                if (countObj instanceof Number) {
                    total = ((Number) countObj).intValue();
                } else if (countObj != null) {
                    try {
                        total = Integer.parseInt(countObj.toString());
                    } catch (NumberFormatException e) {
                        total = 0;
                    }
                }
            }
        }
        
        // 计算总页数
        int totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", totalPages);
        
        return result;
    }
    
    /**
     * 根据ID获取食堂信息
     * @param canteenId 食堂ID
     * @return 食堂信息
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getCanteenById(int canteenId) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE canteen_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, canteenId);
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }
    
    /**
     * 获取所有营业的食堂
     * @return 食堂列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getAllActiveCanteens() throws SQLException {
        String sql = "SELECT * FROM canteen WHERE active_flag = 'Y' ORDER BY canteen_id ASC";
        return executeQuery(sql);
    }
    
    /**
     * 根据餐饮类型查询食堂
     * @param foodType 餐饮类型
     * @return 符合条件的食堂列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getCanteensByFoodType(String foodType) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE food_type = ? AND active_flag = 'Y'";
        return executeQuery(sql, foodType);
    }
    
    /**
     * 根据建造日期范围查询食堂
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 符合条件的食堂列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getCanteensByConstructionDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM canteen WHERE construction_date BETWEEN ? AND ? AND active_flag = 'Y'";
        return executeQuery(sql, startDate, endDate);
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
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}