// 2. 字符串工具类（处理文本排版、占比计算）
package com.polyu.cmms.util;

public class StringUtils {
    // 生成文本进度条（■表示占比，□表示剩余）
    public static String getProgressBar(double ratio, int totalLength) {
        int filledLength = (int) (ratio * totalLength);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filledLength; i++) bar.append("■");
        for (int i = 0; i < totalLength - filledLength; i++) bar.append("□");
        return bar.toString();
    }

    // 格式化数字（保留1位小数）
    public static String formatNumber(double num) {
        return String.format("%.1f", num);
    }

    // 左对齐文本（补空格，确保长度一致）
    public static String leftPad(String text, int length) {
        if (text == null) text = "";
        return String.format("%-" + length + "s", text);
    }
}