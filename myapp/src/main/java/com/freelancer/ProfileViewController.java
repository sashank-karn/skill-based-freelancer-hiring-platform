package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProfileViewController {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private PasswordField passwordField;
    @FXML private FontIcon togglePasswordIcon;
    @FXML private FontIcon verificationIcon;
    @FXML private Label verificationLabel;
    @FXML private HBox verificationBox;
    
    @FXML private TableView<Skill> skillsTable;
    @FXML private TableColumn<Skill, String> skillNameColumn;
    @FXML private TableColumn<Skill, String> skillStatusColumn;
    @FXML private TableColumn<Skill, Button> skillActionColumn;
    
    @FXML private TableView<FileShare> filesTable;
    @FXML private TableColumn<FileShare, String> fileNameColumn;
    @FXML private TableColumn<FileShare, String> fileSkillColumn;
    @FXML private TableColumn<FileShare, String> fileDateColumn;
    @FXML private TableColumn<FileShare, Button> fileActionColumn;

    private int userId;
    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        // Initialize icons using enum constants
        togglePasswordIcon.setIconCode(MaterialDesignE.EYE);
        verificationIcon.setIconCode(MaterialDesignA.ACCOUNT_CHECK);
        
        setupSkillsTable();
        setupFilesTable();
        loadUserData();
        loadSkills();
        loadFiles();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserData();
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        togglePasswordIcon.setIconCode(isPasswordVisible ? MaterialDesignE.EYE_OFF : MaterialDesignE.EYE);
        
        String password = passwordField.getText();
        passwordField.setPromptText(password);
        passwordField.setText(isPasswordVisible ? password : "");
        passwordField.setStyle(isPasswordVisible ? "-fx-text-fill: black;" : "-fx-text-fill: transparent;");
    }

    @FXML
    private void showChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change your password");

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");

        dialog.getDialogPane().setContent(new VBox(10, currentPassword, newPassword, confirmPassword));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (!newPassword.getText().equals(confirmPassword.getText())) {
                    showAlert("Passwords do not match");
                    return null;
                }
                updatePassword(currentPassword.getText(), newPassword.getText());
            }
            return button;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleChangePassword() {
        // Logic for changing password
        showAlert("Password change functionality is under development.");
    }

    private void updatePassword(String currentPassword, String newPassword) {
        try (Connection conn = DBUtil.getConnection()) {
            // First verify current password
            String verifySql = "SELECT password FROM users WHERE user_id = ? AND password = ?";
            PreparedStatement verifyPs = conn.prepareStatement(verifySql);
            verifyPs.setInt(1, userId);
            verifyPs.setString(2, currentPassword);
            if (!verifyPs.executeQuery().next()) {
                showAlert("Current password is incorrect");
                return;
            }

            // Update password
            String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setString(1, newPassword);
            updatePs.setInt(2, userId);
            updatePs.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating password");
        }
    }

    private void setupSkillsTable() {
        skillNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        skillStatusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        // Setup action column
    }

    private void setupFilesTable() {
        fileNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        fileSkillColumn.setCellValueFactory(data -> data.getValue().skillProperty());
        fileDateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        // Setup action column
    }

    private void loadUserData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT first_name, email, role, password, is_verified FROM users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                nameLabel.setText(rs.getString("first_name"));
                emailLabel.setText(rs.getString("email"));
                roleLabel.setText(rs.getString("role"));
                passwordField.setText(rs.getString("password"));
                
                boolean isVerified = rs.getBoolean("is_verified");
                verificationIcon.setIconCode(isVerified ? MaterialDesignA.ACCOUNT_CHECK : MaterialDesignA.ACCOUNT_ALERT);
                verificationLabel.setText(isVerified ? "Verified" : "Not Verified");
                verificationBox.getStyleClass().setAll(isVerified ? "verified" : "not-verified");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading user data");
        }
    }

    private void loadSkills() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT s.skill_name, CASE WHEN fs.skill_id IS NOT NULL THEN 'Selected' ELSE 'Available' END as status " +
                        "FROM skills s LEFT JOIN freelancer_skills fs ON s.skill_id = fs.skill_id AND fs.user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            skillsTable.getItems().clear();
            while (rs.next()) {
                skillsTable.getItems().add(new Skill(
                    rs.getString("skill_name"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading skills");
        }
    }

    private void loadFiles() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT f.file_name, s.skill_name, f.upload_date " +
                        "FROM files f JOIN skills s ON f.skill_id = s.skill_id " +
                        "WHERE f.user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            filesTable.getItems().clear();
            while (rs.next()) {
                filesTable.getItems().add(new FileShare(
                    rs.getString("file_name"),
                    rs.getString("skill_name"),
                    rs.getString("upload_date")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading files");
        }
    }

    @FXML
    private void handleHomeClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/freelancer_dashboard.fxml"));
            Parent root = loader.load();
            
            FreelancerDashboardController controller = loader.getController();
            controller.setUserId(userId);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Freelancer Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not log out: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}
