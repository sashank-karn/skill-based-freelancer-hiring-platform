package com.freelancer;

import javafx.beans.property.*;

public class Proposal {
    private final StringProperty proposalId;
    private final StringProperty projectId;
    private final StringProperty projectTitle;
    private final DoubleProperty bidAmount;
    private final StringProperty timeline;
    private final StringProperty status;

    public Proposal(String proposalId, String projectId, String projectTitle, double bidAmount, String timeline, String status) {
        this.proposalId = new SimpleStringProperty(proposalId);
        this.projectId = new SimpleStringProperty(projectId);
        this.projectTitle = new SimpleStringProperty(projectTitle);
        this.bidAmount = new SimpleDoubleProperty(bidAmount);
        this.timeline = new SimpleStringProperty(timeline);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty proposalIdProperty() {
        return proposalId;
    }

    public String getProposalId() {
        return proposalId.get();
    }

    public StringProperty projectIdProperty() {
        return projectId;
    }

    public String getProjectId() {
        return projectId.get();
    }

    public StringProperty projectTitleProperty() {
        return projectTitle;
    }

    public String getProjectTitle() {
        return projectTitle.get();
    }

    public DoubleProperty bidAmountProperty() {
        return bidAmount;
    }

    public double getBidAmount() {
        return bidAmount.get();
    }

    public StringProperty timelineProperty() {
        return timeline;
    }

    public String getTimeline() {
        return timeline.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount.set(bidAmount);
    }
    public void setTimeline(String timeline) {
        this.timeline.set(timeline);
    }
    
}
