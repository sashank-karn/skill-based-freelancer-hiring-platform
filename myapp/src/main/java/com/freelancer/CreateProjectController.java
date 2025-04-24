package com.freelancer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CreateProjectController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField budgetField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> categoryComboBox;
    
    private int clientId;
    private ClientDashboardController parentController;
    private UserSession userSession;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Web Development",
            "Mobile Development",
            "Graphic Design",
            "Writing",
            "Marketing",
            "Video & Animation",
            "Music & Audio",
            "Programming & Tech",
            "Business",
            "Other"
        ));
        
        categoryComboBox.setValue("Web Development");
        deadlinePicker.setValue(LocalDate.now().plusWeeks(2));
        
        budgetField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                budgetField.setText(oldValue);
            }
        });
        
        userSession = UserSession.getInstance();
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setParentController(ClientDashboardController parentController) {
        this.parentController = parentController;
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        ((Stage) titleField.getScene().getWindow()).close();
    }
    
    @FXML
    private void createNewProject(ActionEvent event) {
        handleCreateProject(event);
    }
    
    private boolean validateForm() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String budget = budgetField.getText().trim();
        
        StringBuilder errors = new StringBuilder();
        
        if (title.isEmpty()) {
            errors.append("- Project title is required\n");
        } else if (title.length() < 5) {
            errors.append("- Project title must be at least 5 characters\n");
        }
        
        if (description.isEmpty()) {
            errors.append("- Project description is required\n");
        } else if (description.length() < 30) {
            errors.append("- Please provide a more detailed description (at least 30 characters)\n");
        }
        
        if (budget.isEmpty()) {
            errors.append("- Budget is required\n");
        } else {
            try {
                double budgetValue = Double.parseDouble(budget);
                
                if (budgetValue <= 0) {
                    errors.append("- Budget must be positive\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Budget must be a valid number\n");
            }
        }
        
        if (deadlinePicker.getValue() == null) {
            errors.append("- Deadline is required\n");
        } else if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("- Deadline cannot be in the past\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }
    
    private String generateClientId(Connection conn) throws SQLException {
        String id = "C001";
        
        try {
            String query = "SELECT MAX(client_id) FROM Client WHERE client_id LIKE 'C%'";
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            if (rs.next() && rs.getString(1) != null) {
                String lastId = rs.getString(1);
                int num = Integer.parseInt(lastId.substring(1)) + 1;
                id = "C" + String.format("%03d", num);
            }
        } catch (Exception e) {
            System.err.println("Error generating client ID: " + e.getMessage());
        }
        
        return id;
    }

    private String generateProjectId(Connection conn) {
        try {
            String query = "SELECT project_id FROM Projects ORDER BY LENGTH(project_id) DESC, project_id DESC LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            String newId = "P001";
            
            if (rs.next()) {
                String currentId = rs.getString("project_id");
                System.out.println("Current highest project ID: " + currentId);
                
                if (currentId != null && currentId.startsWith("P")) {
                    try {
                        int num = Integer.parseInt(currentId.substring(1));
                        newId = "P" + String.format("%03d", num + 1);
                    } catch (NumberFormatException e) {
                        newId = "P" + System.currentTimeMillis() % 10000;
                    }
                } else {
                    newId = "P" + System.currentTimeMillis() % 10000;
                }
            }
            
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Projects WHERE project_id = ?");
            checkStmt.setString(1, newId);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                newId = "P" + System.currentTimeMillis() % 10000;
            }
            
            System.out.println("Generated new project ID: " + newId);
            return newId;
            
        } catch (SQLException e) {
            System.err.println("Error generating project ID: " + e.getMessage());
            return "P" + System.currentTimeMillis() % 10000;
        }
    }

    @FXML
    private void handleCreateProject(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = categoryComboBox.getValue();
        
        double budgetValue;
        try {
            budgetValue = Double.parseDouble(budgetField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Budget must be a valid number");
            return;
        }
        
        String deadline = deadlinePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        System.out.println("Creating project with user ID: " + clientId);
        
        try (Connection conn = DBUtil.getConnection()) {
            String checkClientSQL = "SELECT client_id FROM client WHERE user_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkClientSQL);
            checkPs.setInt(1, clientId);
            ResultSet rs = checkPs.executeQuery();
            
            String actualClientId;
            if (rs.next()) {
                actualClientId = rs.getString("client_id");
                System.out.println("Using existing client_id: " + actualClientId);
            } else {
                actualClientId = generateClientId(conn);
                System.out.println("Generated new client_id: " + actualClientId);
                
                String insertClientSQL = "INSERT INTO client (client_id, user_id, name) VALUES (?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertClientSQL);
                insertPs.setString(1, actualClientId);
                insertPs.setInt(2, clientId);
                insertPs.setString(3, "Client " + clientId);
                
                insertPs.executeUpdate();
                System.out.println("Created new client record with ID: " + actualClientId);
            }
            
            String projectId = generateProjectId(conn);
            
            String insertSQL = "INSERT INTO Projects (project_id, client_id, title, description, category, status, budget, deadline) " +
                         "VALUES (?, ?, ?, ?, ?, 'Open', ?, ?)";
            
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            ps.setString(1, projectId);
            ps.setString(2, actualClientId);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, category);
            ps.setString(6, String.valueOf(budgetValue));
            ps.setString(7, deadline);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Project created successfully");
                
                if (parentController != null) {
                    parentController.refreshProjects();
                }
                
                ((Stage) titleField.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not create project");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save project: " + e.getMessage());
        }
    }
}