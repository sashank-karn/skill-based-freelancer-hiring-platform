package com.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class EditProjectController implements Initializable {
    
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField budgetField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField skillsField;
    
    private Project project;
    private int clientId;
    private ProjectDetailsController detailsController;
    private ClientDashboardController dashboardController;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoryComboBox.getItems().addAll(
            "Web Development", 
            "Mobile Development",
            "UI/UX Design",
            "Content Writing",
            "Graphic Design",
            "Data Analysis",
            "Video Editing",
            "Other"
        );
        
        budgetField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                budgetField.setText(oldValue);
            }
        });
    }
    
    public void setProject(Project project) {
        this.project = project;
        
        titleField.setText(project.getTitle());
        descriptionArea.setText(project.getDescription());
        budgetField.setText(String.valueOf(project.getBudget()));
        
        if (project.getDeadline() != null && !project.getDeadline().isEmpty()) {
            try {
                String[] parts = project.getDeadline().split("-");
                if (parts.length == 3) {
                    deadlinePicker.setValue(java.time.LocalDate.of(
                        Integer.parseInt(parts[0]), 
                        Integer.parseInt(parts[1]), 
                        Integer.parseInt(parts[2])
                    ));
                }
            } catch (Exception e) {
                System.out.println("Could not parse deadline: " + e.getMessage());
            }
        }
        
        categoryComboBox.setValue(project.getCategory());
        skillsField.setText(project.getSkills());
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    
    public void setParentControllers(ProjectDetailsController detailsController, 
                                   ClientDashboardController dashboardController) {
        this.detailsController = detailsController;
        this.dashboardController = dashboardController;
    }
    
    @FXML
    private void handleSave() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Project title cannot be empty");
            return;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Project description cannot be empty");
            return;
        }
        
        if (budgetField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Project budget cannot be empty");
            return;
        }
        
        if (deadlinePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a deadline");
            return;
        }
        
        try {
            double budget = Double.parseDouble(budgetField.getText());
            String deadline = deadlinePicker.getValue().toString();
            String category = categoryComboBox.getValue() != null ? categoryComboBox.getValue() : "General";
            String skills = skillsField.getText();
            
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE Projects SET title = ?, description = ?, budget = ?, " +
                             "deadline = ?, category = ?, skills = ? WHERE project_id = ?";
                
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, titleField.getText().trim());
                ps.setString(2, descriptionArea.getText().trim());
                ps.setDouble(3, budget);
                ps.setString(4, deadline);
                ps.setString(5, category);
                ps.setString(6, skills);
                ps.setString(7, project.getProjectId());
                
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    project.setTitle(titleField.getText().trim());
                    project.setDescription(descriptionArea.getText().trim());
                    project.setBudget(budget);
                    project.setDeadline(deadline);
                    project.setCategory(category);
                    project.setSkills(skills);
                    
                    if (detailsController != null) {
                        detailsController.refreshProjectDetails(project);
                    }
                    
                    if (dashboardController != null) {
                        dashboardController.refreshProjects();
                    }
                    
                    ((Stage) titleField.getScene().getWindow()).close();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Project updated successfully");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update project");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Budget must be a valid number");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update project: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }
}