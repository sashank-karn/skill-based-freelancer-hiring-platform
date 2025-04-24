package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Optional;
import javafx.stage.Modality;
import javafx.beans.property.SimpleStringProperty;

public class ProjectDetailsController {
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label budgetLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label freelancerNameLabel;
    @FXML private Label ratingLabel;
    
    @FXML private VBox proposalsSection;
    @FXML private VBox freelancerSection;
    @FXML private TableView<Proposal> proposalsTable;
    @FXML private TableColumn<Proposal, String> freelancerColumn;
    @FXML private TableColumn<Proposal, String> bidAmountColumn;
    @FXML private TableColumn<Proposal, String> dateColumn;
    @FXML private TableColumn<Proposal, String> statusColumn;
    @FXML private TableColumn<Proposal, Button> actionsColumn;
    
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button completeButton;
    
    private Project project;
    private int clientId;
    private ClientDashboardController parentController;
    private ObservableList<Proposal> proposalsList = FXCollections.observableArrayList();
    
    public void setProject(Project project) {
        this.project = project;
        displayProjectDetails();
    }
    
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    
    public void setParentController(ClientDashboardController controller) {
        this.parentController = controller;
    }
    
    private void displayProjectDetails() {
        titleLabel.setText(project.getTitle());
        descriptionLabel.setText(project.getDescription());
        statusLabel.setText(project.getStatus());
        
        budgetLabel.setText(String.format("$%.2f", project.getBudget()));
        
        deadlineLabel.setText(project.getDeadline());
        
        StyleManager.applyStatusStyle(statusLabel, project.getStatus());
        
        loadProposals(project.getProjectId());
        
        setupProposalTable();
        
        updateAssignedFreelancerVisibility();
    }
    
    private void setupProposalTable() {
        freelancerColumn.setCellValueFactory(cellData -> cellData.getValue().freelancerNameProperty());
        
        bidAmountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedBidAmount()));
        
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().timelineProperty());
        
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        statusColumn.setCellFactory(column -> StyleManager.createStatusCell(column));
        
        actionsColumn.setCellFactory(new Callback<TableColumn<Proposal, Button>, TableCell<Proposal, Button>>() {
            @Override
            public TableCell<Proposal, Button> call(TableColumn<Proposal, Button> param) {
                return new TableCell<Proposal, Button>() {
                    @Override
                    protected void updateItem(Button item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Proposal proposal = getTableView().getItems().get(getIndex());
                            
                            if ("Pending".equalsIgnoreCase(proposal.getStatus())) {
                                HBox actionBox = new HBox(5);
                                
                                Button acceptButton = new Button("Accept");
                                acceptButton.getStyleClass().add("action-button");
                                acceptButton.setOnAction(event -> handleAcceptProposal(proposal));
                                
                                Button rejectButton = new Button("Reject");
                                rejectButton.getStyleClass().add("action-button");
                                rejectButton.setOnAction(event -> handleRejectProposal(proposal));
                                
                                actionBox.getChildren().addAll(acceptButton, rejectButton);
                                setGraphic(actionBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
        
        proposalsTable.setItems(proposalsList);
    }
    
    private void loadProposals(String projectId) {
        proposalsList.clear();
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT p.proposal_id, p.project_id, p.user_id AS freelancer_id, p.bid_amount, " +
                    "p.timeline, p.status, p.submission_date, u.name as freelancer_name " +
                    "FROM Proposals p " +
                    "JOIN Users u ON p.user_id = u.user_id " +
                    "WHERE p.project_id = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String proposalId = rs.getString("proposal_id");
                int freelancerId = rs.getInt("freelancer_id");
                String freelancerName = rs.getString("freelancer_name");
                double bidAmount = rs.getDouble("bid_amount");
                String timeline = rs.getString("timeline");
                String status = rs.getString("status");
                String date = rs.getString("submission_date");
                
                Proposal proposal = new Proposal(
                    proposalId, 
                    projectId, 
                    null,  
                    freelancerId,
                    freelancerName,
                    bidAmount,
                    timeline,  
                    date,
                    status
                );
                
                proposalsList.add(proposal);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not load proposals: " + e.getMessage());
        }
    }
    
    private void updateAssignedFreelancerVisibility() {
        boolean isAssigned = "In Progress".equalsIgnoreCase(project.getStatus()) || 
                            "Completed".equalsIgnoreCase(project.getStatus());
        
        freelancerSection.setVisible(isAssigned);
        freelancerSection.setManaged(isAssigned);
        
        if (isAssigned) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "SELECT assigned_freelancer_name FROM Projects WHERE project_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, project.getProjectId());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String freelancerName = rs.getString("assigned_freelancer_name");
                    if (freelancerName != null && !freelancerName.isEmpty()) {
                        freelancerNameLabel.setText(freelancerName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        boolean shouldShowProposals = "Open".equalsIgnoreCase(project.getStatus());
        proposalsSection.setVisible(shouldShowProposals);
        proposalsSection.setManaged(shouldShowProposals);
        
        boolean canModify = "Open".equalsIgnoreCase(project.getStatus());
        editButton.setDisable(!canModify);
        deleteButton.setDisable(!canModify);
    }
    
    @FXML
    private void handleAcceptProposal(Proposal proposal) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Accept Proposal");
        confirmAlert.setHeaderText("Accept Proposal from " + proposal.getFreelancerName());
        confirmAlert.setContentText("Are you sure you want to accept this proposal? All other proposals will be rejected.");
        StyleManager.styleAlert(confirmAlert);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    String updateProposalSql = "UPDATE Proposals SET status = 'Accepted' WHERE proposal_id = ?";
                    PreparedStatement psProposal = conn.prepareStatement(updateProposalSql);
                    psProposal.setString(1, proposal.getProposalId());
                    psProposal.executeUpdate();
                    
                    String updateProjectSql = "UPDATE Projects SET status = 'In Progress', " +
                                             "assigned_freelancer_id = ?, assigned_freelancer_name = ? " +
                                             "WHERE project_id = ?";
                    PreparedStatement psProject = conn.prepareStatement(updateProjectSql);
                    psProject.setInt(1, proposal.getFreelancerId());
                    psProject.setString(2, proposal.getFreelancerName());
                    psProject.setString(3, project.getProjectId());
                    psProject.executeUpdate();
                    
                    String rejectOthersSql = "UPDATE Proposals SET status = 'Rejected' " +
                                             "WHERE project_id = ? AND proposal_id != ?";
                    PreparedStatement psReject = conn.prepareStatement(rejectOthersSql);
                    psReject.setString(1, project.getProjectId());
                    psReject.setString(2, proposal.getProposalId());
                    psReject.executeUpdate();
                    
                    conn.commit();
                    
                    project.setStatus("In Progress");
                    statusLabel.setText("In Progress");
                    StyleManager.applyStatusStyle(statusLabel, "In Progress");
                    updateAssignedFreelancerVisibility();
                    loadProposals(project.getProjectId());
                    
                    if (parentController != null) {
                        parentController.refreshProjects();
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Proposal accepted. The freelancer will be notified.");
                    
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not accept proposal: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRejectProposal(Proposal proposal) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reject Proposal");
        confirmAlert.setHeaderText("Reject Proposal from " + proposal.getFreelancerName());
        confirmAlert.setContentText("Are you sure you want to reject this proposal?");
        StyleManager.styleAlert(confirmAlert);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                String updateProposalSql = "UPDATE Proposals SET status = 'Rejected' WHERE proposal_id = ?";
                PreparedStatement ps = conn.prepareStatement(updateProposalSql);
                ps.setString(1, proposal.getProposalId());
                int rowsUpdated = ps.executeUpdate();
                
                if (rowsUpdated > 0) {
                    loadProposals(project.getProjectId());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Proposal rejected.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not reject proposal: " + e.getMessage());
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle("Edit Project: " + project.getTitle());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not open edit project window: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteProject() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Project");
        confirmAlert.setHeaderText("Delete Project: " + project.getTitle());
        confirmAlert.setContentText("Are you sure you want to delete this project? This action cannot be undone.");
        StyleManager.styleAlert(confirmAlert);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    String deleteProposalsSql = "DELETE FROM Proposals WHERE project_id = ?";
                    PreparedStatement psProposals = conn.prepareStatement(deleteProposalsSql);
                    psProposals.setString(1, project.getProjectId());
                    psProposals.executeUpdate();
                    
                    String deleteProjectSql = "DELETE FROM Projects WHERE project_id = ?";
                    PreparedStatement psProject = conn.prepareStatement(deleteProjectSql);
                    psProject.setString(1, project.getProjectId());
                    int rowsDeleted = psProject.executeUpdate();
                    
                    conn.commit();
                    
                    if (rowsDeleted > 0) {
                        if (parentController != null) {
                            parentController.refreshProjects();
                        }
                        
                        Stage stage = (Stage) titleLabel.getScene().getWindow();
                        stage.close();
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not delete project: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleMarkComplete() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Complete Project");
        confirmAlert.setHeaderText("Mark Project as Complete");
        confirmAlert.setContentText("Are you sure you want to mark this project as complete?");
        StyleManager.styleAlert(confirmAlert);
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE Projects SET status = 'Completed', completion_date = CURRENT_TIMESTAMP WHERE project_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, project.getProjectId());
                int rowsUpdated = ps.executeUpdate();
                
                if (rowsUpdated > 0) {
                    statusLabel.setText("Completed");
                    StyleManager.applyStatusStyle(statusLabel, "Completed");
                    project.setStatus("Completed");
                    updateAssignedFreelancerVisibility();
                    
                    if (parentController != null) {
                        parentController.refreshProjects();
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Project marked as complete.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not complete project: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleMessageFreelancer() {
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon", 
                "Messaging functionality will be available in a future update.");
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        StyleManager.styleAlert(alert);
        alert.showAndWait();
    }
    
    public void refreshProjectDetails(Project updatedProject) {
        this.project = updatedProject;
        displayProjectDetails();
    }
    
    public void refreshProject(Project updatedProject) {
        refreshProjectDetails(updatedProject);
    }
}