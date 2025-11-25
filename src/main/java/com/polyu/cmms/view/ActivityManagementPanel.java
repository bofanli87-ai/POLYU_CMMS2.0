package com.polyu.cmms.view;

import com.polyu.cmms.model.Activity;

import com.polyu.cmms.service.ActivityService;
import com.polyu.cmms.service.AuthService;
import com.polyu.cmms.service.StaffService;
import com.polyu.cmms.service.WorksForService;
import com.polyu.cmms.util.HtmlLogger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.sql.SQLException;

public class ActivityManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Activity> activityList;  // 存储当前页的数据
    private List<Activity> allActivityList;  // 存储所有数据用于分页
    private AuthService authService;
    private ActivityService activityService;
    
    // 分页相关变量
    private int currentPage = 1;  // 当前页码，从1开始
    private int pageSize = 10;    // 每页显示记录数
    private int totalRecords = 0; // 总记录数
    private int totalPages = 1;   // 总页数
    private JLabel pageInfoLabel; // 显示页码信息的标签
    
    public ActivityManagementPanel() {
        authService = AuthService.getInstance();
        activityService = new ActivityService();
        setLayout(new BorderLayout());
        
        try {
            // 初始化列表
            activityList = new ArrayList<>();
            allActivityList = new ArrayList<>();
            
            // 创建表格模型
            String[] columnNames = {"活动ID", "类型", "标题", "状态", "日期", "预计停机时间", "危害等级"};
            tableModel = new DefaultTableModel(columnNames, 0);
            
            // 创建表格
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            
            // 创建操作按钮面板
            JPanel buttonPanel = new JPanel();
            
            // 根据权限显示不同按钮
            if (authService.hasPermission("MANAGE_ACTIVITY")) {
                JButton createButton = new JButton("创建活动");
                JButton assignButton = new JButton("分配活动");
                JButton updateButton = new JButton("更新状态");
                
                createButton.addActionListener(e -> createActivity());
                assignButton.addActionListener(e -> assignActivity());
                updateButton.addActionListener(e -> updateActivityStatus());
                
                buttonPanel.add(createButton);
                buttonPanel.add(assignButton);
                buttonPanel.add(updateButton);
            }
            
            JButton viewDetailsButton = new JButton("查看详情");
            viewDetailsButton.addActionListener(e -> viewActivityDetails());
            buttonPanel.add(viewDetailsButton);
            
            // 添加分页控件面板
            JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton prevButton = new JButton("上一页");
            JButton nextButton = new JButton("下一页");
            JButton firstButton = new JButton("首页");
            JButton lastButton = new JButton("末页");
            // 先初始化pageInfoLabel，然后再使用它
            pageInfoLabel = new JLabel("第 1 页，共 1 页");
            
            // 添加页码信息标签和分页按钮
            paginationPanel.add(firstButton);
            paginationPanel.add(prevButton);
            paginationPanel.add(pageInfoLabel);
            paginationPanel.add(nextButton);
            paginationPanel.add(lastButton);
            
            // 为分页按钮添加事件监听器
            prevButton.addActionListener(e -> goToPreviousPage());
            nextButton.addActionListener(e -> goToNextPage());
            firstButton.addActionListener(e -> goToFirstPage());
            lastButton.addActionListener(e -> goToLastPage());
            
            // 创建底部面板，包含操作按钮和分页控件
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(buttonPanel, BorderLayout.WEST);
            bottomPanel.add(paginationPanel, BorderLayout.EAST);
            
            // 添加组件到主面板
            add(new JLabel("活动管理", JLabel.CENTER), BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
            
            // 从数据库获取所有活动数据（移到UI初始化之后）
            loadAllActivities();
            
            // 初始化分页控件
            initializePagination();
            
            // 显示第一页数据
            loadPageData(1);
        
        } catch (SQLException ex) {
            // 处理数据库异常
            JOptionPane.showMessageDialog(this, "初始化活动管理面板时发生数据库错误: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                    "活动面板初始化", "初始化失败: " + ex.getMessage());
        }
    }


    
    private void loadAllActivities() throws SQLException {
        // 重命名并修改原loadActivities方法，现在它会加载所有数据到allActivityList
        try {
            // 使用ActivityService查询所有活动
            Map<String, Object> conditions = new HashMap<>();
            List<Map<String, Object>> resultList = activityService.queryActivities(conditions);
            
            // 清空列表
            allActivityList.clear();
            
            // 将Map结果转换为Activity对象
            for (Map<String, Object> map : resultList) {
                Activity activity = new Activity();
                activity.setActivityId((int) map.get("activityId"));
                activity.setActivityType((String) map.get("activityType"));
                activity.setTitle((String) map.get("title"));
                activity.setDescription((String) map.get("description"));
                activity.setStatus((String) map.get("status"));
                activity.setPriority((String) map.get("priority"));
                // 处理日期类型转换（LocalDateTime 到 Date）
                Object activityDatetime = map.get("activityDatetime");
                if (activityDatetime instanceof LocalDateTime) {
                    activity.setDate(Date.from(((LocalDateTime) activityDatetime).atZone(ZoneId.systemDefault()).toInstant()));
                } else if (activityDatetime instanceof Date) {
                    activity.setDate((Date) activityDatetime);
                }
                
                // 尝试从不同可能的字段名获取预计不可用时长
                Object expectedDowntime = map.get("expectedUnavailableDuration");
                // 如果找不到，尝试使用"expectedDowntime"字段名
                if (expectedDowntime == null) {
                    expectedDowntime = map.get("expectedDowntime");
                }
                
                // 处理找到的值
                if (expectedDowntime != null) {
                    if (expectedDowntime instanceof Number) {
                        // 如果是数字类型，直接转换为Integer
                        activity.setExpectedDowntime(((Number) expectedDowntime).intValue());
                    } else {
                        try {
                            // 尝试将其他类型转换为整数
                            activity.setExpectedDowntime(Integer.parseInt(expectedDowntime.toString()));
                        } catch (NumberFormatException e) {
                            // 无法转换为数字时设置为null
                            activity.setExpectedDowntime(null);
                        }
                    }
                }
                
                Object actualCompletion = map.get("actualCompletionDatetime");
                if (actualCompletion instanceof LocalDateTime) {
                    activity.setActualCompletionDatetime(Date.from(((LocalDateTime) actualCompletion).atZone(ZoneId.systemDefault()).toInstant()));
                } else if (actualCompletion instanceof Date) {
                    activity.setActualCompletionDatetime((Date) actualCompletion);
                }
                activity.setCreatedByStaffId((int) map.get("createdByStaffId"));
                activity.setWeatherId((Integer) map.get("weatherId"));
                activity.setBuildingId((Integer) map.get("buildingId"));
                activity.setAreaId((Integer) map.get("areaId"));
                activity.setHazardLevel((String) map.get("hazardLevel"));
                activity.setFacilityType((String) map.get("facilityType"));
                activity.setRoomId((Integer) map.get("roomId"));
                activity.setLevelId((Integer) map.get("levelId"));
                activity.setSquareId((Integer) map.get("squareId"));
                activity.setGateId((Integer) map.get("gateId"));
                activity.setCanteenId((Integer) map.get("canteenId"));
                activity.setActiveFlag((String) map.get("activeFlag"));
                
                allActivityList.add(activity);
            }
            
            // 更新总记录数
            totalRecords = allActivityList.size();
        } catch (SQLException ex) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                    "加载活动数据", "加载失败: " + ex.getMessage());
            throw new SQLException("加载活动数据失败: " + ex.getMessage(), ex);
        }
    }
    
    private void initializePagination() {
        // 计算总页数
        totalPages = totalRecords == 0 ? 1 : (int) Math.ceil((double) totalRecords / pageSize);
        currentPage = 1; // 重置为第一页
        
        // 只有当pageInfoLabel不为null时才更新页码信息
        if (pageInfoLabel != null) {
            updatePageInfo();
        }
    }
    
    private void loadPageData(int pageNumber) {
        // 验证页码
        if (pageNumber < 1 || pageNumber > totalPages) {
            return;
        }
        
        // 更新当前页码
        currentPage = pageNumber;
        
        // 清空当前页数据列表
        activityList.clear();
        
        // 计算当前页的起始和结束索引
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);
        
        // 获取当前页的数据
        for (int i = startIndex; i < endIndex; i++) {
            activityList.add(allActivityList.get(i));
        }
        
        // 填充表格数据
        fillTableData();
        
        // 更新页码信息
        updatePageInfo();
    }
    
    private void updatePageInfo() {
        // 更新页码信息标签
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + totalRecords + " 条记录");
        }
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            loadPageData(currentPage - 1);
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages) {
            loadPageData(currentPage + 1);
        }
    }
    
    private void goToFirstPage() {
        if (currentPage != 1) {
            loadPageData(1);
        }
    }
    
    private void goToLastPage() {
        if (currentPage != totalPages) {
            loadPageData(totalPages);
        }
    }
    
    // 移除未使用的方法
    // private void loadActivities() throws SQLException {
    //     // 此方法已被loadAllActivities替代，保留为兼容现有调用
    //     loadAllActivities();
    // }
    
    // 添加获取当前活动的辅助方法
    private Activity getCurrentSelectedActivity() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < activityList.size()) {
            return activityList.get(selectedRow);
        }
        return null;
    }
    
    private void fillTableData() {
        tableModel.setRowCount(0); // 清空表格
        
        for (Activity activity : activityList) {
            // 处理日期字段，确保正确转换
            Object dateValue = activity.getDate();
            if (dateValue instanceof LocalDateTime) {
                dateValue = Date.from(((LocalDateTime) dateValue).atZone(ZoneId.systemDefault()).toInstant());
            } else if (dateValue == null) {
                dateValue = "";
            }
            
            // 处理预计停机时间字段，确保正确显示
            Object expectedDowntimeValue = activity.getExpectedDowntime();
            if (expectedDowntimeValue instanceof LocalDateTime) {
                expectedDowntimeValue = Date.from(((LocalDateTime) expectedDowntimeValue).atZone(ZoneId.systemDefault()).toInstant());
            } else if (expectedDowntimeValue == null) {
                // 如果为null，显示空字符串而不是null
                expectedDowntimeValue = "";
            }
            
            Object[] row = {
                activity.getActivityId(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getStatus(),
                dateValue,
                expectedDowntimeValue,
                activity.getHazardLevel()
            };
            tableModel.addRow(row);
        }
    }
    
    private void createActivity() {
        // 记录创建活动操作
        HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "创建活动", "用户尝试创建新活动");
        
        try {
            // 简单的活动创建对话框
            String activityType = JOptionPane.showInputDialog(this, "请输入活动类型：", "创建活动", JOptionPane.PLAIN_MESSAGE);
            if (activityType == null || activityType.trim().isEmpty()) return;
            
            String title = JOptionPane.showInputDialog(this, "请输入活动标题：", "创建活动", JOptionPane.PLAIN_MESSAGE);
            if (title == null || title.trim().isEmpty()) return;
            
            String description = JOptionPane.showInputDialog(this, "请输入活动描述：", "创建活动", JOptionPane.PLAIN_MESSAGE);
            
            // 获取预计停机时间（分钟数）
            String downtimeStr = JOptionPane.showInputDialog(this, "请输入预计停机时间（分钟）：", "创建活动", JOptionPane.PLAIN_MESSAGE);
            Integer expectedDowntimeMinutes = 0; // 默认值
            if (downtimeStr != null && !downtimeStr.trim().isEmpty()) {
                try {
                    expectedDowntimeMinutes = Integer.parseInt(downtimeStr.trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "请输入有效的数字作为预计停机时间！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // 创建活动对象
            Activity newActivity = new Activity();
            newActivity.setActivityType(activityType);
            newActivity.setTitle(title);
            newActivity.setDescription(description);
            newActivity.setStatus("planned"); // 默认状态为计划中
            newActivity.setPriority("medium"); // 默认优先级
            newActivity.setDate(new Date()); // 默认当前时间
            newActivity.setExpectedDowntime(expectedDowntimeMinutes); // 使用用户输入的分钟数
            newActivity.setCreatedByStaffId(authService.getCurrentUserId()); // 使用当前用户ID
            newActivity.setHazardLevel("low"); // 默认低风险
            
            // 调用ActivityService添加活动
            boolean success = activityService.addActivity(newActivity);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "活动创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                // 重新加载活动列表
                refreshActivityList();
            } else {
                JOptionPane.showMessageDialog(this, "活动创建失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "创建活动时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "创建活动", "创建失败: " + ex.getMessage());
        }
    }
    
    private void refreshActivityList() {
        // 清空并重新加载活动列表（带分页）
        activityList.clear();
        allActivityList.clear();
        try {
            // 重新加载所有数据
            loadAllActivities();
            // 重新初始化分页
            initializePagination();
            // 加载当前页数据
            loadPageData(currentPage);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "刷新活动列表失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                    "刷新活动列表", "刷新失败: " + ex.getMessage());
        }
    }
    
    private void assignActivity() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // 记录分配活动操作
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "分配活动", "用户尝试分配活动ID=" + activityId);
            
            try {
                // 获取WorksForService和StaffService实例
                WorksForService worksForService = new WorksForService();
                StaffService staffService = StaffService.getInstance();
                
                // 创建一个新的对话框用于批量分配
                JDialog assignmentDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
                        "员工分配 - " + selectedActivity.getTitle(), true);
                assignmentDialog.setSize(700, 500);
                assignmentDialog.setLayout(new BorderLayout());
                
                // 创建表格模型，包含复选框列和职责列
                String[] columnNames = {"选择", "员工姓名", "员工编号", "角色", "状态", "职责描述"};
                DefaultTableModel staffTableModel = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public Class<?> getColumnClass(int column) {
                        return column == 0 ? Boolean.class : Object.class;
                    }
                    
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        // 复选框列和职责描述列可编辑
                        return column == 0 || column == 5;
                    }
                };
                
                // 创建表格
                JTable staffTable = new JTable(staffTableModel);
                staffTable.getColumnModel().getColumn(0).setMaxWidth(50);
                staffTable.getColumnModel().getColumn(5).setPreferredWidth(200); // 增加职责描述列宽度
                JScrollPane scrollPane = new JScrollPane(staffTable);
                
                // 创建职责输入区域和按钮面板
                JPanel bottomPanel = new JPanel(new BorderLayout());
                
                // 职责输入区域
                JPanel responsibilityPanel = new JPanel(new BorderLayout());
                responsibilityPanel.setBorder(BorderFactory.createTitledBorder("批量职责描述"));
                JTextArea responsibilityArea = new JTextArea(2, 40);
                responsibilityPanel.add(new JScrollPane(responsibilityArea), BorderLayout.CENTER);
                
                // 创建按钮面板
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton assignButton = new JButton("分配选中员工");
                JButton unassignButton = new JButton("取消选中员工分配");
                JButton cancelButton = new JButton("关闭");
                buttonPanel.add(assignButton);
                buttonPanel.add(unassignButton);
                buttonPanel.add(cancelButton);
                
                // 组织底部面板
                bottomPanel.add(responsibilityPanel, BorderLayout.NORTH);
                bottomPanel.add(buttonPanel, BorderLayout.CENTER);
                
                // 添加组件到对话框
                assignmentDialog.add(new JLabel("请选择要分配到活动的员工：", JLabel.CENTER), BorderLayout.NORTH);
                assignmentDialog.add(scrollPane, BorderLayout.CENTER);
                assignmentDialog.add(bottomPanel, BorderLayout.SOUTH);
                
                // 获取所有员工和已分配员工信息
                List<Map<String, Object>> allStaff = staffService.queryStaff(new HashMap<>());
                List<Map<String, Object>> assignedStaffDetails = worksForService.queryStaffByActivityId(activityId);
                
                // 添加日志记录，检查是否有员工数据
                System.out.println("分配活动-员工总数: " + (allStaff != null ? allStaff.size() : 0));
                System.out.println("分配活动-已分配员工数: " + (assignedStaffDetails != null ? assignedStaffDetails.size() : 0));
                
                // 创建已分配员工信息Map，存储员工ID到职责的映射
                Map<Integer, String> assignedStaffInfo = new HashMap<>();
                
                // 创建已分配员工ID的Set用于快速查找
                Set<Integer> assignedStaffIds = new HashSet<>();
                if (assignedStaffDetails != null) {
                    for (Map<String, Object> staff : assignedStaffDetails) {
                        try {
                            // 尝试从不同的键名获取staff_id
                            Object staffIdObj = staff.get("staff_id");
                            if (staffIdObj == null) {
                                staffIdObj = staff.get("staffId");
                            }
                            
                            if (staffIdObj instanceof Number) {
                                int staffId = ((Number) staffIdObj).intValue();
                                assignedStaffIds.add(staffId);
                                
                                // 存储员工职责，尝试不同的键名
                                String responsibility = "";
                                Object respObj = staff.get("activity_responsibility");
                                if (respObj == null) {
                                    respObj = staff.get("responsibility");
                                }
                                if (respObj != null) {
                                    responsibility = respObj.toString();
                                }
                                assignedStaffInfo.put(staffId, responsibility);
                            }
                        } catch (Exception e) {
                            // 安全处理，避免单个数据错误影响整体
                            System.err.println("处理已分配员工数据错误: " + e.getMessage());
                            continue;
                        }
                    }
                }
                
                // 填充员工表格
                Map<Integer, Integer> staffIdToRowMap = new HashMap<>(); // 员工ID到表格行的映射
                int addedCount = 0;
                
                if (allStaff != null) {
                    for (Map<String, Object> staff : allStaff) {
                        try {
                            // 尝试从不同的键名获取员工ID
                            Object staffIdObj = staff.get("staff_id");
                            if (staffIdObj == null) {
                                staffIdObj = staff.get("staffId");
                            }
                            
                            Integer staffId = null;
                            if (staffIdObj instanceof Number) {
                                staffId = ((Number) staffIdObj).intValue();
                            } else if (staffIdObj != null) {
                                try {
                                    staffId = Integer.parseInt(staffIdObj.toString());
                                } catch (NumberFormatException ex) {
                                    System.err.println("无效的员工ID格式: " + staffIdObj);
                                }
                            }
                            
                            // 如果获取不到员工ID，仍然尝试添加该员工（使用其他信息）
                            
                            // 尝试从不同的键名获取员工姓名
                            String firstName = "";
                            Object firstNameObj = staff.get("first_name");
                            if (firstNameObj == null) {
                                firstNameObj = staff.get("firstName");
                            }
                            if (firstNameObj != null) {
                                firstName = firstNameObj.toString();
                            }
                            
                            String lastName = "";
                            Object lastNameObj = staff.get("last_name");
                            if (lastNameObj == null) {
                                lastNameObj = staff.get("lastName");
                            }
                            if (lastNameObj != null) {
                                lastName = lastNameObj.toString();
                            }
                            
                            String staffName = firstName + " " + lastName;
                            
                            // 尝试从不同的键名获取员工编号
                            String staffNumber = "";
                            Object staffNumberObj = staff.get("staff_number");
                            if (staffNumberObj == null) {
                                staffNumberObj = staff.get("staffNumber");
                            }
                            if (staffNumberObj != null) {
                                staffNumber = staffNumberObj.toString();
                            }
                            
                            // 尝试从不同的键名获取角色
                            String role = "";
                            Object roleObj = staff.get("role_id");
                            if (roleObj == null) {
                                roleObj = staff.get("roleId");
                            }
                            if (roleObj != null) {
                                role = roleObj.toString();
                            }
                            
                            // 检查员工是否已分配
                            boolean isAssigned = staffId != null && assignedStaffIds.contains(staffId);
                            String status = isAssigned ? "已分配" : "空闲";
                            String responsibility = isAssigned && staffId != null ? assignedStaffInfo.getOrDefault(staffId, "") : "";
                            
                            // 空闲员工默认选中，已分配员工不选中
                            int rowIndex = staffTableModel.getRowCount();
                            staffTableModel.addRow(new Object[]{!isAssigned, staffName, staffNumber, role, status, responsibility});
                            addedCount++;
                            
                            // 保存员工ID和行索引的映射关系（如果有员工ID）
                            if (staffId != null) {
                                staffIdToRowMap.put(staffId, rowIndex);
                            }
                        } catch (Exception e) {
                            // 安全处理，跳过有问题的记录
                            System.err.println("添加员工到表格时出错: " + e.getMessage());
                            continue;
                        }
                    }
                }
                
                // 记录添加到表格的员工数量
                System.out.println("成功添加到表格的员工数量: " + addedCount);
                
                // 分配按钮事件处理
                assignButton.addActionListener(e -> {
                    try {
                        String batchResponsibility = responsibilityArea.getText().trim();
                        if (batchResponsibility.isEmpty()) {
                            JOptionPane.showMessageDialog(assignmentDialog, "请输入职责描述", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        // 收集选中的员工ID列表用于批量分配
                        List<Integer> selectedStaffIds = new ArrayList<>();
                        
                        for (int i = 0; i < staffTableModel.getRowCount(); i++) {
                            Boolean isSelected = (Boolean) staffTableModel.getValueAt(i, 0);
                            String status = (String) staffTableModel.getValueAt(i, 4);
                            
                            // 只处理选中且状态为空闲的员工
                            if (Boolean.TRUE.equals(isSelected) && "空闲".equals(status)) {
                                try {
                                    // 从原始数据中找到对应的员工ID
                                    String staffNumber = (String) staffTableModel.getValueAt(i, 2);
                                    Integer staffId = null;
                                    
                                    for (Map<String, Object> staff : allStaff) {
                                        String currentStaffNumber = staff.get("staff_number") != null ? 
                                                staff.get("staff_number").toString() : "";
                                        
                                        if (staffNumber.equals(currentStaffNumber)) {
                                            Object staffIdObj = staff.get("staff_id");
                                            if (staffIdObj instanceof Number) {
                                                staffId = ((Number) staffIdObj).intValue();
                                            }
                                            break;
                                        }
                                    }
                                    
                                    if (staffId != null && !worksForService.isStaffInActivity(staffId, activityId)) {
                                        selectedStaffIds.add(staffId);
                                    }
                                } catch (Exception ex) {
                                    // 记录错误但继续处理其他员工
                                    HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                            "分配活动", "处理员工时出错: " + ex.getMessage());
                                }
                            }
                        }
                        
                        // 使用批量分配方法
                        if (!selectedStaffIds.isEmpty()) {
                            int assignedCount = worksForService.batchAddStaffToActivity(selectedStaffIds, activityId, batchResponsibility);
                            
                            // 更新表格中的状态
                            for (int i = 0; i < staffTableModel.getRowCount(); i++) {
                                String staffNumber = (String) staffTableModel.getValueAt(i, 2);
                                for (Integer staffId : selectedStaffIds) {
                                    // 找到对应的行并更新状态
                                    for (Map<String, Object> staff : allStaff) {
                                        String currentStaffNumber = staff.get("staff_number") != null ? 
                                                staff.get("staff_number").toString() : "";
                                        Object staffIdObj = staff.get("staff_id");
                                        Integer currentStaffId = staffIdObj instanceof Number ? ((Number) staffIdObj).intValue() : null;
                                        
                                        if (currentStaffId != null && currentStaffId.equals(staffId) && 
                                            staffNumber.equals(currentStaffNumber)) {
                                            staffTableModel.setValueAt("已分配", i, 4);
                                            staffTableModel.setValueAt(batchResponsibility, i, 5);
                                            staffTableModel.setValueAt(false, i, 0);
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            // 记录日志
                            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                    "分配活动", "成功将" + assignedCount + "名员工分配到活动ID=" + activityId);
                            
                            // 显示结果摘要
                            JOptionPane.showMessageDialog(assignmentDialog, 
                                    "分配完成：成功 " + assignedCount + " 人", 
                                    "分配结果", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 刷新主界面的活动列表
                            refreshActivityList();
                        } else {
                            JOptionPane.showMessageDialog(assignmentDialog, "没有选中可分配的员工", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                        
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(assignmentDialog, "数据库错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                "分配活动", "数据库异常: " + ex.getMessage());
                    }
                });
                
                // 取消分配按钮事件处理
                unassignButton.addActionListener(e -> {
                    try {
                        // 显示确认对话框
                        int confirmResult = JOptionPane.showConfirmDialog(assignmentDialog,
                                "确定要取消选中员工的活动分配吗？",
                                "确认取消分配",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        
                        if (confirmResult != JOptionPane.YES_OPTION) {
                            return; // 用户取消操作
                        }
                        
                        // 收集选中的已分配员工
                        int unassignedCount = 0;
                        int failedCount = 0;
                        
                        for (int i = 0; i < staffTableModel.getRowCount(); i++) {
                            Boolean isSelected = (Boolean) staffTableModel.getValueAt(i, 0);
                            String status = (String) staffTableModel.getValueAt(i, 4);
                            
                            // 只处理选中且状态为已分配的员工
                            if (Boolean.TRUE.equals(isSelected) && "已分配".equals(status)) {
                                try {
                                    // 从原始数据中找到对应的员工ID
                                    String staffNumber = (String) staffTableModel.getValueAt(i, 2);
                                    Integer staffId = null;
                                    
                                    for (Map<String, Object> staff : allStaff) {
                                        String currentStaffNumber = staff.get("staff_number") != null ? 
                                                staff.get("staff_number").toString() : "";
                                        
                                        if (staffNumber.equals(currentStaffNumber)) {
                                            Object staffIdObj = staff.get("staff_id");
                                            if (staffIdObj instanceof Number) {
                                                staffId = ((Number) staffIdObj).intValue();
                                            }
                                            break;
                                        }
                                    }
                                    
                                    if (staffId != null) {
                                        // 取消分配员工（软删除）
                                        boolean success = worksForService.removeStaffFromActivityByStaffAndActivity(staffId, activityId);
                                        
                                        if (success) {
                                            unassignedCount++;
                                            // 更新表格中的状态
                                            staffTableModel.setValueAt("空闲", i, 4);
                                            staffTableModel.setValueAt("", i, 5); // 清空职责
                                            staffTableModel.setValueAt(false, i, 0);
                                            
                                            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                                    "分配活动", "成功将员工ID=" + staffId + "从活动ID=" + activityId + "移除");
                                        } else {
                                            failedCount++;
                                            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                                    "分配活动", "移除员工ID=" + staffId + "失败");
                                        }
                                    }
                                } catch (Exception ex) {
                                    failedCount++;
                                    // 记录错误但继续处理其他员工
                                    HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                            "分配活动", "移除员工时出错: " + ex.getMessage());
                                }
                            }
                        }
                        
                        if (unassignedCount > 0) {
                            // 显示结果摘要
                            String message = "取消分配完成：成功 " + unassignedCount + " 人";
                            if (failedCount > 0) {
                                message += "，失败 " + failedCount + " 人";
                            }
                            JOptionPane.showMessageDialog(assignmentDialog, message, "取消分配结果", JOptionPane.INFORMATION_MESSAGE);
                            
                            // 刷新主界面的活动列表
                            refreshActivityList();
                        } else if (failedCount > 0) {
                            JOptionPane.showMessageDialog(assignmentDialog, 
                                    "取消分配失败，请检查所选员工是否已被分配", "操作失败", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(assignmentDialog, "未选择要取消分配的员工", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(assignmentDialog, "取消分配过程中出错: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                                "分配活动", "批量取消分配异常: " + ex.getMessage());
                    }
                });
                
                // 关闭按钮事件处理
                cancelButton.addActionListener(e -> assignmentDialog.dispose());
                
                // 监听对话框关闭事件
                assignmentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                // 居中显示对话框
                assignmentDialog.setLocationRelativeTo(this);
                assignmentDialog.setVisible(true);
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载员工数据时发生数据库错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                        "分配活动", "加载员工数据失败: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "系统错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                        "分配活动", "系统异常: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个活动", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void updateActivityStatus() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // 记录更新活动状态操作
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "更新活动状态", "用户尝试更新活动ID=" + activityId + "的状态");
            
            try {
                // 提供状态选择
                String[] options = {"planned", "进行中", "completed", "取消"};
                String newStatus = (String) JOptionPane.showInputDialog(
                    this,
                    "请选择新状态：",
                    "更新活动状态",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );
                
                if (newStatus != null) {
                    // 调用ActivityService更新状态
                    boolean success = activityService.updateActivityStatus(activityId, newStatus);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(this, "活动状态更新成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        // 重新加载活动列表（带分页）
                        refreshActivityList();
                    } else {
                        JOptionPane.showMessageDialog(this, "活动状态更新失败！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "更新活动状态时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "更新活动状态", "更新失败: " + ex.getMessage());
            } catch (Exception ex) {
                // 捕获其他可能的异常
                JOptionPane.showMessageDialog(this, "更新状态时发生未知错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "更新活动状态", "未知错误: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个活动", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void viewActivityDetails() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // 记录查看活动详情操作
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "查看活动详情", "用户查看活动ID=" + activityId + "的详情");
            
            try {
                // 使用ActivityService查询特定活动
                Map<String, Object> conditions = new HashMap<>();
                conditions.put("activityId", activityId);
                List<Map<String, Object>> results = activityService.queryActivities(conditions);
                
                if (!results.isEmpty()) {
                    Map<String, Object> activityData = results.get(0);
                    // 构建详情文本
                    StringBuilder details = new StringBuilder();
                    details.append("活动详情\n\n");
                    details.append("活动ID: ").append(activityData.get("activityId")).append("\n");
                    details.append("活动类型: ").append(activityData.get("activityType")).append("\n");
                    details.append("标题: ").append(activityData.get("title")).append("\n");
                    details.append("描述: ").append(activityData.get("description")).append("\n");
                    details.append("状态: ").append(activityData.get("status")).append("\n");
                    details.append("优先级: ").append(activityData.get("priority")).append("\n");
                    
                    // 处理日期字段，确保正确转换
                    Object datetimeValue = activityData.get("activityDatetime");
                    String datetimeStr = "";
                    if (datetimeValue instanceof LocalDateTime) {
                        datetimeStr = Date.from(((LocalDateTime) datetimeValue).atZone(ZoneId.systemDefault()).toInstant()).toString();
                    } else if (datetimeValue instanceof Date) {
                        datetimeStr = datetimeValue.toString();
                    } else if (datetimeValue != null) {
                        datetimeStr = datetimeValue.toString();
                    }
                    details.append("活动时间: ").append(datetimeStr).append("\n");
                    
                    // 处理预计不可用时长字段，支持多个可能的字段名
                    Object durationValue = activityData.get("expectedUnavailableDuration");
                    // 如果找不到，尝试使用"expectedDowntime"字段名
                    if (durationValue == null) {
                        durationValue = activityData.get("expectedDowntime");
                    }
                    
                    String durationStr = "";
                    if (durationValue instanceof LocalDateTime) {
                        durationStr = Date.from(((LocalDateTime) durationValue).atZone(ZoneId.systemDefault()).toInstant()).toString();
                    } else if (durationValue instanceof Date) {
                        durationStr = durationValue.toString();
                    } else if (durationValue != null) {
                        durationStr = durationValue.toString();
                    }
                    details.append("预计不可用时长: ").append(durationStr).append("\n");
                    
                    // 处理实际完成时间字段
                    Object completionValue = activityData.get("actualCompletionDatetime");
                    String completionStr = "";
                    if (completionValue instanceof LocalDateTime) {
                        completionStr = Date.from(((LocalDateTime) completionValue).atZone(ZoneId.systemDefault()).toInstant()).toString();
                    } else if (completionValue instanceof Date) {
                        completionStr = completionValue.toString();
                    } else if (completionValue != null) {
                        completionStr = completionValue.toString();
                    }
                    details.append("实际完成时间: ").append(completionStr).append("\n");
                    
                    details.append("风险等级: ").append(activityData.get("hazardLevel") != null ? activityData.get("hazardLevel") : "").append("\n");
                    details.append("设施类型: ").append(activityData.get("facilityType") != null ? activityData.get("facilityType") : "").append("\n");
                    details.append("创建员工ID: ").append(activityData.get("createdByStaffId") != null ? activityData.get("createdByStaffId") : "").append("\n");
                    
                    // 显示详情对话框
                    JOptionPane.showMessageDialog(this, details.toString(), "活动详情", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "未找到活动详情", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "查询活动详情时发生错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "查看活动详情", "查询失败: " + ex.getMessage());
            } catch (Exception ex) {
                // 捕获其他可能的异常，如类型转换错误
                JOptionPane.showMessageDialog(this, "查看详情时发生未知错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "查看活动详情", "未知错误: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "请先选择一个活动", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
}