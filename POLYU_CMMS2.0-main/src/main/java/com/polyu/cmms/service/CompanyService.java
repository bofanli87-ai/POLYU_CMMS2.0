
package com.polyu.cmms.service;
import java.sql.SQLException;
import java.util.*;
/**
 Outsourcing company service class, provides data access functions related to outsourcing companies
 */
public class CompanyService extends BaseService {
    private static CompanyService instance;
    // Singleton pattern
    private CompanyService() {}
    public static synchronized CompanyService getInstance() {
        if (instance == null) {
            instance = new CompanyService();
        }
        return instance;
    }
    /**
     Add an outsourcing company record
     @param companyData Company data
     @return Whether the insertion was successful
     @throws SQLException SQL exception
     */
    public boolean addCompany(Map<String, Object> companyData) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO company (");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
// Add necessary fields
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
// Add default active status
        sqlBuilder.append("active_flag");
        valuesBuilder.append("'Y'");
        String sql = sqlBuilder.toString() + ") " + valuesBuilder.toString() + ")";
        int result = executeUpdate(sql, params.toArray());
        return result > 0;
    }
    /**
     Update outsourcing company information
     @param contractorId Company ID
     @param updates Fields and values to update
     @return Whether the update was successful
     @throws SQLException SQL exception
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
        sqlBuilder.setLength(sqlBuilder.length() - 2); // Remove the last comma and space
        sqlBuilder.append(" WHERE contractor_id = ?");
        params.add(contractorId);
        int result = executeUpdate(sqlBuilder.toString(), params.toArray());
        return result > 0;
    }
    /**
     Delete an outsourcing company (soft delete)
     @param contractorId Company ID
     @return Whether the deletion was successful
     @throws SQLException SQL exception
     */
    public boolean deleteCompany(int contractorId) throws SQLException {
        String sql = "UPDATE company SET active_flag = 'N' WHERE contractor_id = ?";
        int result = executeUpdate(sql, contractorId);
        return result > 0;
    }
    /**
     Query outsourcing companies based on conditions
     @param conditions Query conditions
     @return List of companies
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> queryCompanies(Map<String, Object> conditions) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (conditions != null) {
// Convert Java property names to database column names
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
        return executeQuery(sqlBuilder.toString(), params.toArray());
    }
    /**
     Paginated query for outsourcing companies
     @param page Page number
     @param pageSize Page size
     @param conditions Query conditions
     @param sortField Sort field
     @param sortOrder Sort order
     @return Paginated query results
     @throws SQLException SQL exception
     */
    public Map<String, Object> getCompaniesByPage(int page, int pageSize, Map<String, Object> conditions, String sortField, String sortOrder) throws SQLException {
// Parameter validation
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 30;
        StringBuilder sqlBuilder = new StringBuilder("SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE 1=1");
        List<Object> params = new ArrayList<>();
// Add filter conditions
        if (conditions != null) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String dbColumn = convertToDbColumn(entry.getKey());
                sqlBuilder.append(" AND ").append(dbColumn).append(" = ?");
                params.add(entry.getValue());
            }
        }
// Add sorting
        if (sortField != null && !sortField.trim().isEmpty()) {
            String dbColumn = convertToDbColumn(sortField);
            String order = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
            sqlBuilder.append(" ORDER BY ").append(dbColumn).append(" ").append(order);
        } else {
// Default sort by ID
            sqlBuilder.append(" ORDER BY contractor_id ASC");
        }
// Add pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
// Get data
        List<Map<String, Object>> data = executeQuery(sqlBuilder.toString(), params.toArray());
// Get total count - simplified method
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
// Directly use the first column to get the total count
        if (countResult != null && !countResult.isEmpty() && countResult.get(0) != null && !countResult.get(0).isEmpty()) {
// Get the value of the first column in the result set
            Object firstValue = countResult.get(0).values().iterator().next();
            if (firstValue instanceof Number) {
                total = ((Number) firstValue).intValue();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total); // Use "total" key to be consistent with the test class
        result.put("currentPage", page);
        result.put("pageSize", pageSize);
// Safely calculate total pages to avoid type conversion issues
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
     Get outsourcing company details by ID
     @param contractorId Company ID
     @return Company details
     @throws SQLException SQL exception
     */
    public Map<String, Object> getCompanyById(int contractorId) throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE contractor_id = ?";
        List<Map<String, Object>> results = executeQuery(sql, contractorId);
        return results.isEmpty() ? null : results.get(0);
    }
    /**
     Get all active outsourcing companies
     @return List of active companies
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getAllActiveCompanies() throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE active_flag = 'Y' ORDER BY name";
        return executeQuery(sql);
    }
    /**
     Query outsourcing companies by expertise
     @param expertise Expertise field
     @return List of companies
     @throws SQLException SQL exception
     */
    public List<Map<String, Object>> getCompaniesByExpertise(String expertise) throws SQLException {
        String sql = "SELECT contractor_id, contractor_code, name, contact_name, contract_quote, email, phone, address_id, expertise, tax_id, bank_account, active_flag FROM company WHERE expertise LIKE ? AND active_flag = 'Y' ORDER BY name";
        return executeQuery(sql, "%" + expertise + "%");
    }
    /**
     Convert Java camel case naming to database underscore naming
     @param javaName Java property name (camel case)
     @return Database column name (underscore naming)
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