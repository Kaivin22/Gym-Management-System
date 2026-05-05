// ============= PackageDAO.java =============
package com.gym.dao;

import com.gym.entity.Package;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

public class PackageDAO extends GenericDAO<Package, Long> {
    
    public PackageDAO() {
        super(Package.class);
    }
    
    public Optional<Package> findByPackageCode(String packageCode) {
        EntityManager em = getEntityManager();
        try {
            Package pkg = em.createQuery(
                "SELECT p FROM Package p WHERE p.packageCode = :code", Package.class)
                .setParameter("code", packageCode)
                .getSingleResult();
            return Optional.of(pkg);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Package> findActivePackages() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Package p WHERE p.isActive = true ORDER BY p.durationMonths", Package.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
    
    public String generatePackageCode() {
        EntityManager em = getEntityManager();
        try {
            Long maxId = em.createQuery(
                "SELECT COALESCE(MAX(p.id), 0) FROM Package p", Long.class)
                .getSingleResult();
            return String.format("PKG%03d", maxId + 1);
        } finally {
            em.close();
        }
    }
}