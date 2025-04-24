package com.freelancer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {
    // Add FontIcon fields
    @FXML private FontIcon profileIcon;
    @FXML private FontIcon logoutIcon;
    
    @FXML private Label welcomeLabel;
    @FXML private Label projectsCountLabel;
    @FXML private Label activeProjectsLabel;
    @FXML private Label completedProjectsLabel;
    
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> titleColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, String> deadlineColumn;
    @FXML private TableColumn<Project, String> budgetColumn;
    @FXML private TableColumn<Project, Integer> proposalsColumn;
    
    private int userId;
    private ObservableList<Project> projectsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
    }
    
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        deadlineColumn.setCellValueFactory(cellData -> cellData.getValue().deadlineProperty());
        // Change this line in your setupTableColumns() method
budgetColumn.setCellValueFactory(cellData -> cellData.getValue().formattedBudgetProperty());
        proposalsColumn.setCellValueFactory(cellData -> cellData.getValue().proposalCountProperty().asObject());
        
        // Apply styling to status column
        statusColumn.setCellFactory(column -> StyleManager.createStatusCell(column));
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
        loadUserData();
        loadProjects();
    }
    
    private void loadUserData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Users WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Try different column names for username with better fallback
                String userName = "Client"; // Default value if nothing else works
                
                // Try each possible column name in order of preference
                try {
                    String nameValue = rs.getString("username");
                    if (nameValue != null && !nameValue.isEmpty()) {
                        userName = nameValue;
                    }
                } catch (Exception ignored) {}
                
                if (userName.equals("Client")) {
                    try {
                        String nameValue = rs.getString("name");
                        if (nameValue != null && !nameValue.isEmpty()) {
                            userName = nameValue;
                        }
                    } catch (Exception ignored) {}
                }
                
                if (userName.equals("Client")) {
                    try {
                        String nameValue = rs.getString("first_name");
                        if (nameValue != null && !nameValue.isEmpty()) {
                            userName = nameValue;
                        }
                    } catch (Exception ignored) {}
                }
                
                // Only set welcome message if welcomeLabel exists
                if (welcomeLabel != null) {
                    welcomeLabel.setText("Welcome, " + userName + "!");
                }
            } else {
                // User not found, use default
                if (welcomeLabel != null) {
                    welcomeLabel.setText("Welcome, Client!");
                }
            }
        } catch (Exception e) {
            // Log error but don't show alert - less intrusive
            System.err.println("Could not load user data: " + e.getMessage());
            e.printStackTrace();
            
            // Set default welcome message
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome to your dashboard!");
            }
        }
    }
    
    private void loadProjects() {
    // Clear existing projects
    projectsList.clear();
    
    try (Connection conn = DBUtil.getConnection()) {
        // First get client ID for this user
        String clientIdQuery = "SELECT client_id FROM client WHERE user_id = ?";
        PreparedStatement clientStmt = conn.prepareStatement(clientIdQuery);
        clientStmt.setInt(1, userId);
        ResultSet clientRs = clientStmt.executeQuery();
        
        String actualClientId = null;
        if (clientRs.next()) {
            actualClientId = clientRs.getString("client_id");
        }
        
        if (actualClientId == null) {
            // If we can't find a client ID, there's nothing to load
            updateProjectCountsWithDefaults();
            return;
        }
        
        // Now fetch projects with the client ID
        String sql = "SELECT * FROM Projects WHERE client_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, actualClientId);
        ResultSet rs = ps.executeQuery();
        
        int totalCount = 0;
        int activeCount = 0;
        int completedCount = 0;
        
        while (rs.next()) {
            // Add to project list
            String projectId = rs.getString("project_id");
            String title = rs.getString("title");
            String description = rs.getString("description");
            String status = rs.getString("status");
            String budget = rs.getString("budget");
            String deadline = rs.getString("deadline");
            
            Project project = new Project(projectId, title, description, status, budget, deadline, userId);
            projectsList.add(project);
            
            // Update counts
            totalCount++;
            if ("Completed".equalsIgnoreCase(status)) {
                completedCount++;
            } else {
                activeCount++;
            }
        }
        
        // Update counts safely
        updateProjectCounts(totalCount, activeCount, completedCount);
        
        // Set the projects in the table
        projectsTable.setItems(projectsList);
        
    } catch (Exception e) {
        e.printStackTrace();
        updateProjectCountsWithDefaults();
        showAlert(Alert.AlertType.ERROR, "Database Error", 
                  "Could not load projects: " + e.getMessage());
    }
}

    private void createProjectsTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE Projects (" +
                "project_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "client_id INT NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "status VARCHAR(20) DEFAULT 'Open', " +
                "budget VARCHAR(50), " +
                "deadline VARCHAR(50), " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "category VARCHAR(50), " +                          
                "skills VARCHAR(200), " +                           
                "creation_date DATE DEFAULT (CURDATE()), " +  // Changed this line
                "completion_date DATE, " +                   
                "assigned_freelancer_id INT, " +             
                "assigned_freelancer_name VARCHAR(100), " +   
                "featured TINYINT(1) DEFAULT 0, " +          // Changed this line
                "payment_status VARCHAR(20) DEFAULT 'Unpaid', " +  
                "FOREIGN KEY (client_id) REFERENCES Users(user_id))";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Projects table created successfully");
            
            // Also create Proposals table if it doesn't exist
            String createProposalsTableSQL = "CREATE TABLE Proposals (" +
                    "proposal_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "project_id INT NOT NULL, " +
                    "freelancer_id INT NOT NULL, " +
                    "bid_amount DECIMAL(10,2) NOT NULL, " +
                    "cover_letter TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (project_id) REFERENCES Projects(project_id), " +
                    "FOREIGN KEY (freelancer_id) REFERENCES Users(user_id))";
            
            stmt.execute(createProposalsTableSQL);
            System.out.println("Proposals table created successfully");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            throw e;
        }
    }
    
    private void addSampleProjects(Connection conn) throws SQLException {
        String[] sampleProjects = {
            "Website Redesign", "Modern website redesign with responsive design", "Open", "$500", "2023-09-30",
            "Mobile App Development", "Android app for inventory management", "In Progress", "$2000", "2023-10-15",
            "Logo Design", "New company logo and branding materials", "Completed", "$200", "2023-08-20"
        };
        
        String insertSQL = "INSERT INTO Projects (client_id, title, description, status, budget, deadline) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            for (int i = 0; i < sampleProjects.length; i += 5) {
                ps.setInt(1, userId);  // Use the current user's ID
                ps.setString(2, sampleProjects[i]);
                ps.setString(3, sampleProjects[i + 1]);
                ps.setString(4, sampleProjects[i + 2]);
                ps.setString(5, sampleProjects[i + 3]);
                ps.setString(6, sampleProjects[i + 4]);
                
                int rowsInserted = ps.executeUpdate();
                System.out.println("Added sample project: " + sampleProjects[i] + " (rows: " + rowsInserted + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting sample project: " + e.getMessage());
            e.printStackTrace();
            throw e;  // Rethrow to handle in calling method
        }
    }
    
    private void addSampleProjectsToUI() {
    projectsList.clear();
    
    // First ensure client exists in the client table
    try (Connection conn = DBUtil.getConnection()) {
        // Check if client exists
        String checkClientSQL = "SELECT client_id FROM client WHERE user_id = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkClientSQL);
        checkPs.setInt(1, userId);
        ResultSet rs = checkPs.executeQuery();
        
        int clientIdToUse = userId; // Default if not found
        
        if (rs.next()) {
            // Get actual client_id from database
            clientIdToUse = rs.getInt("client_id");
            System.out.println("Using existing client_id: " + clientIdToUse);
        } else {
            // Client doesn't exist, create one
            String insertClientSQL = "INSERT INTO client (client_id, user_id, name) VALUES (?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertClientSQL);
            insertPs.setInt(1, userId); // Use userId as clientId for simplicity
            insertPs.setInt(2, userId);
            insertPs.setString(3, "Client " + userId);
            insertPs.executeUpdate();
            System.out.println("Created new client with ID: " + userId);
        }
        
        // Now create sample projects with the valid client ID
        projectsList.addAll(
            new Project("P001", "Website Redesign", "Modern website redesign with responsive design", "Open", "$500", "2023-09-30", clientIdToUse),
            new Project("P002", "Mobile App Development", "Android app for inventory management", "In Progress", "$2000", "2023-10-15", clientIdToUse),
            new Project("P003", "Logo Design", "New company logo and branding materials", "Completed", "$200", "2023-08-20", clientIdToUse)
        );
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Error ensuring client exists: " + e.getMessage());
        
        // If database operations fail, just show UI without DB interaction
        projectsList.addAll(
            new Project("P001", "Website Redesign", "Modern website redesign with responsive design", "Open", "$500", "2023-09-30", userId),
            new Project("P002", "Mobile App Development", "Android app for inventory management", "In Progress", "$2000", "2023-10-15", userId),
            new Project("P003", "Logo Design", "New company logo and branding materials", "Completed", "$200", "2023-08-20", userId)
        );
    }
    
    // Update stats
    projectsCountLabel.setText(String.valueOf(3));
    activeProjectsLabel.setText(String.valueOf(2));
    completedProjectsLabel.setText(String.valueOf(1));
    
    projectsTable.setItems(projectsList);
}
    
    @FXML
    public void createNewProject(ActionEvent event) {  // Changed to public
        System.out.println("Create project button clicked!");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_project.fxml"));
            Parent root = loader.load();
            
            CreateProjectController controller = loader.getController();
            controller.setClientId(userId);
            controller.setParentController(this);
            
            // Debug output
            System.out.println("Creating project with user ID: " + userId);
            
            // Create window
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);
            
            Stage stage = new Stage();
            stage.setTitle("Create New Project");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open create project window: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleProjectSelection() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            showProjectDetailsDialog(selectedProject);
        }
    }
    
    private void showProjectDetailsDialog(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/project_details.fxml"));
            Parent root = loader.load();
            
            ProjectDetailsController controller = loader.getController();
            controller.setProject(project);
            controller.setClientId(userId);
            controller.setParentController(this);
            
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Project Details: " + project.getTitle());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open project details window.");
        }
    }
    
    // Update method signature to accept MouseEvent instead of ActionEvent
    @FXML
    private void handleViewProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile_view.fxml"));
            Parent root = loader.load();
            
            ProfileViewController controller = loader.getController();
            controller.setUserId(userId);
            controller.setUserRole("client");  // Explicitly set as client
            
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);
            
            Stage stage = (Stage) projectsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open profile: " + e.getMessage());
        }
    }
    
    // Update method signature to accept MouseEvent instead of ActionEvent
    @FXML
    private void handleLogout(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        StyleManager.styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Clear user session
                UserSession.getInstance().clear();
                
                // Return to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                StyleManager.applyStylesheets(scene);
                
                Stage stage = (Stage) projectsTable.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Freelancer Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not return to login page: " + e.getMessage());
            }
        }
    }
    
    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }
    
    // Method to refresh projects list (called from child windows)
    public void refreshProjects() {
        loadProjects();
    }
    
    // Add these two helper methods for project counts
private void updateProjectCounts(int total, int active, int completed) {
    // Safely update the label counters
    Platform.runLater(() -> {
        try {
            if (projectsCountLabel != null) {
                projectsCountLabel.setText(String.valueOf(total));
            }
            
            if (activeProjectsLabel != null) {
                activeProjectsLabel.setText(String.valueOf(active));
            }
            
            if (completedProjectsLabel != null) {
                completedProjectsLabel.setText(String.valueOf(completed));
            }
        } catch (Exception e) {
            System.err.println("Could not update project counts: " + e.getMessage());
            e.printStackTrace();
        }
    });
}

// Add this helper method for defaults
private void updateProjectCountsWithDefaults() {
    updateProjectCounts(0, 0, 0);
}
}
