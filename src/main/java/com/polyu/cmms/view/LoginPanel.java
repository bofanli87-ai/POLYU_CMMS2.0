package com.polyu.cmms.view;

import com.polyu.cmms.service.AuthService;
import com.polyu.cmms.util.HtmlLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    protected MainFrame parent;
    
    public LoginPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Campus Maintenance Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
        
        // Login form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton resetButton = new JButton("Reset");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(resetButton);
        
        // Add login listener
        loginButton.addActionListener(new LoginListener());
        resetButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
        });
        
        // Add panels to main panel
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPanel.this, "Username and password cannot be empty", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AuthService authService = AuthService.getInstance();
            try {
                if (authService.login(username, password)) {
                    // Login successful, record log
                    HtmlLogger.logSuccess(authService.getCurrentUserId(), authService.getCurrentRole(), "User Login", "User " + username + " login successfully");
                    
                    // 显示主界面并根据权限调整
                    // 由于系统已取消登录功能，此处代码已不再使用
                } else {
                // Login failed, record log
                HtmlLogger.logError(-1, "Unknown User", "User Login", "User " + username + " login failed: Staff ID does not exist or password is incorrect");
                JOptionPane.showMessageDialog(LoginPanel.this, "Staff ID does not exist or password is incorrect\nNote: Use staff ID as username, password is fixed to 123", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            } catch (Exception ex) {
                // Handle database exception
                HtmlLogger.logError(-1, "Unknown User", "User Login", "User " + username + " login failed: Database error - " + ex.getMessage());
                JOptionPane.showMessageDialog(LoginPanel.this, "Database error: " + ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}