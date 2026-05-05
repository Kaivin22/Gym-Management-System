// ============= UserDAO.java =============
package com.gym.dao;

import com.gym.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;

public class UserDAO extends GenericDAO<User, Long> {
    
    public UserDAO() {
        super(User.class);
    }
    
    public Optional<User> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            User user = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", 
                User.class)
                .setParameter("username", username)
                .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public boolean authenticate(String username, String password) {
        Optional<User> user = findByUsername(username);
        // Trong thực tế nên dùng BCrypt để mã hóa mật khẩu
        return user.isPresent() && user.get().getPassword().equals(password);
    }
}