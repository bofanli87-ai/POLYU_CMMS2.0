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
                JOptionPane.showMessageDialog(ReportGenerationPanel.this, "请选择一个报表类型", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- 核心修改点 ---
            // 1. 在文本区域显示“生成中”提示，使用更醒目的格式
            reportTextArea.setText("=========================\n");
            reportTextArea.append("  正在生成报表，请稍候...\n");
            reportTextArea.append("=========================\n");
            reportTextArea.append("\n报表生成过程中请勿关闭页面...\n");
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
                    reportTextArea.setText("=========================\n");
                    reportTextArea.append("  报表生成失败\n");
                    reportTextArea.append("=========================\n\n");
                    reportTextArea.append("错误信息: " + ex.getMessage() + "\n\n");
                    reportTextArea.append("请查看日志文件获取详细错误信息。\n");
                    reportTextArea.append("建议操作: \n");
                    reportTextArea.append("1. 检查数据库连接是否正常\n");
                    reportTextArea.append("2. 确认您有足够的权限访问数据\n");
                    reportTextArea.append("3. 如问题持续，请联系系统管理员\n");
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