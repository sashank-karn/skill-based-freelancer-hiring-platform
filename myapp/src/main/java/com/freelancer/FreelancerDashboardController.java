package com.freelancer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayDeque; 
import java.util.Deque;
import java.util.Optional;

import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

public class FreelancerDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button notificationsButton;
    @FXML private Button backButton;
    @FXML private Label totalEarningsLabel;
    @FXML private Label activeProjectsLabel;
    @FXML private Label completedProjectsLabel;
    @FXML private TabPane tabPane;
    @FXML private ListView<String> skillsListView;
    @FXML private ListView<String> projectsListView;
    @FXML private ListView<String> earningsListView;
    @FXML private TableView<Project> projectsTableView;
    @FXML private TableColumn<Project, String> colProjectId;
    @FXML private TableColumn<Project, String> colTitle;
    @FXML private TableColumn<Project, String> colDescription;
    @FXML private TableColumn<Project, Number> colBudget;
    @FXML private TableColumn<Project, String> colDeadline;
    @FXML private TableColumn<Project, String> colStatus;
    @FXML private TableView<Proposal> proposalsTableView;
    @FXML private TableColumn<Proposal, String> colProposalId;
    @FXML private TableColumn<Proposal, String> colProposalProject;
    @FXML private TableColumn<Proposal, Double> colProposalBid;
    @FXML private TableColumn<Proposal, String> colProposalTimeline;
    @FXML private TableColumn<Proposal, String> colProposalStatus;
    @FXML private Label projectsErrorLabel;
    @FXML private Label proposalsErrorLabel;
    @FXML private Tab projectsTab;
    @FXML private TableView<FileRecord> filesTableView;
    @FXML private TableColumn<FileRecord, String> colFileName;
    @FXML private TableColumn<FileRecord, String> colFileDate;

    @FXML private TableView<Project> browseProjectsTable;
    @FXML private TableColumn<Project, String> projectTitleColumn;
    @FXML private TableColumn<Project, String> projectDescriptionColumn;
    @FXML private TableColumn<Project, Double> budgetColumn;
    @FXML private TableColumn<Project, String> deadlineColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableView<Proposal> proposalsTable;
    @FXML private TableColumn<Proposal, String> proposalProjectTitleColumn;
    @FXML private TableColumn<Proposal, Double> proposalBidAmountColumn;
    @FXML private TableColumn<Proposal, String> proposalTimelineColumn;
    @FXML private TableColumn<Proposal, String> proposalStatusColumn;

    @FXML private TextField bidAmountField;
    @FXML private TextField timelineField;

    @FXML private Label browseProjectsErrorLabel;
    @FXML private Label myProjectsErrorLabel;

    @FXML private FontIcon profileIcon;
    @FXML private FontIcon logoutIcon;
    @FXML private FontIcon settingsIcon;

    private String userName;
    private int userId;
    private boolean isDarkMode = false;
    private final Deque<String> pageHistory = new ArrayDeque<>();
    private String currentPage = "/fxml/freelancer_dashboard.fxml";
    private boolean initialized = false;

    @FXML
    private void initialize() {
        // Hide the back button by default
        if (backButton != null) {
            backButton.setVisible(false);
        }

        initializeTableColumns();
        
        // Don't load data yet - wait for userId to be set
        initialized = true;
    }
    
    private void initializeTableColumns() {
        // Initialize table columns only if they exist
        if (colProjectId != null) {
            colProjectId.setCellValueFactory(cellData -> cellData.getValue().projectIdProperty());
        }
        if (colTitle != null) {
            colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        }
        if (colDescription != null) {
            colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        }
        if (colBudget != null) {
            colBudget.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());
        }
        if (colDeadline != null) {
            colDeadline.setCellValueFactory(cellData -> cellData.getValue().deadlineProperty());
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }

        if (projectTitleColumn != null) {
            projectTitleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        }
        if (projectDescriptionColumn != null) {
            projectDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        }
        if (budgetColumn != null) {
            budgetColumn.setCellValueFactory(cellData -> cellData.getValue().budgetProperty().asObject());
        }
        if (deadlineColumn != null) {
            deadlineColumn.setCellValueFactory(cellData -> cellData.getValue().deadlineProperty());
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }

        if (proposalProjectTitleColumn != null) {
            proposalProjectTitleColumn.setCellValueFactory(cellData -> cellData.getValue().projectTitleProperty());
        }
        if (proposalBidAmountColumn != null) {
            proposalBidAmountColumn.setCellValueFactory(cellData -> cellData.getValue().bidAmountProperty().asObject());
        }
        if (proposalTimelineColumn != null) {
            proposalTimelineColumn.setCellValueFactory(cellData -> cellData.getValue().timelineProperty());
        }
        if (proposalStatusColumn != null) {
            proposalStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }

        if (colProposalId != null) {
            colProposalId.setCellValueFactory(cellData -> cellData.getValue().proposalIdProperty());
        }
        if (colProposalProject != null) {
            colProposalProject.setCellValueFactory(cellData -> cellData.getValue().projectIdProperty());
        }
        if (colProposalBid != null) {
            colProposalBid.setCellValueFactory(cellData -> cellData.getValue().bidAmountProperty().asObject());
        }
        if (colProposalTimeline != null) {
            colProposalTimeline.setCellValueFactory(cellData -> cellData.getValue().timelineProperty());
        }
        if (colProposalStatus != null) {
            colProposalStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        }

        // Set up cell factories for text wrapping
        setupCellFactories();
    }
    
    private void setupCellFactories() {
        // Set up cell factories for text wrapping only if columns exist
        if (colProposalProject != null) {
            colProposalProject.setCellFactory(tc -> {
                TableCell<Proposal, String> cell = new TableCell<>() {
                    private final Text text = new Text();
                    {
                        text.wrappingWidthProperty().bind(tc.widthProperty());
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        text.setText(empty ? null : item);
                    }
                };
                return cell;
            });
        }
        
        if (colProposalTimeline != null) {
            colProposalTimeline.setCellFactory(tc -> {
                TableCell<Proposal, String> cell = new TableCell<>() {
                    private final Text text = new Text();
                    {
                        text.wrappingWidthProperty().bind(tc.widthProperty());
                        setGraphic(text);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        text.setText(empty ? null : item);
                    }
                };
                return cell;
            });
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + userName);
        }
        
        // Load data if user ID is already set
        if (userId > 0) {
            loadAllData();
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
        
        // Only load data if already initialized
        if (initialized && userId > 0) {
            loadAllData();
        }
    }
    
    private void loadAllData() {
        try {
            loadProjects();
            loadProposals();
            loadDashboardOverview();
            if (filesTableView != null) {
                loadFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading data: " + e.getMessage());
        }
    }

    private void loadDashboardOverview() {
        if (userId <= 0) {
            System.err.println("Cannot load dashboard overview: userId not set");
            return;
        }
        
        try (Connection conn = DBUtil.getConnection()) {
            if (totalEarningsLabel != null) {
                String sql = "SELECT calculate_total_earnings(?) AS total_earnings";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalEarningsLabel.setText("Total Earnings: $" + rs.getDouble("total_earnings"));
                }
            }

            if (activeProjectsLabel != null) {
                String sql = "SELECT COUNT(*) AS active_projects FROM Projects P " +
                             "JOIN Proposals Pr ON P.project_id = Pr.project_id " +
                             "WHERE Pr.user_id = ? AND P.status = 'Active'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    activeProjectsLabel.setText("Active Projects: " + rs.getInt("active_projects"));
                }
            }

            if (completedProjectsLabel != null) {
                String sql = "SELECT COUNT(*) AS completed_projects FROM Projects P " +
                             "JOIN Proposals Pr ON P.project_id = Pr.project_id " +
                             "WHERE Pr.user_id = ? AND P.status = 'Completed'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    completedProjectsLabel.setText("Completed Projects: " + rs.getInt("completed_projects"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while loading dashboard overview.");
        }
    }

    @FXML
    private void loadProposals() {
        if (userId <= 0) {
            System.err.println("Cannot load proposals: userId not set");
            return;
        }
        
        // First check which table view is actually in your UI
        TableView<Proposal> targetTable = null;
        
        if (proposalsTable != null) {
            targetTable = proposalsTable;
        } else if (proposalsTableView != null) {
            targetTable = proposalsTableView;
        }
        
        if (targetTable != null) {
            targetTable.getItems().clear(); // Clear the table before loading new data
            
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "SELECT Pr.proposal_id, Pr.project_id, P.title AS project_title, Pr.bid_amount, Pr.timeline, Pr.status " +
                             "FROM Proposals Pr " +
                             "JOIN Projects P ON Pr.project_id = P.project_id " +
                             "WHERE Pr.user_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
    
                boolean dataLoaded = false;
                while (rs.next()) {
                    dataLoaded = true;
                    targetTable.getItems().add(new Proposal(
                        rs.getString("proposal_id"),
                        rs.getString("project_id"),
                        rs.getString("project_title"),
                        rs.getDouble("bid_amount"),
                        rs.getString("timeline"),
                        rs.getString("status")
                    ));
                }
                
                // Provide feedback if no data was found
                if (!dataLoaded) {
                    System.out.println("No proposals found for user ID: " + userId);
                    // Optionally update a label in the UI to show "No proposals found"
                    if (proposalsErrorLabel != null) {
                        proposalsErrorLabel.setText("No proposals found");
                        proposalsErrorLabel.setVisible(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading proposals: " + e.getMessage());
                showAlert("An error occurred while loading proposals: " + e.getMessage());
            }
        } else {
            System.err.println("Both proposalsTable and proposalsTableView are null");
        }
    }
    

    @FXML
    private void loadProjects() {
        if (browseProjectsTable != null) {
            browseProjectsTable.getItems().clear();
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "SELECT project_id, title, description, budget, deadline, status FROM Projects";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                boolean dataLoaded = false;
                while (rs.next()) {
                    dataLoaded = true;
                    browseProjectsTable.getItems().add(new Project(
                        rs.getString("project_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDouble("budget"),
                        rs.getString("deadline"),
                        rs.getString("status")
                    ));
                }
                
                if (!dataLoaded && browseProjectsErrorLabel != null) {
                    browseProjectsErrorLabel.setText("No available projects found");
                    browseProjectsErrorLabel.setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("An error occurred while loading projects.");
            }
        }
    }

    @FXML
    private void loadFiles() {
        if (userId <= 0 || filesTableView == null) {
            return;
        }
        
        filesTableView.getItems().clear();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT file_id, file_name, upload_date FROM Files WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filesTableView.getItems().add(new FileRecord(
                    rs.getString("file_id"),
                    rs.getString("file_name"),
                    rs.getString("upload_date")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while loading files.");
        }
    }

    @FXML
    private void handleUploadFile() {
        if (filesTableView == null || myProjectsErrorLabel == null) {
            showAlert("File upload components not available");
            return;
        }
        
        // Open a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(filesTableView.getScene().getWindow());
        if (selectedFile != null) {
            try (Connection conn = DBUtil.getConnection()) {
                // Insert the file record into the database
                String sql = "INSERT INTO Files (file_name, user_id, upload_date) VALUES (?, ?, NOW())";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedFile.getName());
                ps.setInt(2, userId);
                ps.executeUpdate();

                // Refresh the files table
                loadFiles();

                showAlert("File uploaded successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                myProjectsErrorLabel.setText("Error uploading file: " + e.getMessage());
                myProjectsErrorLabel.setVisible(true);
            }
        }
    }

    private void styleDialog(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog-style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            System.err.println("Failed to style dialog: " + e.getMessage());
        }
    }

    private void styleAlert(Alert alert) {
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog-style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("custom-dialog");
        } catch (Exception e) {
            System.err.println("Failed to style alert: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply custom CSS to the alert
        styleAlert(alert);

        alert.showAndWait();
    }


    private String getPageTitle(String fxmlPath) {
        switch (fxmlPath) {
            case "/fxml/help_center.fxml":
                return "Help Center";
            case "/fxml/account_settings.fxml":
                return "Account Settings";
            default:
                return "Freelancer Dashboard";
        }
    }

 

    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Get any control in the scene to access the window
            Control control = backButton != null ? backButton : welcomeLabel;
            if (control == null) {
                throw new RuntimeException("No control available to access scene window");
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) control.getScene().getWindow();
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while logging out: " + e.getMessage());
        }
    }

    @FXML
    private void openProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile_view.fxml"));
            Parent root = loader.load();
            
            ProfileViewController controller = loader.getController();
            controller.setUserId(userId); // Pass the user ID
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) profileIcon.getScene().getWindow(); 
            stage.setScene(scene);
            stage.setTitle("Profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading profile page: " + e.getMessage());
        }
    }

    @FXML
    private void openSkillsDialog() {
        if (skillsListView == null) {
            showAlert("Skills list not available");
            return;
        }
        
        try {
            // Create a dialog to display skills
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Skills");
            dialog.setHeaderText("Your Skills");

            // Create a ListView to display skills
            ListView<String> skillsList = new ListView<>();
            skillsList.setItems(skillsListView.getItems()); // Use the existing skillsListView items

            // Add the ListView to the dialog
            dialog.getDialogPane().setContent(skillsList);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Apply custom CSS to the dialog
            styleDialog(dialog);

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while opening the Skills dialog: " + e.getMessage());
        }
    }

    @FXML
    private void switchToBrowseProjects() {
        if (tabPane == null || projectsTab == null) {
            showAlert("Project browsing is not available");
            return;
        }
        
        try {
            // Switch to the "Browse Projects" tab
            tabPane.getSelectionModel().select(projectsTab);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred while switching to the Browse Projects tab: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyToProject() {
        if (browseProjectsTable == null || browseProjectsErrorLabel == null || 
            bidAmountField == null || timelineField == null) {
            showAlert("Project application components not available");
            return;
        }
        
        // Validate that a project is selected
        Project selectedProject = browseProjectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            browseProjectsErrorLabel.setText("Please select a project to apply.");
            browseProjectsErrorLabel.setVisible(true);
            return;
        }
        browseProjectsErrorLabel.setVisible(false);

        // Validate bid amount and timeline
        String bidAmount = bidAmountField.getText();
        String timeline = timelineField.getText();

        if (bidAmount.isEmpty() || timeline.isEmpty()) {
            browseProjectsErrorLabel.setText("Please enter bid amount and timeline.");
            browseProjectsErrorLabel.setVisible(true);
            return;
        }

        try {
            double bid = Double.parseDouble(bidAmount);
            if (bid <= 0) {
                browseProjectsErrorLabel.setText("Bid amount must be greater than 0.");
                browseProjectsErrorLabel.setVisible(true);
                return;
            }
        } catch (NumberFormatException e) {
            browseProjectsErrorLabel.setText("Bid amount must be a valid number.");
            browseProjectsErrorLabel.setVisible(true);
            return;
        }

        // Save the proposal to the database
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO Proposals (project_id, user_id, bid_amount, timeline, status) VALUES (?, ?, ?, ?, 'Pending')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, selectedProject.getProjectId());
            ps.setInt(2, userId);
            ps.setDouble(3, Double.parseDouble(bidAmount));
            ps.setString(4, timeline);
            ps.executeUpdate();

            showAlert("Proposal submitted successfully!");
            bidAmountField.clear();
            timelineField.clear();
            
            // Refresh proposals display
            loadProposals();
        } catch (Exception e) {
            e.printStackTrace();
            browseProjectsErrorLabel.setText("Error submitting proposal: " + e.getMessage());
            browseProjectsErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleUpdateProposal() {
        if (proposalsTable == null || proposalsErrorLabel == null) {
            showAlert("Proposal update components not available");
            return;
        }
        
        // Validate that a proposal is selected
        Proposal selectedProposal = proposalsTable.getSelectionModel().getSelectedItem();
        if (selectedProposal == null) {
            proposalsErrorLabel.setText("Please select a proposal to update.");
            proposalsErrorLabel.setVisible(true);
            return;
        }
        proposalsErrorLabel.setVisible(false);

        // Show a dialog to update the bid amount and timeline
        TextInputDialog bidDialog = new TextInputDialog(String.valueOf(selectedProposal.getBidAmount()));
        bidDialog.setTitle("Update Proposal");
        bidDialog.setHeaderText("Update Bid Amount");
        bidDialog.setContentText("Enter new bid amount:");

        TextInputDialog timelineDialog = new TextInputDialog(selectedProposal.getTimeline());
        timelineDialog.setTitle("Update Proposal");
        timelineDialog.setHeaderText("Update Timeline");
        timelineDialog.setContentText("Enter new timeline:");

        Optional<String> bidResult = bidDialog.showAndWait();
        Optional<String> timelineResult = timelineDialog.showAndWait();

        if (bidResult.isPresent() && timelineResult.isPresent()) {
            try {
                double newBidAmount = Double.parseDouble(bidResult.get());
                String newTimeline = timelineResult.get();

                if (newBidAmount <= 0 || newTimeline.isEmpty()) {
                    proposalsErrorLabel.setText("Invalid bid amount or timeline.");
                    proposalsErrorLabel.setVisible(true);
                    return;
                }

                // Update the proposal in the database
                try (Connection conn = DBUtil.getConnection()) {
                    String sql = "UPDATE Proposals SET bid_amount = ?, timeline = ? WHERE proposal_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setDouble(1, newBidAmount);
                    ps.setString(2, newTimeline);
                    ps.setString(3, selectedProposal.getProposalId());
                    ps.executeUpdate();

                    // Update the table view
                    selectedProposal.setBidAmount(newBidAmount);
                    selectedProposal.setTimeline(newTimeline);
                    proposalsTable.refresh();

                    showAlert("Proposal updated successfully!");
                }
            } catch (NumberFormatException e) {
                proposalsErrorLabel.setText("Bid amount must be a valid number.");
                proposalsErrorLabel.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                proposalsErrorLabel.setText("Error updating proposal: " + e.getMessage());
                proposalsErrorLabel.setVisible(true);
            }
        }
    }

    @FXML
    private void handleDeleteProposal() {
        if (proposalsTable == null || proposalsErrorLabel == null) {
            showAlert("Proposal deletion components not available");
            return;
        }
        
        // Validate that a proposal is selected
        Proposal selectedProposal = proposalsTable.getSelectionModel().getSelectedItem();
        if (selectedProposal == null) {
            proposalsErrorLabel.setText("Please select a proposal to delete.");
            proposalsErrorLabel.setVisible(true);
            return;
        }
        proposalsErrorLabel.setVisible(false);

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Proposal");
        confirmAlert.setHeaderText("Are you sure you want to delete this proposal?");
        confirmAlert.setContentText("Proposal ID: " + selectedProposal.getProposalId());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the proposal from the database
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "DELETE FROM Proposals WHERE proposal_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedProposal.getProposalId());
                ps.executeUpdate();

                // Remove the proposal from the table view
                proposalsTable.getItems().remove(selectedProposal);

                showAlert("Proposal deleted successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                proposalsErrorLabel.setText("Error deleting proposal: " + e.getMessage());
                proposalsErrorLabel.setVisible(true);
            }
        }
    }
}