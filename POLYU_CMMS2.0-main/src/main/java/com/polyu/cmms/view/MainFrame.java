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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Custom close operation
        setLocationRelativeTo(null);

        // Add window close listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // Directly create and display main interface
        createMainInterface();
        add(tabbedPane);
    }

    // Create main interface
    private void createMainInterface() {
        tabbedPane = new JTabbedPane();

        // Add various function panels (based on permissions)
        AuthService authService = AuthService.getInstance();

        try {
            // Data Management Panel - requires MANAGE_STAFF permission
            if (authService.hasPermission("MANAGE_STAFF")) {
                tabbedPane.addTab("Data Management", new DataManagementPanel());
            }

            // Activity Management Panel - requires MANAGE_ACTIVITY permission
            if (authService.hasPermission("MANAGE_ACTIVITY")) {
                tabbedPane.addTab("Activity Management", new ActivityManagementPanel());
            } else if (authService.hasPermission("VIEW_ACTIVITY")) {
                // If only view permission, add restricted version
                tabbedPane.addTab("Activity View", new ActivityViewPanel());
            }

            // Staff Management Panel - requires MANAGE_STAFF permission
            if (authService.hasPermission("MANAGE_STAFF")) {
                tabbedPane.addTab("Staff Management", new StaffManagementPanel());
            } else if (authService.hasPermission("VIEW_STAFF")) {
                // If only view permission, add restricted version
                tabbedPane.addTab("Staff View", new StaffViewPanel());
            }

            // Query & Reports Panel - requires VIEW_REPORT permission
            if (authService.hasPermission("VIEW_REPORT")) {
                tabbedPane.addTab("Query & Reports", new QueryReportPanel());
            }

            // Report Generation Panel - requires GENERATE_REPORT permission
            if (authService.hasPermission("GENERATE_REPORT")) {
                tabbedPane.addTab("Report Generation", new ReportGenerationPanel());
            }
        } catch (Exception ex) {
            // Handle database exception
            JOptionPane.showMessageDialog(this, "Database error during permission check: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            // Ensure userId is not null to avoid null pointer exception
            Integer userId = authService.getCurrentUserId();
            String role = authService.getCurrentRole();
            HtmlLogger.logError(userId != null ? userId : -1, role != null ? role : "Unknown Role",
                    "Permission Check", "Permission check failed: " + ex.getMessage());
        }

        // Add user information menu
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User: " + authService.getCurrentRole());
        menuBar.add(userMenu);
        setJMenuBar(menuBar);
    }

    // Handle exit
    private void handleExit() {
        // Use custom button text, change to "Yes", "No"
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
            // Log system exit
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "System Exit", "User exits the system");
            System.exit(0);
        }
    }

    // Create simplified activity view panel for users with view-only permission
    private class ActivityViewPanel extends JPanel {
        public ActivityViewPanel() {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Activity Information View (View Permission)", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }

    // Create simplified staff view panel for users with view-only permission
    private class StaffViewPanel extends JPanel {
        public StaffViewPanel() {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Staff Information View (View Permission)", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }
}