package com.polyu.cmms.view;

import com.polyu.cmms.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class QueryReportPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    // 服务类实例
    private QueryService queryService;
    private BuildingService buildingService;
    private StaffService staffService;
    private ChemicalService chemicalService;
    

    // 颜色主题
    private final Color PRIMARY_COLOR = new Color(70, 130, 180); // 钢蓝色
    private final Color ACCENT_COLOR = new Color(46, 125, 50); // 成功绿色
    private final Color BACKGROUND_COLOR = new Color(240, 245, 250); // 浅蓝背景
    private final Color PANEL_BACKGROUND = Color.WHITE;

    public QueryReportPanel() {
        // 初始化服务类
        queryService = new QueryService();
        buildingService = BuildingService.getInstance();
        staffService = StaffService.getInstance();
        chemicalService = ChemicalService.getInstance();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // 左侧导航面板
        JPanel navigationPanel = createNavigationPanel();

        // 创建内容面板，使用CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(PANEL_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加各个查询面板 - 直接使用内部类实例
        contentPanel.add(new SqlQueryPanel(), "sql");
        contentPanel.add(new BuildingActivityPanel(), "buildingActivity");
        contentPanel.add(new StaffActivityPanel(), "staffActivity");
        contentPanel.add(new ChemicalUsagePanel(), "chemicalUsage");
        // contentPanel.add(new EquipmentActivityQueryPanel(), "equipmentActivity");
        // contentPanel.add(new MaintenanceRecordQueryPanel(), "maintenanceRecord");

        // 添加面板到主面板
        add(navigationPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createNavigationPanel() {
        JPanel navigationPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Query"));
        navigationPanel.setBackground(PANEL_BACKGROUND);

        // 创建导航按钮
        JButton sqlQueryButton = createNavButton("SQL Query");
        JButton buildingActivityButton = createNavButton("Building Activity Query");
        JButton staffActivityButton = createNavButton("Staff Activity Query");
        JButton chemicalUsageButton = createNavButton("Chemical Usage Query");
        // JButton equipmentActivityButton = createNavButton("Equipment Activity Query");
        // JButton maintenanceRecordButton = createNavButton("Maintenance Record Query");

        // 添加按钮监听器
        sqlQueryButton.addActionListener(new NavigationListener("sql"));
        buildingActivityButton.addActionListener(new NavigationListener("buildingActivity"));
        staffActivityButton.addActionListener(new NavigationListener("staffActivity"));
        chemicalUsageButton.addActionListener(new NavigationListener("chemicalUsage"));
        // equipmentActivityButton.addActionListener(new NavigationListener("equipmentActivity"));
        // maintenanceRecordButton.addActionListener(new NavigationListener("maintenanceRecord"));

        // 添加按钮到导航面板
        navigationPanel.add(sqlQueryButton);
        navigationPanel.add(buildingActivityButton);
        navigationPanel.add(staffActivityButton);
        navigationPanel.add(chemicalUsageButton);
        // navigationPanel.add(equipmentActivityButton);
        // navigationPanel.add(maintenanceRecordButton);

        return navigationPanel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        // 使用系统默认样式以保持一致性
        return button;
    }

    private class NavigationListener implements ActionListener {
        private String panelName;

        public NavigationListener(String panelName) {
            this.panelName = panelName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            cardLayout.show(contentPanel, panelName);
        }
    }

    // SQL查询面板
    private class SqlQueryPanel extends JPanel {
        private JTextArea sqlTextArea;
        private JTextArea resultTextArea;

        public SqlQueryPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(PANEL_BACKGROUND);

            // 标题
            JLabel titleLabel = createSectionTitle("SQL Query");
            add(titleLabel, BorderLayout.NORTH);

            // 主内容区域
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(PANEL_BACKGROUND);

            // SQL输入区域
            JPanel sqlInputPanel = new JPanel(new BorderLayout(5, 5));
            sqlInputPanel.setBorder(BorderFactory.createTitledBorder("SQL Statement Input"));
            sqlTextArea = new JTextArea(8, 50);
            sqlTextArea.setLineWrap(true);
            sqlTextArea.setWrapStyleWord(true);
            JScrollPane sqlScrollPane = new JScrollPane(sqlTextArea);
            sqlInputPanel.add(sqlScrollPane, BorderLayout.CENTER);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(PANEL_BACKGROUND);

            JButton executeButton = new JButton("Execute Query");
            JButton clearButton = new JButton("Clear");

            // 执行查询按钮事件
            executeButton.addActionListener(e -> executeSqlQuery());

            // 清空按钮事件
            clearButton.addActionListener(e -> {
                sqlTextArea.setText("");
                resultTextArea.setText("");
            });

            buttonPanel.add(executeButton);
            buttonPanel.add(clearButton);
            sqlInputPanel.add(buttonPanel, BorderLayout.SOUTH);

            // 结果区域
            JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
            resultPanel.setBorder(BorderFactory.createTitledBorder("Query Results"));
            resultTextArea = new JTextArea(15, 50);
            resultTextArea.setEditable(false);
            resultTextArea.setLineWrap(true);
            resultTextArea.setWrapStyleWord(true);
            JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
            resultPanel.add(resultScrollPane, BorderLayout.CENTER);

            mainPanel.add(sqlInputPanel, BorderLayout.NORTH);
            mainPanel.add(resultPanel, BorderLayout.CENTER);

            add(mainPanel, BorderLayout.CENTER);
        }

        private void executeSqlQuery() {
            String sql = sqlTextArea.getText().trim();
            if (sql.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a SQL statement", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                List<Map<String, Object>> results = queryService.executeCustomQuery(sql);
                displayResults(results);
            } catch (SQLException ex) {
                resultTextArea.setText("Error executing SQL query:\n" + ex.getMessage());
            } catch (Exception ex) {
                resultTextArea.setText("Error:\n" + ex.getMessage());
            }
        }

        private void displayResults(List<Map<String, Object>> results) {
            if (results == null || results.isEmpty()) {
                resultTextArea.setText("Query executed successfully, but no results returned.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            // 显示列名
            Map<String, Object> firstRow = results.get(0);
            sb.append("Column Names: ");
            for (String column : firstRow.keySet()) {
                sb.append(column).append(" | ");
            }
            sb.append("\n").append("=".repeat(80)).append("\n");

            // 显示数据
            for (Map<String, Object> row : results) {
                for (Object value : row.values()) {
                    sb.append(value != null ? value.toString() : "NULL").append(" | ");
                }
                sb.append("\n");
            }

            sb.append("\nTotal Records: ").append(results.size());
            resultTextArea.setText(sb.toString());
        }
    }
    // 建筑物活动查询面板
    private class BuildingActivityPanel extends JPanel {
        private JComboBox<String> buildingComboBox;
        private JTextField startDateField;
        private JTextField endDateField;
        private JCheckBox hazardousCheckBox;
        private JTable resultTable;

        public BuildingActivityPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(PANEL_BACKGROUND);

            // 标题
            JLabel titleLabel = new JLabel("Building Activity Query", JLabel.CENTER);
            add(titleLabel, BorderLayout.NORTH);

            // 查询条件面板
            JPanel criteriaPanel = createCriteriaPanel();
            add(criteriaPanel, BorderLayout.NORTH);

            // 结果表格
            resultTable = new JTable() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JScrollPane scrollPane = new JScrollPane(resultTable);
            add(scrollPane, BorderLayout.CENTER);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(PANEL_BACKGROUND);

            JButton queryButton = new JButton("Query");
            JButton exportButton = new JButton("Export Results");

            queryButton.addActionListener(e -> queryBuildingActivities());
            exportButton.addActionListener(e -> exportResults());

            buttonPanel.add(queryButton);
            buttonPanel.add(exportButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JPanel createCriteriaPanel() {
            JPanel criteriaPanel = new JPanel(new GridLayout(4, 2));
            criteriaPanel.setBorder(BorderFactory.createTitledBorder("Query Criteria"));
            criteriaPanel.setBackground(PANEL_BACKGROUND);

            // 建筑物选择
            JLabel buildingLabel = new JLabel("Building:");
            buildingComboBox = new JComboBox<>();

            // 日期选择
            JLabel startDateLabel = new JLabel("Start Date:");
            startDateField = new JTextField();
            startDateField.setToolTipText("Format: YYYY-MM-DD, e.g., 2024-01-01");

            JLabel endDateLabel = new JLabel("End Date:");
            endDateField = new JTextField();
            endDateField.setToolTipText("Format: YYYY-MM-DD, e.g., 2024-12-31");

            // 危险化学品选项
            JLabel hazardousLabel = new JLabel("Contains Hazardous Chemicals:");
            hazardousCheckBox = new JCheckBox();
            hazardousCheckBox.setBackground(PANEL_BACKGROUND);

            // 添加组件
            criteriaPanel.add(buildingLabel);
            criteriaPanel.add(buildingComboBox);
            criteriaPanel.add(startDateLabel);
            criteriaPanel.add(startDateField);
            criteriaPanel.add(endDateLabel);
            criteriaPanel.add(endDateField);
            criteriaPanel.add(hazardousLabel);
            criteriaPanel.add(hazardousCheckBox);

            // 加载建筑物数据
            loadBuildings();

            return criteriaPanel;
        }

        private void loadBuildings() {
            try {
                List<Map<String, Object>> buildings = buildingService.getAllActiveBuildings();
                buildingComboBox.addItem("All"); // 添加"全部"选项
                for (Map<String, Object> building : buildings) {
                    String buildingCode = (String) building.get("buildingCode");
                    if (buildingCode != null) {
                        buildingComboBox.addItem(buildingCode);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load building list: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void queryBuildingActivities() {
            try {
                String selectedBuilding = (String) buildingComboBox.getSelectedItem();
                String startDate = startDateField.getText().trim();
                String endDate = endDateField.getText().trim();
                boolean includeHazardous = hazardousCheckBox.isSelected();

                // 构建查询SQL
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT a.activity_id, a.title, a.activity_type, a.status, ");
                sql.append("a.activity_datetime, a.hazard_level, b.building_code ");
                sql.append("FROM activity a ");
                sql.append("LEFT JOIN buildings b ON a.building_id = b.building_id ");
                sql.append("WHERE a.active_flag = 'Y' ");

                // 添加建筑物条件
                if (!"All".equals(selectedBuilding)) {
                    sql.append("AND b.building_code = ? ");
                }

                // 添加日期条件
                if (!startDate.isEmpty()) {
                    sql.append("AND a.activity_datetime >= ? ");
                }
                if (!endDate.isEmpty()) {
                    sql.append("AND a.activity_datetime <= ? ");
                }

                // 添加危险化学品条件
                if (includeHazardous) {
                    sql.append("AND a.hazard_level IN ('medium', 'high') ");
                }

                sql.append("ORDER BY a.activity_datetime DESC");

                // 执行查询
                List<Object> params = new java.util.ArrayList<>();
                if (!"All".equals(selectedBuilding)) {
                    params.add(selectedBuilding);
                }
                if (!startDate.isEmpty()) {
                    params.add(startDate);
                }
                if (!endDate.isEmpty()) {
                    params.add(endDate + " 23:59:59"); // 包含当天的所有时间
                }

                List<Map<String, Object>> results = queryService.executeCustomQuery(
                        sql.toString(), params.toArray());

                displayTableResults(results);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to query activities: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void displayTableResults(List<Map<String, Object>> results) {
            if (results == null || results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching activity records found", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 创建表模型
            Vector<String> columnNames = new Vector<>();
            if (!results.isEmpty()) {
                columnNames.addAll(results.get(0).keySet());
            }

            Vector<Vector<Object>> data = new Vector<>();
            for (Map<String, Object> row : results) {
                Vector<Object> rowData = new Vector<>();
                for (String column : columnNames) {
                    rowData.add(row.get(column));
                }
                data.add(rowData);
            }

            resultTable.setModel(new DefaultTableModel(data, columnNames));
        }

        private void exportResults() {
            JOptionPane.showMessageDialog(this, "Export feature is not implemented yet", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 员工活动查询面板
    private class StaffActivityPanel extends JPanel {
        private JComboBox<String> staffComboBox;
        private JTable resultTable;

        public StaffActivityPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(PANEL_BACKGROUND);

            // 查询条件面板
            JPanel criteriaPanel = createCriteriaPanel();
            add(criteriaPanel, BorderLayout.NORTH);

            // 结果表格
            resultTable = new JTable() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JScrollPane scrollPane = new JScrollPane(resultTable);
            add(scrollPane, BorderLayout.CENTER);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(PANEL_BACKGROUND);

            JButton queryButton = new JButton("Query Activities");
            JButton exportButton = new JButton("Export Results");

            queryButton.addActionListener(e -> queryStaffActivities());
            exportButton.addActionListener(e -> exportResults());

            buttonPanel.add(queryButton);
            buttonPanel.add(exportButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JPanel createCriteriaPanel() {
            JPanel criteriaPanel = new JPanel(new GridLayout(2, 2));
            criteriaPanel.setBorder(BorderFactory.createTitledBorder("Query Criteria"));
            criteriaPanel.setBackground(PANEL_BACKGROUND);

            // 员工选择
            JLabel staffLabel = new JLabel("Staff Name:");
            staffComboBox = new JComboBox<>();

            criteriaPanel.add(staffLabel);
            criteriaPanel.add(staffComboBox);

            // 加载员工数据
            loadStaff();

            return criteriaPanel;
        }

        private void loadStaff() {
            try {
                List<Map<String, Object>> staffList = staffService.queryStaff(new java.util.HashMap<>());
                for (Map<String, Object> staff : staffList) {
                    String firstName = (String) staff.get("firstName");
                    String lastName = (String) staff.get("lastName");
                    String staffNumber = (String) staff.get("staffNumber");
                    if (firstName != null && lastName != null) {
                        String displayName = firstName + " " + lastName + " (" + staffNumber + ")";
                        staffComboBox.addItem(displayName);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load staff list: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void queryStaffActivities() {
            try {
                String selectedStaff = (String) staffComboBox.getSelectedItem();
                if (selectedStaff == null) {
                    JOptionPane.showMessageDialog(this, "Please select a staff member", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 从显示文本中提取员工编号
                String staffNumber = selectedStaff.substring(selectedStaff.lastIndexOf("(") + 1, selectedStaff.lastIndexOf(")"));

                // 构建查询SQL
                String sql = """
                    SELECT a.activity_id, a.title, a.activity_type, a.status, 
                           a.activity_datetime, wf.activity_responsibility,
                           CONCAT(s.first_name, ' ', s.last_name) as staff_name
                    FROM activity a
                    JOIN works_for wf ON a.activity_id = wf.activity_id
                    JOIN staff s ON wf.staff_id = s.staff_id
                    WHERE s.staff_number = ? AND a.active_flag = 'Y' AND wf.active_flag = 'Y'
                    ORDER BY a.activity_datetime DESC
                """;

                List<Map<String, Object>> results = queryService.executeCustomQuery(sql, staffNumber);
                displayTableResults(results);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to query staff activities: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void displayTableResults(List<Map<String, Object>> results) {
            if (results == null || results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No activities found for the selected staff member", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Vector<String> columnNames = new Vector<>();
            if (!results.isEmpty()) {
                columnNames.addAll(results.get(0).keySet());
            }

            Vector<Vector<Object>> data = new Vector<>();
            for (Map<String, Object> row : results) {
                Vector<Object> rowData = new Vector<>();
                for (String column : columnNames) {
                    rowData.add(row.get(column));
                }
                data.add(rowData);
            }

            resultTable.setModel(new DefaultTableModel(data, columnNames));
        }

        private void exportResults() {
            JOptionPane.showMessageDialog(this, "Export feature is not implemented yet", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 化学物质使用查询面板
    private class ChemicalUsagePanel extends JPanel {
        private JComboBox<String> chemicalComboBox;
        private JTable resultTable;

        public ChemicalUsagePanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(PANEL_BACKGROUND);
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // 标题
            JLabel titleLabel = createSectionTitle("Chemical Usage Query");
            add(titleLabel, BorderLayout.NORTH);

            // 查询条件面板
            JPanel criteriaPanel = createCriteriaPanel();
            add(criteriaPanel, BorderLayout.NORTH);

            // 结果表格
            resultTable = createStyledTable();
            JScrollPane scrollPane = new JScrollPane(resultTable);
            add(scrollPane, BorderLayout.CENTER);

            // 按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(PANEL_BACKGROUND);

            JButton queryButton = QueryReportPanel.this.createActionButton("Query", QueryReportPanel.this.ACCENT_COLOR);
            JButton exportButton = QueryReportPanel.this.createActionButton("Export Results", QueryReportPanel.this.PRIMARY_COLOR);

            queryButton.addActionListener(e -> queryChemicalUsage());
            exportButton.addActionListener(e -> exportResults());

            buttonPanel.add(queryButton);
            buttonPanel.add(exportButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JPanel createCriteriaPanel() {
            JPanel criteriaPanel = new JPanel(new GridLayout(2, 2, 15, 15));
            criteriaPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(QueryReportPanel.this.PRIMARY_COLOR, 1),
                            "Query Criteria"
                    ),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            criteriaPanel.setBackground(PANEL_BACKGROUND);

            // 化学品选择
            JLabel chemicalLabel = QueryReportPanel.this.createFieldLabel("Chemical Name:");
            chemicalComboBox = QueryReportPanel.this.createStyledComboBox();

            criteriaPanel.add(chemicalLabel);
            criteriaPanel.add(chemicalComboBox);

            // 加载化学品数据
            loadChemicals();

            return criteriaPanel;
        }

        private void loadChemicals() {
            try {
                List<Map<String, Object>> chemicals = chemicalService.getAllActiveChemicals();
                for (Map<String, Object> chemical : chemicals) {
                    String chemicalName = (String) chemical.get("name");
                    String productCode = (String) chemical.get("productCode");
                    if (chemicalName != null) {
                        String displayName = chemicalName + " (" + productCode + ")";
                        chemicalComboBox.addItem(displayName);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Load Chemical List Failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void queryChemicalUsage() {
            try {
                String selectedChemical = (String) chemicalComboBox.getSelectedItem();
                if (selectedChemical == null) {
                    JOptionPane.showMessageDialog(this, "Please select a chemical", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 从显示文本中提取化学品名称
                String chemicalName = selectedChemical.substring(0, selectedChemical.lastIndexOf(" ("));

                // 构建查询SQL - 查询使用该化学品的活动
                String sql = """
                    SELECT DISTINCT a.activity_id, a.title, a.activity_type, a.status, 
                           a.activity_datetime, a.hazard_level, sc.check_datetime,
                           c.name as chemical_name, sc.check_result
                    FROM activity a
                    JOIN safety_check sc ON a.activity_id = sc.activity_id
                    JOIN chemical c ON sc.chemical_id = c.chemical_id
                    WHERE c.name = ? AND a.active_flag = 'Y'
                    ORDER BY a.activity_datetime DESC
                """;

                List<Map<String, Object>> results = queryService.executeCustomQuery(sql, chemicalName);
                displayTableResults(results);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Query Failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void displayTableResults(List<Map<String, Object>> results) {
            if (results == null || results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No activities using this chemical", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Vector<String> columnNames = new Vector<>();
            if (!results.isEmpty()) {
                columnNames.addAll(results.get(0).keySet());
            }

            Vector<Vector<Object>> data = new Vector<>();
            for (Map<String, Object> row : results) {
                Vector<Object> rowData = new Vector<>();
                for (String column : columnNames) {
                    rowData.add(row.get(column));
                }
                data.add(rowData);
            }

            resultTable.setModel(new DefaultTableModel(data, columnNames));
        }

        private void exportResults() {
            JOptionPane.showMessageDialog(this, "Export Functionality is Under Development", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 辅助方法
    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        // 简化标题样式，与其他面板保持一致
        return label;
    }



    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        // 使用系统默认样式以保持一致性
        return button;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        // 使用系统默认样式
        return label;
    }

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        // 使用系统默认样式
        return comboBox;
    }



    private JTable createStyledTable() {
        JTable table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 使表格不可编辑
            }
        };

        // 简化表格样式，与其他面板保持一致
        table.getTableHeader().setReorderingAllowed(false);
        
        return table;
    }
}