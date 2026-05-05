// ============= MemberDAO.java =============
package com.gym.dao;

import com.gym.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public class MemberDAO extends GenericDAO<Member, Long> {
    
    public MemberDAO() {
        super(Member.class);
    }
    
    public Optional<Member> findByMemberCode(String memberCode) {
        EntityManager em = getEntityManager();
        try {
            Member member = em.createQuery(
                "SELECT m FROM Member m WHERE m.memberCode = :code", Member.class)
                .setParameter("code", memberCode)
                .getSingleResult();
            return Optional.of(member);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public Optional<Member> findByPhone(String phone) {
        EntityManager em = getEntityManager();
        try {
            Member member = em.createQuery(
                "SELECT m FROM Member m WHERE m.phone = :phone", Member.class)
                .setParameter("phone", phone)
                .getSingleResult();
            return Optional.of(member);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Member> searchMembers(String keyword) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM Member m WHERE " +
                "m.memberCode LIKE :keyword OR " +
                "m.fullName LIKE :keyword OR " +
                "m.phone LIKE :keyword OR " +
                "m.email LIKE :keyword", Member.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Member> findByStatus(Member.MemberStatus status) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM Member m WHERE m.status = :status", Member.class)
                .setParameter("status", status)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public String generateMemberCode() {
        EntityManager em = getEntityManager();
        try {
            Long maxId = em.createQuery(
                "SELECT COALESCE(MAX(m.id), 0) FROM Member m", Long.class)
                .getSingleResult();
            return String.format("M%05d", maxId + 1);
        } finally {
            em.close();
        }
    }
}