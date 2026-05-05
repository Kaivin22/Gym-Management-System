package com.gym.dao;

import com.gym.entity.Payment;
import com.gym.entity.Registration;
import jakarta.persistence.EntityManager;
import java.util.List;

public class PaymentDAO extends GenericDAO<Payment, Long> {
    
    public PaymentDAO() {
        super(Payment.class);
    }
    
    public List<Payment> findByRegistration(Registration registration) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Payment p WHERE p.registration = :registration ORDER BY p.createdAt DESC", 
                Payment.class)
                .setParameter("registration", registration)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public String generatePaymentCode() {
        EntityManager em = getEntityManager();
        try {
            Long maxId = em.createQuery(
                "SELECT COALESCE(MAX(p.id), 0) FROM Payment p", Long.class)
                .getSingleResult();
            return String.format("PAY%08d", maxId + 1);
        } finally {
            em.close();
        }
    }
}