package com.gym.dao;

import com.gym.entity.Promotion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PromotionDAO extends GenericDAO<Promotion, Long> {
    
    public PromotionDAO() {
        super(Promotion.class);
    }
    
    public Optional<Promotion> findByPromoCode(String code) {
        EntityManager em = getEntityManager();
        try {
            Promotion promotion = em.createQuery(
                "SELECT p FROM Promotion p WHERE p.promoCode = :code", Promotion.class)
                .setParameter("code", code)
                .getSingleResult();
            return Optional.of(promotion);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Promotion> findActivePromotions() {
        EntityManager em = getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            return em.createQuery(
                "SELECT p FROM Promotion p WHERE p.isActive = true " +
                "AND (p.startDate IS NULL OR p.startDate <= :today) " +
                "AND (p.endDate IS NULL OR p.endDate >= :today) " +
                "AND (p.maxUsage IS NULL OR p.currentUsage < p.maxUsage) " +
                "ORDER BY p.discountPercent DESC, p.createdAt DESC", Promotion.class)
                .setParameter("today", today)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Promotion> findPromotionsForMember(Long memberId, BigDecimal orderAmount) {
        EntityManager em = getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            return em.createQuery(
                "SELECT p FROM Promotion p WHERE p.isActive = true " +
                "AND (p.startDate IS NULL OR p.startDate <= :today) " +
                "AND (p.endDate IS NULL OR p.endDate >= :today) " +
                "AND (p.maxUsage IS NULL OR p.currentUsage < p.maxUsage) " +
                "AND (p.minOrderAmount IS NULL OR :orderAmount >= p.minOrderAmount) " +
                "ORDER BY p.discountPercent DESC", Promotion.class)
                .setParameter("today", today)
                .setParameter("orderAmount", orderAmount)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public void incrementUsageCount(Long promotionId) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE Promotion p SET p.currentUsage = p.currentUsage + 1, " +
                "p.updatedAt = CURRENT_TIMESTAMP " +
                "WHERE p.id = :id")
                .setParameter("id", promotionId)
                .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi khi cập nhật usage count: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public boolean isPromoCodeExists(String promoCode) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Promotion p WHERE p.promoCode = :code", Long.class)
                .setParameter("code", promoCode)
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
    
    public List<Promotion> findAllOrderByCreatedAt() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Promotion p ORDER BY p.createdAt DESC, p.id DESC", 
                Promotion.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
}