package com.freelancer;

/**
 * Singleton class to manage user session data across the application
 */
public class UserSession {
    private static UserSession instance;
    
    private int userId;
    private String userName;
    private String userRole;
    private boolean loggedIn;
    
    private UserSession() {
        userId = -1;
        loggedIn = false;
    }
    
    public static synchronized UserSession getInstance() {
        if(instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setUser(int userId, String userName, String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.loggedIn = true;
        System.out.println("Session started for user: " + userId + " (" + userName + ", " + userRole + ")");
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void clear() {
        this.userId = -1;
        this.userName = null;
        this.userRole = null;
        this.loggedIn = false;
        System.out.println("User session cleared");
    }
}