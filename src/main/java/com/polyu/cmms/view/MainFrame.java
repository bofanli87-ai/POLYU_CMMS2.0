package com.polyu.cmms.view;

import com.polyu.cmms.service.AuthService;
import com.polyu.cmms.util.HtmlLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        setTitle("Campus Maintenance Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 自定义关闭操作
        setLocationRelativeTo(null);
        
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // 直接创建并显示主界面
        createMainInterface();
        add(tabbedPane);
    }
    
    // 创建主界面
    private void createMainInterface() {
        tabbedPane = new JTabbedPane();
        
        // 添加各个功能面板（根据权限）
        AuthService authService = AuthService.getInstance();
        
        try {
            // 数据管理面板 - 需要MANAGE_STAFF权限
            if (authService.hasPermission("MANAGE_STAFF")) {
                tabbedPane.addTab("Data Management", new DataManagementPanel());
            }
            
            // 活动管理面板 - 需要MANAGE_ACTIVITY权限
            if (authService.hasPermission("MANAGE_ACTIVITY")) {
                tabbedPane.addTab("Activity Management", new ActivityManagementPanel());
            } else if (authService.hasPermission("VIEW_ACTIVITY")) {
                // 如果只有查看权限，添加受限版本
                tabbedPane.addTab("Activity View", new ActivityViewPanel());
            }
            
            // 人员管理面板 - 需要MANAGE_STAFF权限
            if (authService.hasPermission("MANAGE_STAFF")) {
                tabbedPane.addTab("Staff Management", new StaffManagementPanel());
            } else if (authService.hasPermission("VIEW_STAFF")) {
                // 如果只有查看权限，添加受限版本
                tabbedPane.addTab("Staff View", new StaffViewPanel());
            }
            
            // 查询与报告面板 - 需要VIEW_REPORT权限
            if (authService.hasPermission("VIEW_REPORT")) {
                tabbedPane.addTab("Query & Reports", new QueryReportPanel());
            }
            
            // 报表生成面板 - 需要GENERATE_REPORT权限
            if (authService.hasPermission("GENERATE_REPORT")) {
                tabbedPane.addTab("Report Generation", new ReportGenerationPanel());
            }
        } catch (Exception ex) {
            // 处理数据库异常
            JOptionPane.showMessageDialog(this, "Database error during permission check: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            // 确保userId不为null，避免空指针异常
            Integer userId = authService.getCurrentUserId();
            String role = authService.getCurrentRole();
            HtmlLogger.logError(userId != null ? userId : -1, role != null ? role : "Unknown Role", 
                    "Permission Check", "Permission check failed: " + ex.getMessage());
        }
        
        // 添加用户信息菜单
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User: " + authService.getCurrentRole());
        menuBar.add(userMenu);
        setJMenuBar(menuBar);
    }
    

    
    // 处理退出
    private void handleExit() {
        // 使用自定义按钮文本，将"是"、"否"改为"Yes"、"No"
        Object[] options = {"Yes", "No"};
        int confirm = JOptionPane.showOptionDialog(this, 
                "Are you sure you want to exit the system?", 
                "Exit Confirmation", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);
        
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService authService = AuthService.getInstance();
            // 记录系统退出日志
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "System Exit", "User exits the system");
            System.exit(0);
        }
    }
    
    // 为只有查看权限的用户创建简化的活动查看面板
    private class ActivityViewPanel extends JPanel {
        public ActivityViewPanel() {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Activity Information View (View Permission)", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }
    
    // 为只有查看权限的用户创建简化的人员查看面板
    private class StaffViewPanel extends JPanel {
        public StaffViewPanel() {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Staff Information View (View Permission)", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }
}