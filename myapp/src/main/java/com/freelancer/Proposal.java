package com.freelancer;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Proposal {
    // Basic properties
    private final StringProperty proposalId;
    private final StringProperty projectId;
    private final StringProperty projectTitle;
    private final IntegerProperty freelancerId;
    private final StringProperty freelancerName;
    private final DoubleProperty bidAmount;
    private final StringProperty coverLetter;
    private final StringProperty timeline;
    private final StringProperty status;

    /**
     * Constructor for client view (used in ProjectDetailsController)
     */
    public Proposal(int proposalId, String projectId, int freelancerId, 
                   double bidAmount, String coverLetter, String status, 
                   String freelancerName) {
        this.proposalId = new SimpleStringProperty(String.valueOf(proposalId));
        this.projectId = new SimpleStringProperty(projectId);
        this.projectTitle = new SimpleStringProperty(""); // Not provided in this constructor
        this.freelancerId = new SimpleIntegerProperty(freelancerId);
        this.freelancerName = new SimpleStringProperty(freelancerName);
        this.bidAmount = new SimpleDoubleProperty(bidAmount);
        this.coverLetter = new SimpleStringProperty(coverLetter);
        this.timeline = new SimpleStringProperty(""); // Not provided in this constructor
        this.status = new SimpleStringProperty(status);
    }

    /**
     * Constructor for freelancer view (used in FreelancerDashboardController)
     */
    public Proposal(String proposalId, String projectId, String projectTitle,
                   double bidAmount, String timeline, String status) {
        this.proposalId = new SimpleStringProperty(proposalId);
        this.projectId = new SimpleStringProperty(projectId);
        this.projectTitle = new SimpleStringProperty(projectTitle);
        this.freelancerId = new SimpleIntegerProperty(0); // Not provided in this constructor
        this.freelancerName = new SimpleStringProperty(""); // Not provided in this constructor
        this.bidAmount = new SimpleDoubleProperty(bidAmount);
        this.coverLetter = new SimpleStringProperty(""); // Not provided in this constructor
        this.timeline = new SimpleStringProperty(timeline);
        this.status = new SimpleStringProperty(status);
    }

    /**
     * Full constructor with all properties
     */
    public Proposal(String proposalId, String projectId, String projectTitle,
                   int freelancerId, String freelancerName, double bidAmount,
                   String coverLetter, String timeline, String status) {
        this.proposalId = new SimpleStringProperty(proposalId);
        this.projectId = new SimpleStringProperty(projectId);
        this.projectTitle = new SimpleStringProperty(projectTitle);
        this.freelancerId = new SimpleIntegerProperty(freelancerId);
        this.freelancerName = new SimpleStringProperty(freelancerName);
        this.bidAmount = new SimpleDoubleProperty(bidAmount);
        this.coverLetter = new SimpleStringProperty(coverLetter);
        this.timeline = new SimpleStringProperty(timeline);
        this.status = new SimpleStringProperty(status);
    }

    
    public Proposal(String proposalId, String projectId, int freelancerId, 
                   String freelancerName, String bidAmount, String coverLetter, 
                   String status, String submissionDate) {
        this.proposalId = new SimpleStringProperty(proposalId);
        this.projectId = new SimpleStringProperty(projectId);
        this.projectTitle = new SimpleStringProperty(""); // Not provided in this constructor
        this.freelancerId = new SimpleIntegerProperty(freelancerId);
        this.freelancerName = new SimpleStringProperty(freelancerName);
        
        double bidValue = 0.0;
        try {
            bidValue = Double.parseDouble(bidAmount.replace("$", "").trim());
        } catch (NumberFormatException e) {
            System.err.println("Could not parse bid amount: " + bidAmount);
        }
        
        this.bidAmount = new SimpleDoubleProperty(bidValue);
        this.coverLetter = new SimpleStringProperty(coverLetter);
        this.timeline = new SimpleStringProperty(submissionDate); // Use submission date as timeline
        this.status = new SimpleStringProperty(status);
    }

     public String getProposalId() {
        return proposalId.get();
    }

    public int getProposalIdAsInt() {
        try {
            return Integer.parseInt(proposalId.get());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getProjectId() {
        return projectId.get();
    }

    public String getProjectTitle() {
        return projectTitle.get();
    }

    public int getFreelancerId() {
        return freelancerId.get();
    }

    public String getFreelancerName() {
        return freelancerName.get();
    }

    public double getBidAmount() {
        return bidAmount.get();
    }

    public String getFormattedBidAmount() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormatter.format(bidAmount.get());
    }

    public String getCoverLetter() {
        return coverLetter.get();
    }

    public String getTimeline() {
        return timeline.get();
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty proposalIdProperty() {
        return proposalId;
    }

    public StringProperty projectIdProperty() {
        return projectId;
    }

    public StringProperty projectTitleProperty() {
        return projectTitle;
    }

    public IntegerProperty freelancerIdProperty() {
        return freelancerId;
    }

    public StringProperty freelancerNameProperty() {
        return freelancerName;
    }

    public DoubleProperty bidAmountProperty() {
        return bidAmount;
    }

    public StringProperty coverLetterProperty() {
        return coverLetter;
    }

    public StringProperty timelineProperty() {
        return timeline;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setProposalId(String proposalId) {
        this.proposalId.set(proposalId);
    }

    public void setProjectId(String projectId) {
        this.projectId.set(projectId);
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle.set(projectTitle);
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId.set(freelancerId);
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName.set(freelancerName);
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount.set(bidAmount);
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter.set(coverLetter);
    }

    public void setTimeline(String timeline) {
        this.timeline.set(timeline);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}




