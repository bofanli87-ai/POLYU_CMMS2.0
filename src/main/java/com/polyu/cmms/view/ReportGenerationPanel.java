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
    private JButton generateButton; // 提升为成员变量，方便在内部类中访问

    public ReportGenerationPanel() {
        reportService = new ReportService();
        initializeUI();
    }

    private void initializeUI() {
        this.setLayout(new BorderLayout(15, 15)); // 增大间距
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 增大边距，使界面更宽敞

        // 1. 顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); // 增大控件间距

        JLabel selectLabel = new JLabel("Select Report Type:");
        String[] reportTypes = {
                "Worker Activity Distribution Report",
                "Activity Type Distribution Report",
                "Worker Efficiency Report",
                "Weekly Maintenance Trend Report"
        };
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.setPreferredSize(new Dimension(200, 25));

        generateButton = new JButton("Generate Report"); // 现在是成员变量
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

        // 2. 中部报表显示区域
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // 稍小字体更适合格式化报表
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(false);
        reportTextArea.setWrapStyleWord(false);
        reportTextArea.setTabSize(4); // 设置制表符大小为4，使对齐更美观
        reportTextArea.setMargin(new Insets(10, 10, 10, 10)); // 设置内边距，让文本不紧贴边框
        
        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setPreferredSize(new Dimension(900, 600)); // 增大面板尺寸，提供更好的阅读体验
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 3. 组装面板
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

            // --- 核心修改点 ---
            // 1. 在文本区域显示“生成中”提示，使用更醒目的格式
            reportTextArea.setText("=========================\n");
            reportTextArea.append("  Generating Report, Please Wait...\n");
            reportTextArea.append("=========================\n");
            reportTextArea.append("\nPlease do not close the page during the report generation process...\n");
            // 2. 禁用生成按钮和下拉框，防止用户重复操作
            generateButton.setEnabled(false);
            reportTypeComboBox.setEnabled(false);

            // 继续使用 SwingWorker 执行耗时操作
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // 在后台线程中生成报表
                    switch (selectedReport) {
                        case "Worker Activity Distribution Report":
                            return reportService.generateWorkerActivityReport();
                        case "Activity Type Distribution Report":
                            return reportService.generateActivityTypeReport();
                        //case "建筑物维护频次报表":
                         //   return reportService.generateBuildingMaintenanceReport();
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
                        // 获取后台任务的结果并更新UI
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
                        // --- 核心修改点 ---
                        // 3. 无论成功或失败，都重新启用按钮和下拉框
                        generateButton.setEnabled(true);
                        reportTypeComboBox.setEnabled(true);
                    }
                }
            }.execute();
        }
    }
}