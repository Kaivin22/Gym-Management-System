package com.gym.util;

import com.gym.entity.User;

public class RBACUtil {
    
    /**
     * Kiểm tra quyền Admin
     */
    public static boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }
    
    /**
     * Kiểm tra quyền Staff
     */
    public static boolean isStaff(User user) {
        return user != null && user.getRole() == User.Role.STAFF;
    }
    
    /**
     * ADMIN có thể:
     * - Quản lý Users (CRUD)
     * - Quản lý Packages (CRUD)
     * - Quản lý Promotions (CRUD)
     * - Xem tất cả Reports
     * - Xóa dữ liệu
     */
    public static boolean canManageUsers(User user) {
        return isAdmin(user);
    }
    
    public static boolean canDeleteData(User user) {
        return isAdmin(user);
    }
    
    public static boolean canManagePromotions(User user) {
        return isAdmin(user);
    }
    
    public static boolean canViewAllReports(User user) {
        return isAdmin(user);
    }
    
    /**
     * STAFF có thể:
     * - Quản lý Members (CRUD)
     * - Quản lý Registrations (Create, Update - không Delete)
     * - Điểm danh Attendance
     * - Xem Reports cơ bản
     * - KHÔNG được xóa dữ liệu quan trọng
     */
    public static boolean canManageMembers(User user) {
        return user != null; // Cả Admin và Staff
    }
    
    public static boolean canManageRegistrations(User user) {
        return user != null; // Cả Admin và Staff
    }
    
    public static boolean canCheckInAttendance(User user) {
        return user != null; // Cả Admin và Staff
    }
    
    /**
     * Hiển thị menu theo role
     */
    public static boolean canAccessMenu(User user, String menuName) {
        if (user == null) return false;
        
        return switch (menuName) {
            case "DASHBOARD" -> true; // Tất cả
            case "MEMBERS" -> true; // Tất cả
            case "PACKAGES" -> isAdmin(user); // Chỉ Admin
            case "REGISTRATIONS" -> true; // Tất cả
            case "ATTENDANCE" -> true; // Tất cả
            case "PROMOTIONS" -> isAdmin(user); // Chỉ Admin
            case "REPORTS" -> isAdmin(user); // Chỉ Admin xem full
            case "USERS" -> isAdmin(user); // Chỉ Admin
            default -> false;
        };
    }
}
