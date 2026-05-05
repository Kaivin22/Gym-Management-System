package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
    
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }
    
    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
    
    public Member getMember() {
        return member;
    }
    
    public void setMember(Member member) {
        this.member = member;
    }
}