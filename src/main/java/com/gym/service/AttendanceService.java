package com.gym.service;

import com.gym.dao.AttendanceDAO;
import com.gym.entity.Attendance;
import com.gym.entity.Member;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    
    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }
    
    public void checkIn(Member member) {
        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setCheckInTime(java.time.LocalDateTime.now());
        
        attendanceDAO.save(attendance);
    }
    
    public void updateAttendance(Attendance attendance) {
        attendanceDAO.update(attendance);
    }
    
    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceDAO.findById(id);
    }
    
    public List<Attendance> getAllAttendances() {
        return attendanceDAO.findAll();
    }
    
    public List<Attendance> getAttendancesByMember(Member member) {
        return attendanceDAO.findByMember(member);
    }
    
    public List<Attendance> getAttendancesByDate(LocalDate date) {
        return attendanceDAO.findByDate(date);
    }
    
    public List<Attendance> getAttendancesByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceDAO.findByDateRange(startDate, endDate);
    }
    
    public long getTodayAttendanceCount() {
        return attendanceDAO.countTodayAttendance();
    }
}