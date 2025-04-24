package com.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    
    private int userId;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize components
    }
    
    /**
     * Sets the user ID and loads admin data
     * @param userId The ID of the admin user
     */
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("AdminDashboardController: userId set to " + this.userId);
        loadAdminData();
    }
    
    /**
     * Loads admin-specific data
     */
    private void loadAdminData() {
        // Load admin data using userId
        // For example: load users, projects, statistics, etc.
        try {
            // Your database loading logic here
            System.out.println("Loading admin data for user ID: " + userId);
        } catch (Exception e) {
            System.err.println("Error loading admin data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Rest of your controller code
}
