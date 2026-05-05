// ============= SessionManager.java =============
package com.gym.util;

import com.gym.entity.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
    
    public void logout() {
        currentUser = null;
    }
}