package com.gym.util;

import com.gym.controller.ReportController;
import com.gym.entity.Registration;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExportUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Create CSV content with UTF-8 BOM
     */
    private static String createCSVContent(String[][] data) {
        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF"); // UTF-8 BOM for Excel compatibility
        
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                String cell = row[i] != null ? row[i] : "";
                // Escape special characters
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    cell = "\"" + cell.replace("\"", "\"\"") + "\"";
                }
                csv.append(cell);
                if (i < row.length - 1) {
                    csv.append(",");
                }
            }
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Export Revenue by Package report
     */
    public static void exportRevenueByPackage(List<ReportController.RevenueRow> revenueData, File file) 
            throws IOException {
        String[][] data = new String[revenueData.size() + 1][4];
        
        // Header
        data[0] = new String[]{
            "Gói tập", 
            "Số lượt đăng ký", 
            "Doanh thu", 
            "Tỷ lệ %"
        };
        
        // Data rows
        for (int i = 0; i < revenueData.size(); i++) {
            ReportController.RevenueRow row = revenueData.get(i);
            data[i + 1] = new String[]{
                row.getPackageName(),
                String.valueOf(row.getRegistrationCount()),
                String.format("%,d ₫", row.getRevenue().intValue()),
                String.format("%.2f%%", row.getPercentage())
            };
        }
        
        writeToFile(file, createCSVContent(data));
    }
    
    /**
     * Export Top Members report
     */
    public static void exportTopMembersToCSV(List<ReportController.TopMemberRow> items, File file) 
            throws IOException {
        String[][] data = new String[items.size() + 1][5];
        
        // Header
        data[0] = new String[]{
            "Hạng", 
            "Mã HV", 
            "Họ tên", 
            "Giá trị", 
            "Chi tiết"
        };
        
        // Data rows
        for (int i = 0; i < items.size(); i++) {
            ReportController.TopMemberRow row = items.get(i);
            data[i + 1] = new String[]{
                String.valueOf(row.getRank()),
                row.getMemberCode(),
                row.getMemberName(),
                row.getValue(),
                row.getDetail()
            };
        }
        
        writeToFile(file, createCSVContent(data));
    }
    
    /**
     * Export Monthly Statistics report
     */
    public static void exportMonthlyStats(List<ReportController.MonthlyStatsRow> monthlyStats, File file) 
            throws IOException {
        String[][] data = new String[monthlyStats.size() + 1][5];
        
        // Header
        data[0] = new String[]{
            "Tháng", 
            "HV mới", 
            "ĐK mới", 
            "Doanh thu", 
            "Điểm danh"
        };
        
        // Data rows
        for (int i = 0; i < monthlyStats.size(); i++) {
            ReportController.MonthlyStatsRow row = monthlyStats.get(i);
            data[i + 1] = new String[]{
                row.getMonth(),
                String.valueOf(row.getNewMembers()),
                String.valueOf(row.getNewRegistrations()),
                String.format("%,d ₫", row.getRevenue().intValue()),
                String.valueOf(row.getAttendance())
            };
        }
        
        writeToFile(file, createCSVContent(data));
    }
    
    /**
     * Export Expiring Registrations report
     */
    public static void exportRegistrationsToCSV(List<Registration> registrations, File file) 
            throws IOException {
        String[][] data = new String[registrations.size() + 1][7];
        
        // Header
        data[0] = new String[]{
            "Mã HV", 
            "Họ tên", 
            "SĐT", 
            "Gói tập", 
            "Ngày hết hạn", 
            "Còn lại (ngày)",
            "Trạng thái"
        };
        
        // Data rows
        LocalDate today = LocalDate.now();
        for (int i = 0; i < registrations.size(); i++) {
            Registration reg = registrations.get(i);
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, reg.getEndDate());
            
            data[i + 1] = new String[]{
                reg.getMember().getMemberCode(),
                reg.getMember().getFullName(),
                reg.getMember().getPhone(),
                reg.getPkg().getPackageName(),
                reg.getEndDate().format(DATE_FORMATTER),
                String.valueOf(daysLeft),
                daysLeft <= 3 ? "Khẩn cấp" : (daysLeft <= 7 ? "Cần chú ý" : "Bình thường")
            };
        }
        
        writeToFile(file, createCSVContent(data));
    }
    
    /**
     * Show save file dialog
     */
    public static File showSaveDialog(Stage stage, String title, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setInitialFileName(defaultFileName + "_" + timestamp + ".csv");
        
        return fileChooser.showSaveDialog(stage);
    }
    
    /**
     * Write content to file
     */
    private static void writeToFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}