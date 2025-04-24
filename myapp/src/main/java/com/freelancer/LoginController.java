package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel; // Add this to reference the error label

    @FXML
    public void initialize() {
        // Initialize the ComboBox with roles
        roleBox.getItems().addAll("Client", "Freelancer", "Admin");
        roleBox.setValue("Client"); // Set default value
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String userName = rs.getString("name"); // Assuming there's a name column
                
                // Debug output
                System.out.println("Login successful! User ID: " + userId + ", Role: " + role);
                
                // Create a session for the user
                UserSession session = UserSession.getInstance();
                session.setUser(userId, userName, role);
                
                if (role.equalsIgnoreCase("client")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_dashboard.fxml"));
                    Parent root = loader.load();
                    
                    ClientDashboardController controller = loader.getController();
                    controller.setUserId(userId);
                    
                    Scene scene = new Scene(root);
                    StyleManager.applyStylesheets(scene);
                    
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Client Dashboard");
                    stage.show();
                } 
                else if (role.equalsIgnoreCase("freelancer")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/freelancer_dashboard.fxml"));
                    Parent root = loader.load();
                    
                    FreelancerDashboardController controller = loader.getController();
                    controller.setUserId(userId);
                    
                    Scene scene = new Scene(root);
                    StyleManager.applyStylesheets(scene);
                    
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Freelancer Dashboard");
                    stage.show();
                }
                else if (role.equalsIgnoreCase("admin")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
                    Parent root = loader.load();
                    
                    AdminDashboardController controller = loader.getController();
                    controller.setUserId(userId);  // This will now work with our new method
                    
                    Scene scene = new Scene(root);
                    StyleManager.applyStylesheets(scene);
                    
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Admin Dashboard");
                    stage.show();
                }
                else {
                    // Unknown role
                    showAlert(Alert.AlertType.ERROR, "Login Error", 
                        "Unknown user role: " + role);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not connect to database: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load(); // Load the FXML file only once
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) {
            System.err.println("Could not load register page: " + e.getMessage());
        }
    }
}
