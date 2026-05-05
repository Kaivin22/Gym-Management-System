// ============= RegistrationDAO.java =============
package com.gym.dao;

import com.gym.entity.Member;
import com.gym.entity.Registration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RegistrationDAO extends GenericDAO<Registration, Long> {
    
    public RegistrationDAO() {
        super(Registration.class);
    }
    
    public Optional<Registration> findByRegistrationCode(String code) {
        EntityManager em = getEntityManager();
        try {
            Registration reg = em.createQuery(
                "SELECT r FROM Registration r WHERE r.registrationCode = :code", Registration.class)
                .setParameter("code", code)
                .getSingleResult();
            return Optional.of(reg);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Registration> findByMember(Member member) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Registration r WHERE r.member = :member ORDER BY r.createdAt DESC", 
                Registration.class)
                .setParameter("member", member)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public Optional<Registration> findActiveRegistrationByMember(Long memberId) {
        EntityManager em = getEntityManager();
        try {
            Registration reg = em.createQuery(
                "SELECT r FROM Registration r WHERE r.member.id = :memberId " +
                "AND r.status = 'ACTIVE' AND r.endDate >= :today " +
                "ORDER BY r.endDate DESC", Registration.class)
                .setParameter("memberId", memberId)
                .setParameter("today", LocalDate.now())
                .setMaxResults(1)
                .getSingleResult();
            return Optional.of(reg);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Registration> findExpiringRegistrations(int days) {
        EntityManager em = getEntityManager();
        try {
            LocalDate endDate = LocalDate.now().plusDays(days);
            return em.createQuery(
                "SELECT r FROM Registration r WHERE r.status = 'ACTIVE' " +
                "AND r.endDate BETWEEN :today AND :endDate " +
                "ORDER BY r.endDate", Registration.class)
                .setParameter("today", LocalDate.now())
                .setParameter("endDate", endDate)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public String generateRegistrationCode() {
        EntityManager em = getEntityManager();
        try {
            Long maxId = em.createQuery(
                "SELECT COALESCE(MAX(r.id), 0) FROM Registration r", Long.class)
                .getSingleResult();
            return String.format("REG%06d", maxId + 1);
        } finally {
            em.close();
        }
    }
}