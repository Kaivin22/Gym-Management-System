package com.gym.controller;

import com.gym.App;
import com.gym.entity.Promotion;
import com.gym.service.PromotionService;
import com.gym.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PromotionController {
    
    // TableView và các cột
    @FXML private TableView<Promotion> tblPromotions;
    @FXML private TableColumn<Promotion, String> colPromotionCode;
    @FXML private TableColumn<Promotion, String> colPromotionName;
    @FXML private TableColumn<Promotion, String> colPromotionType;
    @FXML private TableColumn<Promotion, String> colDiscount;
    @FXML private TableColumn<Promotion, LocalDate> colStartDate;
    @FXML private TableColumn<Promotion, LocalDate> colEndDate;
    @FXML private TableColumn<Promotion, Integer> colUsageCount;
    @FXML private TableColumn<Promotion, Boolean> colIsActive;
    
    // Form controls
    @FXML private TextField txtPromotionCode;
    @FXML private TextField txtPromotionName;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cboPromotionType;
    @FXML private ComboBox<String> cboDiscountType;
    @FXML private TextField txtDiscountValue;
    @FXML private TextField txtMinOrderAmount;
    @FXML private TextField txtMaxDiscountAmount;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtUsageLimit;
    @FXML private ComboBox<String> cboApplicableFor;
    @FXML private CheckBox chkIsActive;
    
    private PromotionService promotionService = new PromotionService();
    private ObservableList<Promotion> promotionList = FXCollections.observableArrayList();
    private Promotion selectedPromotion = null;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("✅ PromotionController initialized");
            
            // Khởi tạo ComboBoxes
            initializeComboBoxes();
            
            // Thiết lập TableView
            setupTableView();
            
            // Thiết lập DatePickers với giá trị mặc định
            dpStartDate.setValue(LocalDate.now());
            dpEndDate.setValue(LocalDate.now().plusMonths(1));
            
            // Tự động sinh mã
            handleGenerateCode();
            
            // Tải dữ liệu
            loadPromotions();
            
            // Thiết lập selection listener
            tblPromotions.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadPromotionToForm(newSelection);
                    }
                }
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khởi tạo: " + e.getMessage());
        }
    }
    
    private void initializeComboBoxes() {
        // Loại khuyến mãi
        ObservableList<String> promotionTypes = FXCollections.observableArrayList(
            "Giảm giá phần trăm",
            "Giảm giá số tiền",
            "Miễn phí vận chuyển",
            "Mua 1 tặng 1",
            "Voucher giảm giá"
        );
        cboPromotionType.setItems(promotionTypes);
        cboPromotionType.setValue("Giảm giá phần trăm");
        
        // Loại giảm giá
        ObservableList<String> discountTypes = FXCollections.observableArrayList(
            "Phần trăm (%)",
            "Số tiền (VNĐ)"
        );
        cboDiscountType.setItems(discountTypes);
        cboDiscountType.setValue("Phần trăm (%)");
        
        // Áp dụng cho
        ObservableList<String> applicableFor = FXCollections.observableArrayList(
            "Tất cả",
            "Thành viên mới",
            "Thành viên VIP",
            "Gói tập cao cấp",
            "Sản phẩm cụ thể"
        );
        cboApplicableFor.setItems(applicableFor);
        cboApplicableFor.setValue("Tất cả");
    }
    
    private void setupTableView() {
        // Thiết lập các cột TableView
        colPromotionCode.setCellValueFactory(new PropertyValueFactory<>("promoCode"));
        
        colPromotionName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription() != null ? 
                cellData.getValue().getDescription() : ""
            )
        );
        
        colPromotionType.setCellValueFactory(cellData -> {
            Double discountPercent = cellData.getValue().getDiscountPercent();
            if (discountPercent != null) {
                return new javafx.beans.property.SimpleStringProperty("Giảm %");
            } else {
                return new javafx.beans.property.SimpleStringProperty("Giảm tiền");
            }
        });
        
        colDiscount.setCellValueFactory(cellData -> {
            Promotion p = cellData.getValue();
            String discount = "";
            if (p.getDiscountPercent() != null) {
                discount = p.getDiscountPercent() + "%";
            } else if (p.getDiscountAmount() != null) {
                discount = String.format("%,.0f VNĐ", p.getDiscountAmount().doubleValue());
            }
            return new javafx.beans.property.SimpleStringProperty(discount);
        });
        
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStartDate.setCellFactory(column -> new TableCell<Promotion, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colEndDate.setCellFactory(column -> new TableCell<Promotion, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        
        colUsageCount.setCellValueFactory(new PropertyValueFactory<>("currentUsage"));
        colUsageCount.setCellFactory(column -> new TableCell<Promotion, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "0" : item.toString());
            }
        });
        
        colIsActive.setCellValueFactory(new PropertyValueFactory<>("isActive"));
        colIsActive.setCellFactory(column -> new TableCell<Promotion, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Hoạt động" : "Ngừng");
                    setStyle(item ? 
                        "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : 
                        "-fx-text-fill: #e74c3c;");
                }
            }
        });
        
        tblPromotions.setItems(promotionList);
    }
    
    private void loadPromotions() {
        try {
            promotionList.clear();
            List<Promotion> promotions = promotionService.getAllPromotions();
            promotionList.addAll(promotions);
            System.out.println("✅ Đã tải " + promotions.size() + " khuyến mãi");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi tải danh sách khuyến mãi: " + e.getMessage());
        }
    }
    
    private void loadPromotionToForm(Promotion promotion) {
    	this.selectedPromotion = promotion;
        
        txtPromotionCode.setText(promotion.getPromoCode());
        
        // Sử dụng getPromoName() thay vì getPromotionName()
        String promoName = promotion.getPromoName(); // SỬA
        txtPromotionName.setText(promoName != null ? promoName : "");
        
        txtDescription.setText(promotion.getDescription());
        
        // Xác định loại khuyến mãi
        if (promotion.getDiscountPercent() != null) {
            cboPromotionType.setValue("Giảm giá phần trăm");
            cboDiscountType.setValue("Phần trăm (%)");
            txtDiscountValue.setText(promotion.getDiscountPercent().toString());
        } else if (promotion.getDiscountAmount() != null) {
            cboPromotionType.setValue("Giảm giá số tiền");
            cboDiscountType.setValue("Số tiền (VNĐ)");
            txtDiscountValue.setText(promotion.getDiscountAmount().toString());
        }
        
        // Các trường khác (có thể cần thêm vào Entity)
        txtMinOrderAmount.setText("");
        txtMaxDiscountAmount.setText("");
        
        dpStartDate.setValue(promotion.getStartDate());
        dpEndDate.setValue(promotion.getEndDate());
        
        if (promotion.getMaxUsage() != null) {
            txtUsageLimit.setText(promotion.getMaxUsage().toString());
        } else {
            txtUsageLimit.setText("");
        }
        
        cboApplicableFor.setValue("Tất cả");
        chkIsActive.setSelected(promotion.getIsActive() != null ? 
            promotion.getIsActive() : true);
    }
    
    @FXML
    private void handleGenerateCode() {
        try {
            // Tạo mã khuyến mãi tự động: PROMO + timestamp
            String code = "PROMO" + System.currentTimeMillis() % 10000;
            txtPromotionCode.setText(code);
        } catch (Exception e) {
            AlertUtil.showError("Lỗi tạo mã: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNew() {
        clearForm();
        handleGenerateCode();
        tblPromotions.getSelectionModel().clearSelection();
        selectedPromotion = null;
    }
    
    @FXML
    private void handleSave() {
        try {
        	// VALIDATION
            String promoName = txtPromotionName.getText().trim(); // Vẫn giữ tên biến cho Controller
            if (promoName.isEmpty()) {
                AlertUtil.showError("Tên khuyến mãi không được để trống!");
                txtPromotionName.requestFocus();
                return;
            }
            
            String promoCode = txtPromotionCode.getText().trim();
            if (promoCode.isEmpty()) {
                AlertUtil.showError("Mã khuyến mãi không được để trống!");
                txtPromotionCode.requestFocus();
                return;
            }
            
            if (txtDiscountValue.getText().trim().isEmpty()) {
                AlertUtil.showError("Giá trị giảm giá không được để trống!");
                txtDiscountValue.requestFocus();
                return;
            }
            
            // Parse các giá trị
            Double discountPercent = null;
            BigDecimal discountAmount = null;
            BigDecimal discountValue = null; // THÊM BIẾN NÀY
            
            String discountType = cboDiscountType.getValue();
            String discountValueStr = txtDiscountValue.getText().trim();
            
            try {
                if ("Phần trăm (%)".equals(discountType)) {
                    discountPercent = Double.parseDouble(discountValueStr);
                    discountValue = new BigDecimal(discountValueStr); // SET discountValue
                    if (discountPercent < 0 || discountPercent > 100) {
                        AlertUtil.showError("Phần trăm giảm giá phải từ 0 đến 100!");
                        return;
                    }
                } else {
                    discountAmount = new BigDecimal(discountValueStr);
                    discountValue = discountAmount; // SET discountValue
                    if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                        AlertUtil.showError("Số tiền giảm giá phải >= 0!");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                AlertUtil.showError("Giá trị giảm giá không hợp lệ!");
                return;
            }
            
            // Parse các giá trị khác...
            BigDecimal minOrderAmount = null;
            if (!txtMinOrderAmount.getText().trim().isEmpty()) {
                try {
                    minOrderAmount = new BigDecimal(txtMinOrderAmount.getText().trim());
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Đơn hàng tối thiểu không hợp lệ!");
                    return;
                }
            }
            
            BigDecimal maxDiscountAmount = null;
            if (!txtMaxDiscountAmount.getText().trim().isEmpty()) {
                try {
                    maxDiscountAmount = new BigDecimal(txtMaxDiscountAmount.getText().trim());
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Giảm tối đa không hợp lệ!");
                    return;
                }
            }
            
            Integer maxUsage = null;
            if (!txtUsageLimit.getText().trim().isEmpty()) {
                try {
                    maxUsage = Integer.parseInt(txtUsageLimit.getText().trim());
                    if (maxUsage < 0) {
                        AlertUtil.showError("Lượt dùng tối đa phải >= 0!");
                        return;
                    }
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Lượt dùng tối đa không hợp lệ!");
                    return;
                }
            }
            
            // Kiểm tra ngày tháng
            LocalDate startDate = dpStartDate.getValue();
            LocalDate endDate = dpEndDate.getValue();
            
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                AlertUtil.showError("Ngày kết thúc phải sau ngày bắt đầu!");
                return;
            }
            
            boolean isActive = chkIsActive.isSelected();
            
            String description = txtDescription.getText();
            String promotionType = cboPromotionType.getValue();
            String applicableFor = cboApplicableFor.getValue();
            
            // Nếu là cập nhật khuyến mãi đã chọn
            if (selectedPromotion != null) {
                selectedPromotion.setPromoCode(promoCode);
                selectedPromotion.setPromoName(promoName); 
                selectedPromotion.setDescription(description);
                selectedPromotion.setPromotionType(promotionType);
                selectedPromotion.setDiscountType(discountType);
                selectedPromotion.setDiscountPercent(discountPercent);
                selectedPromotion.setDiscountAmount(discountAmount);
                selectedPromotion.setDiscountValue(discountValue);
                selectedPromotion.setMinOrderAmount(minOrderAmount);
                selectedPromotion.setMaxDiscountAmount(maxDiscountAmount);
                selectedPromotion.setStartDate(startDate);
                selectedPromotion.setEndDate(endDate);
                selectedPromotion.setMaxUsage(maxUsage);
                selectedPromotion.setApplicableFor(applicableFor);
                selectedPromotion.setIsActive(isActive);
                
                // Gọi phương thức cập nhật
                promotionService.updatePromotion(selectedPromotion);
                
                AlertUtil.showSuccess("Đã cập nhật khuyến mãi thành công!");
            } 
            // Nếu là tạo mới
            else {
                Promotion newPromotion = promotionService.createPromotion(
                    promoCode,
                    promoName,
                    description,
                    discountPercent,
                    discountAmount,
                    discountValue,
                    startDate,
                    endDate,
                    maxUsage,
                    isActive
                );
                
                AlertUtil.showSuccess("Đã tạo khuyến mãi thành công!");
            }
            
            // Làm mới danh sách và form
            loadPromotions();
            clearForm();
            handleGenerateCode();
            
        } catch (IllegalArgumentException e) {
            AlertUtil.showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        try {
            Promotion selected = tblPromotions.getSelectionModel().getSelectedItem();
            
            if (selected == null) {
                AlertUtil.showError("Vui lòng chọn khuyến mãi cần xóa!");
                return;
            }
            
            if (AlertUtil.showConfirm("Xác nhận xóa", 
                "Xóa khuyến mãi " + selected.getPromoCode(), 
                "Bạn có chắc chắn muốn xóa khuyến mãi này?\nHành động này không thể hoàn tác.")) {
                
                promotionService.deletePromotion(selected.getId());
                AlertUtil.showSuccess("Đã xóa khuyến mãi thành công!");
                
                loadPromotions();
                clearForm();
                handleGenerateCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi xóa: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            App.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtPromotionName.clear();
        txtDescription.clear();
        txtDiscountValue.clear();
        txtMinOrderAmount.clear();
        txtMaxDiscountAmount.clear();
        dpStartDate.setValue(LocalDate.now());
        dpEndDate.setValue(LocalDate.now().plusMonths(1));
        txtUsageLimit.clear();
        cboApplicableFor.setValue("Tất cả");
        chkIsActive.setSelected(true);
        selectedPromotion = null;
    }
}