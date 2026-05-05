package com.gym.dao;

import com.gym.entity.PromotionUsage;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

public class PromotionUsageDAO extends GenericDAO<PromotionUsage, Long> {
    
    public PromotionUsageDAO() {
        super(PromotionUsage.class);
    }
    
    public List<PromotionUsage> findByPromotionId(Long promotionId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT pu FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId " +
                "ORDER BY pu.appliedAt DESC", PromotionUsage.class)
                .setParameter("promotionId", promotionId)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<PromotionUsage> findByMemberId(Long memberId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT pu FROM PromotionUsage pu WHERE pu.member.id = :memberId " +
                "ORDER BY pu.appliedAt DESC", PromotionUsage.class)
                .setParameter("memberId", memberId)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public BigDecimal getTotalDiscountAmount() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT COALESCE(SUM(pu.discountAmount), 0) FROM PromotionUsage pu", 
                BigDecimal.class)
                .getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public long countByPromotion(Long promotionId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId", 
                Long.class)
                .setParameter("promotionId", promotionId)
                .getSingleResult();
        } finally {
            em.close();
        }
    }
}