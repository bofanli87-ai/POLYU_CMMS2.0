package com.polyu.cmms.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HtmlLogger {
    private static final String LOG_DIR = "logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String HTML_HEADER = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>系统操作日志 - %s</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                h1 { color: #333; }
                table { border-collapse: collapse; width: 100%%; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f2f2f2; }
                tr:nth-child(even) { background-color: #f9f9f9; }
                .ERROR { color: red; }
                .WARNING { color: orange; }
                .INFO { color: blue; }
                .SUCCESS { color: green; }
            </style>
        </head>
        <body>
            <h1>系统操作日志 - %s</h1>
            <table>
                <tr>
                    <th>时间</th>
                    <th>用户ID</th>
                    <th>角色</th>
                    <th>操作类型</th>
                    <th>操作描述</th>
                    <th>状态</th>
                    <th>IP地址</th>
                </tr>
    """;
    private static final String HTML_FOOTER = """
            </table>
        </body>
        </html>
    """;
    
    public enum LogLevel {
        INFO, WARNING, ERROR, SUCCESS
    }
    
    // 记录日志
    public static void log(int userId, String role, String operationType, String description, LogLevel level, String ipAddress) {
        String dateStr = DATE_FORMAT.format(new Date());
        String logFileName = LOG_DIR + "/log_" + dateStr + ".html";
        
        // 确保日志目录存在
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        File logFile = new File(logFileName);
        boolean isNewFile = !logFile.exists();
        
        try (FileWriter writer = new FileWriter(logFile, true)) {
            if (isNewFile) {
                // 新文件需要写入HTML头部
                writer.write(String.format(HTML_HEADER, dateStr, dateStr));
            } else {
                // 已有文件需要先移除HTML尾部，然后添加新记录，最后重新添加尾部
                String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
                int footerIndex = content.lastIndexOf(HTML_FOOTER);
                if (footerIndex > 0) {
                    java.nio.file.Files.write(logFile.toPath(), content.substring(0, footerIndex).getBytes());
                }
            }
            
            // 写入日志记录
            String timestamp = TIME_FORMAT.format(new Date());
            String logRow = String.format("""
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td class="%s">%s</td>
                    <td>%s</td>
                </tr>
            """, timestamp, userId, role, operationType, description, level.name(), level.name(), ipAddress);
            
            writer.write(logRow);
            writer.write(HTML_FOOTER);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 便捷方法：记录成功操作
    public static void logSuccess(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.SUCCESS, "127.0.0.1");
    }
    
    // 便捷方法：记录错误操作
    public static void logError(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.ERROR, "127.0.0.1");
    }
    
    // 便捷方法：记录信息
    public static void logInfo(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.INFO, "127.0.0.1");
    }
    
    // 便捷方法：记录警告
    public static void logWarning(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.WARNING, "127.0.0.1");
    }

    // ==================== 新增：通用系统日志方法 ====================

    /**
     * 记录系统错误日志（无需用户上下文）
     * @param source 日志来源（通常是类名或方法名）
     * @param message 日志消息
     * @param throwable 异常对象（可为null）
     */
    public static void error(String source, String message, Throwable throwable) {
        logSystem(source, message, LogLevel.ERROR, throwable);
    }

    /**
     * 记录系统警告日志（无需用户上下文）
     * @param source 日志来源
     * @param message 日志消息
     * @param throwable 异常对象（可为null）
     */
    public static void warn(String source, String message, Throwable throwable) {
        logSystem(source, message, LogLevel.WARNING, throwable);
    }

    /**
     * 记录系统信息日志（无需用户上下文）
     * @param source 日志来源
     * @param message 日志消息
     */
    public static void info(String source, String message) {
        logSystem(source, message, LogLevel.INFO, null);
    }

    /**
     * 系统日志核心方法
     */
    private static void logSystem(String source, String message, LogLevel level, Throwable throwable) {
        // 对于系统日志，填充通用信息
        int dummyUserId = 0; // 用0表示无特定用户
        String dummyRole = "SYSTEM"; // 角色标记为系统
        String operationType = source; // 将来源作为操作类型

        // 如果有异常，将异常堆栈信息附加到消息中
        if (throwable != null) {
            message += "。详细信息：" + getStackTraceAsString(throwable);
        }

        // 调用现有的log方法
        log(dummyUserId, dummyRole, operationType, message, level, "SYSTEM");
    }

    /**
     * 将异常堆栈信息转换为字符串
     */
    private static String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

}