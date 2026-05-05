package com.gym.controller;

import com.gym.App;
import com.gym.entity.Package;
import com.gym.service.PackageService;
import com.gym.service.RegistrationService;
import com.gym.util.AlertUtil;
import com.gym.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PackageController {
    
    @FXML private TextField txtPackageName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtDurationMonths;
    @FXML private TextField txtDurationDays;
    @FXML private TextField txtPrice;
    @FXML private TextField txtDiscountPercent;
    @FXML private TextField txtMaxFreezeDays;
    @FXML private TextArea txtFeatures;
    @FXML private CheckBox chkIsActive;
    
    @FXML private TableView<Package> tblPackages;
    @FXML private TableColumn<Package, String> colPackageCode;
    @FXML private TableColumn<Package, String> colPackageName;
    @FXML private TableColumn<Package, String> colDuration;
    @FXML private TableColumn<Package, BigDecimal> colPrice;
    @FXML private TableColumn<Package, BigDecimal> colDiscount;
    @FXML private TableColumn<Package, BigDecimal> colFinalPrice;
    @FXML private TableColumn<Package, Boolean> colIsActive;
    
    private PackageService packageService = new PackageService();
    private RegistrationService registrationService = new RegistrationService();
    private ObservableList<Package> packageList = FXCollections.observableArrayList();
    private Package selectedPackage = null;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    @FXML
    public void initialize() {
        System.out.println("✅ PackageController initialized");
        
        // Setup TableView
        colPackageCode.setCellValueFactory(new PropertyValueFactory<>("packageCode"));
        colPackageName.setCellValueFactory(new PropertyValueFactory<>("packageName"));
        
        colDuration.setCellValueFactory(cellData -> {
            Package pkg = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                pkg.getDurationMonths() + " tháng (" + pkg.getDurationDays() + " ngày)"
            );
        });
        
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(col -> new TableCell<Package, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });
        
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));
        colDiscount.setCellFactory(col -> new TableCell<Package, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item + "%");
                }
            }
        });
        
        colFinalPrice.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getFinalPrice())
        );
        colFinalPrice.setCellFactory(col -> new TableCell<Package, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });
        
        colIsActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        colIsActive.setCellFactory(col -> new TableCell<Package, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Hoạt động" : "Ngừng");
                    setStyle(item ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
                }
            }
        });
        
        tblPackages.setItems(packageList);
        
        // Selection listener
        tblPackages.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedPackage = newVal;
                    displayPackageInfo(newVal);
                }
            }
        );
        
        loadPackages();
    }
    
    private void loadPackages() {
        try {
            packageList.clear();
            List<Package> packages = packageService.getAllPackages();
            packageList.addAll(packages);
            System.out.println("✅ Đã load " + packages.size() + " gói tập");
        } catch (Exception e) {
            System.err.println("❌ Lỗi load packages: " + e.getMessage());
            AlertUtil.showError("Lỗi khi tải danh sách gói tập: " + e.getMessage());
        }
    }
    
    private void displayPackageInfo(Package pkg) {
        txtPackageName.setText(pkg.getPackageName());
        txtDescription.setText(pkg.getDescription());
        txtDurationMonths.setText(String.valueOf(pkg.getDurationMonths()));
        txtDurationDays.setText(String.valueOf(pkg.getDurationDays()));
        txtPrice.setText(pkg.getPrice().toString());
        txtDiscountPercent.setText(pkg.getDiscountPercent().toString());
        txtMaxFreezeDays.setText(String.valueOf(pkg.getMaxFreezeDays()));
        txtFeatures.setText(pkg.getFeatures());
        chkIsActive.setSelected(pkg.getIsActive());
    }
    
    private void clearForm() {
        txtPackageName.clear();
        txtDescription.clear();
        txtDurationMonths.clear();
        txtDurationDays.clear();
        txtPrice.clear();
        txtDiscountPercent.setText("0");
        txtMaxFreezeDays.setText("0");
        txtFeatures.clear();
        chkIsActive.setSelected(true);
        
        selectedPackage = null;
        tblPackages.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void handleSave() {
        // Validation
        String packageName = txtPackageName.getText().trim();
        String durationMonthsStr = txtDurationMonths.getText().trim();
        String durationDaysStr = txtDurationDays.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String discountStr = txtDiscountPercent.getText().trim();
        String maxFreezeDaysStr = txtMaxFreezeDays.getText().trim();
        
        if (ValidationUtil.isEmpty(packageName)) {
            AlertUtil.showError("Tên gói không được để trống!");
            return;
        }
        
        if (!ValidationUtil.isPositiveNumber(durationMonthsStr) || 
            !ValidationUtil.isPositiveNumber(durationDaysStr) ||
            !ValidationUtil.isPositiveNumber(priceStr)) {
            AlertUtil.showError("Thời hạn và giá phải là số dương!");
            return;
        }
        
        try {
            int durationMonths = Integer.parseInt(durationMonthsStr);
            int durationDays = Integer.parseInt(durationDaysStr);
            BigDecimal price = new BigDecimal(priceStr);
            BigDecimal discount = new BigDecimal(discountStr.isEmpty() ? "0" : discountStr);
            int maxFreezeDays = Integer.parseInt(maxFreezeDaysStr.isEmpty() ? "0" : maxFreezeDaysStr);
            
            if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal(100)) > 0) {
                AlertUtil.showError("Giảm giá phải từ 0-100%!");
                return;
            }
            
            if (selectedPackage == null) {
                // Create new
                Package pkg = new Package();
                pkg.setPackageName(packageName);
                pkg.setDescription(txtDescription.getText());
                pkg.setDurationMonths(durationMonths);
                pkg.setDurationDays(durationDays);
                pkg.setPrice(price);
//                pkg.setDiscountPercent(discount);
                pkg.setMaxFreezeDays(maxFreezeDays);
                pkg.setFeatures(txtFeatures.getText());
                pkg.setIsActive(chkIsActive.isSelected());
                
                packageService.createPackage(pkg);
                AlertUtil.showSuccess("Thêm gói tập thành công!");
            } else {
                // Update
                selectedPackage.setPackageName(packageName);
                selectedPackage.setDescription(txtDescription.getText());
                selectedPackage.setDurationMonths(durationMonths);
                selectedPackage.setDurationDays(durationDays);
                selectedPackage.setPrice(price);
//                selectedPackage.setDiscountPercent(discount);
                selectedPackage.setMaxFreezeDays(maxFreezeDays);
                selectedPackage.setFeatures(txtFeatures.getText());
                selectedPackage.setIsActive(chkIsActive.isSelected());
                
                packageService.updatePackage(selectedPackage);
                AlertUtil.showSuccess("Cập nhật gói tập thành công!");
            }
            
            clearForm();
            loadPackages();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Định dạng số không hợp lệ!");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleDelete() {
        if (selectedPackage == null) {
            AlertUtil.showError("Vui lòng chọn gói tập cần xóa!");
            return;
        }
        
        // Kiểm tra xem gói tập có đang được sử dụng không
        try {
            long usageCount = registrationService.getAllRegistrations().stream()
                .filter(r -> r.getPkg().getId().equals(selectedPackage.getId()))
                .count();
            
            if (usageCount > 0) {
                AlertUtil.showWarning(
                    "Không thể xóa gói tập", 
                    "Gói tập đang được sử dụng",
                    "Gói tập \"" + selectedPackage.getPackageName() + "\" đang được " + 
                    usageCount + " khách hàng sử dụng.\n\n" +
                    "Bạn có thể:\n" +
                    "• Đặt trạng thái gói tập thành 'Ngừng hoạt động'\n" +
                    "• Đợi các đăng ký hết hạn rồi mới xóa"
                );
                return;
            }
            
            // Nếu không có ai dùng, cho phép xóa
            if (AlertUtil.confirmDelete(selectedPackage.getPackageName())) {
                packageService.deletePackage(selectedPackage.getId());
                AlertUtil.showSuccess("Xóa gói tập thành công!");
                clearForm();
                loadPackages();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi kiểm tra/xóa gói tập: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Lỗi: Không thể xóa gói tập này vì đang có dữ liệu liên quan.");
        }
    }
    
    @FXML
    public void handleNew() {
        clearForm();
        txtPackageName.requestFocus();
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