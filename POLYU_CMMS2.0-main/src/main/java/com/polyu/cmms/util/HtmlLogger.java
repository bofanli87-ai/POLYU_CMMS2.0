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
            <title>System Operation Log - %s</title>
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
            <h1>System Operation Log - %s</h1>
            <table>
                <tr>
                    <th>Time</th>
                    <th>User ID</th>
                    <th>Role</th>
                    <th>Operation Type</th>
                    <th>Operation Description</th>
                    <th>Status</th>
                    <th>IP Address</th>
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

    // Log operation
    public static void log(int userId, String role, String operationType, String description, LogLevel level, String ipAddress) {
        String dateStr = DATE_FORMAT.format(new Date());
        String logFileName = LOG_DIR + "/log_" + dateStr + ".html";

        // Ensure log directory exists
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        File logFile = new File(logFileName);
        boolean isNewFile = !logFile.exists();

        try (FileWriter writer = new FileWriter(logFile, true)) {
            if (isNewFile) {
                // New file needs HTML header
                writer.write(String.format(HTML_HEADER, dateStr, dateStr));
            } else {
                // Existing file needs to remove HTML footer, add new record, then re-add footer
                String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
                int footerIndex = content.lastIndexOf(HTML_FOOTER);
                if (footerIndex > 0) {
                    java.nio.file.Files.write(logFile.toPath(), content.substring(0, footerIndex).getBytes());
                }
            }

            // Write log record
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

    // Convenience method: Log successful operation
    public static void logSuccess(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.SUCCESS, "127.0.0.1");
    }

    // Convenience method: Log error operation
    public static void logError(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.ERROR, "127.0.0.1");
    }

    // Convenience method: Log information
    public static void logInfo(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.INFO, "127.0.0.1");
    }

    // Convenience method: Log warning
    public static void logWarning(int userId, String role, String operationType, String description) {
        log(userId, role, operationType, description, LogLevel.WARNING, "127.0.0.1");
    }

    // ==================== NEW: General system logging methods ====================

    /**
     * Log system error (no user context required)
     * @param source Log source (usually class name or method name)
     * @param message Log message
     * @param throwable Exception object (can be null)
     */
    public static void error(String source, String message, Throwable throwable) {
        logSystem(source, message, LogLevel.ERROR, throwable);
    }

    /**
     * Log system warning (no user context required)
     * @param source Log source
     * @param message Log message
     * @param throwable Exception object (can be null)
     */
    public static void warn(String source, String message, Throwable throwable) {
        logSystem(source, message, LogLevel.WARNING, throwable);
    }

    /**
     * Log system information (no user context required)
     * @param source Log source
     * @param message Log message
     */
    public static void info(String source, String message) {
        logSystem(source, message, LogLevel.INFO, null);
    }

    /**
     * Core system logging method
     */
    private static void logSystem(String source, String message, LogLevel level, Throwable throwable) {
        // For system logs, fill in generic information
        int dummyUserId = 0; // Use 0 to indicate no specific user
        String dummyRole = "SYSTEM"; // Mark role as system
        String operationType = source; // Use source as operation type

        // If there's an exception, append stack trace to message
        if (throwable != null) {
            message += ". Details: " + getStackTraceAsString(throwable);
        }

        // Call existing log method
        log(dummyUserId, dummyRole, operationType, message, level, "SYSTEM");
    }

    /**
     * Convert exception stack trace to string
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