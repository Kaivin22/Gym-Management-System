package com.gym.service;

import com.gym.config.HibernateConfig;
import com.gym.entity.Registration;
import com.gym.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

public class ReportService {

    /**
     * Dashboard stats - CHỈ DÙNG CHO DASHBOARD
     */
    public Map<String, Object> getDashboardStats() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            Map<String, Object> stats = new HashMap<>();
            LocalDate today = LocalDate.now();

            // 1. Active Members
            Long activeMembers = em.createQuery("SELECT COUNT(m) FROM Member m WHERE m.status = :st", Long.class)
                    .setParameter("st", Member.MemberStatus.ACTIVE)
                    .getSingleResult();
            stats.put("activeMembers", activeMembers != null ? activeMembers.intValue() : 0);

            // 2. Expired Members
            Long expiredMembers = em.createQuery("SELECT COUNT(m) FROM Member m WHERE m.status = :st", Long.class)
                    .setParameter("st", Member.MemberStatus.EXPIRED)
                    .getSingleResult();
            stats.put("expiredMembers", expiredMembers != null ? expiredMembers.intValue() : 0);

            // 3. Active Registrations
            Long activeRegs = em.createQuery("SELECT COUNT(r) FROM Registration r WHERE r.endDate >= :today", Long.class)
                    .setParameter("today", today)
                    .getSingleResult();
            stats.put("activeRegistrations", activeRegs != null ? activeRegs.intValue() : 0);

            // 4. Attendance Today
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59, 999999999);
            
            Long attendanceToday = em.createQuery(
                    "SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end", 
                    Long.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .getSingleResult();
            stats.put("attendanceToday", attendanceToday != null ? attendanceToday.intValue() : 0);

            // 5. Expiring Registrations (7 days)
            Long expiring = em.createQuery("SELECT COUNT(r) FROM Registration r WHERE r.endDate BETWEEN :today AND :next", Long.class)
                    .setParameter("today", today)
                    .setParameter("next", today.plusDays(7))
                    .getSingleResult();
            stats.put("expiringRegistrations", expiring != null ? expiring.intValue() : 0);

            // 6. Revenue This Month
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            
            BigDecimal revenue = em.createQuery(
                    "SELECT COALESCE(SUM(r.finalAmount), 0) FROM Registration r WHERE r.createdAt BETWEEN :start AND :end", 
                    BigDecimal.class)
                    .setParameter("start", startOfMonth)
                    .setParameter("end", endOfMonth)
                    .getSingleResult();
            stats.put("revenueThisMonth", revenue != null ? revenue : BigDecimal.ZERO);

            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultStats();
        } finally {
            em.close();
        }
    }

    private Map<String, Object> getDefaultStats() {
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("activeMembers", 0);
        defaultStats.put("expiredMembers", 0);
        defaultStats.put("activeRegistrations", 0);
        defaultStats.put("attendanceToday", 0);
        defaultStats.put("expiringRegistrations", 0);
        defaultStats.put("revenueThisMonth", BigDecimal.ZERO);
        return defaultStats;
    }

    /**
     * REPORT 1: Revenue by Package
     */
    public Map<String, BigDecimal> getRevenueByPackage() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            List<Object[]> results = em.createQuery(
                    "SELECT r.pkg.packageName, COALESCE(SUM(r.finalAmount), 0) " +
                    "FROM Registration r " +
                    "GROUP BY r.pkg.packageName " +
                    "ORDER BY SUM(r.finalAmount) DESC", 
                    Object[].class)
                    .getResultList();
            
            Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
            for (Object[] row : results) {
                String packageName = (String) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                revenueMap.put(packageName, revenue != null ? revenue : BigDecimal.ZERO);
            }
            return revenueMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    /**
     * REPORT 2: Top Members by criteria
     * @param criteria: "attendance", "spending", "registrations"
     * @param limit: number of top members
     * @return List of [memberId, memberCode, memberName, value]
     */
    public List<Object[]> getTopMembers(String criteria, int limit) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            List<Object[]> results;
            
            if ("attendance".equalsIgnoreCase(criteria)) {
                results = em.createQuery(
                    "SELECT m.id, m.memberCode, m.fullName, COUNT(a) as attendanceCount " +
                    "FROM Attendance a JOIN a.member m " +
                    "GROUP BY m.id, m.memberCode, m.fullName " +
                    "ORDER BY attendanceCount DESC", 
                    Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            } else if ("spending".equalsIgnoreCase(criteria)) {
                results = em.createQuery(
                    "SELECT m.id, m.memberCode, m.fullName, COALESCE(SUM(r.finalAmount), 0) as totalSpent " +
                    "FROM Registration r JOIN r.member m " +
                    "GROUP BY m.id, m.memberCode, m.fullName " +
                    "ORDER BY totalSpent DESC", 
                    Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            } else {
                // registrations
                results = em.createQuery(
                    "SELECT m.id, m.memberCode, m.fullName, COUNT(r) as registrationCount " +
                    "FROM Registration r JOIN r.member m " +
                    "GROUP BY m.id, m.memberCode, m.fullName " +
                    "ORDER BY registrationCount DESC", 
                    Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
            }
            
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * REPORT 3: Monthly statistics for a specific year
     */
    public List<Object[]> getMonthlyStats(int year) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            List<Object[]> results = new ArrayList<>();
            
            for (int month = 1; month <= 12; month++) {
                Object[] row = new Object[5];
                row[0] = "Tháng " + month;
                
                // New members
                Long newMembers = em.createQuery(
                    "SELECT COUNT(m) FROM Member m WHERE YEAR(m.joinDate) = :year AND MONTH(m.joinDate) = :month", 
                    Long.class)
                    .setParameter("year", year)
                    .setParameter("month", month)
                    .getSingleResult();
                row[1] = newMembers != null ? newMembers.intValue() : 0;
                
                // New registrations
                Long newRegs = em.createQuery(
                    "SELECT COUNT(r) FROM Registration r WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month", 
                    Long.class)
                    .setParameter("year", year)
                    .setParameter("month", month)
                    .getSingleResult();
                row[2] = newRegs != null ? newRegs.intValue() : 0;
                
                // Revenue
                BigDecimal revenue = em.createQuery(
                    "SELECT COALESCE(SUM(r.finalAmount), 0) FROM Registration r " +
                    "WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month", 
                    BigDecimal.class)
                    .setParameter("year", year)
                    .setParameter("month", month)
                    .getSingleResult();
                row[3] = revenue != null ? revenue : BigDecimal.ZERO;
                
                // Attendance count (for that month)
                LocalDate firstDay = LocalDate.of(year, month, 1);
                LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
                LocalDateTime start = firstDay.atStartOfDay();
                LocalDateTime end = lastDay.atTime(23, 59, 59);
                
                Long attendance = em.createQuery(
                    "SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end", 
                    Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
                row[4] = attendance != null ? attendance.intValue() : 0;
                
                results.add(row);
            }
            
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * REPORT 4: Expiring Registrations
     */
    public List<Registration> getExpiringRegistrations(int days) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(days);
            
            List<Registration> results = em.createQuery(
                "SELECT r FROM Registration r " +
                "WHERE r.status = 'ACTIVE' AND r.endDate BETWEEN :today AND :futureDate " +
                "ORDER BY r.endDate ASC", 
                Registration.class)
                .setParameter("today", today)
                .setParameter("futureDate", futureDate)
                .getResultList();
            
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * REPORT 5: Revenue by Day (for chart)
     * @param days: number of days to look back
     */
    public List<Object[]> getRevenueByDay(int days) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            // Native query for precise date grouping
            Query query = em.createNativeQuery(
                "SELECT DATE(created_at) as revenue_date, COALESCE(SUM(final_amount), 0) as daily_revenue " +
                "FROM registrations " +
                "WHERE DATE(created_at) BETWEEN :startDate AND :endDate " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY revenue_date ASC");
            
            query.setParameter("startDate", java.sql.Date.valueOf(startDate));
            query.setParameter("endDate", java.sql.Date.valueOf(endDate));
            
            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = query.getResultList();
            
            // Convert to proper types
            List<Object[]> results = new ArrayList<>();
            for (Object[] row : rawResults) {
                java.sql.Date sqlDate = (java.sql.Date) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                results.add(new Object[]{sqlDate.toLocalDate(), revenue});
            }
            
            // Fill missing days with zero
            return fillMissingDays(results, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    /**
     * Fill missing days with zero revenue
     */
    private List<Object[]> fillMissingDays(List<Object[]> existingData, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();
        
        // Initialize all days with zero
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            revenueMap.put(date, BigDecimal.ZERO);
        }
        
        // Update with actual revenue
        for (Object[] row : existingData) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(date, revenue);
        }
        
        // Convert back to List
        List<Object[]> filledData = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : revenueMap.entrySet()) {
            filledData.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        return filledData;
    }

    /**
     * Total Revenue (all time)
     */
    public BigDecimal getTotalRevenue() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            BigDecimal total = em.createQuery(
                "SELECT COALESCE(SUM(r.finalAmount), 0) FROM Registration r", 
                BigDecimal.class)
                .getSingleResult();
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        } finally {
            em.close();
        }
    }

    /**
     * Get registration count by package (for revenue report)
     */
    public Map<String, Integer> getRegistrationCountByPackage() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            List<Object[]> results = em.createQuery(
                "SELECT r.pkg.packageName, COUNT(r) " +
                "FROM Registration r " +
                "GROUP BY r.pkg.packageName", 
                Object[].class)
                .getResultList();
            
            Map<String, Integer> countMap = new HashMap<>();
            for (Object[] row : results) {
                String packageName = (String) row[0];
                Long count = (Long) row[1];
                countMap.put(packageName, count.intValue());
            }
            return countMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        } finally {
            em.close();
        }
    }
}