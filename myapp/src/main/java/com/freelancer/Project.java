package com.freelancer;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Project {
    private final StringProperty projectId;
    private final StringProperty title;
    private final StringProperty description;
    private final DoubleProperty budget;
    private final StringProperty formattedBudget;
    private final StringProperty deadline;
    private final StringProperty status;
    private final StringProperty clientName;
    
    private final IntegerProperty clientId;
    private final StringProperty creationDate;
    private final StringProperty category;
    private final StringProperty skills;
    private final IntegerProperty proposalCount;
    private final BooleanProperty featured;
    private final StringProperty completionDate;
    private final IntegerProperty assignedFreelancerId;
    private final StringProperty assignedFreelancerName;
    private final StringProperty paymentStatus;

    public Project(String projectId, String title, String description, double budget, String deadline, String status, String clientName) {
        this.projectId = new SimpleStringProperty(projectId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.budget = new SimpleDoubleProperty(budget);
        this.formattedBudget = new SimpleStringProperty("$" + budget);
        this.deadline = new SimpleStringProperty(deadline);
        this.status = new SimpleStringProperty(status);
        this.clientName = new SimpleStringProperty(clientName);
        
        this.clientId = new SimpleIntegerProperty(0);
        this.creationDate = new SimpleStringProperty(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        this.category = new SimpleStringProperty("");
        this.skills = new SimpleStringProperty("");
        this.proposalCount = new SimpleIntegerProperty(0);
        this.featured = new SimpleBooleanProperty(false);
        this.completionDate = new SimpleStringProperty("");
        this.assignedFreelancerId = new SimpleIntegerProperty(0);
        this.assignedFreelancerName = new SimpleStringProperty("");
        this.paymentStatus = new SimpleStringProperty("Unpaid");
    }
    public Project(String projectId, String title, String description, double budget, String deadline, String status) {
        this(projectId, title, description, budget, deadline, status, "");
    }
    
    public Project(String projectId, String title, String description, double budget, 
                  String deadline, String status, String clientName, int clientId,
                  String creationDate, String category, String skills, int proposalCount,
                  boolean featured, String completionDate, int assignedFreelancerId,
                  String assignedFreelancerName, String paymentStatus) {
        this.projectId = new SimpleStringProperty(projectId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.budget = new SimpleDoubleProperty(budget);
        this.formattedBudget = new SimpleStringProperty("$" + budget);
        this.deadline = new SimpleStringProperty(deadline);
        this.status = new SimpleStringProperty(status);
        this.clientName = new SimpleStringProperty(clientName);
        this.clientId = new SimpleIntegerProperty(clientId);
        this.creationDate = new SimpleStringProperty(creationDate);
        this.category = new SimpleStringProperty(category);
        this.skills = new SimpleStringProperty(skills);
        this.proposalCount = new SimpleIntegerProperty(proposalCount);
        this.featured = new SimpleBooleanProperty(featured);
        this.completionDate = new SimpleStringProperty(completionDate);
        this.assignedFreelancerId = new SimpleIntegerProperty(assignedFreelancerId);
        this.assignedFreelancerName = new SimpleStringProperty(assignedFreelancerName);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
    }

     public Project(String projectId, String title, String description, String status, 
                  String budget, String deadline, int clientId) {
        this.projectId = new SimpleStringProperty(projectId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleStringProperty(status);
        this.deadline = new SimpleStringProperty(deadline);
        this.clientName = new SimpleStringProperty("");
        
        double budgetValue = 0.0;
        try {
            String cleanBudget = budget.replace("$", "").trim();
            budgetValue = Double.parseDouble(cleanBudget);
        } catch (Exception e) {
            System.out.println("Could not parse budget: " + budget);
        }
        
        this.budget = new SimpleDoubleProperty(budgetValue);
        this.formattedBudget = new SimpleStringProperty(budget); // Keep original formatting
        
        this.clientId = new SimpleIntegerProperty(clientId);
        this.creationDate = new SimpleStringProperty(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        this.category = new SimpleStringProperty("");
        this.skills = new SimpleStringProperty("");
        this.proposalCount = new SimpleIntegerProperty(0);
        this.featured = new SimpleBooleanProperty(false);
        this.completionDate = new SimpleStringProperty("");
        this.assignedFreelancerId = new SimpleIntegerProperty(0);
        this.assignedFreelancerName = new SimpleStringProperty("");
        this.paymentStatus = new SimpleStringProperty("Unpaid");
    }
    
     public Project(int projectId, String title, String description, String status, 
                  String budget, String deadline, int clientId) {
        this(String.valueOf(projectId), title, description, status, budget, deadline, clientId);
    }
    
    public String getProjectId() { return projectId.get(); }
    public StringProperty projectIdProperty() { return projectId; }
    
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    
    public double getBudget() { return budget.get(); }
    public DoubleProperty budgetProperty() { return budget; }
    
    public String getFormattedBudget() { return formattedBudget.get(); }
    public StringProperty formattedBudgetProperty() { return formattedBudget; }
    
    public String getDeadline() { return deadline.get(); }
    public StringProperty deadlineProperty() { return deadline; }
    
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    
    public String getClientName() { return clientName.get(); }
    public StringProperty clientNameProperty() { return clientName; }
    
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setBudget(double budget) { 
        this.budget.set(budget); 
        this.formattedBudget.set("$" + budget);
    }
    public void setDeadline(String deadline) { this.deadline.set(deadline); }
    public void setStatus(String status) { this.status.set(status); }
    public void setClientName(String clientName) { this.clientName.set(clientName); }
    
    // New properties getters and setters
    public int getClientId() { return clientId.get(); }
    public IntegerProperty clientIdProperty() { return clientId; }
    public void setClientId(int id) { this.clientId.set(id); }
    
    public String getCreationDate() { return creationDate.get(); }
    public StringProperty creationDateProperty() { return creationDate; }
    public void setCreationDate(String date) { this.creationDate.set(date); }
    
    public String getCategory() { return category.get(); }
    public StringProperty categoryProperty() { return category; }
    public void setCategory(String category) { this.category.set(category); }
    
    public String getSkills() { return skills.get(); }
    public StringProperty skillsProperty() { return skills; }
    public void setSkills(String skills) { this.skills.set(skills); }
    
    public int getProposalCount() { return proposalCount.get(); }
    public IntegerProperty proposalCountProperty() { return proposalCount; }
    public void setProposalCount(int count) { this.proposalCount.set(count); }
    public void incrementProposalCount() { this.proposalCount.set(this.proposalCount.get() + 1); }
    
    public boolean isFeatured() { return featured.get(); }
    public BooleanProperty featuredProperty() { return featured; }
    public void setFeatured(boolean featured) { this.featured.set(featured); }
    
    public String getCompletionDate() { return completionDate.get(); }
    public StringProperty completionDateProperty() { return completionDate; }
    public void setCompletionDate(String date) { this.completionDate.set(date); }
    
    public int getAssignedFreelancerId() { return assignedFreelancerId.get(); }
    public IntegerProperty assignedFreelancerIdProperty() { return assignedFreelancerId; }
    public void setAssignedFreelancerId(int id) { this.assignedFreelancerId.set(id); }
    
    public String getAssignedFreelancerName() { return assignedFreelancerName.get(); }
    public StringProperty assignedFreelancerNameProperty() { return assignedFreelancerName; }
    public void setAssignedFreelancerName(String name) { this.assignedFreelancerName.set(name); }
    
    public String getPaymentStatus() { return paymentStatus.get(); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    public void setPaymentStatus(String status) { this.paymentStatus.set(status); }
    
    @Override
    public String toString() {
        return "Project: " + getTitle() + " (ID: " + getProjectId() + ")";
    }
}
