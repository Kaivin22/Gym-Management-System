// ============= AlertUtil.java =============
package com.gym.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AlertUtil {
    
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void showSuccess(String message) {
        showInfo("Thành công", null, message);
    }
    
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void showError(String message) {
        showError("Lỗi", null, message);
    }
    
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void showWarning(String message) {
        showWarning("Cảnh báo", null, message);
    }
    
    public static boolean showConfirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    public static boolean confirmDelete(String itemName) {
        return showConfirm(
            "Xác nhận xóa", 
            "Bạn có chắc chắn muốn xóa?", 
            "Xóa: " + itemName
        );
    }
}