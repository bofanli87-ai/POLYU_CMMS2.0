package com.polyu.cmms.view;

import com.polyu.cmms.service.BuildingService;
import com.polyu.cmms.service.RoomService;
import com.polyu.cmms.service.CompanyService;
import com.polyu.cmms.service.ChemicalService;
// import com.polyu.cmms.service.ActivityService;
// import com.polyu.cmms.service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class DataManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    // 分页组件变量 - 定义在类级别以便createPaginationPanel方法可以访问
    private JLabel pageInfoLabel;
    private JButton prevButton, nextButton;
    
    public DataManagementPanel() {
        setLayout(new BorderLayout());
        
        // 左侧导航面板
        JPanel navigationPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("数据实体"));
        
        // 创建导航按钮
        JButton buildingButton = new JButton("建筑物信息");
        JButton roomButton = new JButton("房间信息");
        // JButton activityButton = new JButton("活动信息");
        JButton companyButton = new JButton("外包公司");
        JButton chemicalButton = new JButton("化学物质");
        
        // 添加按钮监听器
        buildingButton.addActionListener(new NavigationListener("building"));
        roomButton.addActionListener(new NavigationListener("room"));
        // activityButton.addActionListener(new NavigationListener("activity"));
        companyButton.addActionListener(new NavigationListener("company"));
        chemicalButton.addActionListener(new NavigationListener("chemical"));
        
        // 添加按钮到导航面板
        navigationPanel.add(buildingButton);
        navigationPanel.add(roomButton);
        // navigationPanel.add(activityButton);
        navigationPanel.add(companyButton);
        navigationPanel.add(chemicalButton);
        
        // 创建内容面板，使用CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // 添加各个数据实体的管理面板
        contentPanel.add(new BuildingDataPanel(), "building");
        contentPanel.add(new RoomDataPanel(), "room");
        // contentPanel.add(new GenericDataPanel("活动"), "activity"); // 保留通用面板，等待活动部分完善
        contentPanel.add(new CompanyDataPanel(), "company");
        contentPanel.add(new ChemicalDataPanel(), "chemical");
        
        // 添加面板到主面板
        add(navigationPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
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
    
    // 建筑物数据管理面板
    private class BuildingDataPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private int currentPage = 1;
        private int pageSize = 10;
        private BuildingService buildingService;
        private JButton prevButton, nextButton;
        private JLabel pageInfoLabel;
        
        public BuildingDataPanel() {
            setLayout(new BorderLayout());
            buildingService = BuildingService.getInstance();
            
            // 创建搜索面板
            JPanel searchPanel = createSearchPanel("building");
            add(searchPanel, BorderLayout.NORTH);
            
            // 创建表格
            String[] columnNames = {"ID", "建筑物代码", "建造日期", "地址ID", "楼层数", "负责人ID", "状态"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("添加建筑物");
            JButton updateButton = new JButton("更新建筑物");
            JButton deleteButton = new JButton("删除建筑物");
            
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // 创建分页面板
            // 创建分页面板并保存自己的按钮引用
            JPanel paginationPanel = createPaginationPanel();
            add(paginationPanel, BorderLayout.AFTER_LAST_LINE);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("上一页".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("下一页".equals(btn.getText())) {
                        nextButton = btn;
                    }
                } else if (comp instanceof JLabel) {
                    pageInfoLabel = (JLabel) comp;
                }
            }
            
            // 添加事件监听器
            addButton.addActionListener(e -> addBuilding());
            updateButton.addActionListener(e -> updateBuilding());
            deleteButton.addActionListener(e -> deleteBuilding());
            if (prevButton != null) {
                prevButton.addActionListener(e -> goToPreviousPage());
            }
            if (nextButton != null) {
                nextButton.addActionListener(e -> goToNextPage());
            }
            
            // 加载数据
            loadBuildingData();
        }
        
        private void loadBuildingData() {
            try {
                Map<String, Object> result = buildingService.getBuildingsByPage(currentPage, pageSize, null, null, null);
                @SuppressWarnings("unchecked")
            List<Map<String, Object>> buildings = (List<Map<String, Object>>) result.get("data");
                int total = getIntValue(result.get("total"), 0);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                
                // 清空表格
                tableModel.setRowCount(0);
                
                // 填充表格
                for (Map<String, Object> building : buildings) {
                    Object[] rowData = {
                        building.get("buildingId"),
                        building.get("buildingCode"),
                        building.get("constructionDate"),
                        building.get("addressId"),
                        building.get("numFloors"),
                        building.get("supervisorStaffId"),
                        building.get("activeFlag")  
                    };
                    tableModel.addRow(rowData);
                }
                
                // 更新分页信息 - 使用自己面板的组件
                if (this.pageInfoLabel != null) {
                    this.pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + total + " 条记录");
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载建筑物数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addBuilding() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "添加建筑物", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("建筑物代码:"));
            JTextField buildingCodeField = new JTextField();
            dialog.add(buildingCodeField);
            
            dialog.add(new JLabel("建造日期:"));
            JTextField constructionDateField = new JTextField();
            dialog.add(constructionDateField);
            
            dialog.add(new JLabel("地址ID:"));
            JTextField addressIdField = new JTextField();
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("楼层数:"));
            JTextField numFloorsField = new JTextField();
            dialog.add(numFloorsField);
            
            dialog.add(new JLabel("负责人ID:"));
            JTextField supervisorStaffIdField = new JTextField();
            dialog.add(supervisorStaffIdField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> buildingData = new HashMap<>();
                    buildingData.put("buildingCode", buildingCodeField.getText());
                    buildingData.put("constructionDate", constructionDateField.getText());
                    if (!addressIdField.getText().isEmpty()) {
                        buildingData.put("addressId", Integer.parseInt(addressIdField.getText()));
                    }
                    if (!numFloorsField.getText().isEmpty()) {
                        buildingData.put("numFloors", Integer.parseInt(numFloorsField.getText()));
                    }
                    if (!supervisorStaffIdField.getText().isEmpty()) {
                        buildingData.put("supervisorStaffId", Integer.parseInt(supervisorStaffIdField.getText()));
                    }
                    
                    if (buildingService.addBuilding(buildingData)) {
                        JOptionPane.showMessageDialog(dialog, "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void updateBuilding() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要更新的建筑物", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int buildingId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "更新建筑物", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("建筑物代码:"));
            JTextField buildingCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(buildingCodeField);
            
            dialog.add(new JLabel("建造日期:"));
            JTextField constructionDateField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
            dialog.add(constructionDateField);
            
            dialog.add(new JLabel("地址ID:"));
            JTextField addressIdField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("楼层数:"));
            JTextField numFloorsField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(numFloorsField);
            
            dialog.add(new JLabel("负责人ID:"));
            JTextField supervisorStaffIdField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString());
            dialog.add(supervisorStaffIdField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("buildingCode", buildingCodeField.getText());
                    updates.put("constructionDate", constructionDateField.getText());
                    if (!addressIdField.getText().isEmpty()) {
                        updates.put("addressId", Integer.parseInt(addressIdField.getText()));
                    }
                    if (!numFloorsField.getText().isEmpty()) {
                        updates.put("numFloors", Integer.parseInt(numFloorsField.getText()));
                    }
                    if (!supervisorStaffIdField.getText().isEmpty()) {
                        updates.put("supervisorStaffId", Integer.parseInt(supervisorStaffIdField.getText()));
                    }
                    
                    if (buildingService.updateBuilding(buildingId, updates)) {
                        JOptionPane.showMessageDialog(dialog, "更新成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "更新失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void deleteBuilding() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的建筑物", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int buildingId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "确定要删除该建筑物吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (buildingService.deleteBuilding(buildingId)) {
                        JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        // 分页导航方法实现
        private void goToPreviousPage() {
            if (currentPage > 1) {
                currentPage--;
                loadBuildingData();
            }
        }
        
        private void goToNextPage() {
            // 重新获取数据以检查是否有更多页面
            try {
                Map<String, Object> result = buildingService.getBuildingsByPage(currentPage, pageSize, null, null, null);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                if (currentPage < totalPages) {
                    currentPage++;
                    loadBuildingData();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "获取分页信息失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    // 房间数据管理面板
    private class RoomDataPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private int currentPage = 1;
        private int pageSize = 10;
        private RoomService roomService;
        private JButton prevButton, nextButton;
        private JLabel pageInfoLabel;
        
        public RoomDataPanel() {
            setLayout(new BorderLayout());
            roomService = RoomService.getInstance();
            
            // 创建搜索面板
            JPanel searchPanel = createSearchPanel("room");
            add(searchPanel, BorderLayout.NORTH);
            
            // 创建表格
            String[] columnNames = {"ID", "建筑物ID", "房间名称", "房间类型", "容量", "房间特性", "状态"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("添加房间");
            JButton updateButton = new JButton("更新房间");
            JButton deleteButton = new JButton("删除房间");
            
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // 创建分页面板
            // 创建分页面板并保存自己的按钮引用
            JPanel paginationPanel = createPaginationPanel();
            add(paginationPanel, BorderLayout.AFTER_LAST_LINE);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("上一页".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("下一页".equals(btn.getText())) {
                        nextButton = btn;
                    }
                } else if (comp instanceof JLabel) {
                    pageInfoLabel = (JLabel) comp;
                }
            }
            
            // 添加事件监听器
            addButton.addActionListener(e -> addRoom());
            updateButton.addActionListener(e -> updateRoom());
            deleteButton.addActionListener(e -> deleteRoom());
            if (prevButton != null) {
                prevButton.addActionListener(e -> goToPreviousPage());
            }
            if (nextButton != null) {
                nextButton.addActionListener(e -> goToNextPage());
            }
            
            // 加载数据
            loadRoomData();
        }
        
        private void loadRoomData() {
            try {
                Map<String, Object> result = roomService.getRoomsByPage(currentPage, pageSize, null, null, null);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) result.get("data");
                int total = getIntValue(result.get("total"), 0);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                
                // 清空表格
                tableModel.setRowCount(0);
                
                // 填充表格
                for (Map<String, Object> room : rooms) {
                    Object[] rowData = {
                        room.get("roomId"),
                        room.get("buildingId"),
                        room.get("name"),
                        room.get("roomType"),
                        room.get("capacity"),
                        room.get("roomFeatures"),
                        room.get("activeFlag")  
                    };
                    tableModel.addRow(rowData);
                }
                
                // 更新分页信息 - 使用自己面板的组件
                if (this.pageInfoLabel != null) {
                    this.pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + total + " 条记录");
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载房间数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addRoom() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "添加房间", true);
            dialog.setLayout(new GridLayout(6, 2, 10, 10));
            dialog.setSize(400, 250);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("建筑物ID:"));
            JTextField buildingIdField = new JTextField();
            dialog.add(buildingIdField);
            
            dialog.add(new JLabel("房间名称:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("房间类型:"));
            JTextField roomTypeField = new JTextField();
            dialog.add(roomTypeField);
            
            dialog.add(new JLabel("容量:"));
            JTextField capacityField = new JTextField();
            dialog.add(capacityField);
            
            dialog.add(new JLabel("房间特性:"));
            JTextField roomFeaturesField = new JTextField();
            dialog.add(roomFeaturesField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> roomData = new HashMap<>();
                    roomData.put("buildingId", Integer.parseInt(buildingIdField.getText()));
                    roomData.put("name", nameField.getText());
                    roomData.put("roomType", roomTypeField.getText());
                    if (!capacityField.getText().isEmpty()) {
                        roomData.put("capacity", Integer.parseInt(capacityField.getText()));
                    }
                    roomData.put("roomFeatures", roomFeaturesField.getText());
                    
                    if (roomService.addRoom(roomData)) {
                        JOptionPane.showMessageDialog(dialog, "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void updateRoom() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要更新的房间", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "更新房间", true);
            dialog.setLayout(new GridLayout(6, 2, 10, 10));
            dialog.setSize(400, 250);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("建筑物ID:"));
            JTextField buildingIdField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
            dialog.add(buildingIdField);
            
            dialog.add(new JLabel("房间名称:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("房间类型:"));
            JTextField roomTypeField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(roomTypeField);
            
            dialog.add(new JLabel("容量:"));
            JTextField capacityField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(capacityField);
            
            dialog.add(new JLabel("房间特性:"));
            JTextField roomFeaturesField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(roomFeaturesField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("buildingId", Integer.parseInt(buildingIdField.getText()));
                    updates.put("name", nameField.getText());
                    updates.put("roomType", roomTypeField.getText());
                    if (!capacityField.getText().isEmpty()) {
                        updates.put("capacity", Integer.parseInt(capacityField.getText()));
                    }
                    updates.put("roomFeatures", roomFeaturesField.getText());
                    
                    if (roomService.updateRoom(roomId, updates)) {
                        JOptionPane.showMessageDialog(dialog, "更新成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "更新失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void deleteRoom() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的房间", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "确定要删除该房间吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (roomService.deleteRoom(roomId)) {
                        JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        // 分页导航方法实现
        private void goToPreviousPage() {
            if (currentPage > 1) {
                currentPage--;
                loadRoomData();
            }
        }
        
        private void goToNextPage() {
            // 重新获取数据以检查是否有更多页面
            try {
                Map<String, Object> result = roomService.getRoomsByPage(currentPage, pageSize, null, null, null);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                if (currentPage < totalPages) {
                    currentPage++;
                    loadRoomData();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "获取分页信息失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    // 公司数据管理面板
    private class CompanyDataPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private int currentPage = 1;
        private int pageSize = 10;
        private CompanyService companyService;
        private JButton prevButton, nextButton;
        private JLabel pageInfoLabel;
        
        public CompanyDataPanel() {
            setLayout(new BorderLayout());
            companyService = CompanyService.getInstance();
            
            // 创建搜索面板
            JPanel searchPanel = createSearchPanel("company");
            add(searchPanel, BorderLayout.NORTH);
            
            // 创建表格
            String[] columnNames = {"ID", "公司代码", "公司名称", "联系人", "报价", "邮箱", "电话", "地址ID", "专业领域", "税号", "银行账户", "状态"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("添加公司");
            JButton updateButton = new JButton("更新公司");
            JButton deleteButton = new JButton("删除公司");
            
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // 创建分页面板
            // 创建分页面板并保存自己的按钮引用
            JPanel paginationPanel = createPaginationPanel();
            add(paginationPanel, BorderLayout.AFTER_LAST_LINE);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("上一页".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("下一页".equals(btn.getText())) {
                        nextButton = btn;
                    }
                } else if (comp instanceof JLabel) {
                    pageInfoLabel = (JLabel) comp;
                }
            }
            
            // 添加事件监听器
            addButton.addActionListener(e -> addCompany());
            updateButton.addActionListener(e -> updateCompany());
            deleteButton.addActionListener(e -> deleteCompany());
            if (prevButton != null) {
                prevButton.addActionListener(e -> goToPreviousPage());
            }
            if (nextButton != null) {
                nextButton.addActionListener(e -> goToNextPage());
            }
            
            // 加载数据
            loadCompanyData();
        }
        
        private void loadCompanyData() {
            try {
                Map<String, Object> result = companyService.getCompaniesByPage(currentPage, pageSize, null, null, null);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> companies = (List<Map<String, Object>>) result.get("data");
                int total = getIntValue(result.get("total"), 0);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                
                // 清空表格
                tableModel.setRowCount(0);
                
                // 填充表格
                for (Map<String, Object> company : companies) {
                    Object[] rowData = {
                        company.get("contractorId"),
                        company.get("contractorCode"),
                        company.get("name"),
                        company.get("contactName"),
                        company.get("contractQuote"),
                        company.get("email"),
                        company.get("phone"),
                        company.get("addressId"),
                        company.get("expertise"),
                        company.get("taxId"),
                        company.get("bankAccount"),
                        company.get("activeFlag")
                    };
                    tableModel.addRow(rowData);
                }
                
                // 更新分页信息 - 使用自己面板的组件
                if (this.pageInfoLabel != null) {
                    this.pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + total + " 条记录");
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载公司数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addCompany() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "添加外包公司", true);
            dialog.setLayout(new GridLayout(12, 2, 10, 10));
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("公司代码:"));
            JTextField contractorCodeField = new JTextField();
            dialog.add(contractorCodeField);
            
            dialog.add(new JLabel("公司名称:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("联系人:"));
            JTextField contactNameField = new JTextField();
            dialog.add(contactNameField);
            
            dialog.add(new JLabel("报价:"));
            JTextField contractQuoteField = new JTextField();
            dialog.add(contractQuoteField);
            
            dialog.add(new JLabel("邮箱:"));
            JTextField emailField = new JTextField();
            dialog.add(emailField);
            
            dialog.add(new JLabel("电话:"));
            JTextField phoneField = new JTextField();
            dialog.add(phoneField);
            
            dialog.add(new JLabel("地址ID:"));
            JTextField addressIdField = new JTextField();
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("专业领域:"));
            JTextField expertiseField = new JTextField();
            dialog.add(expertiseField);
            
            dialog.add(new JLabel("税号:"));
            JTextField taxIdField = new JTextField();
            dialog.add(taxIdField);
            
            dialog.add(new JLabel("银行账户:"));
            JTextField bankAccountField = new JTextField();
            dialog.add(bankAccountField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> companyData = new HashMap<>();
                    companyData.put("contractorCode", contractorCodeField.getText());
                    companyData.put("name", nameField.getText());
                    companyData.put("contactName", contactNameField.getText());
                    if (!contractQuoteField.getText().isEmpty()) {
                        companyData.put("contractQuote", Double.parseDouble(contractQuoteField.getText()));
                    }
                    companyData.put("email", emailField.getText());
                    companyData.put("phone", phoneField.getText());
                    if (!addressIdField.getText().isEmpty()) {
                        companyData.put("addressId", Integer.parseInt(addressIdField.getText()));
                }
                companyData.put("expertise", expertiseField.getText());
                companyData.put("taxId", taxIdField.getText());
                companyData.put("bankAccount", bankAccountField.getText());
                
                if (companyService.addCompany(companyData)) {
                        JOptionPane.showMessageDialog(dialog, "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void updateCompany() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要更新的公司", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int contractorId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "更新外包公司", true);
            dialog.setLayout(new GridLayout(12, 2, 10, 10));
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("公司代码:"));
            JTextField contractorCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(contractorCodeField);
            
            dialog.add(new JLabel("公司名称:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("联系人:"));
            JTextField contactNameField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(contactNameField);
            
            dialog.add(new JLabel("报价:"));
            JTextField contractQuoteField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(contractQuoteField);
            
            dialog.add(new JLabel("邮箱:"));
            JTextField emailField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(emailField);
            
            dialog.add(new JLabel("电话:"));
            JTextField phoneField = new JTextField((String) tableModel.getValueAt(selectedRow, 6));
            dialog.add(phoneField);
            
            dialog.add(new JLabel("地址ID:"));
            JTextField addressIdField = new JTextField(tableModel.getValueAt(selectedRow, 7).toString());
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("专业领域:"));
            JTextField expertiseField = new JTextField((String) tableModel.getValueAt(selectedRow, 8));
            dialog.add(expertiseField);
            
            dialog.add(new JLabel("税号:"));
            JTextField taxIdField = new JTextField(tableModel.getValueAt(selectedRow, 9) != null ? tableModel.getValueAt(selectedRow, 9).toString() : "");
            dialog.add(taxIdField);
            
            dialog.add(new JLabel("银行账户:"));
            JTextField bankAccountField = new JTextField(tableModel.getValueAt(selectedRow, 10) != null ? tableModel.getValueAt(selectedRow, 10).toString() : "");
            dialog.add(bankAccountField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("contractorCode", contractorCodeField.getText());
                    updates.put("name", nameField.getText());
                    updates.put("contactName", contactNameField.getText());
                    if (!contractQuoteField.getText().isEmpty()) {
                        updates.put("contractQuote", Double.parseDouble(contractQuoteField.getText()));
                    }
                    updates.put("email", emailField.getText());
                    updates.put("phone", phoneField.getText());
                    if (!addressIdField.getText().isEmpty()) {
                        updates.put("addressId", Integer.parseInt(addressIdField.getText()));
                }
                updates.put("expertise", expertiseField.getText());
                updates.put("taxId", taxIdField.getText());
                updates.put("bankAccount", bankAccountField.getText());
                
                if (companyService.updateCompany(contractorId, updates)) {
                        JOptionPane.showMessageDialog(dialog, "更新成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "更新失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void deleteCompany() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的公司", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int contractorId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "确定要删除该公司吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (companyService.deleteCompany(contractorId)) {
                        JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        // 分页导航方法实现
        private void goToPreviousPage() {
            if (currentPage > 1) {
                currentPage--;
                loadCompanyData();
            }
        }
        
        private void goToNextPage() {
            // 重新获取数据以检查是否有更多页面
            try {
                Map<String, Object> result = companyService.getCompaniesByPage(currentPage, pageSize, null, null, null);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                if (currentPage < totalPages) {
                    currentPage++;
                    loadCompanyData();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "获取分页信息失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    // 化学物质数据管理面板
    private class ChemicalDataPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private int currentPage = 1;
        private int pageSize = 10;
        private ChemicalService chemicalService;
        private JButton prevButton, nextButton;
        private JLabel pageInfoLabel;
        
        public ChemicalDataPanel() {
            setLayout(new BorderLayout());
            chemicalService = ChemicalService.getInstance();
            
            // 创建搜索面板
            JPanel searchPanel = createSearchPanel("chemical");
            add(searchPanel, BorderLayout.NORTH);
            
            // 创建表格
            String[] columnNames = {"ID", "产品代码", "名称", "类型", "制造商", "MSDS链接", "危险类别", "存储要求", "状态"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("添加化学物质");
            JButton updateButton = new JButton("更新化学物质");
            JButton deleteButton = new JButton("删除化学物质");
            
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            // 创建分页面板
            // 创建分页面板并保存自己的按钮引用
            JPanel paginationPanel = createPaginationPanel();
            add(paginationPanel, BorderLayout.AFTER_LAST_LINE);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("上一页".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("下一页".equals(btn.getText())) {
                        nextButton = btn;
                    }
                } else if (comp instanceof JLabel) {
                    pageInfoLabel = (JLabel) comp;
                }
            }
            
            // 添加事件监听器
            addButton.addActionListener(e -> addChemical());
            updateButton.addActionListener(e -> updateChemical());
            deleteButton.addActionListener(e -> deleteChemical());
            if (prevButton != null) {
                prevButton.addActionListener(e -> goToPreviousPage());
            }
            if (nextButton != null) {
                nextButton.addActionListener(e -> goToNextPage());
            }
            
            // 加载数据
            loadChemicalData();
        }
        
        private void loadChemicalData() {
            try {
                Map<String, Object> result = chemicalService.getChemicalsByPage(currentPage, pageSize, null, null, null);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> chemicals = (List<Map<String, Object>>) result.get("data");
                int total = getIntValue(result.get("total"), 0);
                int totalPages = getIntValue(result.get("totalPages"), 0);
                
                // 清空表格
                tableModel.setRowCount(0);
                
                // 填充表格
                for (Map<String, Object> chemical : chemicals) {
                    Object[] rowData = {
                        chemical.get("chemicalId"),
                        chemical.get("productCode"),
                        chemical.get("name"),
                        chemical.get("type"),
                        chemical.get("manufacturer"),
                        chemical.get("msdsUrl"),
                        chemical.get("hazardCategory"),
                        chemical.get("storageRequirements"),
                        chemical.get("activeFlag")
                    };
                    tableModel.addRow(rowData);
                }
                
                // 更新分页信息 - 使用自己面板的组件
                if (this.pageInfoLabel != null) {
                    this.pageInfoLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页，共 " + total + " 条记录");
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载化学物质数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addChemical() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "添加化学物质", true);
            dialog.setLayout(new GridLayout(8, 2, 10, 10));
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("产品代码:"));
            JTextField productCodeField = new JTextField();
            dialog.add(productCodeField);
            
            dialog.add(new JLabel("名称:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("类型:"));
            JTextField typeField = new JTextField();
            dialog.add(typeField);
            
            dialog.add(new JLabel("制造商:"));
            JTextField manufacturerField = new JTextField();
            dialog.add(manufacturerField);
            
            dialog.add(new JLabel("MSDS链接:"));
            JTextField msdsUrlField = new JTextField();
            dialog.add(msdsUrlField);
            
            dialog.add(new JLabel("危险类别:"));
            JTextField hazardCategoryField = new JTextField();
            dialog.add(hazardCategoryField);
            
            dialog.add(new JLabel("存储要求:"));
            JTextField storageRequirementsField = new JTextField();
            dialog.add(storageRequirementsField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> chemicalData = new HashMap<>();
                    chemicalData.put("productCode", productCodeField.getText());
                    chemicalData.put("name", nameField.getText());
                    chemicalData.put("type", typeField.getText());
                    chemicalData.put("manufacturer", manufacturerField.getText());
                    chemicalData.put("msdsUrl", msdsUrlField.getText());
                    chemicalData.put("hazardCategory", hazardCategoryField.getText());
                    chemicalData.put("storageRequirements", storageRequirementsField.getText());
                    
                    if (chemicalService.addChemical(chemicalData)) {
                        JOptionPane.showMessageDialog(dialog, "添加成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "添加失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void updateChemical() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要更新的化学物质", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int chemicalId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "更新化学物质", true);
            dialog.setLayout(new GridLayout(8, 2, 10, 10));
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("产品代码:"));
            JTextField productCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(productCodeField);
            
            dialog.add(new JLabel("名称:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("类型:"));
            JTextField typeField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(typeField);
            
            dialog.add(new JLabel("制造商:"));
            JTextField manufacturerField = new JTextField((String) tableModel.getValueAt(selectedRow, 4));
            dialog.add(manufacturerField);
            
            dialog.add(new JLabel("MSDS链接:"));
            JTextField msdsUrlField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(msdsUrlField);
            
            dialog.add(new JLabel("危险类别:"));
            JTextField hazardCategoryField = new JTextField((String) tableModel.getValueAt(selectedRow, 6));
            dialog.add(hazardCategoryField);
            
            dialog.add(new JLabel("存储要求:"));
            JTextField storageRequirementsField = new JTextField((String) tableModel.getValueAt(selectedRow, 7));
            dialog.add(storageRequirementsField);
            
            // 添加按钮
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");
            
            saveButton.addActionListener(e -> {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("productCode", productCodeField.getText());
                    updates.put("name", nameField.getText());
                    updates.put("type", typeField.getText());
                    updates.put("manufacturer", manufacturerField.getText());
                    updates.put("msdsUrl", msdsUrlField.getText());
                    updates.put("hazardCategory", hazardCategoryField.getText());
                    updates.put("storageRequirements", storageRequirementsField.getText());
                    
                    if (chemicalService.updateChemical(chemicalId, updates)) {
                        JOptionPane.showMessageDialog(dialog, "更新成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "更新失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "更新失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            dialog.add(saveButton);
            dialog.add(cancelButton);
            
            dialog.setVisible(true);
        }
        
        private void deleteChemical() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请选择要删除的化学物质", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int chemicalId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "确定要删除该化学物质吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (chemicalService.deleteChemical(chemicalId)) {
                        JOptionPane.showMessageDialog(this, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "删除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // 通用搜索面板
    private JPanel createSearchPanel(String entityType) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("搜索"));
        
        // 根据不同实体类型添加不同的搜索字段
        switch (entityType) {
            case "building":
                panel.add(new JLabel("建筑物代码:"));
                JTextField buildingCodeField = new JTextField(10);
                panel.add(buildingCodeField);
                break;
            case "room":
                panel.add(new JLabel("房间名称:"));
                JTextField roomNameField = new JTextField(10);
                panel.add(roomNameField);
                break;
            case "company":
                panel.add(new JLabel("公司名称:"));
                JTextField companyNameField = new JTextField(10);
                panel.add(companyNameField);
                break;
            case "chemical":
                panel.add(new JLabel("化学物质名称:"));
                JTextField chemicalNameField = new JTextField(10);
                panel.add(chemicalNameField);
                break;
        }
        
        JButton searchButton = new JButton("搜索");
        JButton resetButton = new JButton("重置");
        
        panel.add(searchButton);
        panel.add(resetButton);
        
        return panel;
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
    
    // 通用分页面板
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageInfoLabel = new JLabel("第 1 页，共 0 页，共 0 条记录");
        
        panel.add(prevButton);
        panel.add(pageInfoLabel);
        panel.add(nextButton);
        
        return panel;
    }
    
    // 通用分页导航方法 - 这些方法在各个子面板中有具体实现，这里只是占位
    private void goToPreviousPage() {
        // 此方法在各个具体的子面板类中有实际实现
    }
    
    private void goToNextPage() {
        // 此方法在各个具体的子面板类中有实际实现
    }
    
    // 通用数据管理面板类已被移除（未使用）
}