package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AccountSettingsController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox notificationPreference;
    @FXML private TextField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label verificationStatusLabel;
    @FXML private TextField skillField;
    @FXML private ListView<String> skillListView;
    @FXML private Label accountNameLabel;

    private List<String> skills = new ArrayList<>(); // Store skills
    private String userName; // Store the username for navigation

    public void setUserName(String userName) {
        this.userName = userName;
        accountNameLabel.setText("Account Name: " + userName);
    }

    @FXML
    private void handleSaveChanges() {
        System.out.println("Changes saved: Email=" + emailField.getText() +
                           ", Notifications=" + notificationPreference.isSelected());
    }

    @FXML
    private void handleUpdatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("New password and confirmation do not match.");
            return;
        }

        // Check the current password from the database
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT password FROM users WHERE name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (!dbPassword.equals(currentPassword)) {
                    showAlert("Current password is incorrect.");
                    return;
                }
            } else {
                showAlert("User not found.");
                return;
            }

            // Update the password in the database
            sql = "UPDATE users SET password = ? WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setString(2, userName);
            ps.executeUpdate();

            showAlert("Password updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while updating the password.");
        }
    }

    @FXML
    private void handleDocumentVerification() {
        // Simulate document verification logic
        verificationStatusLabel.setText("Verification Status: Verified");
        showAlert("Documents uploaded successfully. Verification is now complete.");
    }

    @FXML
    private void handleAddSkill() {
        String skill = skillField.getText();
        if (skill.isEmpty()) {
            showAlert("Please enter a skill.");
            return;
        }

        skills.add(skill);
        skillListView.getItems().add(skill);
        skillField.clear();
        showAlert("Skill added successfully.");
    }

    @FXML
    private void handleUpdateSkill() {
        String selectedSkill = skillListView.getSelectionModel().getSelectedItem();
        String newSkill = skillField.getText();

        if (selectedSkill == null) {
            showAlert("Please select a skill to update.");
            return;
        }

        if (newSkill.isEmpty()) {
            showAlert("Please enter the updated skill.");
            return;
        }

        int index = skills.indexOf(selectedSkill);
        skills.set(index, newSkill);
        skillListView.getItems().set(index, newSkill);
        skillField.clear();
        showAlert("Skill updated successfully.");
    }

    @FXML
    private void handleDeleteSkill() {
        String selectedSkill = skillListView.getSelectionModel().getSelectedItem();

        if (selectedSkill == null) {
            showAlert("Please select a skill to delete.");
            return;
        }

        skills.remove(selectedSkill);
        skillListView.getItems().remove(selectedSkill);
        showAlert("Skill deleted successfully.");
    }

    @FXML
    private void handleDeleteAccount() {
        System.out.println("Account deleted successfully.");
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/freelancer_dashboard.fxml"));
            Parent root = loader.load();

            // Pass the username back to the FreelancerDashboardController
            FreelancerDashboardController controller = loader.getController();
            controller.setUserName(userName);

            Stage stage = (Stage) currentPasswordField.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600); // Set constant size
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Freelancer Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while navigating back.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
