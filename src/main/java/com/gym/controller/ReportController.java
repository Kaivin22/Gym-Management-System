package com.gym.controller;

import com.gym.App;
import com.gym.entity.Registration;
import com.gym.service.ReportService;
import com.gym.util.AlertUtil;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportController {

    @FXML private TableView<RevenueRow> tblRevenueByPackage;
    @FXML private TableColumn<RevenueRow, String> colPackage;
    @FXML private TableColumn<RevenueRow, Integer> colRegistrationCount;
    @FXML private TableColumn<RevenueRow, BigDecimal> colPackageRevenue;
    @FXML private TableColumn<RevenueRow, Double> colPercentage;

    @FXML private ComboBox<String> cboTopType;
    @FXML private TableView<TopMemberRow> tblTopMembers;
    @FXML private TableColumn<TopMemberRow, Integer> colRank;
    @FXML private TableColumn<TopMemberRow, String> colTopMemberCode;
    @FXML private TableColumn<TopMemberRow, String> colTopMemberName;
    @FXML private TableColumn<TopMemberRow, String> colTopValue;
    @FXML private TableColumn<TopMemberRow, String> colTopDetail;

    @FXML private ComboBox<Integer> cboYear;
    @FXML private TableView<MonthlyStatsRow> tblMonthlyStats;
    @FXML private TableColumn<MonthlyStatsRow, String> colMonth;
    @FXML private TableColumn<MonthlyStatsRow, Integer> colNewMembers;
    @FXML private TableColumn<MonthlyStatsRow, Integer> colNewRegistrations;
    @FXML private TableColumn<MonthlyStatsRow, BigDecimal> colMonthlyRevenue;
    @FXML private TableColumn<MonthlyStatsRow, Integer> colMonthlyAttendance;

    @FXML private ComboBox<String> cboDaysFilter;
    @FXML private TableView<Registration> tblExpiringRegistrations;
    @FXML private TableColumn<Registration, String> colExpiringMemberCode;
    @FXML private TableColumn<Registration, String> colExpiringMemberName;
    @FXML private TableColumn<Registration, String> colExpiringPhone;
    @FXML private TableColumn<Registration, String> colExpiringPackage;
    @FXML private TableColumn<Registration, LocalDate> colExpiringEndDate;
    @FXML private TableColumn<Registration, Long> colExpiringDaysLeft;

    @FXML private LineChart<String, Number> lineRevenue;

    private final ReportService reportService = new ReportService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class RevenueRow {
        private final String packageName;
        private final int registrationCount;
        private final BigDecimal revenue;
        private final double percentage;
        
        public RevenueRow(String packageName, int registrationCount, BigDecimal revenue, double percentage) {
            this.packageName = packageName;
            this.registrationCount = registrationCount;
            this.revenue = revenue;
            this.percentage = percentage;
        }
        
        public String getPackageName() { return packageName; }
        public int getRegistrationCount() { return registrationCount; }
        public BigDecimal getRevenue() { return revenue; }
        public double getPercentage() { return percentage; }
    }

    public static class TopMemberRow {
        private final int rank;
        private final String memberCode;
        private final String memberName;
        private final String value;
        private final String detail;
        
        public TopMemberRow(int rank, String memberCode, String memberName, String value, String detail) {
            this.rank = rank;
            this.memberCode = memberCode;
            this.memberName = memberName;
            this.value = value;
            this.detail = detail;
        }
        
        public int getRank() { return rank; }
        public String getMemberCode() { return memberCode; }
        public String getMemberName() { return memberName; }
        public String getValue() { return value; }
        public String getDetail() { return detail; }
    }

    public static class MonthlyStatsRow {
        private final String month;
        private final int newMembers;
        private final int newRegistrations;
        private final BigDecimal revenue;
        private final int attendance;
        
        public MonthlyStatsRow(String month, int newMembers, int newRegistrations, BigDecimal revenue, int attendance) {
            this.month = month;
            this.newMembers = newMembers;
            this.newRegistrations = newRegistrations;
            this.revenue = revenue;
            this.attendance = attendance;
        }
        
        public String getMonth() { return month; }
        public int getNewMembers() { return newMembers; }
        public int getNewRegistrations() { return newRegistrations; }
        public BigDecimal getRevenue() { return revenue; }
        public int getAttendance() { return attendance; }
    }

    @FXML
    public void initialize() {
        try {
            System.out.println("✅ ReportController initialized");
            
            setupComboBoxes();
            setupRevenueByPackageTable();
            setupTopMembersTable();
            setupMonthlyStatsTable();
            setupExpiringRegistrationsTable();
            
            loadRevenueByPackage();
            loadTopMembers();
            loadMonthlyStats();
            loadExpiringRegistrations();
            populateRevenueLineChart();
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khởi tạo báo cáo: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        ObservableList<String> topTypes = FXCollections.observableArrayList(
            "Điểm danh nhiều nhất", 
            "Đăng ký nhiều nhất", 
            "Chi tiêu cao nhất"
        );
        cboTopType.setItems(topTypes);
        cboTopType.setValue("Điểm danh nhiều nhất");
        
        ObservableList<String> daysFilters = FXCollections.observableArrayList(
            "7 ngày", 
            "15 ngày", 
            "30 ngày"
        );
        cboDaysFilter.setItems(daysFilters);
        cboDaysFilter.setValue("7 ngày");
        
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int year = currentYear - 5; year <= currentYear; year++) {
            years.add(year);
        }
        cboYear.setItems(years);
        cboYear.setValue(currentYear);
    }

    private void setupRevenueByPackageTable() {
        colPackage.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPackageName()));
        
        colRegistrationCount.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getRegistrationCount()));
        
        colPackageRevenue.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getRevenue()));
        colPackageRevenue.setCellFactory(col -> new TableCell<RevenueRow, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        
        colPercentage.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPercentage()));
        colPercentage.setCellFactory(col -> new TableCell<RevenueRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f%%", item));
            }
        });
    }

    private void setupTopMembersTable() {
        colRank.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getRank()));
        colTopMemberCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMemberCode()));
        colTopMemberName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMemberName()));
        colTopValue.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getValue()));
        colTopDetail.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDetail()));
    }

    private void setupMonthlyStatsTable() {
        colMonth.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMonth()));
        colNewMembers.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getNewMembers()));
        colNewRegistrations.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getNewRegistrations()));
        colMonthlyRevenue.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getRevenue()));
        colMonthlyRevenue.setCellFactory(col -> new TableCell<MonthlyStatsRow, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        colMonthlyAttendance.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getAttendance()));
    }

    private void setupExpiringRegistrationsTable() {
        colExpiringMemberCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMember().getMemberCode()));
        colExpiringMemberName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMember().getFullName()));
        colExpiringPhone.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMember().getPhone()));
        colExpiringPackage.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPkg().getPackageName()));
        colExpiringEndDate.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEndDate()));
        colExpiringEndDate.setCellFactory(col -> new TableCell<Registration, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
        colExpiringDaysLeft.setCellValueFactory(cellData -> {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), cellData.getValue().getEndDate());
            return new SimpleObjectProperty<>(days);
        });
        colExpiringDaysLeft.setCellFactory(col -> new TableCell<Registration, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " ngày");
                    if (item <= 3) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item <= 7) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void loadRevenueByPackage() {
        try {
            Map<String, BigDecimal> revenueMap = reportService.getRevenueByPackage();
            Map<String, Integer> countMap = reportService.getRegistrationCountByPackage();
            
            if (revenueMap == null || revenueMap.isEmpty()) {
                tblRevenueByPackage.setItems(FXCollections.observableArrayList());
                return;
            }
            
            BigDecimal totalRevenue = revenueMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            ObservableList<RevenueRow> data = FXCollections.observableArrayList();
            for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
                String packageName = entry.getKey();
                BigDecimal revenue = entry.getValue();
                int count = countMap.getOrDefault(packageName, 0);
                
                double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                    revenue.divide(totalRevenue, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue() : 0;
                
                data.add(new RevenueRow(packageName, count, revenue, percentage));
            }
            
            data.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));
            tblRevenueByPackage.setItems(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi load revenue: " + e.getMessage());
        }
    }

    private void loadTopMembers() {
        try {
            String type = cboTopType.getValue();
            String key;
            
            if (type.contains("Điểm danh")) {
                key = "attendance";
            } else if (type.contains("Chi tiêu")) {
                key = "spending";
            } else {
                key = "registrations";
            }
            
            List<Object[]> rows = reportService.getTopMembers(key, 10);
            ObservableList<TopMemberRow> data = FXCollections.observableArrayList();
            
            int rank = 1;
            for (Object[] r : rows) {
                String memberCode = r[1] != null ? r[1].toString() : "";
                String memberName = r[2] != null ? r[2].toString() : "";
                String value = r[3] != null ? r[3].toString() : "0";
                
                String detail;
                if (key.equals("spending")) {
                    BigDecimal amount = new BigDecimal(value);
                    detail = "Tổng chi: " + currencyFormat.format(amount);
                } else {
                    detail = value + " lượt";
                }
                
                data.add(new TopMemberRow(rank++, memberCode, memberName, value, detail));
            }
            
            tblTopMembers.setItems(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi load top members: " + e.getMessage());
        }
    }

    private void loadMonthlyStats() {
        try {
            int year = cboYear.getValue();
            List<Object[]> results = reportService.getMonthlyStats(year);
            
            ObservableList<MonthlyStatsRow> data = FXCollections.observableArrayList();
            
            for (Object[] row : results) {
                String month = (String) row[0];
                int newMembers = (Integer) row[1];
                int newRegs = (Integer) row[2];
                BigDecimal revenue = (BigDecimal) row[3];
                int attendance = (Integer) row[4];
                
                data.add(new MonthlyStatsRow(month, newMembers, newRegs, revenue, attendance));
            }
            
            tblMonthlyStats.setItems(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi load monthly stats: " + e.getMessage());
        }
    }

    private void loadExpiringRegistrations() {
        try {
            String filter = cboDaysFilter.getValue();
            int days = Integer.parseInt(filter.split(" ")[0]);
            
            List<Registration> expiring = reportService.getExpiringRegistrations(days);
            tblExpiringRegistrations.setItems(FXCollections.observableArrayList(expiring));
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi load expiring: " + e.getMessage());
        }
    }

    private void populateRevenueLineChart() {
        try {
            lineRevenue.getData().clear();
            List<Object[]> rows = reportService.getRevenueByDay(30);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");
            
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
            for (Object[] r : rows) {
                LocalDate day = (LocalDate) r[0];
                BigDecimal rev = (BigDecimal) r[1];
                series.getData().add(new XYChart.Data<>(day.format(df), rev));
            }
            
            lineRevenue.getData().add(series);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTopTypeChange() {
        loadTopMembers();
    }

    @FXML
    public void handleYearChange() {
        loadMonthlyStats();
    }

    @FXML
    public void handleDaysFilterChange() {
        loadExpiringRegistrations();
    }

    @FXML
    public void handleRefresh() {
        try {
            loadRevenueByPackage();
            loadTopMembers();
            loadMonthlyStats();
            loadExpiringRegistrations();
            populateRevenueLineChart();
            AlertUtil.showSuccess("✅ Đã làm mới dữ liệu báo cáo!");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi làm mới: " + e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        try {
            App.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
}