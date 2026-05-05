package com.gym.controller;

import com.gym.App;
import com.gym.entity.Attendance;
import com.gym.entity.Member;
import com.gym.service.AttendanceService;
import com.gym.service.MemberService;
import com.gym.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AttendanceController {
    
    @FXML private Label lblCurrentTime;
    @FXML private TextField txtMemberSearch;
    @FXML private HBox pnlMemberInfo;
    @FXML private Label lblMemberName;
    @FXML private Label lblMemberCode;
    @FXML private Label lblMemberPhone;
    @FXML private Label lblTodayCount;
    
    @FXML private TableView<Attendance> tblAttendance;
    @FXML private TableColumn<Attendance, String> colMemberCode;
    @FXML private TableColumn<Attendance, String> colMemberName;
    @FXML private TableColumn<Attendance, LocalDateTime> colCheckInTime;
    
    private AttendanceService attendanceService = new AttendanceService();
    private MemberService memberService = new MemberService();
    private ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();
    
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @FXML
    public void initialize() {
        // Setup clock
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblCurrentTime.setText(LocalDateTime.now().format(timeFormatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        
        // Setup TableView
        colMemberCode.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMember().getMemberCode())
        );
        colMemberName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMember().getFullName())
        );
        colCheckInTime.setCellValueFactory(new PropertyValueFactory<>("checkInTime"));
        colCheckInTime.setCellFactory(col -> new TableCell<Attendance, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(timeFormatter));
            }
        });
        
        tblAttendance.setItems(attendanceList);
        loadTodayAttendance();
    }
    
    private void loadTodayAttendance() {
        try {
            attendanceList.clear();
            List<Attendance> attendances = attendanceService.getAttendancesByDate(LocalDate.now());
            attendanceList.addAll(attendances);
            lblTodayCount.setText("Tổng: " + attendances.size() + " lượt");
        } catch (Exception e) {
            AlertUtil.showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleCheckIn() {
        String search = txtMemberSearch.getText().trim();
        
        if (search.isEmpty()) {
            AlertUtil.showError("Vui lòng nhập mã hội viên hoặc số điện thoại!");
            txtMemberSearch.requestFocus();
            return;
        }
        
        try {
            // Find member
            Optional<Member> memberOpt = memberService.getMemberByCode(search);
            if (memberOpt.isEmpty()) {
                memberOpt = memberService.getMemberByPhone(search);
            }
            
            if (memberOpt.isEmpty()) {
                AlertUtil.showError("Không tìm thấy hội viên!");
                txtMemberSearch.clear();
                txtMemberSearch.requestFocus();
                return;
            }
            
            Member currentMember = memberOpt.get();
            
            // Display member info
            lblMemberName.setText("Họ tên: " + currentMember.getFullName());
            lblMemberCode.setText("Mã HV: " + currentMember.getMemberCode());
            lblMemberPhone.setText("SĐT: " + currentMember.getPhone());
            pnlMemberInfo.setVisible(true);
            
            // Confirm check-in
            if (AlertUtil.showConfirm("Xác nhận", "Check-in cho hội viên", 
                "Xác nhận check-in cho:\n" + currentMember.getFullName() + " (" + currentMember.getMemberCode() + ")")) {
                
                attendanceService.checkIn(currentMember);
                AlertUtil.showSuccess("Check-in thành công!\nChào mừng " + currentMember.getFullName());
                
                txtMemberSearch.clear();
                pnlMemberInfo.setVisible(false);
                loadTodayAttendance();
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleRefresh() {
        loadTodayAttendance();
        pnlMemberInfo.setVisible(false);
        txtMemberSearch.clear();
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