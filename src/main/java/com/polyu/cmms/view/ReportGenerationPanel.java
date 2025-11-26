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
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 顶部控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel selectLabel = new JLabel("请选择报表类型：");
        String[] reportTypes = {
                "工人活动分布报表",
                "活动类型分布报表",
                "工人工作效率报表",
                "周维护趋势报表"
        };
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.setPreferredSize(new Dimension(200, 25));

        generateButton = new JButton("生成报表"); // 现在是成员变量
        generateButton.addActionListener(new GenerateButtonListener());

        JButton printButton = new JButton("打印报表");
        printButton.addActionListener(e -> {
            try {
                reportTextArea.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "打印失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.add(selectLabel);
        controlPanel.add(reportTypeComboBox);
        controlPanel.add(generateButton);
        controlPanel.add(printButton);

        // 2. 中部报表显示区域
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reportTextArea);

        // 3. 组装面板
        this.add(controlPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedReport = (String) reportTypeComboBox.getSelectedItem();
            if (selectedReport == null) {
                JOptionPane.showMessageDialog(ReportGenerationPanel.this, "请选择一个报表类型", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- 核心修改点 ---
            // 1. 在文本区域显示“生成中”提示，而不是弹出模态对话框
            reportTextArea.setText("正在生成报表，请稍候...");
            // 2. 禁用生成按钮和下拉框，防止用户重复操作
            generateButton.setEnabled(false);
            reportTypeComboBox.setEnabled(false);

            // 继续使用 SwingWorker 执行耗时操作
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // 在后台线程中生成报表
                    switch (selectedReport) {
                        case "工人活动分布报表":
                            return reportService.generateWorkerActivityReport();
                        case "活动类型分布报表":
                            return reportService.generateActivityTypeReport();
                        //case "建筑物维护频次报表":
                         //   return reportService.generateBuildingMaintenanceReport();
                        case "化学品使用消耗报表":
                            return reportService.generateChemicalConsumptionReport();
                        case "工人工作效率报表":
                            return reportService.generateWorkerEfficiencyReport();
                        case "周维护趋势报表":
                            return reportService.generateWeeklyTrendReport();
                        default:
                            return "未知的报表类型！";
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
                        reportTextArea.setText("生成报表时发生错误: " + ex.getMessage() + "\n请查看日志获取详细信息。");
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