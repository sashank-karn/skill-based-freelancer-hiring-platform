package com.freelancer;

import javafx.beans.property.*;

public class Project {
    private final StringProperty projectId;
    private final StringProperty title;
    private final StringProperty description;
    private final DoubleProperty budget;
    private final StringProperty deadline;
    private final StringProperty status;
    private final StringProperty clientName;

    // Constructor for all fields
    public Project(String projectId, String title, String description, double budget, String deadline, String status, String clientName) {
        this.projectId = new SimpleStringProperty(projectId);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.budget = new SimpleDoubleProperty(budget);
        this.deadline = new SimpleStringProperty(deadline);
        this.status = new SimpleStringProperty(status);
        this.clientName = new SimpleStringProperty(clientName);
    }

    // Constructor for projects without clientName
    public Project(String projectId, String title, String description, double budget, String deadline, String status) {
        this(projectId, title, description, budget, deadline, status, "");
    }

    // Getters and property methods...
    public String getProjectId() { return projectId.get(); }
    public StringProperty projectIdProperty() { return projectId; }
    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public double getBudget() { return budget.get(); }
    public DoubleProperty budgetProperty() { return budget; }
    public String getDeadline() { return deadline.get(); }
    public StringProperty deadlineProperty() { return deadline; }
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public String getClientName() { return clientName.get(); }
    public StringProperty clientNameProperty() { return clientName; }
}
