package com.polyu.cmms.view;
import com.polyu.cmms.service.BuildingService;
import com.polyu.cmms.service.RoomService;
import com.polyu.cmms.service.CompanyService;
import com.polyu.cmms.service.ChemicalService;
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
    private JLabel pageInfoLabel;
    private JButton prevButton, nextButton;
    
    public DataManagementPanel() {
        setLayout(new BorderLayout());
        
        // 左侧导航面板
        JPanel navigationPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        navigationPanel.setBorder(BorderFactory.createTitledBorder("Data"));
        
        // 创建导航按钮
        JButton buildingButton = new JButton("Building");
        JButton roomButton = new JButton("Room");
        JButton companyButton = new JButton("Company");
        JButton chemicalButton = new JButton("Chemical");
        
        // 添加按钮监听器
        buildingButton.addActionListener(new NavigationListener("building"));
        roomButton.addActionListener(new NavigationListener("room"));
        companyButton.addActionListener(new NavigationListener("company"));
        chemicalButton.addActionListener(new NavigationListener("chemical"));
        
        // 添加按钮到导航面板
        navigationPanel.add(buildingButton);
        navigationPanel.add(roomButton);
        navigationPanel.add(companyButton);
        navigationPanel.add(chemicalButton);
        
        // 创建内容面板，使用CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // 添加各个数据实体的管理面板
        contentPanel.add(new BuildingDataPanel(), "building");
        contentPanel.add(new RoomDataPanel(), "room");
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
            String[] columnNames = {"ID", "Building Code", "Build Date", "Address ID", "Floor Number", "Manager ID", "Status"}; 
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            // 创建底部面板，包含操作按钮和分页控件
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Add Building");
            JButton updateButton = new JButton("Update Building");
            JButton deleteButton = new JButton("Delete Building");
            
            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            bottomPanel.add(buttonPanel, BorderLayout.NORTH);
            
            // 创建分页面板
            JPanel paginationPanel = createPaginationPanel();
            bottomPanel.add(paginationPanel, BorderLayout.SOUTH);
            
            // 将底部面板添加到主面板的SOUTH位置
            add(bottomPanel, BorderLayout.SOUTH);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("Previous".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("Next".equals(btn.getText())) {
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
                    this.pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total Records: " + total);
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Fail to load building data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addBuilding() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "Add Building", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Building Code:"));
            JTextField buildingCodeField = new JTextField();
            dialog.add(buildingCodeField);
            
            dialog.add(new JLabel("Construction Date:"));
            JTextField constructionDateField = new JTextField();
            dialog.add(constructionDateField);
            
            dialog.add(new JLabel("Address ID:"));
            JTextField addressIdField = new JTextField();
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("Num Floors:"));
            JTextField numFloorsField = new JTextField();
            dialog.add(numFloorsField);
            
            dialog.add(new JLabel("Supervisor Staff ID:"));
            JTextField supervisorStaffIdField = new JTextField();
            dialog.add(supervisorStaffIdField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Add Building Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Add Building Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Add Building Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please input valid number", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select a building to update", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int buildingId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "Update Building", true);
            dialog.setLayout(new GridLayout(7, 2, 10, 10));
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Building Code:"));
            JTextField buildingCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(buildingCodeField);
            
            dialog.add(new JLabel("Construction Date:"));
            JTextField constructionDateField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
            dialog.add(constructionDateField);
            
            dialog.add(new JLabel("Address ID:"));
            JTextField addressIdField = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("Num Floors:"));
            JTextField numFloorsField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(numFloorsField);
            
            dialog.add(new JLabel("Supervisor Staff ID:"));
            JTextField supervisorStaffIdField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString());
            dialog.add(supervisorStaffIdField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Update Building Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Update Building Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Update Building Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please input valid number", "Error", JOptionPane.ERROR_MESSAGE); 
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
                JOptionPane.showMessageDialog(this, "Please select a building to delete", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int buildingId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "Are you sure to delete this building?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (buildingService.deleteBuilding(buildingId)) {
                        JOptionPane.showMessageDialog(this, "Delete Building Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBuildingData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Delete Building Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Delete Building Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Failed to get pagination info: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            String[] columnNames = {"ID", "Building ID", "Room Name", "Room Type", "Capacity", "Room Features", "Status"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            // 创建底部面板，包含操作按钮和分页控件
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Add Room");
            JButton updateButton = new JButton("Update Room");
            JButton deleteButton = new JButton("Delete Room");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            bottomPanel.add(buttonPanel, BorderLayout.NORTH);
            
            // 创建分页面板
            JPanel paginationPanel = createPaginationPanel();
            bottomPanel.add(paginationPanel, BorderLayout.SOUTH);
            
            // 将底部面板添加到主面板的SOUTH位置
            add(bottomPanel, BorderLayout.SOUTH);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("Previous Page".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("Next Page".equals(btn.getText())) {
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
                    this.pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total Records: " + total);
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load room data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addRoom() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "Add Room", true);
            dialog.setLayout(new GridLayout(6, 2, 10, 10));
            dialog.setSize(400, 250);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Building ID:"));
            JTextField buildingIdField = new JTextField();
            dialog.add(buildingIdField);
            
            dialog.add(new JLabel("Room Name:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("Room Type:"));
            JTextField roomTypeField = new JTextField();
            dialog.add(roomTypeField);
            
            dialog.add(new JLabel("Capacity:"));
            JTextField capacityField = new JTextField();
            dialog.add(capacityField);
            
            dialog.add(new JLabel("Room Features:"));
            JTextField roomFeaturesField = new JTextField();
            dialog.add(roomFeaturesField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Add Room Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Add Room Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Add Room Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please input valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select a room to update", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "Update Room", true);
            dialog.setLayout(new GridLayout(6, 2, 10, 10));
            dialog.setSize(400, 250);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Building ID:"));
            JTextField buildingIdField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
            dialog.add(buildingIdField);
            
            dialog.add(new JLabel("Room Name:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("Room Type:"));
            JTextField roomTypeField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(roomTypeField);
            
            dialog.add(new JLabel("Capacity:"));
            JTextField capacityField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(capacityField);
            
            dialog.add(new JLabel("Room Features:"));
            JTextField roomFeaturesField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(roomFeaturesField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Update Room Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Update Room Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Update Room Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please input valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select a room to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "Are you sure to delete this room?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (roomService.deleteRoom(roomId)) {
                        JOptionPane.showMessageDialog(this, "Delete Room Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadRoomData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Delete Room Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Delete Room Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Get Page Info Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            String[] columnNames = {"ID", "Company Code", "Company Name", "Contact Person", "Quote", "Email", "Phone", "Address ID", "Specialization", "Tax ID", "Bank Account", "Status"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            // 创建底部面板，包含操作按钮和分页控件
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Add Company");
            JButton updateButton = new JButton("Update Company");
            JButton deleteButton = new JButton("Delete Company");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            bottomPanel.add(buttonPanel, BorderLayout.NORTH);
            
            // 创建分页面板
            JPanel paginationPanel = createPaginationPanel();
            bottomPanel.add(paginationPanel, BorderLayout.SOUTH);
            
            // 将底部面板添加到主面板的SOUTH位置
            add(bottomPanel, BorderLayout.SOUTH);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("Previous Page".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("Next Page".equals(btn.getText())) {
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
                    this.pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total Records: " + total);
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Load Company Data Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addCompany() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "Add Outsourced Company", true);
            dialog.setLayout(new GridLayout(12, 2, 10, 10));
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Company Code:"));    
            JTextField contractorCodeField = new JTextField();
            dialog.add(contractorCodeField);
            
            dialog.add(new JLabel("Company Name:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("Contact Name:"));
            JTextField contactNameField = new JTextField();
            dialog.add(contactNameField);
            
            dialog.add(new JLabel("Contract Quote:"));
            JTextField contractQuoteField = new JTextField();
            dialog.add(contractQuoteField);
            
            dialog.add(new JLabel("Email:"));
            JTextField emailField = new JTextField();
            dialog.add(emailField);
            
            dialog.add(new JLabel("Phone:"));
            JTextField phoneField = new JTextField();
            dialog.add(phoneField);
            
            dialog.add(new JLabel("Address ID:"));
            JTextField addressIdField = new JTextField();
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("Expertise:"));
            JTextField expertiseField = new JTextField();
            dialog.add(expertiseField);
            
            dialog.add(new JLabel("Tax ID:"));
            JTextField taxIdField = new JTextField();
            dialog.add(taxIdField);
            
            dialog.add(new JLabel("Bank Account:"));
            JTextField bankAccountField = new JTextField();
            dialog.add(bankAccountField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Add Success", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Add Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Add Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please input valid number", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select the company to update", "Error", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int contractorId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "Update Company", true);
            dialog.setLayout(new GridLayout(12, 2, 10, 10));
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Contractor Code:"));
            JTextField contractorCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(contractorCodeField);
            
            dialog.add(new JLabel("Company Name:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("Contact Name:"));
            JTextField contactNameField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(contactNameField);
            
            dialog.add(new JLabel("Contract Quote:"));
            JTextField contractQuoteField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
            dialog.add(contractQuoteField);
            
            dialog.add(new JLabel("Email:"));
            JTextField emailField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(emailField);
            
            dialog.add(new JLabel("Phone:"));
            JTextField phoneField = new JTextField((String) tableModel.getValueAt(selectedRow, 6));
            dialog.add(phoneField);
            
            dialog.add(new JLabel("Address ID:"));  
            JTextField addressIdField = new JTextField(tableModel.getValueAt(selectedRow, 7).toString());
            dialog.add(addressIdField);
            
            dialog.add(new JLabel("Expertise:"));
            JTextField expertiseField = new JTextField((String) tableModel.getValueAt(selectedRow, 8));
            dialog.add(expertiseField);
            
            dialog.add(new JLabel("Tax ID:"));
            JTextField taxIdField = new JTextField(tableModel.getValueAt(selectedRow, 9) != null ? tableModel.getValueAt(selectedRow, 9).toString() : "");
            dialog.add(taxIdField);
            
            dialog.add(new JLabel("Bank Account:"));
            JTextField bankAccountField = new JTextField(tableModel.getValueAt(selectedRow, 10) != null ? tableModel.getValueAt(selectedRow, 10).toString() : "");
            dialog.add(bankAccountField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Update Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Update Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Update Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select the company to delete", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int contractorId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this company?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (companyService.deleteCompany(contractorId)) {
                        JOptionPane.showMessageDialog(this, "Delete Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadCompanyData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Delete Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Delete Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Get Pagination Info Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            String[] columnNames = {"ID", "Product Code", "Name", "Type", "Manufacturer", "MSDS Link", "Hazard Class", "Storage Requirements", "Status"};
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            // 创建底部面板，包含操作按钮和分页控件
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel();
            JButton addButton = new JButton("Add Chemical");
            JButton updateButton = new JButton("Update Chemical");
            JButton deleteButton = new JButton("Delete Chemical");

            buttonPanel.add(addButton);
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            bottomPanel.add(buttonPanel, BorderLayout.NORTH);
            
            // 创建分页面板
            JPanel paginationPanel = createPaginationPanel();
            bottomPanel.add(paginationPanel, BorderLayout.SOUTH);
            
            // 将底部面板添加到主面板的SOUTH位置
            add(bottomPanel, BorderLayout.SOUTH);
            
            // 获取分页面板中的按钮引用
            for (Component comp : paginationPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    if ("Previous Page".equals(btn.getText())) {
                        prevButton = btn;
                    } else if ("Next Page".equals(btn.getText())) {
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
                    this.pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total Records: " + total);
                }
                if (this.prevButton != null) {
                    this.prevButton.setEnabled(currentPage > 1);
                }
                if (this.nextButton != null) {
                    this.nextButton.setEnabled(currentPage < totalPages);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Load Chemical Data Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        
        private void addChemical() {
            // 创建添加对话框
            JDialog dialog = new JDialog((Frame)null, "Add Chemical", true);
            dialog.setLayout(new GridLayout(8, 2, 10, 10));
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Product Code:"));
            JTextField productCodeField = new JTextField();
            dialog.add(productCodeField);
            
            dialog.add(new JLabel("Name:"));
            JTextField nameField = new JTextField();
            dialog.add(nameField);
            
            dialog.add(new JLabel("Type:"));
            JTextField typeField = new JTextField();
            dialog.add(typeField);
            
            dialog.add(new JLabel("Manufacturer:"));
            JTextField manufacturerField = new JTextField();
            dialog.add(manufacturerField);
            
            dialog.add(new JLabel("MSDS URL:"));
            JTextField msdsUrlField = new JTextField();
            dialog.add(msdsUrlField);
            
            dialog.add(new JLabel("Hazard Category:"));
            JTextField hazardCategoryField = new JTextField();
            dialog.add(hazardCategoryField);
            
            dialog.add(new JLabel("Storage Requirements:"));
            JTextField storageRequirementsField = new JTextField();
            dialog.add(storageRequirementsField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Add Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Add Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Add Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select the chemical to update", "Prompt", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int chemicalId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // 创建更新对话框
            JDialog dialog = new JDialog((Frame)null, "Update Chemical", true);
            dialog.setLayout(new GridLayout(8, 2, 10, 10));
            dialog.setSize(450, 350);
            dialog.setLocationRelativeTo(this);
            
            // 添加表单字段
            dialog.add(new JLabel("Product Code:"));    
            JTextField productCodeField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            dialog.add(productCodeField);
            
            dialog.add(new JLabel("Name:"));
            JTextField nameField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            dialog.add(nameField);
            
            dialog.add(new JLabel("Type:"));
            JTextField typeField = new JTextField((String) tableModel.getValueAt(selectedRow, 3));
            dialog.add(typeField);
            
            dialog.add(new JLabel("Manufacturer:"));    
            JTextField manufacturerField = new JTextField((String) tableModel.getValueAt(selectedRow, 4));
            dialog.add(manufacturerField);
            
            dialog.add(new JLabel("MSDS URL:"));    
            JTextField msdsUrlField = new JTextField((String) tableModel.getValueAt(selectedRow, 5));
            dialog.add(msdsUrlField);
            
            dialog.add(new JLabel("Hazard Category:"));
            JTextField hazardCategoryField = new JTextField((String) tableModel.getValueAt(selectedRow, 6));
            dialog.add(hazardCategoryField);
            
            dialog.add(new JLabel("Storage Requirements:"));    
            JTextField storageRequirementsField = new JTextField((String) tableModel.getValueAt(selectedRow, 7));
            dialog.add(storageRequirementsField);
            
            // 添加按钮
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");
            
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
                        JOptionPane.showMessageDialog(dialog, "Update Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Update Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Update Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Please select the chemical to delete", "Prompt", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int chemicalId = (int) tableModel.getValueAt(selectedRow, 0);
            
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this chemical?", "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    if (chemicalService.deleteChemical(chemicalId)) {
                        JOptionPane.showMessageDialog(this, "Delete Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadChemicalData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Delete Failed", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Delete Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    // 通用搜索面板
    private JPanel createSearchPanel(String entityType) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Search"));
        
        // 根据不同实体类型添加不同的搜索字段
        switch (entityType) {
            case "building":
                panel.add(new JLabel("Building Code:"));
                JTextField buildingCodeField = new JTextField(10);
                panel.add(buildingCodeField);
                break;
            case "room":
                panel.add(new JLabel("Room Name:"));    
                JTextField roomNameField = new JTextField(10);
                panel.add(roomNameField);
                break;
            case "company":
                panel.add(new JLabel("Company Name:"));
                JTextField companyNameField = new JTextField(10);
                panel.add(companyNameField);
                break;
            case "chemical":
                panel.add(new JLabel("Chemical Name:"));
                JTextField chemicalNameField = new JTextField(10);
                panel.add(chemicalNameField);
                break;
        }
        
        JButton searchButton = new JButton("Search");
        JButton resetButton = new JButton("Reset");
        
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
        
        prevButton = new JButton("Previous Page");
        nextButton = new JButton("Next Page");
        pageInfoLabel = new JLabel("Page 1 of 0, Total 0 records");
        
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
    
}