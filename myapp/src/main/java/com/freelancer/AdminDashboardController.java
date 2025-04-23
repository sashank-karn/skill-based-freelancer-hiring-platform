package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminDashboardController {
    @FXML private TableView<?> userTable;
    @FXML private TableView<?> skillTable;
    @FXML private TableView<?> verificationTable;
    @FXML private Label welcomeLabel;
    private boolean isDarkMode = false;
    private String userName;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Admin Dashboard");
    }

    public void setUserName(String userName) {
        this.userName = userName;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + userName);
        }
    }

    @FXML
    private void handleManageUsers(ActionEvent event) {
        showAlert("Manage Users functionality is under development.");
    }

    @FXML
    private void handleManageRoles(ActionEvent event) {
        showAlert("Manage Roles functionality is under development.");
    }

    @FXML
    private void handleSkillManagement(ActionEvent event) {
        showAlert("Skill Management functionality is under development.");
    }

    @FXML
    private void handleFreelancerVerification(ActionEvent event) {
        showAlert("Freelancer Verification functionality is under development.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            showAlert("Could not log out: " + e.getMessage());
        }
    }

    @FXML
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        Scene scene = welcomeLabel.getScene();
        if (isDarkMode) {
            scene.getStylesheets().add(getClass().getResource("/css/dark-mode.css").toExternalForm());
        } else {
            scene.getStylesheets().remove(getClass().getResource("/css/dark-mode.css").toExternalForm());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
