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
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("Client", "Freelancer", "Admin");
        roleBox.setValue("Client");
        
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = roleBox.getValue().toLowerCase();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password cannot be empty.");
            return;
        }
        
        try (Connection conn = DBUtil.getConnection()) {
            ensureTablesExist(conn);
            
            String sql = "SELECT * FROM Users WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                
                if (!password.equals(dbPassword)) {
                    showError("Invalid password.");
                    return;
                }
                
                String dbRole = rs.getString("role");
                
                if (dbRole == null || !dbRole.equalsIgnoreCase(selectedRole)) {
                    showError("You do not have access as " + selectedRole + ".");
                    return;
                }
                
                int userId = rs.getInt("user_id");
                String userName = rs.getString("name"); 
                
                UserSession session = UserSession.getInstance();
                session.setUser(userId, userName, dbRole);
                
                if (dbRole.equalsIgnoreCase("client")) {
                    navigateToDashboard("/fxml/client_dashboard.fxml", "Client Dashboard", userId);
                } 
                else if (dbRole.equalsIgnoreCase("freelancer")) {
                    navigateToDashboard("/fxml/freelancer_dashboard.fxml", "Freelancer Dashboard", userId);
                }
                else if (dbRole.equalsIgnoreCase("admin")) {
                    navigateToDashboard("/fxml/admin_dashboard.fxml", "Admin Dashboard", userId);
                }
                else {
                    showError("Unknown user role: " + dbRole);
                }
            } else {
                showError("No account found with this email.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    private void navigateToDashboard(String fxmlPath, String title, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof ClientDashboardController) {
                ((ClientDashboardController) controller).setUserId(userId);
            } else if (controller instanceof FreelancerDashboardController) {
                ((FreelancerDashboardController) controller).setUserId(userId);
            } else if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setUserId(userId);
            }
            
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Could not load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Error", message);
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load register page: " + e.getMessage());
        }
    }
    
    private void ensureTablesExist(Connection conn) throws SQLException {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, "USERS", null);
            
            if (!rs.next()) {
                String createUsersTable = "CREATE TABLE Users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                    
                Statement stmt = conn.createStatement();
                stmt.execute(createUsersTable);
                
                String insertAdmin = "INSERT INTO Users (name, email, password, role) " +
                                    "VALUES ('Admin User', 'admin@example.com', 'admin123', 'admin')";
                stmt.execute(insertAdmin);
                
                String insertClient = "INSERT INTO Users (name, email, password, role) " +
                                     "VALUES ('Client User', 'client@example.com', 'client123', 'client')";
                stmt.execute(insertClient);
                
                String insertFreelancer = "INSERT INTO Users (name, email, password, role) " +
                                         "VALUES ('Freelancer User', 'freelancer@example.com', 'freelancer123', 'freelancer')";
                stmt.execute(insertFreelancer);
                
                System.out.println("Created Users table and test accounts");
            }
        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
            throw e;
        }
    }
}
