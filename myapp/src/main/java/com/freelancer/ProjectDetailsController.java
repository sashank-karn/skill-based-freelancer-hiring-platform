package com.freelancer;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProjectDetailsController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label categoryLabel;
    @FXML private Label budgetLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label dateCreatedLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea descriptionText;
    @FXML private Label proposalsLabel;
    
    @FXML private TableView<Proposal> proposalsTable;
    @FXML private TableColumn<Proposal, String> freelancerColumn;
    @FXML private TableColumn<Proposal, String> bidColumn;
    @FXML private TableColumn<Proposal, String> messageColumn;
    @FXML private TableColumn<Proposal, String> actionColumn;

    private ObservableList<Proposal> proposalsList = FXCollections.observableArrayList();
    private Project project;
    private int clientId;
    private ClientDashboardController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initial setup for proposal table columns
        setupProposalsTable();
    }
    
    private void setupProposalsTable() {
        // Check if proposalsTable exists in this view
        if (proposalsTable != null) {
            freelancerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFreelancerName()));
            bidColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedBidAmount()));
            messageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCoverLetter()));
            
            // Setup action column with Accept/Reject buttons
            actionColumn.setCellFactory(col -> new TableCell<Proposal, String>() {
                final Button acceptButton = new Button("Accept");
                final Button rejectButton = new Button("Reject");
                final HBox buttonsBox = new HBox(5, acceptButton, rejectButton);
                
                {
                    acceptButton.getStyleClass().add("accept-button");
                    rejectButton.getStyleClass().add("reject-button");
                    
                    acceptButton.setOnAction(event -> {
                        Proposal proposal = getTableRow().getItem();
                        if (proposal != null) {
                            handleAcceptProposal(proposal);
                        }
                    });
                    
                    rejectButton.setOnAction(event -> {
                        Proposal proposal = getTableRow().getItem();
                        if (proposal != null) {
                            handleRejectProposal(proposal);
                        }
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            });
            
            proposalsTable.setItems(proposalsList);
        }
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setParentController(ClientDashboardController controller) {
        this.parentController = controller;
    }

    public void setProject(Project project) {
        this.project = project;
        populateProjectDetails();
    }
    
    private void populateProjectDetails() {
        if (project != null) {
            titleLabel.setText(project.getTitle());
            descriptionText.setText(project.getDescription());
            statusLabel.setText(project.getStatus());
            budgetLabel.setText(project.getFormattedBudget());
            deadlineLabel.setText(project.getDeadline());
            
            loadProposals(project.getProjectId());
        }
    }
    
    private void loadProposals(String projectId) {
        proposalsList.clear();
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT p.*, u.name as freelancer_name " +
                        "FROM Proposals p " +
                        "JOIN Users u ON p.freelancer_id = u.user_id " +
                        "WHERE p.project_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Proposal proposal = new Proposal(
                    rs.getInt("proposal_id"),
                    rs.getString("project_id"),
                    rs.getInt("freelancer_id"),
                    rs.getDouble("bid_amount"),
                    rs.getString("cover_letter"),
                    rs.getString("status"),
                    rs.getString("freelancer_name")
                );
                proposalsList.add(proposal);
            }
            
            // Update UI based on proposal count
            if (proposalsList.isEmpty()) {
                proposalsLabel.setText("No proposals yet");
            } else {
                proposalsLabel.setText("Proposals (" + proposalsList.size() + ")");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load proposals: " + e.getMessage());
        }
    }
    
    private void loadProposals() {
        if (project != null) {
            loadProposals(project.getProjectId());
        }
    }
    
    private void handleAcceptProposal(Proposal proposal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Accept Proposal");
        alert.setHeaderText("Accept proposal from " + proposal.getFreelancerName() + "?");
        alert.setContentText("This will change the project status to 'In Progress' and notify the freelancer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                // Update proposal status
                String updateProposalSql = "UPDATE Proposals SET status = 'Accepted' WHERE proposal_id = ?";
                PreparedStatement psProposal = conn.prepareStatement(updateProposalSql);
                // Fix: Use Integer.parseInt to convert the String to int
                psProposal.setInt(1, Integer.parseInt(proposal.getProposalId()));
                psProposal.executeUpdate();
                
                // Update project status and assign freelancer
                String updateProjectSql = "UPDATE Projects SET status = 'In Progress', " +
                                          "assigned_freelancer_id = ?, assigned_freelancer_name = ? " +
                                          "WHERE project_id = ?";
                PreparedStatement psProject = conn.prepareStatement(updateProjectSql);
                psProject.setInt(1, proposal.getFreelancerId());
                psProject.setString(2, proposal.getFreelancerName());
                psProject.setString(3, project.getProjectId());
                psProject.executeUpdate();
                
                // Update status of all other proposals for this project
                String rejectOthersSql = "UPDATE Proposals SET status = 'Rejected' " +
                                         "WHERE project_id = ? AND proposal_id != ?";
                PreparedStatement psReject = conn.prepareStatement(rejectOthersSql);
                psReject.setString(1, project.getProjectId());
                // Fix: Use Integer.parseInt for the proposal ID here too
                psReject.setInt(2, Integer.parseInt(proposal.getProposalId()));
                psReject.executeUpdate();
                
                // Refresh proposals and project details
                loadProposals(project.getProjectId());
                
                // Update project status in UI
                statusLabel.setText("In Progress");
                project.setStatus("In Progress");
                
                // Update parent controller if available
                if (parentController != null) {
                    parentController.refreshProjects();
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Proposal accepted successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not accept proposal: " + e.getMessage());
            }
        }
    }
    
    private void handleRejectProposal(Proposal proposal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reject Proposal");
        alert.setHeaderText("Reject proposal from " + proposal.getFreelancerName() + "?");
        alert.setContentText("The freelancer will be notified that their proposal was rejected.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                // Update proposal status
                String updateProposalSql = "UPDATE Proposals SET status = 'Rejected' WHERE proposal_id = ?";
                PreparedStatement ps = conn.prepareStatement(updateProposalSql);
                // Fix: Use Integer.parseInt to convert the String to int
                ps.setInt(1, Integer.parseInt(proposal.getProposalId()));
                ps.executeUpdate();
                
                // Refresh proposals
                loadProposals(project.getProjectId());
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Proposal rejected successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not reject proposal: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleEditProject() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_project.fxml"));
            Parent root = loader.load();
            
            EditProjectController controller = loader.getController();
            controller.setProject(project);
            controller.setClientId(clientId);
            controller.setParentControllers(this, parentController);
            
            Scene scene = new Scene(root);
            StyleManager.applyStylesheets(scene);
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Edit Project");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit project window: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCloseProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Project");
        alert.setHeaderText("Are you sure you want to close this project?");
        alert.setContentText("This will mark the project as completed and no more proposals will be accepted.");
        
        StyleManager.styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE Projects SET status = 'Completed', completion_date = CURRENT_DATE WHERE project_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, project.getProjectId());
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    project.setStatus("Completed");
                    populateProjectDetails();
                    
                    // Update parent controller
                    if (parentController != null) {
                        parentController.refreshProjects();
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Project has been marked as completed.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not update project status.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not close project: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleDeleteProject() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Project");
        alert.setHeaderText("Are you sure you want to delete this project?");
        alert.setContentText("This action cannot be undone. All associated proposals will also be deleted.");
        
        StyleManager.styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                
                // First delete related proposals
                String deleteProposalsSql = "DELETE FROM Proposals WHERE project_id = ?";
                PreparedStatement psProposals = conn.prepareStatement(deleteProposalsSql);
                psProposals.setString(1, project.getProjectId());
                psProposals.executeUpdate();
                
                // Then delete the project
                String deleteProjectSql = "DELETE FROM Projects WHERE project_id = ?";
                PreparedStatement psProject = conn.prepareStatement(deleteProjectSql);
                psProject.setString(1, project.getProjectId());
                int rowsAffected = psProject.executeUpdate();
                
                conn.commit();
                
                if (rowsAffected > 0) {
                    // Close this window
                    ((Stage) titleLabel.getScene().getWindow()).close();
                    
                    // Update parent
                    if (parentController != null) {
                        parentController.refreshProjects();
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Project has been deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not delete project.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete project: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }
    
    // Method for external controllers to refresh this view
    public void refreshProject() {
        populateProjectDetails();
        loadProposals();
    }
}