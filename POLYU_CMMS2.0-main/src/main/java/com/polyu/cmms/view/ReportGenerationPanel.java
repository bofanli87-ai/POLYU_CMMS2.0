package com.polyu.cmms.view;

import com.polyu.cmms.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;

public class ReportGenerationPanel extends JPanel {

    private final ReportService reportService;
    private JComboBox<String> reportTypeComboBox;
    private JTextArea reportTextArea;
    private JButton generateButton; // Promoted to member variable for easy access in inner class

    public ReportGenerationPanel() {
        reportService = new ReportService();
        initializeUI();
    }

    private void initializeUI() {
        this.setLayout(new BorderLayout(15, 15)); // Increase spacing
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Increase margins for a more spacious interface

        // 1. Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); // Increase control spacing

        JLabel selectLabel = new JLabel("Select Report Type:");
        String[] reportTypes = {
                "Worker Activity Distribution Report",
                "Activity Type Distribution Report",
                "Worker Efficiency Report",
                "Weekly Maintenance Trend Report"
        };
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.setPreferredSize(new Dimension(200, 25));

        generateButton = new JButton("Generate Report"); // Now a member variable
        generateButton.addActionListener(new GenerateButtonListener());

        JButton printButton = new JButton("Print Report");
        printButton.addActionListener(e -> {
            try {
                reportTextArea.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(selectLabel);
        controlPanel.add(reportTypeComboBox);
        controlPanel.add(generateButton);
        controlPanel.add(printButton);

        // 2. Middle report display area
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Slightly smaller font is better for formatted reports
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(false);
        reportTextArea.setWrapStyleWord(false);
        reportTextArea.setTabSize(4); // Set tab size to 4 for better alignment
        reportTextArea.setMargin(new Insets(10, 10, 10, 10)); // Set padding so text doesn't stick to the border

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setPreferredSize(new Dimension(900, 600)); // Increase panel size for better reading experience
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 3. Assemble panels
        this.add(controlPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedReport = (String) reportTypeComboBox.getSelectedItem();
            if (selectedReport == null) {
                JOptionPane.showMessageDialog(ReportGenerationPanel.this, "Please select a report type", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- Core modification points ---
            // 1. Display "Generating..." prompt in text area with more noticeable format
            reportTextArea.setText("=========================\n");
            reportTextArea.append("  Generating Report, Please Wait...\n");
            reportTextArea.append("=========================\n");
            reportTextArea.append("\nPlease do not close the page during the report generation process...\n");
            // 2. Disable generate button and dropdown to prevent duplicate operations
            generateButton.setEnabled(false);
            reportTypeComboBox.setEnabled(false);

            // Continue using SwingWorker for time-consuming operations
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // Generate report in background thread
                    switch (selectedReport) {
                        case "Worker Activity Distribution Report":
                            return reportService.generateWorkerActivityReport();
                        case "Activity Type Distribution Report":
                            return reportService.generateActivityTypeReport();
                        case "Chemicals Usage Consumption Report":
                            return reportService.generateChemicalConsumptionReport();
                        case "Worker Efficiency Report":
                            return reportService.generateWorkerEfficiencyReport();
                        case "Weekly Maintenance Trend Report":
                            return reportService.generateWeeklyTrendReport();
                        default:
                            return "Unknown Report Type!";
                    }
                }

                @Override
                protected void done() {
                    try {
                        // Get background task result and update UI
                        String reportContent = get();
                        reportTextArea.setText(reportContent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        reportTextArea.setText("=========================\n");
                        reportTextArea.append("  Report Generation Failed\n");
                        reportTextArea.append("=========================\n\n");
                        reportTextArea.append("Error Message: " + ex.getMessage() + "\n\n");
                        reportTextArea.append("Please check the log file for more details.\n");
                        reportTextArea.append("Suggested Actions: \n");
                        reportTextArea.append("1. Check if the database connection is normal\n");
                        reportTextArea.append("2. Confirm that you have sufficient permissions to access the data\n");
                        reportTextArea.append("3. If the problem persists, please contact the system administrator\n");
                    } finally {
                        // --- Core modification points ---
                        // 3. Re-enable button and dropdown regardless of success or failure
                        generateButton.setEnabled(true);
                        reportTypeComboBox.setEnabled(true);
                    }
                }
            }.execute();
        }
    }
}