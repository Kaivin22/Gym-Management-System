package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "member_code", unique = true, nullable = false, length = 20)
    private String memberCode;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "id_card", length = 20)
    private String idCard;
    
    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;
    
    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;
    
    @Column(name = "health_notes", columnDefinition = "TEXT")
    private String healthNotes;
    
    @Column(name = "photo_url", length = 255)
    private String photoUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Registration> registrations = new ArrayList<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attendance> attendances = new ArrayList<>();
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    public enum MemberStatus {
        ACTIVE, EXPIRED, SUSPENDED
    }
    
    // Constructors
    public Member() {}
    
    public Member(String memberCode, String fullName, Gender gender, String phone, LocalDate joinDate) {
        this.memberCode = memberCode;
        this.fullName = fullName;
        this.gender = gender;
        this.phone = phone;
        this.joinDate = joinDate;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinDate == null) {
            joinDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMemberCode() {
        return memberCode;
    }
    
    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getIdCard() {
        return idCard;
    }
    
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
    
    public String getEmergencyContact() {
        return emergencyContact;
    }
    
    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
    
    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }
    
    public String getHealthNotes() {
        return healthNotes;
    }
    
    public void setHealthNotes(String healthNotes) {
        this.healthNotes = healthNotes;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public MemberStatus getStatus() {
        return status;
    }
    
    public void setStatus(MemberStatus status) {
        this.status = status;
    }
    
    public LocalDate getJoinDate() {
        return joinDate;
    }
    
    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Registration> getRegistrations() {
        return registrations;
    }
    
    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
    
    public List<Attendance> getAttendances() {
        return attendances;
    }
    
    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }
    
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", memberCode='" + memberCode + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}