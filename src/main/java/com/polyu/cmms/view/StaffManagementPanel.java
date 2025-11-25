package com.polyu.cmms.view;

// import com.polyu.cmms.model.Staff;
import com.polyu.cmms.service.StaffService;
import com.polyu.cmms.service.SuperviseService;
// import com.polyu.cmms.util.DatabaseUtil;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.Font;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffManagementPanel extends JPanel {
    public StaffManagementPanel() {
        setLayout(new BorderLayout());
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 添加各个功能面板
        tabbedPane.addTab("员工列表", new StaffListPanel());
        tabbedPane.addTab("监督关系", new SupervisionPanel());
        tabbedPane.addTab("人员统计", new StaffStatisticsPanel());
        
        add(new JLabel("人员管理", JLabel.CENTER), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    // 员工列表面板
    private class StaffListPanel extends JPanel {
            private JTable staffTable;
            private DefaultTableModel tableModel;
            private JTextField staffNumberField, firstNameField, lastNameField;
            private JComboBox<String> genderComboBox, departmentComboBox, roleComboBox, activeFlagComboBox;
            private JButton searchButton, resetButton, addButton, deleteButton;
            private int currentPage = 1;
            private int pageSize = 10;
            private JLabel pageInfoLabel;
            private JButton prevButton, nextButton;
        
        public StaffListPanel() {
            setLayout(new BorderLayout());
            
            // 创建搜索面板
            JPanel searchPanel = createSearchPanel();
            
            // 创建表格
            createTable();
            JScrollPane scrollPane = new JScrollPane(staffTable);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板，使用默认FlowLayout
            JPanel buttonPanel = new JPanel();
            
            // 创建按钮，使用系统默认样式
            addButton = new JButton("添加员工");
            deleteButton = new JButton("删除员工");
            
            buttonPanel.add(addButton);
            buttonPanel.add(deleteButton);
            
            // 将按钮面板移到搜索面板下方，使其更明显
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(searchPanel, BorderLayout.NORTH);
            topPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(topPanel, BorderLayout.NORTH);
            
            // 创建分页面板，并将其放在底部
            JPanel paginationPanel = createPaginationPanel();
            add(paginationPanel, BorderLayout.SOUTH);
            
            // 添加事件监听器
            addEventListeners();
            
            // 加载数据
            loadStaffData();
        }
        
        private JPanel createSearchPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // 员工编号
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("员工编号:"), gbc);
            gbc.gridx = 1;
            staffNumberField = new JTextField(10);
            panel.add(staffNumberField, gbc);
            
            // 名字
            gbc.gridx = 2;
            panel.add(new JLabel("名字:"), gbc);
            gbc.gridx = 3;
            firstNameField = new JTextField(10);
            panel.add(firstNameField, gbc);
            
            // 姓氏
            gbc.gridx = 4;
            panel.add(new JLabel("姓氏:"), gbc);
            gbc.gridx = 5;
            lastNameField = new JTextField(10);
            panel.add(lastNameField, gbc);
            
            // 性别
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("性别:"), gbc);
            gbc.gridx = 1;
            genderComboBox = new JComboBox<>(new String[]{"", "M", "F"});
            panel.add(genderComboBox, gbc);
            
            // 部门
            gbc.gridx = 2;
            panel.add(new JLabel("部门:"), gbc);
            gbc.gridx = 3;
            departmentComboBox = new JComboBox<>(new String[]{"", "设施管理", "维修部门", "清洁部门", "安全部门", "行政部门"});
            panel.add(departmentComboBox, gbc);
            
            // 角色
            gbc.gridx = 4;
            panel.add(new JLabel("角色:"), gbc);
            gbc.gridx = 5;
            roleComboBox = new JComboBox<>(new String[]{"", "行政官", "中层经理", "基层员工"});
            panel.add(roleComboBox, gbc);
            
            // 状态
            gbc.gridx = 6;
            panel.add(new JLabel("状态:"), gbc);
            gbc.gridx = 7;
            activeFlagComboBox = new JComboBox<>(new String[]{"", "Y", "N"});
            panel.add(activeFlagComboBox, gbc);
            
            // 按钮
            gbc.gridx = 8;
            searchButton = new JButton("搜索");
            panel.add(searchButton, gbc);
            
            gbc.gridx = 9;
            resetButton = new JButton("重置");
            panel.add(resetButton, gbc);
            
            return panel;
        }
        
        private void createTable() {
            // 根据staff表结构定义列名
            String[] columnNames = {"员工ID", "员工编号", "名字", "姓氏", "性别", "电话", "邮箱", 
                                    "入职日期", "角色ID", "状态"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            staffTable = new JTable(tableModel);
            staffTable.getTableHeader().setReorderingAllowed(false);
            
            // 设置列宽，确保索引正确
            staffTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // 员工ID
            staffTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 员工编号
            staffTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // 名字
            staffTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // 姓氏
            staffTable.getColumnModel().getColumn(4).setPreferredWidth(50);   // 性别
            staffTable.getColumnModel().getColumn(5).setPreferredWidth(120);  // 电话
            staffTable.getColumnModel().getColumn(6).setPreferredWidth(150);  // 邮箱
            staffTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // 入职日期
            staffTable.getColumnModel().getColumn(8).setPreferredWidth(60);   // 角色ID
            staffTable.getColumnModel().getColumn(9).setPreferredWidth(60);   // 状态
        }
        
        private JPanel createPaginationPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            prevButton = new JButton("上一页");
            nextButton = new JButton("下一页");
            pageInfoLabel = new JLabel("第 1 页");
            
            panel.add(prevButton);
            panel.add(pageInfoLabel);
            panel.add(nextButton);
            
            return panel;
        }
        
        private void addEventListeners() {
            // 搜索按钮
            searchButton.addActionListener(e -> {
                currentPage = 1;
                loadStaffData();
            });
            
            // 重置按钮
            resetButton.addActionListener(e -> {
                staffNumberField.setText("");
                firstNameField.setText("");
                lastNameField.setText("");
                genderComboBox.setSelectedIndex(0);
                departmentComboBox.setSelectedIndex(0);
                roleComboBox.setSelectedIndex(0);
                activeFlagComboBox.setSelectedIndex(0);
                currentPage = 1;
                loadStaffData();
            });
            
            // 添加按钮
            addButton.addActionListener(e -> addStaff());
            
            // 删除按钮
            deleteButton.addActionListener(e -> deleteStaff());
            
            // 分页按钮
            prevButton.addActionListener(e -> {
                if (currentPage > 1) {
                    currentPage--;
                    loadStaffData();
                }
            });
            
            nextButton.addActionListener(e -> {
                currentPage++;
                loadStaffData();
            });
        }
        
        // 安全获取整数值的辅助方法
        private int getIntValue(Object value, int defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        
        private void loadStaffData() {
            try {
                StaffService staffService = StaffService.getInstance();
                Map<String, Object> filters = new HashMap<>();
                
                // 添加过滤条件
                if (!staffNumberField.getText().trim().isEmpty()) {
                    filters.put("staffNumber", staffNumberField.getText().trim());
                }
                if (!firstNameField.getText().trim().isEmpty()) {
                    filters.put("firstName", firstNameField.getText().trim());
                }
                if (!lastNameField.getText().trim().isEmpty()) {
                    filters.put("lastName", lastNameField.getText().trim());
                }
                if (!genderComboBox.getSelectedItem().toString().isEmpty()) {
                    filters.put("gender", genderComboBox.getSelectedItem().toString());
                }
                if (!departmentComboBox.getSelectedItem().toString().isEmpty()) {
                    filters.put("department", departmentComboBox.getSelectedItem().toString());
                }
                if (!roleComboBox.getSelectedItem().toString().isEmpty()) {
                    String roleName = roleComboBox.getSelectedItem().toString();
                    // 根据角色名称获取角色ID
                    int roleId = 0;
                    if (roleName.equals("行政官")) roleId = 1;
                    else if (roleName.equals("中层经理")) roleId = 2;
                    else if (roleName.equals("基层员工")) roleId = 3;
                    if (roleId > 0) {
                        filters.put("roleId", roleId);
                    }
                }
                if (!activeFlagComboBox.getSelectedItem().toString().isEmpty()) {
                    filters.put("activeFlag", activeFlagComboBox.getSelectedItem().toString());
                }
                
                // 获取数据
                Map<String, Object> result = staffService.getStaffByPage(currentPage, pageSize, filters, "staff_id", "ASC");
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> staffList = (java.util.List<Map<String, Object>>) result.get("data");
                int totalPages = getIntValue(result.get("totalPages"), 0);
                int total = getIntValue(result.get("total"), 0);
                
                // 更新表格
                tableModel.setRowCount(0);
                if (staffList != null) {
                    for (Map<String, Object> staff : staffList) {
                        Object[] rowData = {
                            staff.get("staffId"),
                            staff.get("staffNumber"),
                            staff.get("firstName"),
                            staff.get("lastName"),
                            staff.get("gender"),
                            staff.get("phone"),  // 与表结构匹配
                            staff.get("email"),
                            staff.get("hireDate"),
                            staff.get("roleId"),
                            staff.get("activeFlag")
                        };
                        tableModel.addRow(rowData);
                    }
                }
                
                // 更新分页信息
                pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + total + " 条记录");
                prevButton.setEnabled(currentPage > 1);
                nextButton.setEnabled(currentPage < totalPages);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "加载员工数据失败: " + ex.getMessage(), 
                                            "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addStaff() {
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加员工", true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(this);
            
            // 创建表单面板
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // 员工信息表单字段
            JTextField staffNumberInput = new JTextField(20);
            JTextField firstNameInput = new JTextField(20);
            JTextField lastNameInput = new JTextField(20);
            JComboBox<String> genderInput = new JComboBox<>(new String[]{"M", "F"});
            JTextField dateOfBirthInput = new JTextField(20);
            JTextField phoneInput = new JTextField(20);
            JTextField emailInput = new JTextField(20);
            JTextField hireDateInput = new JTextField(20);
            JComboBox<String> roleInput = new JComboBox<>(new String[]{"行政官", "中层经理", "基层员工"});
            JTextField emergencyContactInput = new JTextField(20);
            JTextField emergencyPhoneInput = new JTextField(20);
            
            // 添加表单字段
            int row = 0;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("员工编号:*"), gbc);
            gbc.gridx = 1; formPanel.add(staffNumberInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("名字:*"), gbc);
            gbc.gridx = 1; formPanel.add(firstNameInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("姓氏:*"), gbc);
            gbc.gridx = 1; formPanel.add(lastNameInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("性别:*"), gbc);
            gbc.gridx = 1; formPanel.add(genderInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("出生日期(yyyy-MM-dd):*"), gbc);
            gbc.gridx = 1; formPanel.add(dateOfBirthInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("电话:*"), gbc);
            gbc.gridx = 1; formPanel.add(phoneInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("邮箱:*"), gbc);
            gbc.gridx = 1; formPanel.add(emailInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("入职日期(yyyy-MM-dd):*"), gbc);
            gbc.gridx = 1; formPanel.add(hireDateInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("角色:*"), gbc);
            gbc.gridx = 1; formPanel.add(roleInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("紧急联系人:"), gbc);
            gbc.gridx = 1; formPanel.add(emergencyContactInput, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("紧急联系电话:"), gbc);
            gbc.gridx = 1; formPanel.add(emergencyPhoneInput, gbc);
            
           
            // 按钮面板
            JPanel buttonPanel = new JPanel();
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            // 添加面板到对话框
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            // 保存按钮事件
            saveButton.addActionListener(e -> {
                try {
                    // 参数验证
                    if (staffNumberInput.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "员工编号不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // 日期格式化
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date dateOfBirth = sdf.parse(dateOfBirthInput.getText().trim());
                    Date hireDate = sdf.parse(hireDateInput.getText().trim());
                    
                    // 角色转换
                    String roleName = roleInput.getSelectedItem().toString();
                    int roleId = 0;
                    if (roleName.equals("行政官")) roleId = 1;
                    else if (roleName.equals("中层经理")) roleId = 2;
                    else if (roleName.equals("基层员工")) roleId = 3;
                    
                    // 创建员工
                    StaffService staffService = StaffService.getInstance();
                    boolean success = staffService.createStaff(
                        staffNumberInput.getText().trim(),
                        firstNameInput.getText().trim(),
                        lastNameInput.getText().trim(),
                        genderInput.getSelectedItem().toString(),
                        dateOfBirth,
                        phoneInput.getText().trim(),
                        emailInput.getText().trim(),
                        hireDate,
                        roleId,
                        emergencyContactInput.getText().trim(),
                        emergencyPhoneInput.getText().trim()
                    );
                    
                    if (success) {
                        // 注意：根据需求移除了日志记录相关代码
                        
                        JOptionPane.showMessageDialog(dialog, "员工添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadStaffData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "员工添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (ParseException pe) {
                    JOptionPane.showMessageDialog(dialog, "日期格式不正确，请使用yyyy-MM-dd格式", "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(dialog, "地址ID必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "添加员工失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // 取消按钮事件
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.setVisible(true);
        }
        

        
        private void deleteStaff() {
            int selectedRow = staffTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的员工", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int staffId = (int) tableModel.getValueAt(selectedRow, 0);
            String staffNumber = (String) tableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, "确定要删除员工 " + staffNumber + " 吗？", 
                                                       "确认删除", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    StaffService staffService = StaffService.getInstance();
                    boolean success = staffService.deleteStaff(staffId);
                    
                    if (success) {
                        // 注意：根据需求移除了日志记录相关代码
                        
                        JOptionPane.showMessageDialog(this, "员工删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadStaffData();
                    } else {
                        JOptionPane.showMessageDialog(this, "员工删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "删除员工失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // 监督关系面板
    private class SupervisionPanel extends JPanel {
        private JTree supervisionTree;
        private JButton refreshButton;
        private JComboBox<String> supervisorComboBox; // 上级员工下拉框
        private JComboBox<String> subordinateComboBox; // 下级员工下拉框
        private JTextField startDateField; // 开始日期
        private JTextField endDateField; // 结束日期
        private JButton addSupervisionButton; // 添加监督关系按钮
        private SimpleDateFormat dateFormat; // 日期格式化工具
        
        public SupervisionPanel() {
            setLayout(new BorderLayout());
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            // 添加标题和刷新按钮
            JPanel topPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("三层监督关系结构", JLabel.CENTER);
            refreshButton = new JButton("刷新");
            topPanel.add(titleLabel, BorderLayout.CENTER);
            topPanel.add(refreshButton, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);
            
            // 添加创建监督关系的表单区域
            add(createSupervisionFormPanel(), BorderLayout.NORTH);
            
            // 创建树组件
            supervisionTree = new JTree();
            supervisionTree.setRootVisible(true);
            supervisionTree.setShowsRootHandles(true);
            
            // 设置树的样式
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(null);
            renderer.setClosedIcon(null);
            renderer.setOpenIcon(null);
            supervisionTree.setCellRenderer(renderer);
            
            JScrollPane scrollPane = new JScrollPane(supervisionTree);
            add(scrollPane, BorderLayout.CENTER);
            
            // 添加刷新按钮事件监听
            refreshButton.addActionListener(e -> loadSupervisionTree());
            
            // 添加监督关系按钮事件监听
            addSupervisionButton.addActionListener(e -> addSupervisionRelationship());
            
            // 初始加载数据
            loadSupervisionTree();
            // 加载员工选择框数据
            loadStaffForComboBox();
        }
        
        private void loadSupervisionTree() {
            try {
                System.out.println("\n===== 开始加载三层树状目录结构 =====");
                
                // 创建根节点，明确显示三层结构标题
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("三层监督关系结构");
                
                // 创建一个Set来跟踪已经添加过的员工ID，避免重复添加
                Set<Integer> addedStaffIds = new HashSet<>();
                
                // 获取所有role_id为1的员工（行政官）作为顶级节点
                List<Map<String, Object>> topLevelStaff = getTopLevelStaff();
                
                System.out.println("开始构建三层结构:");
                System.out.println("第1层 - 行政官(role_id=1)数量: " + topLevelStaff.size());
                
                // 为每个顶级员工（行政官）构建其子树
                for (Map<String, Object> staff : topLevelStaff) {
                    // 安全地获取staff_id
                    Integer staffId = null;
                    Object staffIdObj = staff.containsKey("staff_id") ? staff.get("staff_id") : 
                                      staff.containsKey("staffId") ? staff.get("staffId") : null;
                    if (staffIdObj instanceof Number) {
                        staffId = ((Number) staffIdObj).intValue();
                        
                        // 只有当员工ID未被添加过时才处理
                        if (!addedStaffIds.contains(staffId)) {
                            DefaultMutableTreeNode staffNode = createStaffNode(staff);
                            rootNode.add(staffNode);
                            addedStaffIds.add(staffId);
                            
                            // 调用修改后的buildSubordinateTree方法，传递addedStaffIds集合
                            System.out.println("为行政官(ID=" + staffId + ")构建下属树");
                            buildSubordinateTree(staffNode, staffId, 1, addedStaffIds);
                        }
                    }
                }
                
                // 创建树模型并设置给JTree
                DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
                supervisionTree.setModel(treeModel);
                
                // 设置树的样式，突出三层结构
                DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
                renderer.setLeafIcon(null);
                renderer.setClosedIcon(null);
                renderer.setOpenIcon(null);
                supervisionTree.setCellRenderer(renderer);
                
                // 展开所有节点
                expandAllNodes(supervisionTree, 0, supervisionTree.getRowCount());
                
                System.out.println("三层树状目录结构加载完成");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载三层监督关系失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        
        /**
         * 创建监督关系表单面板
         */
        private JPanel createSupervisionFormPanel() {
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("创建监督关系（行政官→中层经理→基层员工）"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // 上级员工选择
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("选择上级员工（行政官/中层经理）:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
            supervisorComboBox = new JComboBox<>();
            supervisorComboBox.setPreferredSize(new Dimension(250, 25));
            formPanel.add(supervisorComboBox, gbc);
            
            // 下级员工选择
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("选择下级员工（中层经理/基层员工）:"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            subordinateComboBox = new JComboBox<>();
            subordinateComboBox.setPreferredSize(new Dimension(250, 25));
            formPanel.add(subordinateComboBox, gbc);
            
            // 开始日期
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("开始日期 (YYYY-MM-DD):"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
            startDateField = new JTextField();
            startDateField.setPreferredSize(new Dimension(150, 25));
            startDateField.setToolTipText("格式: 2024-11-24");
            formPanel.add(startDateField, gbc);
            
            // 结束日期（可选）
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("结束日期 (可选):"), gbc);
            
            gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
            endDateField = new JTextField();
            endDateField.setPreferredSize(new Dimension(150, 25));
            endDateField.setToolTipText("格式: 2024-11-24，留空表示持续有效");
            formPanel.add(endDateField, gbc);
            
            // 添加按钮
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            JPanel buttonPanel = new JPanel();
            addSupervisionButton = new JButton("添加监督关系");
            buttonPanel.add(addSupervisionButton);
            formPanel.add(buttonPanel, gbc);
            
            return formPanel;
        }
        
        /**
         * 加载员工数据到下拉框
         */
        private void loadStaffForComboBox() {
            try {
                StaffService staffService = StaffService.getInstance();
                
                // 加载上级员工（行政官role_id=1和中层经理role_id=2）
                Map<String, Object> supervisorConditions = new HashMap<>();
                supervisorConditions.put("activeFlag", "Y");
                supervisorConditions.put("roleId", Arrays.asList(1, 2)); // 只加载行政官和中层经理
                List<Map<String, Object>> supervisors = staffService.queryStaff(supervisorConditions);
                
                supervisorComboBox.addItem("请选择上级员工");
                for (Map<String, Object> staff : supervisors) {
                    // 安全获取staffId
                    int staffId = 0;
                    Object staffIdObj = staff.containsKey("staff_id") ? staff.get("staff_id") : staff.get("staffId");
                    if (staffIdObj instanceof Number) {
                        staffId = ((Number) staffIdObj).intValue();
                    } else if (staffIdObj instanceof String) {
                        try {
                            staffId = Integer.parseInt((String) staffIdObj);
                        } catch (NumberFormatException e) {
                            System.err.println("无效的员工ID格式: " + staffIdObj);
                            continue;
                        }
                    } else {
                        System.err.println("员工ID为空或格式无效");
                        continue;
                    }
                    
                    String staffNumber = (String) (staff.containsKey("staff_number") ? staff.get("staff_number") : staff.get("staffNumber"));
                    String firstName = (String) (staff.containsKey("first_name") ? staff.get("first_name") : staff.get("firstName"));
                    String lastName = (String) (staff.containsKey("last_name") ? staff.get("last_name") : staff.get("lastName"));
                    
                    // 安全获取roleId
                    int roleId = 0;
                    Object roleIdObj = staff.containsKey("role_id") ? staff.get("role_id") : staff.get("roleId");
                    if (roleIdObj instanceof Number) {
                        roleId = ((Number) roleIdObj).intValue();
                    } else if (roleIdObj instanceof String) {
                        try {
                            roleId = Integer.parseInt((String) roleIdObj);
                        } catch (NumberFormatException e) {
                            System.err.println("无效的角色ID格式: " + roleIdObj);
                            continue;
                        }
                    } else {
                        System.err.println("角色ID为空或格式无效");
                        continue;
                    }
                    
                    String roleText = roleId == 1 ? "[行政官]" : "[中层经理]";
                    
                    supervisorComboBox.addItem(staffId + ": " + staffNumber + " - " + lastName + " " + firstName + " " + roleText);
                }
                
                // 加载下级员工（中层经理role_id=2和基层员工role_id=3）
                Map<String, Object> subordinateConditions = new HashMap<>();
                subordinateConditions.put("activeFlag", "Y");
                subordinateConditions.put("roleId", Arrays.asList(2, 3)); // 只加载中层经理和基层员工
                List<Map<String, Object>> subordinates = staffService.queryStaff(subordinateConditions);
                
                subordinateComboBox.addItem("请选择下级员工");
                for (Map<String, Object> staff : subordinates) {
                    // 安全获取staffId
                    int staffId = 0;
                    Object staffIdObj = staff.containsKey("staff_id") ? staff.get("staff_id") : staff.get("staffId");
                    if (staffIdObj instanceof Number) {
                        staffId = ((Number) staffIdObj).intValue();
                    } else if (staffIdObj instanceof String) {
                        try {
                            staffId = Integer.parseInt((String) staffIdObj);
                        } catch (NumberFormatException e) {
                            System.err.println("无效的员工ID格式: " + staffIdObj);
                            continue;
                        }
                    } else {
                        System.err.println("员工ID为空或格式无效");
                        continue;
                    }
                    
                    String staffNumber = (String) (staff.containsKey("staff_number") ? staff.get("staff_number") : staff.get("staffNumber"));
                    String firstName = (String) (staff.containsKey("first_name") ? staff.get("first_name") : staff.get("firstName"));
                    String lastName = (String) (staff.containsKey("last_name") ? staff.get("last_name") : staff.get("lastName"));
                    
                    // 安全获取roleId
                    int roleId = 0;
                    Object roleIdObj = staff.containsKey("role_id") ? staff.get("role_id") : staff.get("roleId");
                    if (roleIdObj instanceof Number) {
                        roleId = ((Number) roleIdObj).intValue();
                    } else if (roleIdObj instanceof String) {
                        try {
                            roleId = Integer.parseInt((String) roleIdObj);
                        } catch (NumberFormatException e) {
                            System.err.println("无效的角色ID格式: " + roleIdObj);
                            continue;
                        }
                    } else {
                        System.err.println("角色ID为空或格式无效");
                        continue;
                    }
                    
                    String roleText = roleId == 2 ? "[中层经理]" : "[基层员工]";
                    
                    subordinateComboBox.addItem(staffId + ": " + staffNumber + " - " + lastName + " " + firstName + " " + roleText);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "加载员工数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        
        /**
         * 添加监督关系
         */
        private void addSupervisionRelationship() {
            try {
                // 验证选择
                if (supervisorComboBox.getSelectedIndex() <= 0) {
                    JOptionPane.showMessageDialog(this, "请选择上级员工", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (subordinateComboBox.getSelectedIndex() <= 0) {
                    JOptionPane.showMessageDialog(this, "请选择下级员工", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 获取选择的员工ID
                String supervisorText = (String) supervisorComboBox.getSelectedItem();
                int supervisorId = Integer.parseInt(supervisorText.split(":")[0].trim());
                
                String subordinateText = (String) subordinateComboBox.getSelectedItem();
                int subordinateId = Integer.parseInt(subordinateText.split(":")[0].trim());
                
                // 验证不能自我监督
                if (supervisorId == subordinateId) {
                    JOptionPane.showMessageDialog(this, "不能设置自己为自己的监督者", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 验证并解析日期
                Date startDate = null;
                String startDateText = startDateField.getText().trim();
                if (startDateText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "请输入开始日期", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try {
                    startDate = dateFormat.parse(startDateText);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(this, "开始日期格式错误，请使用YYYY-MM-DD格式", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Date endDate = null;
                String endDateText = endDateField.getText().trim();
                if (!endDateText.isEmpty()) {
                    try {
                        endDate = dateFormat.parse(endDateText);
                        if (endDate.before(startDate)) {
                            JOptionPane.showMessageDialog(this, "结束日期不能早于开始日期", "错误", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(this, "结束日期格式错误，请使用YYYY-MM-DD格式", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // 调用服务添加监督关系
                SuperviseService superviseService = SuperviseService.getInstance();
                boolean success = superviseService.createSupervise(supervisorId, subordinateId, startDate, endDate);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "监督关系添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    
                    // 清空表单并刷新树
                    supervisorComboBox.setSelectedIndex(0);
                    subordinateComboBox.setSelectedIndex(0);
                    startDateField.setText("");
                    endDateField.setText("");
                    loadSupervisionTree();
                }
                
            } catch (IllegalArgumentException e) {
                // 显示业务逻辑错误（如角色层级不匹配）
                JOptionPane.showMessageDialog(this, e.getMessage(), "业务规则错误", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "添加监督关系失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        
        private List<Map<String, Object>> getTopLevelStaff() throws SQLException {
            // 使用StaffService获取role_id为1的员工（行政官）作为顶级节点
            StaffService staffService = StaffService.getInstance();
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("activeFlag", "Y");
            conditions.put("roleId", 1); // 只获取role_id为1的员工
            List<Map<String, Object>> topLevelStaff = staffService.queryStaff(conditions);
            
            System.out.println("===== 三层树状目录结构 - 顶级员工(role_id=1) =====");
            System.out.println("获取到的顶级员工数量: " + topLevelStaff.size());
            for (Map<String, Object> staff : topLevelStaff) {
                int staffId = 0;
                Object staffIdObj = staff.containsKey("staff_id") ? staff.get("staff_id") : staff.get("staffId");
                if (staffIdObj instanceof Number) {
                    staffId = ((Number) staffIdObj).intValue();
                } else if (staffIdObj instanceof String) {
                    try {
                        staffId = Integer.parseInt((String) staffIdObj);
                    } catch (NumberFormatException e) {
                        System.err.println("无效的员工ID格式: " + staffIdObj);
                    }
                } else {
                    System.err.println("员工ID为空或格式无效");
                }
                String staffNumber = staff.containsKey("staff_number") ? (String) staff.get("staff_number") : 
                                    staff.containsKey("staffNumber") ? (String) staff.get("staffNumber") : "";
                String firstName = staff.containsKey("first_name") ? (String) staff.get("first_name") : 
                                  staff.containsKey("firstName") ? (String) staff.get("firstName") : "";
                String lastName = staff.containsKey("last_name") ? (String) staff.get("last_name") : 
                                 staff.containsKey("lastName") ? (String) staff.get("lastName") : "";
                System.out.println("顶级员工: ID=" + staffId + ", " + staffNumber + " - " + lastName + " " + firstName);
            }
            System.out.println("=========================================");
            
            return topLevelStaff;
        }
        //         
        private DefaultMutableTreeNode createStaffNode(Map<String, Object> staff) {
            // 构建员工节点显示文本，包含角色信息以突出层级
            String staffNumber = staff.containsKey("staff_number") ? (String) staff.get("staff_number") : 
                               staff.containsKey("staffNumber") ? (String) staff.get("staffNumber") : "";
            String firstName = staff.containsKey("first_name") ? (String) staff.get("first_name") : 
                             staff.containsKey("firstName") ? (String) staff.get("firstName") : "";
            String lastName = staff.containsKey("last_name") ? (String) staff.get("last_name") : 
                            staff.containsKey("lastName") ? (String) staff.get("lastName") : "";
            
            // 获取角色信息
            String roleText = "";
            int roleId = 0;
            Object roleIdObj = staff.containsKey("role_id") ? staff.get("role_id") : 
                             staff.containsKey("roleId") ? staff.get("roleId") : null;
            if (roleIdObj instanceof Number) {
                roleId = ((Number) roleIdObj).intValue();
            } else if (roleIdObj instanceof String) {
                try {
                    roleId = Integer.parseInt((String) roleIdObj);
                } catch (NumberFormatException e) {
                    System.err.println("无效的角色ID格式: " + roleIdObj);
                }
            } else {
                System.err.println("角色ID为空或格式无效");
            }
                // 根据role_id显示相应的角色名称
                if (roleId == 1) {
                    roleText = "[行政官]";
                } else if (roleId == 2) {
                    roleText = "[中层经理]";
                } else if (roleId == 3) {
                    roleText = "[基层员工]";
                }
            
            
            // 显示员工编号、角色和全名
            String displayText = staffNumber + " " + roleText + " - " + lastName + " " + firstName;
            return new DefaultMutableTreeNode(displayText);
        }
        
        private void buildSubordinateTree(DefaultMutableTreeNode parentNode, int supervisorStaffId, int currentLevel, Set<Integer> addedStaffIds) throws SQLException {
            // 获取当前员工信息，确定其role_id
            StaffService staffService = StaffService.getInstance();
            Map<String, Object> supervisorInfo = staffService.getStaffById(supervisorStaffId);
            if (supervisorInfo == null) {
                return;
            }
            
            // 获取监督者的role_id
            int supervisorRoleId = 0;
            Object roleIdObj = supervisorInfo.containsKey("role_id") ? supervisorInfo.get("role_id") : 
                              supervisorInfo.containsKey("roleId") ? supervisorInfo.get("roleId") : null;
            if (roleIdObj != null) {
                if (roleIdObj instanceof Number) {
                    supervisorRoleId = ((Number) roleIdObj).intValue();
                } else if (roleIdObj instanceof String) {
                    try {
                        supervisorRoleId = Integer.parseInt((String) roleIdObj);
                    } catch (NumberFormatException e) {
                        System.err.println("无效的角色ID格式: " + roleIdObj);
                    }
                }
            } else {
                System.err.println("员工信息中角色ID为空");
            }
            
            // 根据监督者的角色确定下一级应该显示的角色
            Integer expectedSubordinateRoleId = null;
            if (supervisorRoleId == 1) {
                // 行政官(role_id=1)的下一级应为中层经理(role_id=2)
                expectedSubordinateRoleId = 2;
            } else if (supervisorRoleId == 2) {
                // 中层经理(role_id=2)的下一级应为基层员工(role_id=3)
                expectedSubordinateRoleId = 3;
            } else {
                // 基层员工(role_id=3)不应有下级
                return;
            }
            
            System.out.println("构建下属树: 监督者ID=" + supervisorStaffId + ", 监督者角色ID=" + supervisorRoleId + ", 期望下属角色ID=" + expectedSubordinateRoleId);
            
            // 获取当前员工的所有直接下属
            SuperviseService superviseService = SuperviseService.getInstance();
            List<Map<String, Object>> subordinates = superviseService.getSubordinatesByStaffId(supervisorStaffId);
            
            // 为每个下属构建节点
            for (Map<String, Object> subordinate : subordinates) {
                // 获取下属员工详细信息
                Object subordinateIdObj = subordinate.get("subordinate_staff_id");
                Integer subordinateId = null;
                if (subordinateIdObj != null) {
                    if (subordinateIdObj instanceof Number) {
                        subordinateId = ((Number) subordinateIdObj).intValue();
                    } else if (subordinateIdObj instanceof String) {
                        try {
                            subordinateId = Integer.parseInt((String) subordinateIdObj);
                        } catch (NumberFormatException e) {
                            System.err.println("无效的下属员工ID格式: " + subordinateIdObj);
                        }
                    }
                } else {
                    System.err.println("下属员工ID为空");
                }
                
                // 只有当subordinateId有效时才继续处理
                if (subordinateId == null) {
                    continue;
                }
                
                int id = subordinateId;
                    
                    // 检查这个员工ID是否已经被添加过，如果是则跳过
                    if (addedStaffIds.contains(id)) {
                        System.out.println("跳过已添加的员工ID=" + id);
                        continue;
                    }
                    
                    try {
                        Map<String, Object> staffInfo = staffService.getStaffById(id);
                
                        if (staffInfo != null) {
                            // 检查下属员工的roleId是否符合预期
            int subordinateRoleId = 0;
            Object subRoleIdObj = staffInfo.containsKey("roleId") ? staffInfo.get("roleId") : 
                                staffInfo.containsKey("role_id") ? staffInfo.get("role_id") : null;
            if (subRoleIdObj != null) {
                if (subRoleIdObj instanceof Number) {
                    subordinateRoleId = ((Number) subRoleIdObj).intValue();
                } else if (subRoleIdObj instanceof String) {
                    try {
                        subordinateRoleId = Integer.parseInt((String) subRoleIdObj);
                    } catch (NumberFormatException e) {
                        System.err.println("无效的角色ID格式: " + subRoleIdObj);
                    }
                }
            } else {
                System.err.println("员工信息中角色ID为空");
            }
                            
                            // 只有当下属角色ID符合预期时才添加到树中
                            if (subordinateRoleId == expectedSubordinateRoleId) {
                                DefaultMutableTreeNode staffNode = createStaffNode(staffInfo);
                                parentNode.add(staffNode);
                                addedStaffIds.add(subordinateId); // 将添加的员工ID加入集合
                                
                                // 记录添加的下属信息
                                String staffNumber = staffInfo.containsKey("staff_number") ? (String) staffInfo.get("staff_number") : 
                                                    staffInfo.containsKey("staffNumber") ? (String) staffInfo.get("staffNumber") : "";
                                String firstName = staffInfo.containsKey("first_name") ? (String) staffInfo.get("first_name") : 
                                                  staffInfo.containsKey("firstName") ? (String) staffInfo.get("firstName") : "";
                                String lastName = staffInfo.containsKey("last_name") ? (String) staffInfo.get("last_name") : 
                                                 staffInfo.containsKey("lastName") ? (String) staffInfo.get("lastName") : "";
                                System.out.println("添加下属: ID=" + id + ", 角色ID=" + subordinateRoleId + ", " + 
                                                 staffNumber + " - " + lastName + " " + firstName);
                                
                                // 只递归处理中层经理(role_id=2)的下属，基层员工(role_id=3)不应该有下级
                                    // 并且确保只构建三层结构
                                    if (subordinateRoleId == 2 && currentLevel < 2) {
                                        buildSubordinateTree(staffNode, id, currentLevel + 1, addedStaffIds);
                                    }
                            }
                        }
                    } catch (Exception e) {
                            // 记录错误但继续处理其他下属
                            System.out.println("处理下属ID=" + id + "时出错: " + e.getMessage());
                            e.printStackTrace();
                        }
                }
            }
        }
        
        // // 兼容旧方法调用的重载版本（不建议使用，会导致重复节点）
        // private void buildSubordinateTree(DefaultMutableTreeNode parentNode, int supervisorStaffId) throws SQLException {
        //     // 创建一个临时的Set，但这不能完全解决重复问题，主要用于兼容性
        //     Set<Integer> tempAddedStaffIds = new HashSet<>();
        //     buildSubordinateTree(parentNode, supervisorStaffId, 1, tempAddedStaffIds);
        // }
        
        private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
            for (int i = startingIndex; i < rowCount; ++i) {
                tree.expandRow(i);
            }
            
            // 如果树展开后行数增加，继续展开
            if (tree.getRowCount() != rowCount) {
                expandAllNodes(tree, rowCount, tree.getRowCount());
            }
        }
        

    }
    
    /**
     * 人员统计面板
     */
    class StaffStatisticsPanel extends JPanel {
        public StaffStatisticsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // 标题
            JLabel titleLabel = new JLabel("人员统计信息");
            titleLabel.setFont(new Font("SimHei", Font.BOLD, 16));
            add(titleLabel, BorderLayout.NORTH);
            
            // 统计表格面板
            JPanel statsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
            
            // 总人数统计
            JPanel totalCountPanel = new JPanel(new BorderLayout());
            totalCountPanel.setBorder(new TitledBorder("总人数统计"));
            JTable totalCountTable = createTotalCountTable();
            totalCountPanel.add(new JScrollPane(totalCountTable), BorderLayout.CENTER);
            
            // 角色统计
            JPanel roleStatsPanel = new JPanel(new BorderLayout());
            roleStatsPanel.setBorder(new TitledBorder("角色分布统计"));
            JTable roleStatsTable = createRoleStatsTable();
            roleStatsPanel.add(new JScrollPane(roleStatsTable), BorderLayout.CENTER);
            
            // 性别统计
            JPanel genderStatsPanel = new JPanel(new BorderLayout());
            genderStatsPanel.setBorder(new TitledBorder("性别分布统计"));
            JTable genderStatsTable = createGenderStatsTable();
            genderStatsPanel.add(new JScrollPane(genderStatsTable), BorderLayout.CENTER);
            
            statsPanel.add(totalCountPanel);
            statsPanel.add(roleStatsPanel);
            statsPanel.add(genderStatsPanel);
            
            add(statsPanel, BorderLayout.CENTER);
        }
        
        /**
         * 创建总人数统计表格
         */
        private JTable createTotalCountTable() {
            String[] columnNames = {"统计项", "数量"};
            Object[][] data = {{"总员工人数", getTotalStaffCount()
            }};
            return new JTable(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }
        
        /**
         * 创建角色统计表格
         */
        private JTable createRoleStatsTable() {
            String[] columnNames = {"角色", "数量"};
            
            List<Object[]> dataList = new ArrayList<>();
            try {
                Map<Integer, Integer> roleCountMap = StaffService.getInstance().getStaffCountByRole();
                
                // 添加所有角色，即使数量为0
                dataList.add(new Object[]{"行政官", roleCountMap.getOrDefault(1, 0)});
                dataList.add(new Object[]{"中层经理", roleCountMap.getOrDefault(2, 0)});
                dataList.add(new Object[]{"基层员工", roleCountMap.getOrDefault(3, 0)});
            } catch (SQLException e) {
                e.printStackTrace();
                dataList.add(new Object[]{"加载失败", "错误"});
            }
            
            Object[][] data = dataList.toArray(new Object[0][0]);
            return new JTable(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }
        
        /**
         * 创建性别统计表格
         */
        private JTable createGenderStatsTable() {
            String[] columnNames = {"性别", "数量"};
            
            List<Object[]> dataList = new ArrayList<>();
            try {
                Map<String, Integer> genderCountMap = StaffService.getInstance().getStaffCountByGender();
                
                // 添加所有可能的性别
                dataList.add(new Object[]{"男", genderCountMap.getOrDefault("M", 0)});
                dataList.add(new Object[]{"女", genderCountMap.getOrDefault("F", 0)});
            } catch (SQLException e) {
                e.printStackTrace();
                dataList.add(new Object[]{"加载失败", "错误"});
            }
            
            Object[][] data = dataList.toArray(new Object[0][0]);
            return new JTable(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }
        
        /**
         * 获取总员工数量
         */
        private int getTotalStaffCount() {
            try {
                return StaffService.getInstance().getTotalStaffCount();
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }