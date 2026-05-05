// ============= AttendanceDAO.java =============
package com.gym.dao;

import com.gym.entity.Attendance;
import com.gym.entity.Member;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceDAO extends GenericDAO<Attendance, Long> {
    
    public AttendanceDAO() {
        super(Attendance.class);
    }
    
    public List<Attendance> findByMember(Member member) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Attendance a WHERE a.member = :member ORDER BY a.checkInTime DESC", 
                Attendance.class)
                .setParameter("member", member)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Attendance> findByDate(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            return em.createQuery(
                "SELECT a FROM Attendance a WHERE a.checkInTime >= :start " +
                "AND a.checkInTime < :end ORDER BY a.checkInTime DESC", Attendance.class)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Attendance> findByDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            return em.createQuery(
                "SELECT a FROM Attendance a WHERE a.checkInTime >= :start " +
                "AND a.checkInTime < :end ORDER BY a.checkInTime DESC", Attendance.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public long countTodayAttendance() {
        EntityManager em = getEntityManager();
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
            return em.createQuery(
                "SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime >= :start " +
                "AND a.checkInTime < :end", Long.class)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getSingleResult();
        } finally {
            em.close();
        }
    }
}