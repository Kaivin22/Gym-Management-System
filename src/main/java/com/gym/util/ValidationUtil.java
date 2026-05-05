// ============= ValidationUtil.java =============
package com.gym.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^(0|\\+84)(\\d{9,10})$");
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isPositiveNumber(String str) {
        if (!isNumeric(str)) return false;
        return Double.parseDouble(str) > 0;
    }
    
    public static String validateRequired(String value, String fieldName) {
        if (isEmpty(value)) {
            return fieldName + " không được để trống";
        }
        return null;
    }
    
    public static String validateEmail(String email) {
        if (isEmpty(email)) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        return null;
    }
    
    public static String validatePhone(String phone) {
        if (isEmpty(phone)) {
            return "Số điện thoại không được để trống";
        }
        if (!isValidPhone(phone)) {
            return "Số điện thoại không hợp lệ (phải có 10-11 số và bắt đầu bằng 0)";
        }
        return null;
    }
}