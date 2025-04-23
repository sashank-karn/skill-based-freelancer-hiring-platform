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
        String email = emailField.getText();
        String password = passwordField.getText();
        String selectedRole = roleBox.getValue();

        // Clear the error label before validation
        errorLabel.setVisible(false);
        errorLabel.setText("");

        // Validate email and password fields
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields.");
            errorLabel.setVisible(true);
            return;
        }

        // Validate role selection
        if (selectedRole == null || selectedRole.isEmpty()) {
            errorLabel.setText("Please select a role.");
            errorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE email=? AND password=? AND role=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password); // In production, use hashed passwords!
            ps.setString(3, selectedRole);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id"); // Retrieve userId
                String userName = rs.getString("name");
                loadDashboard(selectedRole, userName, userId); // Pass userId
            } else {
                errorLabel.setText("Invalid credentials or role mismatch.");
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private void loadDashboard(String role, String userName, int userId) {
        String fxmlFile = "";

        switch (role) {
            case "Client":
                fxmlFile = "/fxml/client_dashboard.fxml";
                break;
            case "Freelancer":
                fxmlFile = "/fxml/freelancer_dashboard.fxml";
                break;
            case "Admin":
                fxmlFile = "/fxml/admin_dashboard.fxml";
                break;
            default:
                showAlert("Unknown role: " + role);
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (role.equals("Freelancer")) {
                FreelancerDashboardController controller = loader.getController();
                controller.setUserId(userId); // Pass userId to the controller
                controller.setUserName(userName);
            }

            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(role + " Dashboard");
            stage.show();
        } catch (Exception e) {
            showAlert("Could not load the dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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
