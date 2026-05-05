// ============= HibernateConfig.java =============
package com.gym.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateConfig {
    private static EntityManagerFactory entityManagerFactory;
    
    static {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("GymManagementPU");
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
    
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}