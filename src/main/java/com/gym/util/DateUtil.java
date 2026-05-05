// ============= DateUtil.java =============
package com.gym.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMATTER);
    }
    
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    public static boolean isExpired(LocalDate endDate) {
        return LocalDate.now().isAfter(endDate);
    }
    
    public static boolean isExpiringSoon(LocalDate endDate, int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return endDate.isBefore(threshold) || endDate.isEqual(threshold);
    }
    
    public static String getRelativeTimeString(LocalDate date) {
        long days = daysBetween(LocalDate.now(), date);
        
        if (days < 0) {
            return "Đã hết hạn " + Math.abs(days) + " ngày";
        } else if (days == 0) {
            return "Hết hạn hôm nay";
        } else if (days == 1) {
            return "Còn 1 ngày";
        } else if (days <= 7) {
            return "Còn " + days + " ngày";
        } else {
            return "Còn " + (days / 7) + " tuần";
        }
    }
}