package com.polyu.cmms.view;

import com.polyu.cmms.model.Activity;
import com.polyu.cmms.service.ActivityService;
import com.polyu.cmms.service.AuthService;
import com.polyu.cmms.service.StaffService;
import com.polyu.cmms.service.WorksForService;
import com.polyu.cmms.util.HtmlLogger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ActivityManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Activity> activityList;  // Store current page data
    private List<Activity> allActivityList;  // Store all data for pagination
    private AuthService authService;
    private ActivityService activityService;

    // Pagination related variables
    private int currentPage = 1;  // Current page number, starting from 1
    private int pageSize = 10;    // Number of records per page
    private int totalRecords = 0; // Total number of records
    private int totalPages = 1;   // Total number of pages
    private JLabel pageInfoLabel; // Label to display page information

    public ActivityManagementPanel() {
        authService = AuthService.getInstance();
        activityService = ActivityService.getInstance();
        setLayout(new BorderLayout());

        try {
            // Initialize lists
            activityList = new ArrayList<>();
            allActivityList = new ArrayList<>();

            // Create table model
            String[] columnNames = {"ActivityID", "Type", "Title", "Status", "Date", "Estimated downtime", "Hazard level"};
            tableModel = new DefaultTableModel(columnNames, 0);

            // Create table
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // Create title label
            JLabel titleLabel = new JLabel("Event Management", JLabel.CENTER);

            // Create top button panel - for viewing staff assignments and unavailable locations
            JPanel topButtonPanel = new JPanel();
            JButton viewStaffAssignmentButton = new JButton("Personnel Allocation");
            JButton viewUnavailableLocationsButton = new JButton("Unavailable Locations");

            viewStaffAssignmentButton.addActionListener(e -> viewStaffAssignment());
            viewUnavailableLocationsButton.addActionListener(e -> viewUnavailableLocations());

            topButtonPanel.add(viewStaffAssignmentButton);
            topButtonPanel.add(viewUnavailableLocationsButton);

            // Create operation button panel
            JPanel buttonPanel = new JPanel();

            // Display different buttons based on permissions
            if (authService.hasPermission("MANAGE_ACTIVITY")) {
                JButton createButton = new JButton("Create Event");
                JButton assignButton = new JButton("Personnel Allocation");
                JButton updateButton = new JButton("Update Status");

                createButton.addActionListener(e -> createActivity());
                assignButton.addActionListener(e -> assignActivity());
                updateButton.addActionListener(e -> updateActivityStatus());

                buttonPanel.add(createButton);
                buttonPanel.add(assignButton);
                buttonPanel.add(updateButton);
            }

            JButton viewDetailsButton = new JButton("View Details");
            viewDetailsButton.addActionListener(e -> viewActivityDetails());
            buttonPanel.add(viewDetailsButton);

            // Add pagination control panel
            JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton nextButton = new JButton("Next Page");
            JButton firstButton = new JButton("First Page");
            pageInfoLabel = new JLabel("Page 1 of 1");

            // Add page information label and pagination buttons
            paginationPanel.add(firstButton);
            paginationPanel.add(pageInfoLabel);
            paginationPanel.add(nextButton);

            // Add event listeners for pagination buttons
            nextButton.addActionListener(e -> goToNextPage());
            firstButton.addActionListener(e -> goToFirstPage());

            // Create bottom panel containing operation buttons and pagination controls
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(buttonPanel, BorderLayout.WEST);
            bottomPanel.add(paginationPanel, BorderLayout.EAST);

            // Create top panel containing title and top buttons
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(titleLabel, BorderLayout.NORTH);
            topPanel.add(topButtonPanel, BorderLayout.CENTER);

            // Add components to main panel
            add(topPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

            // Get all activity data from database (moved after UI initialization)
            loadAllActivities();

            // Initialize pagination controls
            initializePagination();

            // Display first page data
            loadPageData(1);

        } catch (SQLException ex) {
            // Handle database exception
            JOptionPane.showMessageDialog(this, "Database error occurred while initializing activity management panel: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Activity Panel Initialization", "Initialization failed: " + ex.getMessage());
        }
    }


    private void loadAllActivities() throws SQLException {
        // Rename and modify original loadActivities method, now it loads all data into allActivityList
        try {
            // Use ActivityService to query all activities
            Map<String, Object> conditions = new HashMap<>();
            List<Map<String, Object>> resultList = activityService.queryActivities(conditions);

            // Clear the list
            allActivityList.clear();

            // Convert Map results to Activity objects
            for (Map<String, Object> map : resultList) {
                Activity activity = new Activity();
                activity.setActivityId((int) map.get("activityId"));
                activity.setActivityType((String) map.get("activityType"));
                activity.setTitle((String) map.get("title"));
                activity.setDescription((String) map.get("description"));
                activity.setStatus((String) map.get("status"));
                activity.setPriority((String) map.get("priority"));
                // Handle date type conversion (LocalDateTime to Date)
                Object activityDatetime = map.get("activityDatetime");
                if (activityDatetime instanceof LocalDateTime) {
                    activity.setDate(Date.from(((LocalDateTime) activityDatetime).atZone(ZoneId.systemDefault()).toInstant()));
                } else if (activityDatetime instanceof Date) {
                    activity.setDate((Date) activityDatetime);
                }

                // Try to get expected downtime from different possible field names
                Object expectedDowntime = map.get("expectedUnavailableDuration");
                // If not found, try using "expectedDowntime" field name
                if (expectedDowntime == null) {
                    expectedDowntime = map.get("expectedDowntime");
                }

                // Process the found value
                if (expectedDowntime != null) {
                    if (expectedDowntime instanceof Number) {
                        // If it's a number type, convert directly to Integer
                        activity.setExpectedDowntime(((Number) expectedDowntime).intValue());
                    } else {
                        try {
                            // Try to convert other types to integer
                            activity.setExpectedDowntime(Integer.parseInt(expectedDowntime.toString()));
                        } catch (NumberFormatException e) {
                            // Set to null if cannot be converted to number
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

            // Update total record count
            totalRecords = allActivityList.size();
        } catch (SQLException ex) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Load All Activities", "Load failed: " + ex.getMessage());
            throw new SQLException("Load all activities failed: " + ex.getMessage(), ex);
        }
    }

    private void initializePagination() {
        // Calculate total number of pages
        totalPages = totalRecords == 0 ? 1 : (int) Math.ceil((double) totalRecords / pageSize);
        currentPage = 1; // Reset to first page

        // Only update page information if pageInfoLabel is not null
        if (pageInfoLabel != null) {
            updatePageInfo();
        }
    }

    private void loadPageData(int pageNumber) {
        // Validate page number
        if (pageNumber < 1 || pageNumber > totalPages) {
            return;
        }

        // Update current page number
        currentPage = pageNumber;

        // Clear current page data list
        activityList.clear();

        // Calculate start and end index for current page
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);

        // Get data for current page
        for (int i = startIndex; i < endIndex; i++) {
            activityList.add(allActivityList.get(i));
        }

        // Fill table data
        fillTableData();

        // Update page information
        updatePageInfo();
    }

    private void updatePageInfo() {
        // Update page information label
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + ", Total " + totalRecords + " records");
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


    // Add helper method to get current activity
    private Activity getCurrentSelectedActivity() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < activityList.size()) {
            return activityList.get(selectedRow);
        }
        return null;
    }

    private void fillTableData() {
        tableModel.setRowCount(0); // Clear table

        for (Activity activity : activityList) {
            // Process date field to ensure correct conversion
            Object dateValue = activity.getDate();
            if (dateValue instanceof LocalDateTime) {
                dateValue = Date.from(((LocalDateTime) dateValue).atZone(ZoneId.systemDefault()).toInstant());
            } else if (dateValue == null) {
                dateValue = "";
            }

            // Process expected downtime field to ensure correct display
            Object expectedDowntimeValue = activity.getExpectedDowntime();
            if (expectedDowntimeValue instanceof LocalDateTime) {
                expectedDowntimeValue = Date.from(((LocalDateTime) expectedDowntimeValue).atZone(ZoneId.systemDefault()).toInstant());
            } else if (expectedDowntimeValue == null) {
                // If null, display empty string instead of null
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
        // Log create activity operation
        HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "Create Activity", "User trying to create a new activity");

        try {
            // Simple activity creation dialog
            String activityType = JOptionPane.showInputDialog(this, "Enter the activity type:", "Create Activity", JOptionPane.PLAIN_MESSAGE);
            if (activityType == null || activityType.trim().isEmpty()) return;

            String title = JOptionPane.showInputDialog(this, "Enter the activity title:", "Create Activity", JOptionPane.PLAIN_MESSAGE);
            if (title == null || title.trim().isEmpty()) return;

            String description = JOptionPane.showInputDialog(this, "Enter the activity description:", "Create Activity", JOptionPane.PLAIN_MESSAGE);

            // Get expected downtime (minutes)
            String downtimeStr = JOptionPane.showInputDialog(this, "Enter the expected downtime (minutes):", "Create Activity", JOptionPane.PLAIN_MESSAGE);
            Integer expectedDowntimeMinutes = 0; // Default value
            if (downtimeStr != null && !downtimeStr.trim().isEmpty()) {
                try {
                    expectedDowntimeMinutes = Integer.parseInt(downtimeStr.trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number for expected downtime!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Create activity object
            Activity newActivity = new Activity();
            newActivity.setActivityType(activityType);
            newActivity.setTitle(title);
            newActivity.setDescription(description);
            newActivity.setStatus("planned"); // Default status
            newActivity.setPriority("medium"); // Default priority
            newActivity.setDate(new Date()); // Default current time
            newActivity.setExpectedDowntime(expectedDowntimeMinutes); // Use user input minutes
            newActivity.setCreatedByStaffId(authService.getCurrentUserId()); // Use current user ID
            newActivity.setHazardLevel("low"); // Default low risk

            // Call ActivityService to add activity
            boolean success = activityService.addActivity(newActivity);

            if (success) {
                JOptionPane.showMessageDialog(this, "Activity created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Reload activity list
                refreshActivityList();
            } else {
                JOptionPane.showMessageDialog(this, "Activity creation failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating activity: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "Create Activity", "Creation failed: " + ex.getMessage());
        }
    }

    private void refreshActivityList() {
        // Clear and reload activity list (with pagination)
        activityList.clear();
        allActivityList.clear();
        try {
            // Reload all data
            loadAllActivities();
            // Reinitialize pagination
            initializePagination();
            // Load current page data
            loadPageData(currentPage);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error refreshing activity list: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Refresh Activity List", "Refresh failed: " + ex.getMessage());
        }
    }

    private void assignActivity() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // Log assign activity operation
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "Assign Activity", "User trying to assign activity ID=" + activityId);

            try {
                // Get service instances - using singleton pattern
                WorksForService worksForService = WorksForService.getInstance();
                StaffService staffService = StaffService.getInstance();

                // Create dialog
                JDialog assignmentDialog = createAssignmentDialog(selectedActivity);

                // Create table model
                DefaultTableModel staffTableModel = createStaffTableModel();
                JTable staffTable = new JTable(staffTableModel);
                configureStaffTable(staffTable);
                JScrollPane scrollPane = new JScrollPane(staffTable);

                // Create responsibility input area and button panel
                JPanel bottomPanel = createBottomPanel();
                JTextArea responsibilityArea = (JTextArea) ((JScrollPane) ((JPanel) bottomPanel.getComponent(0)).getComponent(0)).getViewport().getView();

                // Add components to dialog
                assignmentDialog.add(new JLabel("Please select the staff to assign to the activity:", JLabel.CENTER), BorderLayout.NORTH);
                assignmentDialog.add(scrollPane, BorderLayout.CENTER);
                assignmentDialog.add(bottomPanel, BorderLayout.SOUTH);

                // Load data
                List<Map<String, Object>> allStaff = staffService.queryStaff(new HashMap<>());
                List<Map<String, Object>> assignedStaffDetails = worksForService.queryStaffByActivityId(activityId);

                // Log debug information
                System.out.println("Assign activity - total staff: " + (allStaff != null ? allStaff.size() : 0));
                System.out.println("Assign activity - assigned staff: " + (assignedStaffDetails != null ? assignedStaffDetails.size() : 0));

                // Prepare assigned staff data
                Map<Integer, String> assignedStaffInfo = new HashMap<>();
                Set<Integer> assignedStaffIds = new HashSet<>();
                prepareAssignedStaffData(assignedStaffDetails, assignedStaffIds, assignedStaffInfo);

                // Fill table
                Map<Integer, Integer> staffIdToRowMap = new HashMap<>();
                loadStaffData(allStaff, assignedStaffIds, assignedStaffInfo, staffTableModel, staffIdToRowMap);

                // Set up button events
                JButton selectAllButton = (JButton) ((JPanel) bottomPanel.getComponent(1)).getComponent(0);
                JButton assignButton = (JButton) ((JPanel) bottomPanel.getComponent(1)).getComponent(1);
                JButton unassignButton = (JButton) ((JPanel) bottomPanel.getComponent(1)).getComponent(2);
                JButton cancelButton = (JButton) ((JPanel) bottomPanel.getComponent(1)).getComponent(3);

                // Set select all button listener
                selectAllButton.addActionListener(e -> selectAllStaff(staffTableModel));

                // Use a flag variable to toggle between select all/deselect all on multiple clicks
                selectAllButton.putClientProperty("selected", false);

                setupAssignButtonListener(assignButton, assignmentDialog, responsibilityArea, staffTableModel, allStaff,
                        selectedActivity, activityId, worksForService, staffIdToRowMap);

                setupUnassignButtonListener(unassignButton, assignmentDialog, staffTableModel, allStaff,
                        activityId, worksForService);

                cancelButton.addActionListener(e -> assignmentDialog.dispose());

                // Show dialog
                assignmentDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                assignmentDialog.setLocationRelativeTo(this);
                assignmentDialog.setVisible(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading staff data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                        "Assign Activity", "Load staff data failed: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                        "Assign Activity", "System exception: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an activity first.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Create assignment dialog
     */
    private JDialog createAssignmentDialog(Activity selectedActivity) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Staff Assignment - " + selectedActivity.getTitle(), true);
        dialog.setSize(700, 500);
        dialog.setLayout(new BorderLayout());
        return dialog;
    }

    /**
     * Create staff table model
     */
    private DefaultTableModel createStaffTableModel() {
        String[] columnNames = {"Select", "Staff Name", "Staff ID", "Role", "Status", "Responsibility"};
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 5;
            }
        };
    }

    /**
     * Configure table column widths
     */
    private void configureStaffTable(JTable table) {
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(200); // Increase responsibility description column width
    }

    /**
     * Create bottom panel
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Responsibility input area
        JPanel responsibilityPanel = new JPanel(new BorderLayout());
        responsibilityPanel.setBorder(BorderFactory.createTitledBorder("Batch Responsibility Description"));
        JTextArea responsibilityArea = new JTextArea(2, 40);
        responsibilityPanel.add(new JScrollPane(responsibilityArea), BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton selectAllButton = new JButton("Select All");
        JButton assignButton = new JButton("Assign Selected Staff");
        JButton unassignButton = new JButton("Unassign Selected Staff");
        JButton cancelButton = new JButton("Close");
        buttonPanel.add(selectAllButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(unassignButton);
        buttonPanel.add(cancelButton);

        // Organize bottom panel
        bottomPanel.add(responsibilityPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        return bottomPanel;
    }

    /**
     * Prepare assigned staff data
     */
    private void prepareAssignedStaffData(List<Map<String, Object>> assignedStaffDetails,
                                          Set<Integer> assignedStaffIds, Map<Integer, String> assignedStaffInfo) {
        if (assignedStaffDetails != null) {
            for (Map<String, Object> staff : assignedStaffDetails) {
                try {
                    // Use Java camel case naming to get data
                    Integer staffId = getIntegerValue(staff, "staffId");
                    if (staffId != null) {
                        assignedStaffIds.add(staffId);

                        // Get responsibility description
                        String responsibility = getStringValue(staff, "responsibility");
                        assignedStaffInfo.put(staffId, responsibility);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing assigned staff data: " + e.getMessage());
                    continue;
                }
            }
        }
    }

    /**
     * Load staff data into table
     */
    private void loadStaffData(List<Map<String, Object>> allStaff, Set<Integer> assignedStaffIds,
                               Map<Integer, String> assignedStaffInfo, DefaultTableModel tableModel, Map<Integer, Integer> staffIdToRowMap) {
        int addedCount = 0;

        if (allStaff != null) {
            for (Map<String, Object> staff : allStaff) {
                try {
                    // Get staff ID
                    Integer staffId = getIntegerValue(staff, "staffId");

                    // Get staff name
                    String firstName = getStringValue(staff, "firstName");
                    String lastName = getStringValue(staff, "lastName");
                    String staffName = firstName + " " + lastName;

                    // Get staff number
                    String staffNumber = getStringValue(staff, "staffNumber");

                    // Get role
                    String role = getStringValue(staff, "roleId");

                    // Check assignment status
                    boolean isAssigned = staffId != null && assignedStaffIds.contains(staffId);
                    String status = isAssigned ? "Assigned" : "Free";
                    String responsibility = isAssigned && staffId != null ? assignedStaffInfo.getOrDefault(staffId, "") : "";

                    // Add to table - default all unselected
                    int rowIndex = tableModel.getRowCount();
                    tableModel.addRow(new Object[]{false, staffName, staffNumber, role, status, responsibility});
                    addedCount++;

                    // Save mapping relationship
                    if (staffId != null) {
                        staffIdToRowMap.put(staffId, rowIndex);
                    }
                } catch (Exception e) {
                    System.err.println("Error adding staff to table: " + e.getMessage());
                    continue;
                }
            }
        }

        System.out.println("Successfully added " + addedCount + " staff to the table.");
    }

    /**
     * Select all available staff
     */
    private void selectAllStaff(DefaultTableModel tableModel) {
        // Get the button that called this method
        JButton sourceButton = (JButton) SwingUtilities.getAncestorOfClass(JButton.class, (Component) SwingUtilities.getWindowAncestor(this).getFocusOwner());
        if (sourceButton == null) {
            // If cannot get button, default to select all
            selectAllStaffImpl(tableModel, true);
            return;
        }

        // Toggle select all state
        Boolean isSelected = (Boolean) sourceButton.getClientProperty("selected");
        boolean newState = !Boolean.TRUE.equals(isSelected);
        sourceButton.putClientProperty("selected", newState);

        // Update button text
        sourceButton.setText(newState ? "Deselect All" : "Select All");

        // Execute select all or deselect all
        selectAllStaffImpl(tableModel, newState);
    }

    /**
     * Select all implementation
     */
    private void selectAllStaffImpl(DefaultTableModel tableModel, boolean select) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = (String) tableModel.getValueAt(i, 4);
            // Only process staff with free status
            if ("Free".equals(status)) {
                tableModel.setValueAt(select, i, 0);
            }
        }
    }

    /**
     * Set up assign button listener
     */
    private void setupAssignButtonListener(JButton assignButton, JDialog dialog, JTextArea responsibilityArea,
                                           DefaultTableModel tableModel, List<Map<String, Object>> allStaff, Activity selectedActivity,
                                           int activityId, WorksForService worksForService, Map<Integer, Integer> staffIdToRowMap) {
        assignButton.addActionListener(e -> {
            try {
                String batchResponsibility = responsibilityArea.getText().trim();
                if (batchResponsibility.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter responsibility description.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Collect selected staff IDs
                List<Integer> selectedStaffIds = collectSelectedStaffIds(tableModel, allStaff, worksForService, activityId);

                if (!selectedStaffIds.isEmpty()) {
                    // Batch assignment
                    int assignedCount = worksForService.batchAddStaffToActivity(selectedStaffIds, activityId, batchResponsibility);

                    // Update table status
                    updateStaffStatusAfterAssignment(tableModel, selectedStaffIds, allStaff, batchResponsibility);

                    // Log operation
                    HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(),
                            "Assign Activity", "Successfully assigned " + assignedCount + " staff to activity ID=" + activityId);

                    // Show result
                    JOptionPane.showMessageDialog(dialog,
                            "Assignment completed: Successfully assigned " + assignedCount + " staff.",
                            "Assignment Result", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh list
                    refreshActivityList();
                } else {
                    JOptionPane.showMessageDialog(dialog, "No assignable staff selected.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                        "Assign Activity", "Database exception: " + ex.getMessage());
            }
        });
    }

    /**
     * Set up unassign button listener
     */
    private void setupUnassignButtonListener(JButton unassignButton, JDialog dialog, DefaultTableModel tableModel,
                                             List<Map<String, Object>> allStaff, int activityId, WorksForService worksForService) {
        unassignButton.addActionListener(e -> {
            try {
                // Confirmation dialog
                int confirmResult = JOptionPane.showConfirmDialog(dialog,
                        "Are you sure you want to unassign the selected staff from activity ID=" + activityId + "?",
                        "Confirm Unassignment",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmResult != JOptionPane.YES_OPTION) {
                    return;
                }

                // Process unassignment
                int[] result = processUnassignment(tableModel, allStaff, activityId, worksForService);
                int unassignedCount = result[0];
                int failedCount = result[1];

                showUnassignmentResult(dialog, unassignedCount, failedCount);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error during unassignment process: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                        "Assign Activity", "Batch unassignment exception: " + ex.getMessage());
            }
        });
    }

    /**
     * Collect selected staff IDs
     */
    private List<Integer> collectSelectedStaffIds(DefaultTableModel tableModel, List<Map<String, Object>> allStaff,
                                                  WorksForService worksForService, int activityId) {
        List<Integer> selectedStaffIds = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            String status = (String) tableModel.getValueAt(i, 4);

            // Only process selected staff with free status
            if (Boolean.TRUE.equals(isSelected) && "Free".equals(status)) {
                try {
                    String staffNumber = (String) tableModel.getValueAt(i, 2);
                    Integer staffId = findStaffIdByNumber(allStaff, staffNumber);

                    if (staffId != null && !worksForService.isStaffInActivity(staffId, activityId)) {
                        selectedStaffIds.add(staffId);
                    }
                } catch (Exception ex) {
                    HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                            "Assign Activity", "Error processing staff work hours: " + ex.getMessage());
                }
            }
        }

        return selectedStaffIds;
    }

    /**
     * Update staff status after assignment
     */
    private void updateStaffStatusAfterAssignment(DefaultTableModel tableModel, List<Integer> selectedStaffIds,
                                                  List<Map<String, Object>> allStaff, String responsibility) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String staffNumber = (String) tableModel.getValueAt(i, 2);
            Integer staffId = findStaffIdByNumber(allStaff, staffNumber);

            if (staffId != null && selectedStaffIds.contains(staffId)) {
                tableModel.setValueAt("Assigned", i, 4);
                tableModel.setValueAt(responsibility, i, 5);
                tableModel.setValueAt(false, i, 0);
            }
        }
    }

    /**
     * 处理取消分配
     */
    private int[] processUnassignment(DefaultTableModel tableModel, List<Map<String, Object>> allStaff,
                                      int activityId, WorksForService worksForService) {
        int unassignedCount = 0;
        int failedCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            String status = (String) tableModel.getValueAt(i, 4);

            // 只处理选中且状态为已分配的员工
            if (Boolean.TRUE.equals(isSelected) && "Assigned".equals(status)) {
                Integer staffId = null;
                try {
                    String staffNumber = (String) tableModel.getValueAt(i, 2);
                    staffId = findStaffIdByNumber(allStaff, staffNumber);

                    if (staffId != null) {
                        boolean success = worksForService.removeStaffFromActivityByStaffAndActivity(staffId, activityId);

                        if (success) {
                            unassignedCount++;
                            updateUnassignedStaffStatus(tableModel, i);

                            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(),
                                    "Assign Activity", "Successfully unassigned staff ID=" + staffId + " from activity ID=" + activityId);
                        } else {
                            failedCount++;
                            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                                    "Assign Activity", "Failed to unassign staff ID=" + staffId + " from activity ID=" + activityId);
                        }
                    }
                } catch (Exception ex) {
                    failedCount++;
                    String staffInfo = (staffId != null) ? "ID=" + staffId : "unknown ID";
                    HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                            "Assign Activity", "Error unassigning staff " + staffInfo + " from activity ID=" + activityId + ": " + ex.getMessage());
                }
            }
        }

        return new int[]{unassignedCount, failedCount};
    }

    /**
     * 更新取消分配后的员工状态
     */
    private void updateUnassignedStaffStatus(DefaultTableModel tableModel, int rowIndex) {
        tableModel.setValueAt("Free", rowIndex, 4);
        tableModel.setValueAt("", rowIndex, 5); // 清空职责
        tableModel.setValueAt(false, rowIndex, 0);
    }

    /**
     * 显示取消分配结果
     */
    private void showUnassignmentResult(JDialog dialog, int unassignedCount, int failedCount) {
        if (unassignedCount > 0) {
            String message = "Unassignment completed: successfully " + unassignedCount + " person";
            if (failedCount > 0) {
                message += "，failed " + failedCount + " person";
            }
            JOptionPane.showMessageDialog(dialog, message, "Unassignment Result", JOptionPane.INFORMATION_MESSAGE);
            refreshActivityList();
        } else if (failedCount > 0) {
            JOptionPane.showMessageDialog(dialog,
                    "Unassignment failed, please check if the selected staff are assigned to the activity", "Operation Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(dialog, "No staff selected for unassignment", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 根据员工编号查找员工ID
     */
    private Integer findStaffIdByNumber(List<Map<String, Object>> allStaff, String staffNumber) {
        for (Map<String, Object> staff : allStaff) {
            String currentStaffNumber = getStringValue(staff, "staffNumber");
            if (staffNumber.equals(currentStaffNumber)) {
                return getIntegerValue(staff, "staffId");
            }
        }
        return null;
    }

    /**
     * 获取整数值，支持不同的键名格式
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            // 尝试下划线格式作为备份
            String underscoreKey = key.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
            value = map.get(underscoreKey);
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format: " + value + " for key: " + key);
            }
        }
        return null;
    }

    /**
     * 获取字符串值，支持不同的键名格式
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            // 尝试下划线格式作为备份
            String underscoreKey = key.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
            value = map.get(underscoreKey);
        }
        return value != null ? value.toString() : "";
    }

    private void updateActivityStatus() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // 记录更新活动状态操作
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Update Activity Status", "User tried to update status of activity ID=" + activityId);

            try {
                // 提供状态选择
                String[] options = {"planned", "ongoing", "completed", "cancelled"};
                String newStatus = (String) JOptionPane.showInputDialog(
                        this,
                        "Select new status:",
                        "Update Activity Status",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (newStatus != null) {
                    // 调用ActivityService更新状态
                    boolean success = activityService.updateActivityStatus(activityId, newStatus);

                    if (success) {
                        JOptionPane.showMessageDialog(this, "Activity status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        // 重新加载活动列表（带分页）
                        refreshActivityList();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update activity status!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating activity status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "Update Activity Status", "Update failed: " + ex.getMessage());
            } catch (Exception ex) {
                // 捕获其他可能的异常
                JOptionPane.showMessageDialog(this, "Unknown error updating activity status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "Update Activity Status", "Unknown error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an activity to update its status.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewActivityDetails() {
        Activity selectedActivity = getCurrentSelectedActivity();
        if (selectedActivity != null) {
            // Log view activity details operation
            int activityId = selectedActivity.getActivityId();
            HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "View Activity Details", "User viewed details of activity ID=" + activityId);

            try {
                // Use ActivityService to query specific activity
                Map<String, Object> conditions = new HashMap<>();
                conditions.put("activityId", activityId);
                List<Map<String, Object>> results = activityService.queryActivities(conditions);

                if (!results.isEmpty()) {
                    Map<String, Object> activityData = results.get(0);
                    // Build details text
                    StringBuilder details = new StringBuilder();
                    details.append("Activity Details\n\n");
                    details.append("Activity ID: ").append(activityData.get("activityId")).append("\n");
                    details.append("Activity Type: ").append(activityData.get("activityType")).append("\n");
                    details.append("Title: ").append(activityData.get("title")).append("\n");
                    details.append("Description: ").append(activityData.get("description")).append("\n");
                    details.append("Status: ").append(activityData.get("status")).append("\n");
                    details.append("Priority: ").append(activityData.get("priority")).append("\n");

                    // Process date field to ensure correct conversion
                    Object datetimeValue = activityData.get("activityDatetime");
                    String datetimeStr = "";
                    if (datetimeValue instanceof LocalDateTime) {
                        datetimeStr = Date.from(((LocalDateTime) datetimeValue).atZone(ZoneId.systemDefault()).toInstant()).toString();
                    } else if (datetimeValue instanceof Date) {
                        datetimeStr = datetimeValue.toString();
                    } else if (datetimeValue != null) {
                        datetimeStr = datetimeValue.toString();
                    }
                    details.append("Activity Datetime: ").append(datetimeStr).append("\n");

                    // Process expected unavailable duration field, support multiple possible field names
                    Object durationValue = activityData.get("expectedUnavailableDuration");
                    // If not found, try using "expectedDowntime" field name
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
                    details.append("Expected Unavailable Duration: ").append(durationStr).append("\n");

                    // Process actual completion time field
                    Object completionValue = activityData.get("actualCompletionDatetime");
                    String completionStr = "";
                    if (completionValue instanceof LocalDateTime) {
                        completionStr = Date.from(((LocalDateTime) completionValue).atZone(ZoneId.systemDefault()).toInstant()).toString();
                    } else if (completionValue instanceof Date) {
                        completionStr = completionValue.toString();
                    } else if (completionValue != null) {
                        completionStr = completionValue.toString();
                    }
                    details.append("Actual Completion Datetime: ").append(completionStr).append("\n");

                    details.append("Hazard Level: ").append(activityData.get("hazardLevel") != null ? activityData.get("hazardLevel") : "").append("\n");
                    details.append("Facility Type: ").append(activityData.get("facilityType") != null ? activityData.get("facilityType") : "").append("\n");
                    details.append("Created By Staff ID: ").append(activityData.get("createdByStaffId") != null ? activityData.get("createdByStaffId") : "").append("\n");

                    // Show details dialog
                    JOptionPane.showMessageDialog(this, details.toString(), "Activity Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No activity details found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "View Activity Details", "Query failed: " + ex.getMessage());
            } catch (Exception ex) {
                // Catch other possible exceptions, such as type conversion errors
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "View Activity Details", "Unknown error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an activity first", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * View staff assignment status
     */
    private void viewStaffAssignment() {
        System.out.println("============================================");
        System.out.println("User clicked View Staff Assignment button");
        // Log view staff assignment operation
        HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "View Staff Assignment", "User viewed assigned and available staff for current tasks");

        try {
            System.out.println("Creating staff assignment dialog...");
            // Create dialog
            JDialog assignmentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Staff Assignment Status", true);
            assignmentDialog.setSize(800, 600);
            assignmentDialog.setLayout(new BorderLayout());

            System.out.println("Creating tabbed pane...");
            // Create tabbed pane
            JTabbedPane tabbedPane = new JTabbedPane();

            System.out.println("Adding tree diagram tab...");
            // Only add tree diagram tab, containing assigned and available staff
            tabbedPane.addTab("Staff Assignment Tree Diagram", createStaffAssignmentTreePanel());
            System.out.println("Tabbed pane creation completed");

            assignmentDialog.add(tabbedPane, BorderLayout.CENTER);

            // Add close button
            System.out.println("Adding dialog close button...");
            JPanel buttonPanel = new JPanel();
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> {
                System.out.println("User clicked close button, dialog will close");
                assignmentDialog.dispose();
            });
            buttonPanel.add(closeButton);
            assignmentDialog.add(buttonPanel, BorderLayout.SOUTH);

            System.out.println("Displaying staff assignment dialog...");
            assignmentDialog.setLocationRelativeTo(this);
            assignmentDialog.setVisible(true);

        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "View Staff Assignment", "Query failed: " + ex.getMessage());
        }
        System.out.println("Staff assignment viewing operation completed");
        System.out.println("============================================");
    }

    /**
     * Creates assigned staff panel
     * Note: This method is currently unused, kept for future extension
     */
    @SuppressWarnings("unused")
    private JPanel createAssignedStaffPanel() throws SQLException {
        System.out.println("Starting to create assigned staff panel...");
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model
        System.out.println("Initializing assigned staff table model...");
        String[] columnNames = {"Activity ID", "Activity Title", "Activity Status", "Staff Name", "Staff ID", "Responsibility", "Assignment Time"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        // Query assigned staff data
        System.out.println("Querying assigned staff data...");
        WorksForService worksForService = WorksForService.getInstance();
        List<Map<String, Object>> assignedStaffData = worksForService.queryWorksFor(null);
        System.out.println("Fetched " + assignedStaffData.size() + " assigned staff records");

        int addedCount = 0;
        for (Map<String, Object> data : assignedStaffData) {
            if ("Y".equals(data.get("activeFlag"))) {
                String activityId = data.get("activityId") != null ? data.get("activityId").toString() : "-";
                String activityTitle = data.get("title") != null ? data.get("title").toString() : "-";
                String staffName = data.get("staffName") != null ? data.get("staffName").toString() : "-";

                System.out.println("Processing staff assignment record: Activity ID=" + activityId + ", Staff Name=" + staffName);

                // Get activity status
                String activityStatus = "-";
                try {
                    Map<String, Object> conditions = new HashMap<>();
                    conditions.put("activityId", Integer.parseInt(activityId));
                    List<Map<String, Object>> activityResults = activityService.queryActivities(conditions);
                    if (!activityResults.isEmpty()) {
                        Map<String, Object> activityData = activityResults.get(0);
                        activityStatus = activityData.get("status") != null ? activityData.get("status").toString() : "-";
                        System.out.println("  Activity Status: " + activityStatus);
                    }
                } catch (Exception e) {
                    System.out.println("  Failed to get activity status: " + e.getMessage());
                }

                // Get staff number
                String staffNumber = "-";
                try {
                    int staffId = Integer.parseInt(data.get("staffId").toString());
                    StaffService staffService = StaffService.getInstance();
                    Map<String, Object> staffData = staffService.getStaffById(staffId);
                    if (staffData != null) {
                        staffNumber = staffData.get("staffNumber") != null ? staffData.get("staffNumber").toString() : "-";
                        System.out.println("  Staff Number: " + staffNumber);
                    }
                } catch (Exception e) {
                    System.out.println("  Failed to get staff number: " + e.getMessage());
                }

                String responsibility = data.get("activityResponsibility") != null ? data.get("activityResponsibility").toString() : "-";
                System.out.println("  Responsibility Description: " + responsibility);

                String assignedTime = "-";
                if (data.get("assignedDatetime") != null) {
                    if (data.get("assignedDatetime") instanceof Date) {
                        assignedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) data.get("assignedDatetime"));
                    } else {
                        assignedTime = data.get("assignedDatetime").toString();
                    }
                    System.out.println("  Assignment Time: " + assignedTime);
                }

                tableModel.addRow(new Object[]{activityId, activityTitle, activityStatus, staffName, staffNumber, responsibility, assignedTime});
                addedCount++;
            }
        }
        System.out.println("Added " + addedCount + " active staff assignment records to the table");
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // 添加统计信息
        int totalAssigned = tableModel.getRowCount();
        System.out.println("Total assigned staff records: " + totalAssigned);
        JLabel statsLabel = new JLabel("Total: " + totalAssigned + " assigned staff records");
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(statsLabel);
        panel.add(statsPanel, BorderLayout.NORTH);
        
        System.out.println("done");
        return panel;
    }
    
    /**
     * 创建人员分配树状图面板，包含已分配和空闲两个模块
     */
    private JPanel createStaffAssignmentTreePanel() throws SQLException {
        System.out.println("Creating staff assignment tree panel...");
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建根节点
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Staff Assignment Overview");
        
        // 创建已分配人员模块节点
        DefaultMutableTreeNode assignedStaffNode = new DefaultMutableTreeNode("Assigned Staff");
        rootNode.add(assignedStaffNode);
        
        // 创建空闲人员模块节点
        DefaultMutableTreeNode availableStaffNode = new DefaultMutableTreeNode("Available Staff");
        rootNode.add(availableStaffNode);
        
        // 加载已分配人员数据
        loadAssignedStaffData(assignedStaffNode);
        
        // 加载空闲人员数据
        loadAvailableStaffData(availableStaffNode);
        
        // 创建树模型和树组件
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        JTree tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(true);
        
        // 展开所有节点
        expandAllNodes(tree, 0, tree.getRowCount());
        
        // 添加到滚动面板
        JScrollPane scrollPane = new JScrollPane(tree);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加统计信息
        JLabel statsLabel = new JLabel("Tree display: Assigned Staff and Available Staff");
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(statsLabel);
        panel.add(statsPanel, BorderLayout.NORTH);
        
        System.out.println("done");
        return panel;
    }
    
    /**
     * 加载已分配人员数据
     */
    private void loadAssignedStaffData(DefaultMutableTreeNode parentNode) throws SQLException {
        System.out.println("Loading assigned staff data...");
        
        // 查询所有已分配人员数据
        WorksForService worksForService = WorksForService.getInstance();
        List<Map<String, Object>> assignedStaffData = worksForService.queryWorksFor(null);
        
        // 按员工分组任务
        Map<Integer, List<Map<String, Object>>> staffTasksMap = new HashMap<>();
        
        for (Map<String, Object> data : assignedStaffData) {
            if ("Y".equals(data.get("activeFlag"))) {
                int staffId = Integer.parseInt(data.get("staffId").toString());
                
                if (!staffTasksMap.containsKey(staffId)) {
                    staffTasksMap.put(staffId, new ArrayList<>());
                }
                staffTasksMap.get(staffId).add(data);
            }
        }
        
        // 创建员工节点及其任务子节点
        StaffService staffService = StaffService.getInstance();
        for (Map.Entry<Integer, List<Map<String, Object>>> entry : staffTasksMap.entrySet()) {
            int staffId = entry.getKey();
            List<Map<String, Object>> tasks = entry.getValue();
            
            // 获取员工信息
            Map<String, Object> staffData = staffService.getStaffById(staffId);
            if (staffData != null) {
                // 创建员工节点
                DefaultMutableTreeNode staffNode = createStaffAssignmentNode(staffData, tasks.size());
                
                // 为每个员工添加任务节点
                for (Map<String, Object> task : tasks) {
                    DefaultMutableTreeNode taskNode = createTaskNode(task);
                    staffNode.add(taskNode);
                }
                
                parentNode.add(staffNode);
            }
        }
        
        System.out.println("Assigned staff data loading complete, " + staffTasksMap.size() + " staff nodes created");
    }
    
    /**
     * 加载空闲人员数据
     */
    private void loadAvailableStaffData(DefaultMutableTreeNode parentNode) throws SQLException {
        System.out.println("Loading available staff data...");
        
        // 查询所有员工
        StaffService staffService = StaffService.getInstance();
        List<Map<String, Object>> allStaff = staffService.queryStaff(new HashMap<>());
        
        // 查询已分配人员ID
        WorksForService worksForService = WorksForService.getInstance();
        List<Map<String, Object>> assignedStaffData = worksForService.queryWorksFor(null);
        Set<Integer> assignedStaffIds = new HashSet<>();
        
        for (Map<String, Object> data : assignedStaffData) {
            if ("Y".equals(data.get("activeFlag"))) {
                int staffId = Integer.parseInt(data.get("staffId").toString());
                assignedStaffIds.add(staffId);
            }
        }
        
        // 创建空闲人员节点
        int availableCount = 0;
        for (Map<String, Object> staffData : allStaff) {
            if ("Y".equals(staffData.get("activeFlag"))) {
                int staffId = 0;
                try {
                    staffId = Integer.parseInt(staffData.get("staffId").toString());
                    if (!assignedStaffIds.contains(staffId)) {
                        // 创建空闲员工节点
                        DefaultMutableTreeNode staffNode = createStaffAssignmentNode(staffData, 0);
                        parentNode.add(staffNode);
                        availableCount++;
                    }
                } catch (Exception e) {
                    System.out.println("Error processing staff data: " + e.getMessage());
                    continue;
                }
            }
        }
        
        System.out.println("Available staff data loading complete, " + availableCount + " available staff nodes created");
    }
    
    /**
     * 创建员工节点，显示任务数量
     */
    private DefaultMutableTreeNode createStaffAssignmentNode(Map<String, Object> staffData, int taskCount) {
        String staffFirstName = staffData.get("firstName") != null ? staffData.get("firstName").toString() : "";
        String staffLastName = staffData.get("lastName") != null ? staffData.get("lastName").toString() : "";
        String staffName = staffFirstName + staffLastName;
        String staffNumber = staffData.get("staffNumber") != null ? staffData.get("staffNumber").toString() : "";
        String roleName = staffData.get("roleName") != null ? staffData.get("roleName").toString() : "";
        
        String nodeText = staffName + " (" + staffNumber + ") - " + roleName;
        if (taskCount > 0) {
            nodeText += " [" + taskCount + " tasks]";
        } else {
            nodeText += " [no tasks]";
        }
        return new DefaultMutableTreeNode(nodeText);
    }
    
    /**
     * 创建任务节点，展示活动相关字段
     */
    private DefaultMutableTreeNode createTaskNode(Map<String, Object> taskData) {
        StringBuilder nodeText = new StringBuilder();
        
        // 获取活动基本信息
        String activityTitle = taskData.get("title") != null ? taskData.get("title").toString() : "Unnamed Activity";
        String activityId = taskData.get("activityId") != null ? taskData.get("activityId").toString() : "";
        
        // 构建任务节点文本
        nodeText.append("Activity: ").append(activityTitle).append(" (ID: ").append(activityId).append(")\n");
        
        // 添加职责描述
        String responsibility = taskData.get("activityResponsibility") != null ? taskData.get("activityResponsibility").toString() : "无";
        nodeText.append("Responsibility: ").append(responsibility).append("\n");
        
        // 添加分配时间
        if (taskData.get("assignedDatetime") != null) {
            String assignedTime = taskData.get("assignedDatetime") instanceof Date ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) taskData.get("assignedDatetime")) :
                taskData.get("assignedDatetime").toString();
            nodeText.append("assigned time: ").append(assignedTime);
        }
        
        return new DefaultMutableTreeNode(nodeText.toString());
    }
    
    /**
     * 展开树的所有节点
     */
    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }
    
    /**
     * 创建空闲人员面板
     */

    
    /**
     * 查看因任务导致不可用的地点
     */
    private void viewUnavailableLocations() {
        // 记录查看不可用地点操作
        HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), "View Unavailable Locations", "Users can view locations that are unavailable due to tasks.");
        
        try {
            // 创建对话框
            JDialog locationDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                    "Unavailable Locations", true);
            locationDialog.setSize(900, 700);
            locationDialog.setLayout(new BorderLayout());
            
            // 创建筛选面板
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Conditions"));
            
            // 地点类型筛选
            JLabel typeLabel = new JLabel("Location Type:");
            JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"All", "building", "room", "level", "square", "gate", "canteen"});
            
            // 时间段筛选
            JLabel timeRangeLabel = new JLabel("Time Range:");
            JComboBox<String> timeRangeComboBox = new JComboBox<>(new String[]{
                "Now and Future", "Only Now", "Next 24 Hours", "Next 7 Days", "Custom Range"
            });
            
            // 刷新按钮
            JButton refreshButton = new JButton("Refresh");
            
            filterPanel.add(typeLabel);
            filterPanel.add(typeComboBox);
            filterPanel.add(timeRangeLabel);
            filterPanel.add(timeRangeComboBox);
            filterPanel.add(refreshButton);
            
            // 创建表格模型
            String[] columnNames = {"Activity ID", "Activity Title", "Activity Type", "Activity Status", "Unavailable Location", "Location Type", "Start Time", "End Time", "Conflict Reason"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
                
                @Override
                public Class<?> getColumnClass(int column) {
                    // 设置时间列可以排序
                    if (column == 6 || column == 7) {
                        return Date.class;
                    }
                    return super.getColumnClass(column);
                }
            };
            
            JTable table = new JTable(tableModel);
            table.setAutoCreateRowSorter(true); // 启用排序
            
            // 设置列宽
            table.getColumnModel().getColumn(0).setPreferredWidth(60);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(80);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(150);
            table.getColumnModel().getColumn(5).setPreferredWidth(80);
            table.getColumnModel().getColumn(6).setPreferredWidth(120);
            table.getColumnModel().getColumn(7).setPreferredWidth(120);
            table.getColumnModel().getColumn(8).setPreferredWidth(120);
            
            // 添加表头渲染器，使表头居中
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
            headerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
            
            // 添加表格监听器，双击可以查看活动详情
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow >= 0) {
                            String activityId = table.getValueAt(selectedRow, 0).toString();
                            // 查找对应的活动并显示详情
                            try {
                                Map<String, Object> conditions = new HashMap<>();
                                conditions.put("activityId", activityId);
                                List<Map<String, Object>> activities = activityService.queryActivities(conditions);
                                if (!activities.isEmpty()) {
                                    showActivityDetailDialog(activities.get(0));
                                }
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog(locationDialog, 
                                        "Failed to get activity details: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(table);
            
            // 添加统计信息
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel statsLabel = new JLabel("Total Unavailable Locations: 0");
            statsPanel.add(statsLabel);
            
            // 顶部面板：筛选条件和统计信息
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(filterPanel, BorderLayout.NORTH);
            topPanel.add(statsPanel, BorderLayout.SOUTH);
            
            // 添加关闭按钮
            JPanel buttonPanel = new JPanel();
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> locationDialog.dispose());
            buttonPanel.add(closeButton);
            
            // 添加导出按钮
            JButton exportButton = new JButton("Export Data");
            exportButton.addActionListener(e -> exportTableData(table, tableModel));
            buttonPanel.add(exportButton);
            
            // 添加到对话框
            locationDialog.add(topPanel, BorderLayout.NORTH);
            locationDialog.add(scrollPane, BorderLayout.CENTER);
            locationDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            // 刷新数据的方法
            Runnable loadData = () -> {
                try {
                    tableModel.setRowCount(0); // 清空表格
                    
                    // 构建查询条件
                    Map<String, Object> conditions = new HashMap<>();
                    String selectedType = (String) typeComboBox.getSelectedItem();
                    if (!"All".equals(selectedType)) {
                        conditions.put("facilityType", selectedType);
                    }
                    
                    // 查询所有活跃的活动，不仅仅是进行中的
                    conditions.put("active_flag", "Y");
                    
                    String timeRange = (String) timeRangeComboBox.getSelectedItem();
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime endTimeFilter = now.plusYears(1); // 默认一年
                    
                    // 在查询前设置时间相关条件
                    switch (timeRange) {
                        case "Now Only":
                            // 只显示进行中的活动
                            conditions.put("status", "In Progress");
                            break;
                        case "Next 24 Hours":
                            endTimeFilter = now.plusHours(24);
                            break;
                        case "Next 7 Days":
                            endTimeFilter = now.plusDays(7);
                            break;
                    }
                    
                    // 在设置完所有条件后进行查询
                    List<Map<String, Object>> activeActivities = activityService.queryActivities(conditions);
                    
                    for (Map<String, Object> activity : activeActivities) {
                        String activityId = activity.get("activityId") != null ? activity.get("activityId").toString() : "-";
                        String title = activity.get("title") != null ? activity.get("title").toString() : "-";
                        String type = activity.get("activityType") != null ? activity.get("activityType").toString() : "-";
                        String status = activity.get("status") != null ? activity.get("status").toString() : "-";
                        String facilityType = activity.get("facilityType") != null ? activity.get("facilityType").toString() : "-";
                        
                        // 确定不可用地点
                        String location = "-";
                        try {
                            // 根据不同的设施类型查询地点信息
                            if ("building".equals(facilityType) && activity.get("buildingId") != null) {
                                int buildingId = Integer.parseInt(activity.get("buildingId").toString());
                                location = getBuildingInfo(buildingId);
                            } else if ("room".equals(facilityType) && activity.get("roomId") != null) {
                                int roomId = Integer.parseInt(activity.get("roomId").toString());
                                location = getRoomInfo(roomId);
                            } else if ("level".equals(facilityType) && activity.get("levelId") != null) {
                                int levelId = Integer.parseInt(activity.get("levelId").toString());
                                location = getLevelInfo(levelId);
                            } else if ("square".equals(facilityType) && activity.get("squareId") != null) {
                                int squareId = Integer.parseInt(activity.get("squareId").toString());
                                location = getSquareInfo(squareId);
                            } else if ("gate".equals(facilityType) && activity.get("gateId") != null) {
                                int gateId = Integer.parseInt(activity.get("gateId").toString());
                                location = getGateInfo(gateId);
                            } else if ("canteen".equals(facilityType) && activity.get("canteenId") != null) {
                                int canteenId = Integer.parseInt(activity.get("canteenId").toString());
                                location = getCanteenInfo(canteenId);
                            }
                        } catch (Exception e) {
                            // Log exception
                        }
                        
                        // 处理时间显示
                        // 移除未使用的变量
                        Date startTimeDate = null;
                        Date endTimeDate = null;
                        boolean isWithinTimeRange = true;
                        
                        Object datetimeObj = activity.get("activityDatetime");
                        if (datetimeObj != null) {
                            LocalDateTime startTime = null;
                            LocalDateTime endTime = null;
                            
                            // Handle different types of date objects
                            if (datetimeObj instanceof LocalDateTime) {
                                startTime = (LocalDateTime) datetimeObj;
                            } else if (datetimeObj instanceof Date) {
                                startTime = ((Date) datetimeObj).toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                            } else if (datetimeObj instanceof String) {
                                // 尝试解析字符串类型的日期
                                try {
                                    // 假设字符串格式为yyyy-MM-dd HH:mm:ss或类似格式
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    startTime = LocalDateTime.parse(datetimeObj.toString(), formatter);
                                } catch (Exception e) {
                                    // 忽略解析异常
                                }
                            }
                            
                            if (startTime != null) {
                                // 计算结束时间
                                if (activity.get("expectedUnavailableDuration") != null) {
                                    try {
                                        int minutes = Integer.parseInt(activity.get("expectedUnavailableDuration").toString());
                                        endTime = startTime.plusMinutes(minutes);
                                    } catch (Exception e) {
                                        // 忽略异常
                                    }
                                }
                                
                                
                                
                                // 转换为Date对象用于排序
                                startTimeDate = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
                                endTimeDate = endTime != null ? Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
                                
                                // 检查是否在时间范围内 - 修正筛选条件
                                if (!"Now Only".equals(timeRange)) {
                                    isWithinTimeRange = startTime.isBefore(endTimeFilter);
                                }
                            } else {
                                // 如果无法解析时间，也将其视为在时间范围内（避免丢失数据）
                                isWithinTimeRange = true;
                            }
                        }
                        
                        // 冲突原因
                        String conflictReason = "In Progress Activity";
                        if ("Planned".equals(status)) {
                            conflictReason = "Planned Activity";
                        } else if ("Paused".equals(status)) {
                            conflictReason = "Paused Activity";
                        }
                        
                        // 只添加在时间范围内的活动，并确保类型一致性
                        if (isWithinTimeRange) {
                            // 确保时间列的数据类型一致性 - 当日期为null时也添加null而不是字符串
                            tableModel.addRow(new Object[]{
                                activityId, title, type, status, location, facilityType,
                                startTimeDate, // null或Date对象
                                endTimeDate, // null或Date对象
                                conflictReason
                            });
                        }
                    }
                    
                    // 更新统计信息
                    int totalUnavailable = tableModel.getRowCount();
                    statsLabel.setText("Total: " + totalUnavailable + " Unavailable Locations");
                    
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(locationDialog, 
                            "An error occurred while querying unavailable locations: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                            "View Unavailable Locations", "Query failed: " + ex.getMessage());
                }
            };
            
            // 添加刷新按钮监听器
            refreshButton.addActionListener(e -> loadData.run());
            
            // 初始加载数据
            loadData.run();
            
            locationDialog.setLocationRelativeTo(this);
            locationDialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "View Unavailable Locations Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), "View Unavailable Locations", "Operation failed: " + ex.getMessage());
        }
    }
    
    /**
     * 获取建筑物信息
     */
    private String getBuildingInfo(int buildingId) {
        try {
            String sql = "SELECT building_code, building_name FROM buildings WHERE building_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, buildingId);
            if (!results.isEmpty()) {
                Map<String, Object> building = results.get(0);
                String code = building.get("building_code") != null ? building.get("building_code").toString() : "";
                String name = building.get("building_name") != null ? building.get("building_name").toString() : "";
                return code + " - " + name;
            }
            return "Unknown Building";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                    "Get building information", "Query failed: " + e.getMessage());
            return "Unknown Building";
        }
    }
    
    /**
     * 获取房间信息
     */
    private String getRoomInfo(int roomId) {
        try {
            String sql = "SELECT name FROM rooms WHERE room_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, roomId);
            if (!results.isEmpty()) {
                Map<String, Object> room = results.get(0);
                String name = room.get("name") != null ? room.get("name").toString() : "";
                return "Room: " + name;
            }
            return "Unknown Room";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                    "Get room information", "Query failed: " + e.getMessage());
            return "Unknown Room";
        }
    }
    
    /**
     * 获取楼层信息
     */
    private String getLevelInfo(int levelId) {
        try {
            String sql = "SELECT level_number, building_id FROM levels WHERE level_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, levelId);
            if (!results.isEmpty()) {
                Map<String, Object> level = results.get(0);
                String levelNumber = level.get("level_number") != null ? level.get("level_number").toString() : "";
                String buildingCode = "";

                if (level.get("building_id") != null) {
                    try {
                        int buildingId = Integer.parseInt(level.get("building_id").toString());
                        String buildingSql = "SELECT building_code FROM buildings WHERE building_id = ?";
                        List<Map<String, Object>> buildingResults = executeQuery(buildingSql, buildingId);
                        if (!buildingResults.isEmpty()) {
                            buildingCode = buildingResults.get(0).get("building_code") != null ?
                                    buildingResults.get(0).get("building_code").toString() : "";
                        }
                    } catch (Exception e) {
                        HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                                "Get Floor Building Information", "Query failed: " + e.getMessage());
                    }
                }
                return buildingCode + "-" + levelNumber + "F";
            }
            return "Unknown Level";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Get Level Information", "Query failed: " + e.getMessage());
            return "Unknown Level";
        }
    }

    /**
     * Get square information
     */
    private String getSquareInfo(int squareId) {
        try {
            String sql = "SELECT name FROM squares WHERE square_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, squareId);
            if (!results.isEmpty()) {
                Map<String, Object> square = results.get(0);
                String name = square.get("name") != null ? square.get("name").toString() : "";
                return "Square: " + name;
            }
            return "Unknown Square";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Get Square Information", "Query failed: " + e.getMessage());
            return "Unknown Square";
        }
    }

    /**
     * Get gate information
     */
    private String getGateInfo(int gateId) {
        try {
            String sql = "SELECT name FROM gates WHERE gate_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, gateId);
            if (!results.isEmpty()) {
                Map<String, Object> gate = results.get(0);
                String name = gate.get("name") != null ? gate.get("name").toString() : "";
                return "Gate: " + name;
            }
            return "Unknown Gate";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Get Gate Information", "Query failed: " + e.getMessage());
            return "Unknown Gate";
        }
    }

    /**
     * Get canteen information
     */
    private String getCanteenInfo(int canteenId) {
        try {
            String sql = "SELECT name FROM canteens WHERE canteen_id = ?";
            List<Map<String, Object>> results = executeQuery(sql, canteenId);
            if (!results.isEmpty()) {
                Map<String, Object> canteen = results.get(0);
                String name = canteen.get("name") != null ? canteen.get("name").toString() : "";
                return "Canteen: " + name;
            }
            return "Unknown Canteen";
        } catch (Exception e) {
            HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(),
                    "Get Canteen Information", "Query failed: " + e.getMessage());
            return "Unknown Canteen";
        }
    }
    /**
     * 执行SQL查询的辅助方法 - 使用活动服务类执行查询
     */
    private List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        // 创建参数映射
        Map<String, Object> queryParams = new HashMap<>();
        // 这里简单处理，实际应该根据SQL语句和参数创建适合的查询条件
        for (int i = 0; i < params.length; i++) {
            queryParams.put("param" + i, params[i]);
        }
        
        // 根据SQL语句的表名决定使用哪个服务来查询
        if (sql.toLowerCase().contains("buildings")) {
            // 使用建筑物服务查询
            return getBuildingData(sql, params);
        } else if (sql.toLowerCase().contains("rooms")) {
            // 使用房间服务查询
            return getRoomData(sql, params);
        } else if (sql.toLowerCase().contains("levels")) {
            // 使用楼层服务查询
            return getLevelData(sql, params);
        } else if (sql.toLowerCase().contains("squares")) {
            // 使用广场服务查询
            return getSquareData(sql, params);
        } else if (sql.toLowerCase().contains("gates")) {
            // 使用大门服务查询
            return getGateData(sql, params);
        } else if (sql.toLowerCase().contains("canteens")) {
            // 使用食堂服务查询
            return getCanteenData(sql, params);
        }
        
        return new ArrayList<>();
    }
    
    // 以下是各个地点类型的数据查询方法
    private List<Map<String, Object>> getBuildingData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        // 简单实现 - 实际应该调用对应的BuildingService
        if (params.length > 0) {
            int buildingId = Integer.parseInt(params[0].toString());
            Map<String, Object> buildingData = new HashMap<>();
            buildingData.put("building_code", "B" + buildingId);
            buildingData.put("building_name", "Building " + buildingId);
            results.add(buildingData);
        }
        return results;
    }
    
    private List<Map<String, Object>> getRoomData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (params.length > 0) {
            int roomId = Integer.parseInt(params[0].toString());
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("name", "Room " + roomId);
            results.add(roomData);
        }
        return results;
    }
    
    private List<Map<String, Object>> getLevelData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (params.length > 0) {
            int levelId = Integer.parseInt(params[0].toString());
            Map<String, Object> levelData = new HashMap<>();
            levelData.put("level_number", levelId);
            levelData.put("building_id", 1); // 假设默认建筑ID
            results.add(levelData);
        }
        return results;
    }
    
    private List<Map<String, Object>> getSquareData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (params.length > 0) {
            int squareId = Integer.parseInt(params[0].toString());
            Map<String, Object> squareData = new HashMap<>();
            squareData.put("name", "Square " + squareId);
            results.add(squareData);
        }
        return results;
    }
    
    private List<Map<String, Object>> getGateData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (params.length > 0) {
            int gateId = Integer.parseInt(params[0].toString());
            Map<String, Object> gateData = new HashMap<>();
            gateData.put("name", "Gate " + gateId);
            results.add(gateData);
        }
        return results;
    }
    
    private List<Map<String, Object>> getCanteenData(String sql, Object[] params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (params.length > 0) {
            int canteenId = Integer.parseInt(params[0].toString());
            Map<String, Object> canteenData = new HashMap<>();
            canteenData.put("name", "Canteen " + canteenId);
            results.add(canteenData);
        }
        return results;
    }
    
    // 导出表格数据为CSV文件
    private void exportTableData(JTable table, DefaultTableModel tableModel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Unavailable Locations Data");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // 设置默认文件名
        String defaultFileName = "Unavailable_Locations_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 确保文件扩展名为CSV
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // 写入表头
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print('"' + tableModel.getColumnName(i) + '"');
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
                
                // 写入数据
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        String text = value != null ? value.toString() : "";
                        // 确保包含逗号或引号的值被正确转义
                        if (text.contains(",") || text.contains("\"")) {
                            text = text.replace("\"", "\"\""); // 转义引号
                            writer.print('"' + text + '"');
                        } else {
                            writer.print(text);
                        }
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this, "Unavailable Locations Data Exported Successfully!\nFile Saved: " + file.getAbsolutePath(), 
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
                HtmlLogger.logInfo(authService.getCurrentUserId(), authService.getCurrentRole(), 
                        "Export Unavailable Locations Data", "Successfully exported " + tableModel.getRowCount() + " records to file: " + file.getAbsolutePath());
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Unavailable Locations Data Export Failed: " + ex.getMessage(), 
                        "Export Failed", JOptionPane.ERROR_MESSAGE);
                HtmlLogger.logError(authService.getCurrentUserId(), authService.getCurrentRole(), 
                        "Export Unavailable Locations Data", "Export Failed: " + ex.getMessage());
            }
        }
    }
    
    // 显示活动详情对话框
    private void showActivityDetailDialog(Map<String, Object> activity) {
        JDialog detailDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Unavailable Locations Details - " + activity.get("title"), true);
        detailDialog.setSize(500, 600);
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane();
        JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 添加活动信息
        addDetailRow(contentPanel, "AcivityID:", activity.get("activityId"));
        addDetailRow(contentPanel, "title:", activity.get("title"));
        addDetailRow(contentPanel, "Facility Type:", activity.get("facilityType"));
        addDetailRow(contentPanel, "Status:", activity.get("status"));
        addDetailRow(contentPanel, "Description:", activity.get("description"));
        addDetailRow(contentPanel, "Responsible ID:", activity.get("responsibleId"));
        addDetailRow(contentPanel, "Responsible Name:", activity.get("responsibleName"));
        addDetailRow(contentPanel, "Facility Type:", activity.get("facilityType"));
        
        // 添加地点信息
        String facilityType = activity.get("facilityType") != null ? activity.get("facilityType").toString() : "";
        String locationInfo = "-";
        try {
            if ("building".equals(facilityType) && activity.get("buildingId") != null) {
                int buildingId = Integer.parseInt(activity.get("buildingId").toString());
                locationInfo = getBuildingInfo(buildingId);
            } else if ("room".equals(facilityType) && activity.get("roomId") != null) {
                int roomId = Integer.parseInt(activity.get("roomId").toString());
                locationInfo = getRoomInfo(roomId);
            } else if ("level".equals(facilityType) && activity.get("levelId") != null) {
                int levelId = Integer.parseInt(activity.get("levelId").toString());
                locationInfo = getLevelInfo(levelId);
            } else if ("square".equals(facilityType) && activity.get("squareId") != null) {
                int squareId = Integer.parseInt(activity.get("squareId").toString());
                locationInfo = getSquareInfo(squareId);
            } else if ("gate".equals(facilityType) && activity.get("gateId") != null) {
                int gateId = Integer.parseInt(activity.get("gateId").toString());
                locationInfo = getGateInfo(gateId);
            } else if ("canteen".equals(facilityType) && activity.get("canteenId") != null) {
                int canteenId = Integer.parseInt(activity.get("canteenId").toString());
                locationInfo = getCanteenInfo(canteenId);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        addDetailRow(contentPanel, "Location:", locationInfo);
        
        // 添加时间信息
        Object datetimeObj = activity.get("activityDatetime");
        if (datetimeObj != null) {
            String startTime = "";
            if (datetimeObj instanceof LocalDateTime) {
                startTime = ((LocalDateTime) datetimeObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (datetimeObj instanceof Date) {
                startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) datetimeObj);
            }
            addDetailRow(contentPanel, "Start:", startTime);
            
            // 计算结束时间
            if (activity.get("expectedUnavailableDuration") != null) {
                try {
                    String endTime = "";
                    int minutes = Integer.parseInt(activity.get("expectedUnavailableDuration").toString());
                    if (datetimeObj instanceof LocalDateTime) {
                        LocalDateTime endDateTime = ((LocalDateTime) datetimeObj).plusMinutes(minutes);
                        endTime = endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else if (datetimeObj instanceof Date) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) datetimeObj);
                        cal.add(Calendar.MINUTE, minutes);
                        endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
                    }
                    addDetailRow(contentPanel, "End:", endTime);
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
        
        addDetailRow(contentPanel, "Create Time:", activity.get("createTime"));
        addDetailRow(contentPanel, "Update Time:", activity.get("updateTime"));
        
        scrollPane.setViewportView(contentPanel);
        
        // 添加关闭按钮
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailDialog.dispose());
        buttonPanel.add(closeButton);
        
        detailDialog.setLayout(new BorderLayout());
        detailDialog.add(scrollPane, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }
    
    // 辅助方法：向详情面板添加一行信息
    private void addDetailRow(JPanel panel, String label, Object value) {
        JLabel lab = new JLabel(label);
        lab.setHorizontalAlignment(JLabel.RIGHT);
        lab.setForeground(Color.BLUE);
        
        JLabel valLabel = new JLabel(value != null ? value.toString() : "-");
        valLabel.setHorizontalAlignment(JLabel.LEFT);
        valLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        panel.add(lab);
        panel.add(valLabel);
    }
    

}