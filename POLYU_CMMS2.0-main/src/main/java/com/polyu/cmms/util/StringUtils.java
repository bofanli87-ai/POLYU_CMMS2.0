// 2. String utility class (handles text formatting, ratio calculation)
package com.polyu.cmms.util;

public class StringUtils {
    // Generate text progress bar (■ represents proportion, □ represents remaining)
    public static String getProgressBar(double ratio, int totalLength) {
        int filledLength = (int) (ratio * totalLength);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filledLength; i++) bar.append("■");
        for (int i = 0; i < totalLength - filledLength; i++) bar.append("□");
        return bar.toString();
    }

    // Format number (keep 1 decimal place)
    public static String formatNumber(double num) {
        return String.format("%.1f", num);
    }

    // Left-align text (pad with spaces to ensure consistent length)
    public static String leftPad(String text, int length) {
        if (text == null) text = "";
        return String.format("%-" + length + "s", text);
    }
}