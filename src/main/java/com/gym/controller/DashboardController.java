package com.gym.controller;

import com.gym.App;
import com.gym.entity.Registration;
import com.gym.service.*;
import com.gym.util.AlertUtil;
import com.gym.util.RBACUtil;
import com.gym.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

public class DashboardController {
    
    @FXML private Label lblWelcome;
    @FXML private Label lblActiveMembers;
    @FXML private Label lblExpiredMembers;
    @FXML private Label lblActiveRegistrations;
    @FXML private Label lblRevenueThisMonth;
    @FXML private Label lblAttendanceToday;
    @FXML private Label lblExpiringRegistrations;
    @FXML private Label lblActivePromotions;
    @FXML private Label lblTotalDiscountGiven;
    @FXML private Label lblTopPackage;
    
    @FXML private PieChart pieChartPackages;
    @FXML private BarChart<String, Number> barChartMonthly;
    
    // RBAC Buttons
    @FXML private Button btnPackages;
    @FXML private Button btnPromotions;
    @FXML private Button btnReports;
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    @FXML
    public void initialize() {
        try {
            // Apply RBAC
            applyRBAC();
            
            // Display user info
            String userName = SessionManager.getInstance().getCurrentUser().getFullName();
            String role = SessionManager.getInstance().getCurrentUser().getRole().toString();
            lblWelcome.setText("Xin chào, " + userName + " (" + role + ")!");
            
            loadAllDashboardData();
            loadCharts();
            
        } catch (Exception e) {
            e.printStackTrace();
            showDefaultValues();
        }
    }
    
    /**
     * Áp dụng RBAC - Ẩn/hiện button theo quyền
     */
    private void applyRBAC() {
        var user = SessionManager.getInstance().getCurrentUser();
        
        // Chỉ ADMIN mới thấy các nút này
        if (btnPackages != null) {
            btnPackages.setVisible(RBACUtil.canAccessMenu(user, "PACKAGES"));
            btnPackages.setManaged(RBACUtil.canAccessMenu(user, "PACKAGES"));
        }
        
        if (btnPromotions != null) {
            btnPromotions.setVisible(RBACUtil.canAccessMenu(user, "PROMOTIONS"));
            btnPromotions.setManaged(RBACUtil.canAccessMenu(user, "PROMOTIONS"));
        }
        
        if (btnReports != null) {
            btnReports.setVisible(RBACUtil.canAccessMenu(user, "REPORTS"));
            btnReports.setManaged(RBACUtil.canAccessMenu(user, "REPORTS"));
        }
    }
    
    private void showDefaultValues() {
        lblActiveMembers.setText("0");
        lblExpiredMembers.setText("0");
        lblActiveRegistrations.setText("0");
        lblRevenueThisMonth.setText("0 ₫");
        lblAttendanceToday.setText("0");
        lblExpiringRegistrations.setText("0");
        lblActivePromotions.setText("0");
        lblTotalDiscountGiven.setText("0 ₫");
        lblTopPackage.setText("Không có dữ liệu");
    }
    
    private void loadAllDashboardData() {
        try {
            loadBasicStats();
            loadPromotionStats();
            loadTopPackage();
        } catch (Exception e) {
            showDefaultValues();
        }
    }
    
    private void loadBasicStats() {
        try {
            ReportService reportService = new ReportService();
            Map<String, Object> stats = reportService.getDashboardStats();
            
            if (stats != null) {
                lblActiveMembers.setText(stats.getOrDefault("activeMembers", "0").toString());
                lblExpiredMembers.setText(stats.getOrDefault("expiredMembers", "0").toString());
                lblActiveRegistrations.setText(stats.getOrDefault("activeRegistrations", "0").toString());
                lblAttendanceToday.setText(stats.getOrDefault("attendanceToday", "0").toString());
                lblExpiringRegistrations.setText(stats.getOrDefault("expiringRegistrations", "0").toString());
                
                BigDecimal revenue = (BigDecimal) stats.get("revenueThisMonth");
                if (revenue != null) {
                    lblRevenueThisMonth.setText(currencyFormat.format(revenue));
                } else {
                    lblRevenueThisMonth.setText("0 ₫");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadPromotionStats() {
        try {
            PromotionService promotionService = new PromotionService();
            
            List<com.gym.entity.Promotion> activePromos = promotionService.getActivePromotions();
            if (activePromos != null) {
                lblActivePromotions.setText(activePromos.size() + " mã");
            } else {
                lblActivePromotions.setText("0");
            }
            
            BigDecimal totalDiscount = promotionService.getTotalDiscountAmount();
            if (totalDiscount != null) {
                lblTotalDiscountGiven.setText(currencyFormat.format(totalDiscount));
            } else {
                lblTotalDiscountGiven.setText("0 ₫");
            }
            
        } catch (Exception e) {
            lblActivePromotions.setText("0");
            lblTotalDiscountGiven.setText("0 ₫");
        }
    }
    
    private void loadTopPackage() {
        try {
            RegistrationService registrationService = new RegistrationService();
            List<Registration> allRegistrations = registrationService.getAllRegistrations();
            
            if (allRegistrations == null || allRegistrations.isEmpty()) {
                lblTopPackage.setText("Chưa có dữ liệu");
                return;
            }
            
            Map<String, Long> packageCount = new HashMap<>();
            for (Registration reg : allRegistrations) {
                if (reg != null && reg.getPkg() != null) {
                    String packageName = reg.getPkg().getPackageName();
                    packageCount.put(packageName, packageCount.getOrDefault(packageName, 0L) + 1);
                }
            }
            
            if (packageCount.isEmpty()) {
                lblTopPackage.setText("Không có dữ liệu");
                return;
            }
            
            Map.Entry<String, Long> topPackage = null;
            for (Map.Entry<String, Long> entry : packageCount.entrySet()) {
                if (topPackage == null || entry.getValue() > topPackage.getValue()) {
                    topPackage = entry;
                }
            }
            
            if (topPackage != null) {
                lblTopPackage.setText(topPackage.getKey() + " (" + topPackage.getValue() + " lượt)");
            } else {
                lblTopPackage.setText("Không xác định");
            }
            
        } catch (Exception e) {
            lblTopPackage.setText("Lỗi tải dữ liệu");
        }
    }
    
    private void loadCharts() {
        try {
            setupPackageDistributionChart();
            setupMonthlyRevenueChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupPackageDistributionChart() {
        try {
            RegistrationService registrationService = new RegistrationService();
            List<Registration> registrations = registrationService.getAllRegistrations();
            
            if (registrations == null || registrations.isEmpty()) {
                return;
            }
            
            Map<String, Long> packageDistribution = new HashMap<>();
            for (Registration reg : registrations) {
                if (reg != null && reg.getPkg() != null) {
                    String packageName = reg.getPkg().getPackageName();
                    packageDistribution.put(packageName, 
                        packageDistribution.getOrDefault(packageName, 0L) + 1);
                }
            }
            
            var pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Long> entry : packageDistribution.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            pieChartPackages.setUserData(pieChartData);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupMonthlyRevenueChart() {
        try {
            var xAxis = (CategoryAxis) barChartMonthly.getXAxis();
            var yAxis = (NumberAxis) barChartMonthly.getYAxis();
            xAxis.setLabel("Tháng");
            yAxis.setLabel("Triệu VNĐ");
            
            barChartMonthly.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");
            
            RegistrationService registrationService = new RegistrationService();
            List<Registration> registrations = registrationService.getAllRegistrations();
            
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                String monthLabel = month.getMonthValue() + "/" + (month.getYear() % 100);
                
                BigDecimal revenue = BigDecimal.ZERO;
                if (registrations != null) {
                    for (Registration reg : registrations) {
                        if (reg != null && reg.getCreatedAt() != null) {
                            LocalDate regDate = reg.getCreatedAt().toLocalDate();
                            if (regDate.getMonthValue() == month.getMonthValue() && 
                                regDate.getYear() == month.getYear()) {
                                if (reg.getFinalAmount() != null) {
                                    revenue = revenue.add(reg.getFinalAmount());
                                }
                            }
                        }
                    }
                }
                
                double revenueInMillions = revenue.divide(new BigDecimal(1000000), 2, 
                    java.math.RoundingMode.HALF_UP).doubleValue();
                
                series.getData().add(new XYChart.Data<>(monthLabel, revenueInMillions));
            }
            
            barChartMonthly.getData().add(series);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleMembers() {
        try {
            App.changeScene("Member.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handlePackages() {
        if (!RBACUtil.canAccessMenu(SessionManager.getInstance().getCurrentUser(), "PACKAGES")) {
            AlertUtil.showError("Bạn không có quyền truy cập chức năng này!\nChỉ ADMIN mới được quản lý Gói tập.");
            return;
        }
        try {
            App.changeScene("Package.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRegistrations() {
        try {
            App.changeScene("Registration.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleAttendance() {
        try {
            App.changeScene("Attendance.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleReports() {
        if (!RBACUtil.canAccessMenu(SessionManager.getInstance().getCurrentUser(), "REPORTS")) {
            AlertUtil.showError("Bạn không có quyền truy cập chức năng này!\nChỉ ADMIN mới được xem Báo cáo đầy đủ.");
            return;
        }
        try {
            App.changeScene("Report.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML 
    public void handlePromotions() {
        if (!RBACUtil.canAccessMenu(SessionManager.getInstance().getCurrentUser(), "PROMOTIONS")) {
            AlertUtil.showError("Bạn không có quyền truy cập chức năng này!\nChỉ ADMIN mới được quản lý Khuyến mãi.");
            return;
        }
        try {
            App.changeScene("Promotion.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleLogout() {
        if (AlertUtil.showConfirm("Xác nhận", "Bạn có chắc muốn đăng xuất?", "")) {
            SessionManager.getInstance().logout();
            try {
                App.changeScene("Login.fxml");
            } catch (Exception e) {
                AlertUtil.showError("Lỗi: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleRefresh() {
        try {
            loadAllDashboardData();
            loadCharts();
            AlertUtil.showSuccess("✅ Đã làm mới dữ liệu Dashboard!");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi làm mới dữ liệu: " + e.getMessage());
        }
    }
}