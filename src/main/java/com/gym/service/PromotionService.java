package com.gym.service;

import com.gym.config.HibernateConfig;
import com.gym.entity.Promotion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PromotionService {
    
    /**
     * Tạo mới khuyến mãi đơn giản
     * @param maxUsage 
     * @param discountValue 
     * @param discountAmount 
     */
    public Promotion createPromotion(String promoCode, String promoName, String description,
                                    Double discountPercent, BigDecimal discountAmount, BigDecimal discountValue, LocalDate startDate, 
                                    LocalDate endDate, Integer maxUsage, Boolean isActive) {
        
        if (promoCode == null || promoCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
        }
        
        if (promoName == null || promoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống!");
        }
        
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Kiểm tra trùng mã
            Long count = em.createQuery("SELECT COUNT(p) FROM Promotion p WHERE p.promoCode = :code", Long.class)
                    .setParameter("code", promoCode)
                    .getSingleResult();
            
            if (count > 0) {
                throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại!");
            }
            
            Promotion promotion = new Promotion();
            promotion.setPromoCode(promoCode.trim());
            promotion.setPromoName(promoName.trim());
            promotion.setDescription(description);
            promotion.setDiscountPercent(discountPercent);
            promotion.setDiscountValue(BigDecimal.valueOf(discountPercent));
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setIsActive(isActive != null ? isActive : true);
            promotion.setCurrentUsage(0);
            
            em.persist(promotion);
            em.getTransaction().commit();
            
            return promotion;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi lưu khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Cập nhật khuyến mãi
     */
    public Promotion updatePromotion(Promotion promotion) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Kiểm tra trùng mã (ngoại trừ chính nó)
            if (promotion.getPromoCode() != null) {
                Long count = em.createQuery(
                    "SELECT COUNT(p) FROM Promotion p WHERE p.promoCode = :code AND p.id != :id", 
                    Long.class)
                    .setParameter("code", promotion.getPromoCode())
                    .setParameter("id", promotion.getId())
                    .getSingleResult();
                
                if (count > 0) {
                    throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại!");
                }
            }
            
            // Đảm bảo discountValue khớp với discountPercent
            if (promotion.getDiscountPercent() != null) {
                promotion.setDiscountValue(BigDecimal.valueOf(promotion.getDiscountPercent()));
            }
            
            Promotion merged = em.merge(promotion);
            em.getTransaction().commit();
            
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi cập nhật khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Xóa khuyến mãi (soft delete)
     */
    public void deletePromotion(Long id) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Promotion promotion = em.find(Promotion.class, id);
            if (promotion != null) {
                promotion.setIsActive(false);
                em.merge(promotion);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi xóa khuyến mãi: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Lấy tất cả khuyến mãi
     */
    public List<Promotion> getAllPromotions() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Promotion p ORDER BY p.createdAt DESC", 
                Promotion.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Lấy khuyến mãi đang hoạt động
     */
    public List<Promotion> getActivePromotions() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            
            return em.createQuery(
                "SELECT p FROM Promotion p WHERE p.isActive = true " +
                "AND (p.startDate IS NULL OR p.startDate <= :today) " +
                "AND (p.endDate IS NULL OR p.endDate >= :today) " +
                "ORDER BY p.discountPercent DESC", 
                Promotion.class)
                .setParameter("today", today)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Tìm khuyến mãi theo mã
     */
    public Promotion findPromotionByCode(String promoCode) {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            TypedQuery<Promotion> query = em.createQuery(
                "SELECT p FROM Promotion p WHERE p.promoCode = :code", 
                Promotion.class);
            query.setParameter("code", promoCode);
            
            try {
                return query.getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                return null;
            }
        } finally {
            em.close();
        }
    }
    
    /**
     * Tính tổng số tiền đã giảm giá
     */
    public BigDecimal getTotalDiscountAmount() {
        EntityManager em = HibernateConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT COALESCE(SUM(r.discountAmount), 0) FROM Registration r " +
                "WHERE r.promotion IS NOT NULL", 
                BigDecimal.class)
                .getSingleResult();
        } finally {
            em.close();
        }
    }
}