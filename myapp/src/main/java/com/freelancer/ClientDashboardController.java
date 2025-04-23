package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientDashboardController {
    @FXML private TableView<?> projectTable; // Placeholder for project management table
    @FXML private TableView<?> proposalTable; // Placeholder for proposal management table
    @FXML private Label welcomeLabel;
    private boolean isDarkMode = false;
    private String userName; // Replace with actual logged-in user's name

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Client Dashboard");
    }
    public void setUserName(String userName) {
        this.userName = userName;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to Freelancer Page, " + userName);
        }
    }
    @FXML
    private void handlePostProject(ActionEvent event) {
        showAlert("Post Project functionality is under development.");
    }

    @FXML
    private void handleManageProposals(ActionEvent event) {
        showAlert("Manage Proposals functionality is under development.");
    }

    @FXML
    private void handleMakePayment(ActionEvent event) {
        showAlert("Make Payment functionality is under development.");
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
