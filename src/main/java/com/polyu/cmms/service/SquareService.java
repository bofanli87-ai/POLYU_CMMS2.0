package com.polyu.cmms.service;

import java.sql.SQLException;
import java.util.*;

/**
 * 广场服务类，提供广场相关的数据访问功能
 */
public class SquareService extends BaseService {
    private static SquareService instance;
    
    // 单例模式
    private SquareService() {}
    
    public static synchronized SquareService getInstance() {
        if (instance == null) {
            instance = new SquareService();
        }
        return instance;
    }
    
    /**
     * 添加广场记录
     * @param squareData 广场数据
     * @return 插入是否成功
     * @throws SQLException SQL异常
     */
    public boolean addSquare(Map<String, Object> squareData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO squares (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
        
        // 添加必要字段
        if (squareData.containsKey("name")) {
            sqlBuilder.append("name, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("name"));
        }
        if (squareData.containsKey("addressId")) {
            sqlBuilder.append("address_id, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("addressId"));
        }
        if (squareData.containsKey("capacity")) {
            sqlBuilder.append("capacity, ");
            valuesBuilder.append("?, ");
            params.add(squareData.get("capacity"));
        }
        
        // 添加默认启用状态
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");
        
        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }
    
    /**
     * 更新广场信息
     * @param squareId 广场ID
     * @param updates 更新的字段和值
     * @return 更新是否成功
     * @throws SQLException SQL异常
     */
    public boolean updateSquare(int squareId, Map<String, Object> updates) throws SQLException {
        if (updates.isEmpty()) {
            return true;
        }
        
        StringBuilder sqlBuilder = new StringBuilder("UPDATE squares SET ");
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String dbColumn = convertToDbColumn(entry.getKey());
            sqlBuilder.append(dbColumn).append(" = ?, ");
            params.add(entry.getValue());
        }
        
        sqlBuilder.setLength(sqlBuilder.length() - 2); // 移除最后一个逗号和空格
        sqlBuilder.append(" WHERE square_id = ?");
        params.add(squareId);
        
        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }
    
    /**
     * 删除广场（软删除）
     * @param squareId 广场ID
     * @return 删除是否成功
     * @throws SQLException SQL异常
     */
    public boolean deleteSquare(int squareId) throws SQLException {
        String sql = "UPDATE squares SET active_flag = 'N' WHERE square_id = ?";
        int result = executeUpdate(sql, squareId);
        return result > 0;
    }
    
    /**
     * 根据条件查询广场
     * @param conditions 查询条件
     * @return 广场列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> querySquares(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM squares WHERE 1=1");
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
     * 分页查询广场
     * @param page 页码
     * @param pageSize 每页大小
     * @param conditions 查询条件
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @return 分页查询结果
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getSquaresByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
        // 参数验证
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;
        
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM squares WHERE 1=1");
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
            sqlBuilder.append(" ORDER BY square_id ASC");
        }
        
        // 添加分页
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        // 获取数据
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());
        
        // 获取总数
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM squares WHERE 1=1");
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
     * 根据ID获取广场信息
     * @param squareId 广场ID
     * @return 广场信息
     * @throws SQLException SQL异常
     */
    public Map<String, Object> getSquareById(int squareId) throws SQLException {
        String sql = "SELECT * FROM squares WHERE square_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, squareId);
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }
    
    /**
     * 获取所有在用的广场
     * @return 广场列表
     * @throws SQLException SQL异常
     */
    public List<Map<String, Object>> getAllActiveSquares() throws SQLException {
        String sql = "SELECT * FROM squares WHERE active_flag = 'Y' ORDER BY square_id ASC";
        return executeQuery(sql);
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