package com.polyu.cmms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // Unified date format
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // Format date as "yyyy-MM-dd"
    public static String format(Date date) {
        if (date == null) return "";
        // Better to create a new instance each time or use ThreadLocal to avoid thread safety issues
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // 【New Method】Format date as "yyyy-MM-dd（EEE）", e.g. "2023-10-27（Fri）"
    public static String formatWithWeekday(Date date) {
        if (date == null) return "";
        // Use Locale.ENGLISH to ensure weekday is displayed in English
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd（EEE）", Locale.ENGLISH);
        // Optional: Set timezone to avoid date display deviation due to server timezone issues
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return sdf.format(date);
    }

    // Parse date string
    public static Date parse(String dateStr) {
        try {
            return dateStr == null ? null : SDF.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Calculate the day difference between two dates
    public static long getDayDiff(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diffMs = endDate.getTime() - startDate.getTime();
        return diffMs / (1000 * 60 * 60 * 24);
    }
}