package com.freelancer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileViewController {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private PasswordField passwordField;
    @FXML private FontIcon togglePasswordIcon;
    @FXML private HBox verificationBox;
    @FXML private Label verificationLabel;
    @FXML private TableView<Skill> skillsTable;
    @FXML private TableColumn<Skill, String> skillNameColumn;
    @FXML private TableColumn<Skill, String> skillStatusColumn;
    @FXML private TableColumn<Skill, Button> skillActionColumn;
    @FXML private TableView<FileRecord> filesTable;
    @FXML private TableColumn<FileRecord, String> fileNameColumn;
    @FXML private TableColumn<FileRecord, String> fileSkillColumn;
    @FXML private TableColumn<FileRecord, String> fileDateColumn;
    @FXML private TableColumn<FileRecord, Button> fileActionColumn;
    @FXML private Button updateSkillButton;
    @FXML private Button removeSkillButton;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private VBox skillsSection;     
    @FXML private VBox verificationSection; 
    @FXML private TabPane skillsTabPane;
    @FXML private FontIcon verificationIcon;

    private int userId;
    private String realPassword;
    private String userRole;
    
    @FXML
    private void initialize() {
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
        
        if (userId > 0) {
            if (userRole == null || userRole.isEmpty()) {
                determineUserRole();
            } else {
                loadUserData();
            }
        }
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
        
        try {
            boolean isFreelancer = userRole != null && userRole.equalsIgnoreCase("freelancer");
            
            if (verificationSection != null) {
                verificationSection.setVisible(isFreelancer);
                verificationSection.setManaged(isFreelancer);
            }
            
            if (skillsSection != null) {
                skillsSection.setVisible(isFreelancer);
                skillsSection.setManaged(isFreelancer);
            }
            
            if (userId > 0) {
                loadUserData();
            }
        } catch (Exception e) {
            System.err.println("Error in setUserRole: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserData() {
        try (Connection conn = DBUtil.getConnection()) {
            String userQuery = "SELECT * FROM Users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(userQuery);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String userName = rs.getString("name");
                String userEmail = rs.getString("email");
                
                if (nameField != null) {
                    nameField.setText(userName);
                } else if (nameLabel != null) {
                    nameLabel.setText(userName);
                }
                
                if (emailField != null) {
                    emailField.setText(userEmail);
                } else if (emailLabel != null) {
                    emailLabel.setText(userEmail);
                }
                
                if (roleLabel != null) {
                    roleLabel.setText(userRole != null ? userRole : rs.getString("role"));
                }
                
                if (userRole != null && userRole.equalsIgnoreCase("freelancer")) {
                    loadFreelancerData(conn);
                } else if (userRole != null && userRole.equalsIgnoreCase("client")) {
                    loadClientData(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading profile", e.getMessage());
        }
    }
    
    private int getFreelancerId(Connection conn, int userId) {
        try {
            String sql = "SELECT user_id AS freelancer_id FROM Users WHERE user_id = ? AND role = 'freelancer'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("freelancer_id");
            }
        } catch (Exception e) {
            System.err.println("Error retrieving freelancer_id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return userId;
    }
    
    private void loadFreelancerData(Connection conn) {
        loadSkills();
        loadFiles();
    }
    
    private void loadClientData(Connection conn) {
    }
    
    private void loadSkills() {
        if (skillsTable == null || !userRole.equalsIgnoreCase("freelancer")) {
            return; 
        }
        
        int freelancerId = userId; 
        skillsTable.getItems().clear();
        
        try (Connection conn = DBUtil.getConnection()) {
            freelancerId = getFreelancerId(conn, userId);
            
            String sql = "SELECT skill_id, skill_name, status FROM Skills WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ResultSet rs = ps.executeQuery();
            
            boolean foundSkills = false;
            while (rs.next()) {
                foundSkills = true;
                String skillName = rs.getString("skill_name");
                String status = rs.getString("status");
                skillsTable.getItems().add(new Skill(skillName, status));
            }
            
            if (!foundSkills) {
                skillsTable.getItems().add(new Skill("Java Programming", "Pending"));
                skillsTable.getItems().add(new Skill("Database Design", "Pending"));
                skillsTable.getItems().add(new Skill("Web Development", "Pending"));
            }
        } catch (Exception e) {
            System.err.println("Error loading skills: " + e.getMessage());
            e.printStackTrace();
            
            // Add sample skills as fallback
            skillsTable.getItems().add(new Skill("Java Programming", "Pending"));
            skillsTable.getItems().add(new Skill("Database Design", "Pending"));
            skillsTable.getItems().add(new Skill("Web Development", "Pending"));
        }
        
        createSkillsTableIfNeeded();
    }

    private void createSkillsTableIfNeeded() {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("Creating Skills table if needed...");
            Statement stmt = conn.createStatement();
            String createSql = "CREATE TABLE IF NOT EXISTS Skills (" +
                               "skill_id INT AUTO_INCREMENT PRIMARY KEY, " +
                               "user_id INT NOT NULL, " +  
                               "skill_name VARCHAR(100) NOT NULL, " +
                               "status VARCHAR(20) DEFAULT 'Pending', " +
                               "description VARCHAR(255), " +
                               "FOREIGN KEY (user_id) REFERENCES Users(user_id))";  
            stmt.execute(createSql);
        } catch (Exception e) {
            System.out.println("Error creating Skills table: " + e.getMessage());
        }
    }
    
    private void loadFiles() {
        if (filesTable == null) {  
            return;
        }
        
        filesTable.getItems().clear();
        
        try (Connection conn = DBUtil.getConnection()) {
            int freelancerId = getFreelancerId(conn, userId);  // Get freelancerId here
            
            // Use user_id instead of freelancer_id to match your database structure
            String sql = "SELECT file_id, file_name, category, upload_date FROM Files WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, freelancerId);
            ResultSet rs = ps.executeQuery();
            
            boolean foundFiles = false;
            while (rs.next()) {
                foundFiles = true;
                String fileName = rs.getString("file_name");
                String category = rs.getString("category");
                String uploadDate = rs.getString("upload_date");
                filesTable.getItems().add(new FileRecord(fileName, category, uploadDate));
            }
            
            if (!foundFiles) {
                filesTable.getItems().add(new FileRecord("Sample Resume.pdf", "Resume", "2023-04-24"));
                filesTable.getItems().add(new FileRecord("Portfolio.zip", "Web Development", "2023-04-24"));
                filesTable.getItems().add(new FileRecord("Project Proposal.docx", "Documentation", "2023-04-24"));
            }
        } catch (Exception e) {
            System.out.println("Error loading files: " + e.getMessage());
            e.printStackTrace();
            
            filesTable.getItems().add(new FileRecord("Sample Resume.pdf", "Resume", "2023-04-24"));
            filesTable.getItems().add(new FileRecord("Portfolio.zip", "Web Development", "2023-04-24"));
            filesTable.getItems().add(new FileRecord("Project Proposal.docx", "Documentation", "2023-04-24"));
        }
    }
    
    @FXML
    private void handleHomeClick(MouseEvent event) {
        try {
            String fxmlPath;
            String windowTitle;
            
            if (userRole != null) {
                if (userRole.equalsIgnoreCase("client")) {
                    fxmlPath = "/fxml/client_dashboard.fxml";
                    windowTitle = "Client Dashboard";
                } else if (userRole.equalsIgnoreCase("admin")) {
                    fxmlPath = "/fxml/admin_dashboard.fxml";
                    windowTitle = "Admin Dashboard";
                } else {
                    fxmlPath = "/fxml/freelancer_dashboard.fxml";
                    windowTitle = "Freelancer Dashboard";
                }
            } else {
                fxmlPath = "/fxml/freelancer_dashboard.fxml";
                windowTitle = "Freelancer Dashboard";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (loader.getController() instanceof ClientDashboardController) {
                ClientDashboardController controller = loader.getController();
                controller.setUserId(userId);
            } else if (loader.getController() instanceof FreelancerDashboardController) {
                FreelancerDashboardController controller = loader.getController();
                controller.setUserId(userId);
            } else if (loader.getController() instanceof AdminDashboardController) {
                AdminDashboardController controller = loader.getController();
                controller.setUserId(userId);
            }
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            
            Scene scene = new Scene(root);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("Could not load CSS: " + e.getMessage());
            }
            
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not logout: " + e.getMessage());
        }
    }
    
    @FXML
    private void showChangePasswordDialog(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        
        GridPane grid = new GridPane();
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return newPassword.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(password -> {
            if (!newPassword.getText().equals(confirmPassword.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match");
                return;
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                String verifySql = "SELECT user_id FROM Users WHERE user_id = ? AND password = ?";
                PreparedStatement verifyPs = conn.prepareStatement(verifySql);
                verifyPs.setInt(1, userId);
                verifyPs.setString(2, currentPassword.getText());
                ResultSet rs = verifyPs.executeQuery();
                
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }
                
                String updateSql = "UPDATE Users SET password = ? WHERE user_id = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, password);
                updatePs.setInt(2, userId);
                int rowsUpdated = updatePs.executeUpdate();
                
                if (rowsUpdated > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
                    realPassword = password;
                    if (passwordField != null) {
                        passwordField.setText("********");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating password: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void showFileShareDialog(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "File Sharing", "File sharing dialog will be implemented here");
    }
    
    @FXML
    private void showAddSkillDialog(ActionEvent event) {
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Add Skill");
        dialog.setHeaderText("Enter new skill details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        TextField skillNameField = new TextField();
        skillNameField.setPromptText("Skill Name");
        
        GridPane grid = new GridPane();
        grid.add(new Label("Skill Name:"), 0, 0);
        grid.add(skillNameField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Skill(skillNameField.getText(), "Pending");
            }
            return null;
        });
        
        Optional<Skill> result = dialog.showAndWait();
        result.ifPresent(skill -> {
            if (skill.getName().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Skill name cannot be empty");
                return;
            }
            
            skillsTable.getItems().add(skill);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Skill added successfully");
            
            new Thread(() -> {
                try (Connection conn = DBUtil.getConnection()) {
                    createSkillsTableIfNeeded();
                    
                    String sql = "INSERT INTO Skills (user_id, skill_name, status) VALUES (?, ?, ?)"; // Changed back to user_id
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId); // Use userId directly
                    ps.setString(2, skill.getName());
                    ps.setString(3, "Pending");
                    ps.executeUpdate();
                    System.out.println("Skill saved to database: " + skill.getName());
                } catch (Exception e) {
                    System.out.println("Error saving skill to database: " + e.getMessage());
                }
            }).start();
        });
    }
    
    @FXML
    private void handleUpdateSkill(ActionEvent event) {
        if (skillsTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Skills table not found");
            return;
        }
        
        Skill selectedSkill = skillsTable.getSelectionModel().getSelectedItem();
        if (selectedSkill == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a skill to update");
            return;
        }
        
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Update Skill");
        dialog.setHeaderText("Update skill details");
        
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        TextField skillNameField = new TextField(selectedSkill.getName());
        
        GridPane grid = new GridPane();
        grid.add(new Label("Skill Name:"), 0, 0);
        grid.add(skillNameField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                selectedSkill.setName(skillNameField.getText());
                return selectedSkill;
            }
            return null;
        });
        
        Optional<Skill> result = dialog.showAndWait();
        result.ifPresent(skill -> {
            if (skill.getName().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Skill name cannot be empty");
                return;
            }
            
            skillsTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Skill updated successfully");
        });
    }
    
    @FXML
    private void handleRemoveSkill(ActionEvent event) {
        if (skillsTable == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Skills table not found");
            return;
        }
        
        Skill selectedSkill = skillsTable.getSelectionModel().getSelectedItem();
        if (selectedSkill == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a skill to remove");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Remove Skill");
        alert.setContentText("Are you sure you want to remove the skill: " + selectedSkill.getName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            skillsTable.getItems().remove(selectedSkill);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Skill removed successfully");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void determineUserRole() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT role FROM Users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String role = rs.getString("role");
                System.out.println("Determined user role from database: " + role);
                setUserRole(role);
            } else {
                System.out.println("User not found for ID: " + userId);
                loadUserData();
            }
        } catch (Exception e) {
            System.err.println("Error determining user role: " + e.getMessage());
            e.printStackTrace();
            loadUserData();
        }
    }

    private void hideVerificationRelatedElements() {
        if (verificationBox != null) {
            verificationBox.setVisible(false);
            verificationBox.setManaged(false);
        }
        
        if (verificationLabel != null) {
            verificationLabel.setVisible(false);
            verificationLabel.setManaged(false);
        }
    }

    private void hideSkillsRelatedElements() {
        if (skillsTable != null) {
            skillsTable.setVisible(false);
            skillsTable.setManaged(false);
        }
        
        if (skillsTabPane != null) {
            for (Tab tab : skillsTabPane.getTabs()) {
                if (tab.getId() != null && tab.getId().toLowerCase().contains("skill")) {
                    tab.setDisable(true);
                }
                if (tab.getText() != null && tab.getText().toLowerCase().contains("skill")) {
                    tab.setDisable(true);
                }
            }
        }
    }

    private List<Node> findNodesWithText(Parent root, String... textPatterns) {
        List<Node> result = new ArrayList<>();
        if (root == null) return result;
        
        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof Labeled) {
                Labeled labeled = (Labeled) node;
                String text = labeled.getText();
                if (text != null) {
                    for (String pattern : textPatterns) {
                        if (text.toLowerCase().contains(pattern.toLowerCase())) {
                            result.add(node);
                            break;
                        }
                    }
                }
            } else if (node instanceof Parent) {
                result.addAll(findNodesWithText((Parent) node, textPatterns));
            }
        }
        return result;
    }
}

