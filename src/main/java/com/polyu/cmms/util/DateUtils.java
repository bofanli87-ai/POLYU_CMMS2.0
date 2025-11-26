package com.polyu.cmms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    // 统一日期格式
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // 格式化日期为 "yyyy-MM-dd"
    public static String format(Date date) {
        if (date == null) return "";
        // 最好每次创建新实例或使用 ThreadLocal 以避免线程安全问题
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // 【新增方法】格式化日期为 "yyyy-MM-dd（EEE）"，例如 "2023-10-27（周五）"
    public static String formatWithWeekday(Date date) {
        if (date == null) return "";
        // 使用 Locale.CHINA 确保星期几显示为中文
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd（EEE）", Locale.CHINA);
        // 可选：设置时区，避免因服务器时区问题导致日期显示偏差
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return sdf.format(date);
    }

    // 解析日期字符串
    public static Date parse(String dateStr) {
        try {
            return dateStr == null ? null : SDF.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 计算两个日期的天数差
    public static long getDayDiff(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diffMs = endDate.getTime() - startDate.getTime();
        return diffMs / (1000 * 60 * 60 * 24);
    }
}