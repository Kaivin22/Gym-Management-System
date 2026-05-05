//file member
package com.gym.controller;

import com.gym.App;
import com.gym.entity.Member;
import com.gym.service.MemberService;
import com.gym.service.RegistrationService;
import com.gym.util.AlertUtil;
import com.gym.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class MemberController {
    
    @FXML private TextField txtSearch;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtIdCard;
    @FXML private TextField txtEmergencyContact;
    @FXML private TextField txtEmergencyPhone;
    @FXML private TextArea txtHealthNotes;
    @FXML private ComboBox<Member.Gender> cboGender;
    @FXML private ComboBox<Member.MemberStatus> cboStatus;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private DatePicker dpJoinDate;
    
    @FXML private TableView<Member> tblMembers;
    @FXML private TableColumn<Member, String> colMemberCode;
    @FXML private TableColumn<Member, String> colFullName;
    @FXML private TableColumn<Member, String> colGender;
    @FXML private TableColumn<Member, String> colPhone;
    @FXML private TableColumn<Member, String> colEmail;
    @FXML private TableColumn<Member, String> colStatus;
    @FXML private TableColumn<Member, LocalDate> colJoinDate;
    
    private MemberService memberService = new MemberService();
    private RegistrationService registrationService = new RegistrationService();
    private ObservableList<Member> memberList = FXCollections.observableArrayList();
    private Member selectedMember = null;
    
    @FXML
    public void initialize() {
        System.out.println("✅ MemberController initialized");
        
        // Setup ComboBox
        cboGender.setItems(FXCollections.observableArrayList(Member.Gender.values()));
        cboStatus.setItems(FXCollections.observableArrayList(Member.MemberStatus.values()));
        cboStatus.setValue(Member.MemberStatus.ACTIVE);
        
        // Setup TableView
        colMemberCode.setCellValueFactory(new PropertyValueFactory<>("memberCode"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colJoinDate.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        
        tblMembers.setItems(memberList);
        
        // Selection listener
        tblMembers.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedMember = newValue;
                    displayMemberInfo(newValue);
                }
            }
        );
        
        
        dpJoinDate.setValue(LocalDate.now());
        
        loadMembers();
    }
    
    private void loadMembers() {
        try {
            memberList.clear();
            List<Member> members = memberService.getAllMembers();
            memberList.addAll(members);
            System.out.println("✅ Đã load " + members.size() + " hội viên");
        } catch (Exception e) {
            System.err.println("❌ Lỗi load members: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Lỗi khi tải danh sách hội viên: " + e.getMessage());
        }
    }
    
    private void displayMemberInfo(Member member) {
        txtFullName.setText(member.getFullName());
        txtPhone.setText(member.getPhone());
        txtEmail.setText(member.getEmail());
        txtAddress.setText(member.getAddress());
        txtIdCard.setText(member.getIdCard());
        txtEmergencyContact.setText(member.getEmergencyContact());
        txtEmergencyPhone.setText(member.getEmergencyPhone());
        txtHealthNotes.setText(member.getHealthNotes());
        
        cboGender.setValue(member.getGender());
        cboStatus.setValue(member.getStatus());
        dpDateOfBirth.setValue(member.getDateOfBirth());
        dpJoinDate.setValue(member.getJoinDate());
    }
    
    private void clearForm() {
        txtFullName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        txtIdCard.clear();
        txtEmergencyContact.clear();
        txtEmergencyPhone.clear();
        txtHealthNotes.clear();
        
        cboGender.setValue(null);
        cboStatus.setValue(Member.MemberStatus.ACTIVE);
        dpDateOfBirth.setValue(null);
        dpJoinDate.setValue(LocalDate.now());
        
        selectedMember = null;
        tblMembers.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void handleSave() {
        // Validation
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (ValidationUtil.isEmpty(fullName)) {
            AlertUtil.showError("Họ tên không được để trống!");
            txtFullName.requestFocus();
            return;
        }
        
        String phoneError = ValidationUtil.validatePhone(phone);
        if (phoneError != null) {
            AlertUtil.showError(phoneError);
            txtPhone.requestFocus();
            return;
        }
        
        if (!ValidationUtil.isEmpty(email)) {
            String emailError = ValidationUtil.validateEmail(email);
            if (emailError != null) {
                AlertUtil.showError(emailError);
                txtEmail.requestFocus();
                return;
            }
        }
        
        if (cboGender.getValue() == null) {
            AlertUtil.showError("Vui lòng chọn giới tính!");
            cboGender.requestFocus();
            return;
        }
        
        if (dpJoinDate.getValue() == null) {
            AlertUtil.showError("Vui lòng chọn ngày tham gia!");
            dpJoinDate.requestFocus();
            return;
        }
        
        try {
            if (selectedMember == null) {
                // Create new
                Member member = new Member();
                member.setFullName(fullName);
                member.setPhone(phone);
                member.setEmail(email);
                member.setAddress(txtAddress.getText());
                member.setIdCard(txtIdCard.getText());
                member.setEmergencyContact(txtEmergencyContact.getText());
                member.setEmergencyPhone(txtEmergencyPhone.getText());
                member.setHealthNotes(txtHealthNotes.getText());
                member.setGender(cboGender.getValue());
                member.setDateOfBirth(dpDateOfBirth.getValue());
                member.setJoinDate(dpJoinDate.getValue());
                member.setStatus(cboStatus.getValue());
                
                memberService.createMember(member);
                AlertUtil.showSuccess("Thêm hội viên thành công!");
                System.out.println("✅ Tạo member mới: " + member.getMemberCode());
            } else {
                // Update existing
                selectedMember.setFullName(fullName);
                selectedMember.setPhone(phone);
                selectedMember.setEmail(email);
                selectedMember.setAddress(txtAddress.getText());
                selectedMember.setIdCard(txtIdCard.getText());
                selectedMember.setEmergencyContact(txtEmergencyContact.getText());
                selectedMember.setEmergencyPhone(txtEmergencyPhone.getText());
                selectedMember.setHealthNotes(txtHealthNotes.getText());
                selectedMember.setGender(cboGender.getValue());
                selectedMember.setDateOfBirth(dpDateOfBirth.getValue());
                selectedMember.setStatus(cboStatus.getValue());
                
                memberService.updateMember(selectedMember);
                AlertUtil.showSuccess("Cập nhật hội viên thành công!");
                System.out.println("✅ Update member: " + selectedMember.getMemberCode());
            }
            
            clearForm();
            loadMembers();
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi save: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleDelete() {
        if (selectedMember == null) {
            AlertUtil.showError("Vui lòng chọn hội viên cần xóa!");
            return;
        }
        
        // Kiểm tra xem hội viên có đăng ký gói tập không
        try {
            long registrationCount = registrationService.getRegistrationsByMember(selectedMember).size();
            
            if (registrationCount > 0) {
                AlertUtil.showWarning(
                    "Không thể xóa hội viên", 
                    "Hội viên có dữ liệu liên quan",
                    "Hội viên \"" + selectedMember.getFullName() + "\" có " + 
                    registrationCount + " lượt đăng ký gói tập.\n\n" +
                    "Bạn có thể:\n" +
                    "• Đặt trạng thái hội viên thành 'Tạm ngưng'\n" +
                    "• Xóa các đăng ký liên quan trước"
                );
                return;
            }
            
            // Nếu không có đăng ký, cho phép xóa
            if (AlertUtil.confirmDelete(selectedMember.getFullName())) {
                memberService.deleteMember(selectedMember.getId());
                AlertUtil.showSuccess("Xóa hội viên thành công!");
                System.out.println("✅ Đã xóa member: " + selectedMember.getMemberCode());
                clearForm();
                loadMembers();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi kiểm tra/xóa: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Lỗi: Không thể xóa hội viên này vì đang có dữ liệu liên quan.");
        }
    }
    
    @FXML
    public void handleSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            memberList.clear();
            if (keyword.isEmpty()) {
                loadMembers();
            } else {
                List<Member> results = memberService.searchMembers(keyword);
                memberList.addAll(results);
                System.out.println("🔍 Tìm thấy " + results.size() + " kết quả");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi search: " + e.getMessage());
            AlertUtil.showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        loadMembers();
        AlertUtil.showSuccess("Đã làm mới danh sách!");
    }
    
    @FXML
    public void handleNew() {
        clearForm();
        txtFullName.requestFocus();
    }
    
    @FXML
    public void handleBack() {
        try {
            App.changeScene("Dashboard.fxml");
        } catch (Exception e) {
            System.err.println("❌ Lỗi back: " + e.getMessage());
            AlertUtil.showError("Lỗi: " + e.getMessage());
        }
    }
}