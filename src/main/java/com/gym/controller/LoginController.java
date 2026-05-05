// ============= LoginController.java =============
package com.gym.controller;

import com.gym.App;
import com.gym.dao.UserDAO;
import com.gym.entity.User;
import com.gym.util.AlertUtil;
import com.gym.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController { // Xử lý logic điều khiển login
    
    @FXML
    private TextField txtUsername;
    
    @FXML
    private PasswordField txtPassword;
    
    private UserDAO userDAO = new UserDAO();
    
    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập đầy đủ thông tin đăng nhập!");
            return;
        }
        
        Optional<User> userOpt = userDAO.findByUsername(username);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            SessionManager.getInstance().setCurrentUser(user);
            
            try {
                App.changeScene("Dashboard.fxml");
            } catch (Exception e) {
                AlertUtil.showError("Lỗi khi chuyển màn hình: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            AlertUtil.showError("Tên đăng nhập hoặc mật khẩu không đúng!");
            txtPassword.clear();
        }
    }
    
    @FXML
    public void initialize() {
        // Set default credentials for testing
        txtUsername.setText("admin");
        txtPassword.setText("123456");
    }
}