package com.polyu.cmms.view;

// import com.polyu.cmms.service.*;
import com.polyu.cmms.service.BuildingService;
import com.polyu.cmms.service.RoomService;
import com.polyu.cmms.service.LevelService;
import com.polyu.cmms.service.SquareService;
import com.polyu.cmms.service.GateService;
import com.polyu.cmms.service.CanteenService;
import com.polyu.cmms.service.AuthService;
import com.polyu.cmms.util.HtmlLogger;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class LocationManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> locationTypeComboBox;
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> sortFieldComboBox;
    private JComboBox<String> sortOrderComboBox;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton exportButton;
    private JButton viewDetailsButton;
    private JButton deleteButton;
    private JLabel statsLabel;
    
    // 服务层实例
    private BuildingService buildingService;
    private RoomService roomService;
    private LevelService levelService;
    private SquareService squareService;
    private GateService gateService;
    private CanteenService canteenService;
    private AuthService authService;
    
    // 分页相关
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalRecords = 0;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageInfoLabel;
    
    public LocationManagementPanel() {
        setLayout(new BorderLayout());
        
        // 初始化服务层实例
        buildingService = BuildingService.getInstance();
        roomService = RoomService.getInstance();
        levelService = LevelService.getInstance();
        squareService = SquareService.getInstance();
        gateService = GateService.getInstance();
        canteenService = CanteenService.getInstance();
        authService = AuthService.getInstance();
        
        // 创建顶部面板（搜索和筛选）
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // 创建表格
        createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // 创建底部面板（按钮和分页）
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 初始加载数据
        loadData();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 搜索和筛选面板
        JPanel searchAndFilterPanel = new JPanel(new BorderLayout(10, 10));
        
        // 搜索行
        JPanel searchRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by name or code");
        
        JLabel typeLabel = new JLabel("Location Type:");
        locationTypeComboBox = new JComboBox<>(new String[]{
            "All", "Building", "Room", "Level", "Square", "Gate", "Canteen"
        });
        
        JLabel statusLabel = new JLabel("Status:");
        statusComboBox = new JComboBox<>(new String[]{
            "All", "Active", "Inactive"
        });
        
        searchRowPanel.add(searchLabel);
        searchRowPanel.add(searchField);
        searchRowPanel.add(typeLabel);
        searchRowPanel.add(locationTypeComboBox);
        searchRowPanel.add(statusLabel);
        searchRowPanel.add(statusComboBox);
        
        // 排序和按钮行
        JPanel actionRowPanel = new JPanel(new BorderLayout(10, 10));
        
        // 排序部分
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel sortFieldLabel = new JLabel("Sort by:");
        sortFieldComboBox = new JComboBox<>(new String[]{
            "ID", "Name", "Type", "Status"
        });
        
        JLabel sortOrderLabel = new JLabel("Order:");
        sortOrderComboBox = new JComboBox<>(new String[]{
            "Ascending", "Descending"
        });
        
        sortPanel.add(sortFieldLabel);
        sortPanel.add(sortFieldComboBox);
        sortPanel.add(sortOrderLabel);
        sortPanel.add(sortOrderComboBox);
        
        // 按钮部分
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });
        
        // 添加回车键搜索功能
        searchField.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            resetFilters();
        });
        
        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);
        
        actionRowPanel.add(sortPanel, BorderLayout.WEST);
        actionRowPanel.add(buttonPanel, BorderLayout.EAST);
        
        searchAndFilterPanel.add(searchRowPanel, BorderLayout.NORTH);
        searchAndFilterPanel.add(actionRowPanel, BorderLayout.SOUTH);
        
        // 统计信息面板
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsLabel = new JLabel("Total Locations: 0");
        statsPanel.add(statsLabel);
        
        panel.add(searchAndFilterPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void resetFilters() {
        searchField.setText("");
        locationTypeComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);
        sortFieldComboBox.setSelectedIndex(0);
        sortOrderComboBox.setSelectedIndex(0);
        currentPage = 1;
        loadData();
    }
    
    private void createTable() {
        String[] columnNames = {"ID", "Location Type", "Name/Code", "Description", "Address", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        
        // 设置表格属性
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        
        // 添加双击事件查看详情
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewLocationDetails();
                }
            }
        });
    }
    
    private JComboBox<Integer> pageSizeComboBox;
    private JTextField pageJumpField;
    private JButton jumpButton;
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        // 查看详情按钮
        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewLocationDetails());
        
        // 删除按钮
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedLocation());
        deleteButton.setForeground(Color.RED);
        
        // 导出按钮
        exportButton = new JButton("Export Data");
        exportButton.addActionListener(e -> exportTableData());
        
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        
        // 分页面板
        JPanel paginationPanel = createPaginationPanel();
        
        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(paginationPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        pageInfoLabel = new JLabel("Page 1 of 0, Total 0 records");
        
        // 页面大小选择
        JLabel pageSizeLabel = new JLabel("Page Size:");
        pageSizeComboBox = new JComboBox<>(new Integer[]{10, 20, 50, 100});
        pageSizeComboBox.setSelectedItem(20);
        pageSizeComboBox.addActionListener(e -> {
            pageSize = (Integer) pageSizeComboBox.getSelectedItem();
            currentPage = 1;
            loadData();
        });
        
        // 页码跳转
        JLabel jumpLabel = new JLabel("Go to:");
        pageJumpField = new JTextField(3);
        jumpButton = new JButton("Go");
        jumpButton.addActionListener(e -> jumpToPage());
        
        prevButton.addActionListener(e -> goToPreviousPage());
        nextButton.addActionListener(e -> goToNextPage());
        
        panel.add(prevButton);
        panel.add(pageInfoLabel);
        panel.add(nextButton);
        panel.add(pageSizeLabel);
        panel.add(pageSizeComboBox);
        panel.add(jumpLabel);
        panel.add(pageJumpField);
        panel.add(jumpButton);
        
        return panel;
    }
    
    private void jumpToPage() {
        try {
            int targetPage = Integer.parseInt(pageJumpField.getText().trim());
            int totalPages = (totalRecords + pageSize - 1) / pageSize;
            
            if (targetPage >= 1 && targetPage <= totalPages) {
                currentPage = targetPage;
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a valid page number (1 - " + totalPages + ")",
                        "Invalid Page", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void loadData() {
        String locationType = (String) locationTypeComboBox.getSelectedItem();
        List<Map<String, Object>> allLocations = new ArrayList<>();
        
        try {
            // 根据选择的地点类型加载数据
            if ("All".equals(locationType)) {
                // 加载所有类型的地点
                allLocations.addAll(getBuildings());
                allLocations.addAll(getRooms());
                allLocations.addAll(getLevels());
                allLocations.addAll(getSquares());
                allLocations.addAll(getGates());
                allLocations.addAll(getCanteens());
            } else if ("Building".equals(locationType)) {
                allLocations.addAll(getBuildings());
            } else if ("Room".equals(locationType)) {
                allLocations.addAll(getRooms());
            } else if ("Level".equals(locationType)) {
                allLocations.addAll(getLevels());
            } else if ("Square".equals(locationType)) {
                allLocations.addAll(getSquares());
            } else if ("Gate".equals(locationType)) {
                allLocations.addAll(getGates());
            } else if ("Canteen".equals(locationType)) {
                allLocations.addAll(getCanteens());
            }
            
            // 更新统计信息
            totalRecords = allLocations.size();
            statsLabel.setText("Total Locations: " + totalRecords);
            
            // 处理分页
            int totalPages = (totalRecords + pageSize - 1) / pageSize;
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }
            
            // 更新分页信息
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total " + totalRecords + " records");
            
            // 启用/禁用分页按钮
            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < totalPages);
            
            // 清空表格并添加分页后的数据
            tableModel.setRowCount(0);
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalRecords);
            
            for (int i = startIndex; i < endIndex; i++) {
                Map<String, Object> location = allLocations.get(i);
                Object[] rowData = {
                    location.get("id"),
                    location.get("type"),
                    location.get("name"),
                    location.get("description"),
                    location.get("address"),
                    location.get("status")
                };
                tableModel.addRow(rowData);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load location data: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            if (authService != null) {
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                        "Load Location Data", "Failed: " + ex.getMessage());
            }
        }
    }
    
    private List<Map<String, Object>> getBuildings() throws SQLException {
        // 使用queryBuildings方法代替不存在的getAllBuildings方法
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("activeFlag", "Y"); // 只查询活跃的建筑物
        List<Map<String, Object>> buildings = buildingService.queryBuildings(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> building : buildings) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", building.get("buildingId"));
            location.put("type", "Building");
            location.put("name", building.get("buildingCode") + " - " + building.get("buildingName"));
            location.put("description", "Building with " + building.get("floorNumber") + " floors");
            location.put("address", "Address ID: " + building.get("addressId"));
            location.put("status", building.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> getRooms() throws SQLException {
        // 使用queryRooms方法代替不存在的getAllRooms方法
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("activeFlag", "Y"); // 只查询活跃的房间
        List<Map<String, Object>> rooms = roomService.queryRooms(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> room : rooms) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", room.get("roomId"));
            location.put("type", "Room");
            location.put("name", room.get("roomName"));
            location.put("description", "Type: " + room.get("roomType") + ", Capacity: " + room.get("capacity"));
            location.put("address", "Building ID: " + room.get("buildingId"));
            location.put("status", room.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> getLevels() throws SQLException {
        Map<String, Object> conditions = new HashMap<>();
        List<Map<String, Object>> levels = levelService.queryLevels(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> level : levels) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", level.get("levelId"));
            location.put("type", "Level");
            location.put("name", "Level " + level.get("levelNumber"));
            location.put("description", "Floor level in building");
            location.put("address", "Building ID: " + level.get("buildingId"));
            location.put("status", level.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> getSquares() throws SQLException {
        // 使用querySquares方法代替不存在的getAllSquares方法
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("activeFlag", "Y"); // 只查询活跃的广场
        List<Map<String, Object>> squares = squareService.querySquares(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> square : squares) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", square.get("squareId"));
            location.put("type", "Square");
            location.put("name", square.get("name"));
            location.put("description", "Campus square");
            location.put("address", "Address ID: " + square.get("addressId"));
            location.put("status", square.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> getGates() throws SQLException {
        // 使用queryGates方法代替不存在的getAllGates方法
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("activeFlag", "Y"); // 只查询活跃的大门
        List<Map<String, Object>> gates = gateService.queryGates(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> gate : gates) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", gate.get("gateId"));
            location.put("type", "Gate");
            location.put("name", gate.get("name"));
            location.put("description", "Campus entrance gate");
            location.put("address", "Address ID: " + gate.get("addressId"));
            location.put("status", gate.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> getCanteens() throws SQLException {
        // 使用queryCanteens方法代替不存在的getAllCanteens方法
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("activeFlag", "Y"); // 只查询活跃的食堂
        List<Map<String, Object>> canteens = canteenService.queryCanteens(conditions);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> canteen : canteens) {
            Map<String, Object> location = new HashMap<>();
            location.put("id", canteen.get("canteenId"));
            location.put("type", "Canteen");
            location.put("name", canteen.get("name"));
            location.put("description", "Campus canteen");
            location.put("address", "Address ID: " + canteen.get("addressId"));
            location.put("status", canteen.get("activeFlag"));
            result.add(location);
        }
        
        return result;
    }
    
    private void viewLocationDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a location to view details.", 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String id = table.getValueAt(selectedRow, 0).toString();
        String type = table.getValueAt(selectedRow, 1).toString();
        
        try {
            // 根据类型查询详细信息
            Map<String, Object> details = null;
            
            switch (type) {
                case "Building":
                    details = buildingService.getBuildingById(Integer.parseInt(id));
                    break;
                case "Room":
                    details = roomService.getRoomById(Integer.parseInt(id));
                    break;
                case "Level":
                    details = levelService.getLevelById(Integer.parseInt(id));
                    break;
                case "Square":
                    details = squareService.getSquareById(Integer.parseInt(id));
                    break;
                case "Gate":
                    details = gateService.getGateById(Integer.parseInt(id));
                    break;
                case "Canteen":
                    details = canteenService.getCanteenById(Integer.parseInt(id));
                    break;
            }
            
            if (details != null) {
                showLocationDetailsDialog(type, details);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to get location details: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLocationDetailsDialog(String type, Map<String, Object> details) {
        // 创建对话框并设置基本属性
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                type + " Details", true);
        dialog.setMinimumSize(new Dimension(550, 600));
        dialog.setResizable(true);
        
        // 创建主内容面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 设置网格袋约束
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        // 对字段进行分组显示
        // 1. 基本信息部分
        JPanel basicInfoPanel = createGroupPanel("Basic Information");
        JPanel basicGridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints basicGbc = new GridBagConstraints();
        basicGbc.insets = new Insets(6, 8, 6, 8);
        basicGbc.anchor = GridBagConstraints.WEST;
        
        // 2. 详细信息部分
        JPanel detailInfoPanel = createGroupPanel("Additional Details");
        JPanel detailGridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints detailGbc = new GridBagConstraints();
        detailGbc.insets = new Insets(6, 8, 6, 8);
        detailGbc.anchor = GridBagConstraints.WEST;
        
        int basicRow = 0;
        int detailRow = 0;
        
        // 将字段分类添加到不同面板
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 创建标签和值组件
            JLabel label = createDetailLabel(formatFieldName(key));
            JComponent valueComponent = createValueComponent(value, key);
            
            // 基本信息字段：ID, Name, Code, Type等
            if (isBasicField(key)) {
                basicGbc.gridx = 0;
                basicGbc.gridy = basicRow;
                basicGbc.weightx = 0.3;
                basicGbc.fill = GridBagConstraints.NONE;
                basicGbc.anchor = GridBagConstraints.EAST;
                basicGridPanel.add(label, basicGbc);
                
                basicGbc.gridx = 1;
                basicGbc.weightx = 0.7;
                basicGbc.fill = GridBagConstraints.HORIZONTAL;
                basicGbc.anchor = GridBagConstraints.WEST;
                basicGridPanel.add(valueComponent, basicGbc);
                basicRow++;
            } else {
                // 其他详细信息
                detailGbc.gridx = 0;
                detailGbc.gridy = detailRow;
                detailGbc.weightx = 0.3;
                detailGbc.fill = GridBagConstraints.NONE;
                detailGbc.anchor = GridBagConstraints.EAST;
                detailGridPanel.add(label, detailGbc);
                
                detailGbc.gridx = 1;
                detailGbc.weightx = 0.7;
                detailGbc.fill = GridBagConstraints.HORIZONTAL;
                detailGbc.anchor = GridBagConstraints.WEST;
                detailGridPanel.add(valueComponent, detailGbc);
                detailRow++;
            }
        }
        
        // 添加内容到分组面板
        basicInfoPanel.add(basicGridPanel, BorderLayout.CENTER);
        detailInfoPanel.add(detailGridPanel, BorderLayout.CENTER);
        
        // 创建垂直布局的面板来放置分组面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(basicInfoPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(detailInfoPanel);
        contentPanel.add(Box.createVerticalGlue());
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 提高滚动速度
        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        // 组装对话框
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        
        // 使对话框居中显示
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setVisible(true);
    }
    
    // 创建分组面板
    private JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                title, 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Microsoft YaHei", Font.BOLD, 12)
        ));
        return panel;
    }
    
    // 创建详情标签
    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        label.setForeground(new Color(50, 50, 100));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }
    
    // 创建值组件
    private JComponent createValueComponent(Object value, String fieldName) {
        String displayValue = value != null ? value.toString() : "-";
        
        // 对于长文本，使用文本区域
        if (displayValue.length() > 50) {
            JTextArea textArea = new JTextArea(displayValue);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setRows(3);
            textArea.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            textArea.setBackground(new Color(245, 245, 245));
            return new JScrollPane(textArea);
        } else {
            // 对于普通文本，使用标签
            JLabel label = new JLabel(displayValue);
            label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            
            // 对状态字段进行特殊颜色处理
            if ("status".equalsIgnoreCase(fieldName)) {
                if ("Active".equals(displayValue)) {
                    label.setForeground(Color.GREEN.darker());
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if ("Inactive".equals(displayValue)) {
                    label.setForeground(Color.RED);
                }
            }
            
            return label;
        }
    }
    
    // 格式化字段名（将驼峰命名转换为更友好的格式）
    private String formatFieldName(String fieldName) {
        // 替换下划线并添加空格
        String formatted = fieldName.replace('_', ' ');
        
        // 处理驼峰命名
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < formatted.length(); i++) {
            char c = formatted.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && Character.isLowerCase(formatted.charAt(i-1))) {
                result.append(' ');
            }
            // 首字母大写
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    // 判断是否为基本字段
    private boolean isBasicField(String fieldName) {
        String lowerField = fieldName.toLowerCase();
        return lowerField.equals("id") || lowerField.equals("name") || 
               lowerField.equals("code") || lowerField.equals("type") || 
               lowerField.equals("status") || lowerField.contains("name");
    }
    
    private void exportTableData() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to export",
                    "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show save dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export File");
        fileChooser.setSelectedFile(new File("locations_export_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write header row
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.write("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
                
                // Write data rows
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        String cellValue = value != null ? value.toString() : "";
                        writer.write("\"" + cellValue.replace("\"", "\\\"") + "\"");
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.newLine();
                }
                
                JOptionPane.showMessageDialog(this, "Data successfully exported to " + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    private void deleteSelectedLocation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a location to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get location details
        String locationId = table.getValueAt(selectedRow, 0).toString();
        String locationName = table.getValueAt(selectedRow, 2).toString();
        String locationType = table.getValueAt(selectedRow, 1).toString();
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the " + locationType.toLowerCase() + " '" + locationName + "'?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = false;
                
                // Delete based on location type
                switch (locationType) {
                    case "Building":
                        deleted = buildingService.deleteBuilding(Integer.parseInt(locationId));
                        break;
                    case "Room":
                        deleted = roomService.deleteRoom(Integer.parseInt(locationId));
                        break;
                    case "Level":
                        deleted = levelService.deleteLevel(Integer.parseInt(locationId));
                        break;
                    case "Square":
                        deleted = squareService.deleteSquare(Integer.parseInt(locationId));
                        break;
                    case "Gate":
                        deleted = gateService.deleteGate(Integer.parseInt(locationId));
                        break;
                    case "Canteen":
                        deleted = canteenService.deleteCanteen(Integer.parseInt(locationId));
                        break;
                }
                
                if (deleted) {
                    JOptionPane.showMessageDialog(this, locationType + " successfully deleted",
                            "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete " + locationType.toLowerCase() + ".\nIt may be referenced by other records.",
                            "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting " + locationType.toLowerCase() + ": " + e.getMessage(),
                        "Deletion Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }
    
    private void goToNextPage() {
        int totalPages = (totalRecords + pageSize - 1) / pageSize;
        if (currentPage < totalPages) {
            currentPage++;
            loadData();
        }
    }
}