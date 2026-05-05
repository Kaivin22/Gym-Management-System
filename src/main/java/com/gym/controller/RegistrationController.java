package com.gym.controller;

import com.gym.App;
import com.gym.dao.PaymentDAO;
import com.gym.entity.*;
import com.gym.service.MemberService;
import com.gym.service.PackageService;
import com.gym.service.RegistrationService;
import com.gym.util.AlertUtil;
import com.gym.util.SessionManager;
import com.gym.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class RegistrationController {
    
    // Left side - Table
    @FXML private ComboBox<String> cboFilterStatus;
    @FXML private TableView<Registration> tblRegistrations;
    @FXML private TableColumn<Registration, String> colRegCode;
    @FXML private TableColumn<Registration, String> colMemberName;
    @FXML private TableColumn<Registration, String> colPackageName;
    @FXML private TableColumn<Registration, LocalDate> colStartDate;
    @FXML private TableColumn<Registration, LocalDate> colEndDate;
    @FXML private TableColumn<Registration, BigDecimal> colFinalAmount;
    @FXML private TableColumn<Registration, BigDecimal> colPaidAmount;
    @FXML private TableColumn<Registration, String> colPaymentStatus;
    @FXML private TableColumn<Registration, String> colStatus;
    
    // Right side - Form
    @FXML private TextField txtMemberSearch;
    @FXML private VBox pnlMemberInfo;
    @FXML private Label lblSelectedMember;
    @FXML private Label lblSelectedMemberCode;
    @FXML private Label lblSelectedMemberPhone;
    
    @FXML private ComboBox<com.gym.entity.Package> cboPackage;
    @FXML private VBox pnlPackageInfo;
    @FXML private Label lblPackageDescription;
    @FXML private Label lblPackageDuration;
    @FXML private Label lblPackagePrice;
    @FXML private Label lblPackageDiscount;
    
    @FXML private DatePicker dpStartDate;
    @FXML private Label lblEndDate;
    
    @FXML private Label lblTotalAmount;
    @FXML private Label lblDiscountAmount;
    @FXML private Label lblFinalAmount;
    @FXML private TextField txtPaymentAmount;
    @FXML private ComboBox<Payment.PaymentMethod> cboPaymentMethod;
    @FXML private TextArea txtNotes;
    
    private MemberService memberService = new MemberService();
    private PackageService packageService = new PackageService();
    private RegistrationService registrationService = new RegistrationService();
    private PaymentDAO paymentDAO = new PaymentDAO();
    
    private ObservableList<Registration> registrationList = FXCollections.observableArrayList();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private Member selectedMember = null;
    private com.gym.entity.Package selectedPackage = null;
    
    @FXML
    public void initialize() {
        System.out.println("✅ RegistrationController initialized");
        
        // Setup filter
        cboFilterStatus.setItems(FXCollections.observableArrayList(
            "Tất cả", "Đang hoạt động", "Hết hạn", "Đóng băng", "Đã hủy"
        ));
        cboFilterStatus.setValue("Tất cả");
        
        // Setup payment method
        cboPaymentMethod.setItems(FXCollections.observableArrayList(Payment.PaymentMethod.values()));
        cboPaymentMethod.setValue(Payment.PaymentMethod.CASH);
        
        // Setup date picker
        dpStartDate.setValue(LocalDate.now());
        
        // Setup table
        setupTable();
        
        // Load packages
        loadPackages();
        
        // Load registrations
        loadRegistrations();
    }
    
    private void setupTable() {
        colRegCode.setCellValueFactory(new PropertyValueFactory<>("registrationCode"));
        
        colMemberName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMember().getFullName())
        );
        
        colPackageName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPkg().getPackageName())
        );
        
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStartDate.setCellFactory(col -> new TableCell<Registration, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
        
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colEndDate.setCellFactory(col -> new TableCell<Registration, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(dateFormatter));
            }
        });
        
        colFinalAmount.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colFinalAmount.setCellFactory(col -> new TableCell<Registration, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        
        colPaidAmount.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colPaidAmount.setCellFactory(col -> new TableCell<Registration, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        
        colPaymentStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus().toString())
        );
        colPaymentStatus.setCellFactory(col -> new TableCell<Registration, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (item) {
                        case "PAID" -> {
                            setText("Đã thanh toán");
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                        case "PARTIAL" -> {
                            setText("Chưa đủ");
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        }
                        case "PENDING" -> {
                            setText("Chưa trả");
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            }
        });
        
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString())
        );
        colStatus.setCellFactory(col -> new TableCell<Registration, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (item) {
                        case "ACTIVE" -> {
                            setText("Hoạt động");
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                        case "EXPIRED" -> {
                            setText("Hết hạn");
                            setStyle("-fx-text-fill: #e74c3c;");
                        }
                        case "FROZEN" -> {
                            setText("Đóng băng");
                            setStyle("-fx-text-fill: #3498db;");
                        }
                        case "CANCELLED" -> {
                            setText("Đã hủy");
                            setStyle("-fx-text-fill: #95a5a6;");
                        }
                    }
                }
            }
        });
        
        tblRegistrations.setItems(registrationList);
    }
    
    private void loadRegistrations() {
        try {
            registrationList.clear();
            List<Registration> registrations = registrationService.getAllRegistrations();
            registrationList.addAll(registrations);
            System.out.println("✅ Đã load " + registrations.size() + " đăng ký");
        } catch (Exception e) {
            System.err.println("❌ Lỗi load registrations: " + e.getMessage());
            AlertUtil.showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    private void loadPackages() {
        try {
            List<com.gym.entity.Package> packages = packageService.getActivePackages();
            cboPackage.setItems(FXCollections.observableArrayList(packages));
            
            // Custom cell factory to display package name
            cboPackage.setCellFactory(param -> new ListCell<com.gym.entity.Package>() {
                @Override
                protected void updateItem(com.gym.entity.Package item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getPackageName() + " - " + currencyFormat.format(item.getFinalPrice()));
                    }
                }
            });
            
            cboPackage.setButtonCell(new ListCell<com.gym.entity.Package>() {
                @Override
                protected void updateItem(com.gym.entity.Package item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getPackageName() + " - " + currencyFormat.format(item.getFinalPrice()));
                    }
                }
            });
            
        } catch (Exception e) {
            AlertUtil.showError("Lỗi load packages: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleSearchMember() {
        String search = txtMemberSearch.getText().trim();
        
        if (search.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập mã hội viên hoặc số điện thoại!");
            return;
        }
        
        try {
            Optional<Member> memberOpt = memberService.getMemberByCode(search);
            if (memberOpt.isEmpty()) {
                memberOpt = memberService.getMemberByPhone(search);
            }
            
            if (memberOpt.isEmpty()) {
                AlertUtil.showError("Không tìm thấy hội viên!");
                selectedMember = null;
                pnlMemberInfo.setVisible(false);
                return;
            }
            
            selectedMember = memberOpt.get();
            lblSelectedMember.setText("Họ tên: " + selectedMember.getFullName());
            lblSelectedMemberCode.setText("Mã HV: " + selectedMember.getMemberCode());
            lblSelectedMemberPhone.setText("SĐT: " + selectedMember.getPhone());
            pnlMemberInfo.setVisible(true);
            
            System.out.println("✅ Đã chọn member: " + selectedMember.getFullName());
            
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handlePackageChange() {
        selectedPackage = cboPackage.getValue();
        
        if (selectedPackage == null) {
            pnlPackageInfo.setVisible(false);
            return;
        }
        
        lblPackageDescription.setText("Mô tả: " + selectedPackage.getDescription());
        lblPackageDuration.setText("Thời hạn: " + selectedPackage.getDurationMonths() + " tháng (" + 
                                   selectedPackage.getDurationDays() + " ngày)");
        lblPackagePrice.setText("Giá gốc: " + currencyFormat.format(selectedPackage.getPrice()));
        lblPackageDiscount.setText("Giảm giá: " + selectedPackage.getDiscountPercent() + "%");
        pnlPackageInfo.setVisible(true);
        
        calculateAmount();
        calculateEndDate();
    }
    
    @FXML
    public void handleDateChange() {
        calculateEndDate();
    }
    
    private void calculateEndDate() {
        if (selectedPackage == null || dpStartDate.getValue() == null) {
            lblEndDate.setText("Ngày kết thúc: ---");
            return;
        }
        
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = startDate.plusDays(selectedPackage.getDurationDays());
        lblEndDate.setText("Ngày kết thúc: " + endDate.format(dateFormatter));
    }
    
    private void calculateAmount() {
        if (selectedPackage == null) {
            lblTotalAmount.setText("0 ₫");
            lblDiscountAmount.setText("0 ₫");
            lblFinalAmount.setText("0 ₫");
            return;
        }
        
        BigDecimal totalAmount = selectedPackage.getPrice();
        BigDecimal discountAmount = totalAmount.multiply(selectedPackage.getDiscountPercent())
                                              .divide(new BigDecimal(100));
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        
        lblTotalAmount.setText(currencyFormat.format(totalAmount));
        lblDiscountAmount.setText(currencyFormat.format(discountAmount));
        lblFinalAmount.setText(currencyFormat.format(finalAmount));
        
        // Auto fill payment amount
        txtPaymentAmount.setText(finalAmount.toString());
    }
    
    @FXML
    public void handleSave() {
        // Validation
        if (selectedMember == null) {
            AlertUtil.showError("Vui lòng chọn hội viên!");
            txtMemberSearch.requestFocus();
            return;
        }
        
        if (selectedPackage == null) {
            AlertUtil.showError("Vui lòng chọn gói tập!");
            cboPackage.requestFocus();
            return;
        }
        
        if (dpStartDate.getValue() == null) {
            AlertUtil.showError("Vui lòng chọn ngày bắt đầu!");
            dpStartDate.requestFocus();
            return;
        }
        
        String paymentAmountStr = txtPaymentAmount.getText().trim();
        if (!ValidationUtil.isPositiveNumber(paymentAmountStr)) {
            AlertUtil.showError("Số tiền thanh toán không hợp lệ!");
            txtPaymentAmount.requestFocus();
            return;
        }
        
        try {
            BigDecimal paymentAmount = new BigDecimal(paymentAmountStr);
            
            // Create registration
            registrationService.createRegistration(
                selectedMember, 
                selectedPackage, 
                dpStartDate.getValue(), 
                SessionManager.getInstance().getCurrentUser()
            );
            
            // Get the newly created registration
            Optional<Registration> regOpt = registrationService.getActiveRegistration(selectedMember.getId());
            
            if (regOpt.isPresent()) {
                Registration registration = regOpt.get();
                
                // Create payment if amount > 0
                if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Payment payment = new Payment();
                    payment.setPaymentCode(generatePaymentCode());
                    payment.setRegistration(registration);
                    payment.setAmount(paymentAmount);
                    payment.setPaymentMethod(cboPaymentMethod.getValue());
                    payment.setNotes(txtNotes.getText());
                    payment.setProcessedBy(SessionManager.getInstance().getCurrentUser());
                    
                    paymentDAO.save(payment);
                    
                    // Update registration payment status
                    registrationService.updatePaymentStatus(registration, paymentAmount);
                }
                
                AlertUtil.showSuccess("Đăng ký gói tập thành công!\n" +
                    "Hội viên: " + selectedMember.getFullName() + "\n" +
                    "Gói: " + selectedPackage.getPackageName() + "\n" +
                    "Đã thanh toán: " + currencyFormat.format(paymentAmount));
                
                handleNew();
                loadRegistrations();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi save: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    private String generatePaymentCode() {
        return "PAY" + System.currentTimeMillis();
    }
    
    @FXML
    public void handleNew() {
        selectedMember = null;
        selectedPackage = null;
        
        txtMemberSearch.clear();
        pnlMemberInfo.setVisible(false);
        
        cboPackage.setValue(null);
        pnlPackageInfo.setVisible(false);
        
        dpStartDate.setValue(LocalDate.now());
        lblEndDate.setText("Ngày kết thúc: ---");
        
        lblTotalAmount.setText("0 ₫");
        lblDiscountAmount.setText("0 ₫");
        lblFinalAmount.setText("0 ₫");
        
        txtPaymentAmount.clear();
        cboPaymentMethod.setValue(Payment.PaymentMethod.CASH);
        txtNotes.clear();
        
        txtMemberSearch.requestFocus();
    }
    
    @FXML
    public void handleFilter() {
        String filter = cboFilterStatus.getValue();
        
        try {
            registrationList.clear();
            List<Registration> all = registrationService.getAllRegistrations();
            
            List<Registration> filtered = all.stream().filter(reg -> {
                if (filter.equals("Tất cả")) return true;
                return switch (filter) {
                    case "Đang hoạt động" -> reg.getStatus() == Registration.RegistrationStatus.ACTIVE;
                    case "Hết hạn" -> reg.getStatus() == Registration.RegistrationStatus.EXPIRED;
                    case "Đóng băng" -> reg.getStatus() == Registration.RegistrationStatus.FROZEN;
                    case "Đã hủy" -> reg.getStatus() == Registration.RegistrationStatus.CANCELLED;
                    default -> true;
                };
            }).toList();
            
            registrationList.addAll(filtered);
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRefresh() {
        loadRegistrations();
        cboFilterStatus.setValue("Tất cả");
        AlertUtil.showSuccess("Đã làm mới danh sách đăng ký!");
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