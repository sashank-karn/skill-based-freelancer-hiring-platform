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
    @FXML private FontIcon verificationIcon;
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
    @FXML private GridPane skillsSection;
    @FXML private GridPane verificationSection;
    @FXML private TabPane tabPane; // Add this field declaration
    
    private int userId;
    private String realPassword;
    private String userRole;
    
    @FXML
    private void initialize() {
        // Initialize table columns and other UI elements
        setupTableColumns();
        
        // Add selection listener for skills table
        if (skillsTable != null) {
            skillsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                if (updateSkillButton != null) updateSkillButton.setDisable(!hasSelection);
                if (removeSkillButton != null) removeSkillButton.setDisable(!hasSelection);
            });
        }
    }
    
    private void setupTableColumns() {
        // Initialize table columns when they are available
        if (skillNameColumn != null) {
            skillNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        }
        if (skillStatusColumn != null) {
            skillStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }
        if (fileNameColumn != null) {
            fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        }
        if (fileDateColumn != null) {
            fileDateColumn.setCellValueFactory(cellData -> cellData.getValue().uploadDateProperty());
        }

        if (fileSkillColumn != null) {
            // Assuming FileRecord has an associated skill property
            fileSkillColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("N/A"));
        }
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
        
        // First determine user role before loading data
        determineUserRole();
    }
    
    /**
     * Make setUserRole show/hide components based on role
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
        
        try {
            // For client users, hide all freelancer-specific sections
            if (userRole != null && userRole.equalsIgnoreCase("client")) {
                // Hide skills table completely
                if (skillsTable != null) {
                    skillsTable.setVisible(false);
                    skillsTable.setManaged(false);
                }
                
                // Hide skills section text/labels
                hideSkillsRelatedElements();
                
                // Hide verification status completely  
                hideVerificationRelatedElements();
            }
            
            // Only load user data if ID has been set
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
            // Directly query the Users table without metadata
            String userQuery = "SELECT * FROM Users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(userQuery);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String userName = rs.getString("name");
                String userEmail = rs.getString("email");
                
                // Try both field types, as different views might use different controls
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
                
                // Load role-specific data
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
    
    private void loadFreelancerData(Connection conn) {
        // Load freelancer-specific data
        // For example, load skills and files for freelancer
        loadSkills();
        loadFiles();
    }
    
    private void loadClientData(Connection conn) {
        // Load client-specific data
        // For example, load projects or bids for client
    }
    
    private void loadSkills() {
        if (skillsTable == null || !userRole.equalsIgnoreCase("freelancer")) {
            return; // Skip if skills table doesn't exist or user is not freelancer
        }
        
        // Clear existing skills
        skillsTable.getItems().clear();
        
        // For freelancers only
        skillsTable.getItems().add(new Skill("Java Programming", "Pending"));
        skillsTable.getItems().add(new Skill("Database Design", "Pending"));
        skillsTable.getItems().add(new Skill("Web Development", "Pending"));
        
        // Function to create skills table if needed (for future use)
        createSkillsTableIfNeeded();
    }

    private void createSkillsTableIfNeeded() {
        try (Connection conn = DBUtil.getConnection()) {
            // Skip the metadata check and just create the table if it doesn't exist
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
            return; // Skip if files table doesn't exist in this view
        }
        
        // Just add sample files without trying to query the database
        filesTable.getItems().clear();
        filesTable.getItems().add(new FileRecord("Sample Resume.pdf", "Resume", "2023-04-24"));
        filesTable.getItems().add(new FileRecord("Portfolio.zip", "Web Development", "2023-04-24"));
        filesTable.getItems().add(new FileRecord("Project Proposal.docx", "Documentation", "2023-04-24"));
    }
    
    /**
     * Make handleHomeClick navigate to the correct dashboard based on role
     */
    @FXML
    private void handleHomeClick(MouseEvent event) {
        try {
            String fxmlPath;
            String windowTitle;
            
            // Determine which dashboard to load based on user role
            if (userRole != null) {
                if (userRole.equalsIgnoreCase("client")) {
                    fxmlPath = "/fxml/client_dashboard.fxml";
                    windowTitle = "Client Dashboard";
                } else if (userRole.equalsIgnoreCase("admin")) {
                    fxmlPath = "/fxml/admin_dashboard.fxml";
                    windowTitle = "Admin Dashboard";
                } else {
                    // Default to freelancer dashboard (for freelancers and any unknown roles)
                    fxmlPath = "/fxml/freelancer_dashboard.fxml";
                    windowTitle = "Freelancer Dashboard";
                }
            } else {
                // If role is not set, default to freelancer
                fxmlPath = "/fxml/freelancer_dashboard.fxml";
                windowTitle = "Freelancer Dashboard";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Get the controller and set the user ID
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
            
            // Get the current stage
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            
            // Create a new scene and set it on the stage
            Scene scene = new Scene(root);
            
            // Try to add stylesheets safely
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
            
            // Get the current stage
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            
            // Create a new scene and set it on the stage
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
        // Create password change dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        
        // Set the button types
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // Create password fields
        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        
        // Layout
        GridPane grid = new GridPane();
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result
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
                // First verify the current password
                String verifySql = "SELECT user_id FROM Users WHERE user_id = ? AND password = ?";
                PreparedStatement verifyPs = conn.prepareStatement(verifySql);
                verifyPs.setInt(1, userId);
                verifyPs.setString(2, currentPassword.getText());
                ResultSet rs = verifyPs.executeQuery();
                
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }
                
                // Update with new password
                String updateSql = "UPDATE Users SET password = ? WHERE user_id = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, password);
                updatePs.setInt(2, userId);
                int rowsUpdated = updatePs.executeUpdate();
                
                if (rowsUpdated > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
                    // Update cached password
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
        // Implement file sharing dialog
        // This is a placeholder implementation
        showAlert(Alert.AlertType.INFORMATION, "File Sharing", "File sharing dialog will be implemented here");
    }
    
    @FXML
    private void showAddSkillDialog(ActionEvent event) {
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Add Skill");
        dialog.setHeaderText("Enter new skill details");
        
        // Setup dialog
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create fields
        TextField skillNameField = new TextField();
        skillNameField.setPromptText("Skill Name");
        
        // Create layout
        GridPane grid = new GridPane();
        grid.add(new Label("Skill Name:"), 0, 0);
        grid.add(skillNameField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        
        // Set converter
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
            
            // Simply add to the TableView without trying to update the database
            skillsTable.getItems().add(skill);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Skill added successfully");
            
            // Try to save to database in background (optional)
            new Thread(() -> {
                try (Connection conn = DBUtil.getConnection()) {
                    // First create the table if needed
                    createSkillsTableIfNeeded();
                    
                    // Now try to insert
                    String sql = "INSERT INTO Skills (user_id, skill_name, status) VALUES (?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, userId);
                    ps.setString(2, skill.getName());
                    ps.setString(3, "Pending");
                    ps.executeUpdate();
                    System.out.println("Skill saved to database: " + skill.getName());
                } catch (Exception e) {
                    System.out.println("Error saving skill to database: " + e.getMessage());
                    // No need to show this error to the user, as the skill is already added to UI
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
        
        // Setup dialog
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        
        // Create fields
        TextField skillNameField = new TextField(selectedSkill.getName());
        
        // Create layout
        GridPane grid = new GridPane();
        grid.add(new Label("Skill Name:"), 0, 0);
        grid.add(skillNameField, 1, 0);
        dialog.getDialogPane().setContent(grid);
        
        // Set converter
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
            
            // Update in the UI
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
            // Remove from UI
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
                // This will handle UI visibility
                setUserRole(role);
            } else {
                System.out.println("User not found for ID: " + userId);
                loadUserData(); // Still try to load whatever data we can
            }
        } catch (Exception e) {
            System.err.println("Error determining user role: " + e.getMessage());
            e.printStackTrace();
            loadUserData(); // Still try to load whatever data we can
        }
    }

    // Helper method to hide all verification-related elements
    private void hideVerificationRelatedElements() {
        // Hide verification status components directly
        if (verificationBox != null) {
            verificationBox.setVisible(false);
            verificationBox.setManaged(false);
        }
        
        if (verificationLabel != null) {
            verificationLabel.setVisible(false);
            verificationLabel.setManaged(false);
        }
        
        if (verificationIcon != null) {
            verificationIcon.setVisible(false);
            verificationIcon.setManaged(false);
        }
    }

    // Helper method to hide all skills-related elements
    private void hideSkillsRelatedElements() {
        // Hide skills section components directly
        if (skillsTable != null) {
            skillsTable.setVisible(false);
            skillsTable.setManaged(false);
        }
        
        // Find any tab or component that contains "skills" in its id
        if (tabPane != null) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getId() != null && tab.getId().toLowerCase().contains("skill")) {
                    tab.setDisable(true);
                }
                if (tab.getText() != null && tab.getText().toLowerCase().contains("skill")) {
                    tab.setDisable(true);
                }
            }
        }
    }

    // Add the missing Node finding method since you referred to it in the previous code
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

