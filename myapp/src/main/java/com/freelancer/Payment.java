package com.freelancer;

import javafx.beans.property.*;

public class Payment {
    private final StringProperty projectTitle;
    private final DoubleProperty amount;
    private final StringProperty status;
    private final StringProperty paymentDate;

    public Payment(String projectTitle, double amount, String status, String paymentDate) {
        this.projectTitle = new SimpleStringProperty(projectTitle);
        this.amount = new SimpleDoubleProperty(amount);
        this.status = new SimpleStringProperty(status);
        this.paymentDate = new SimpleStringProperty(paymentDate);
    }

    public String getProjectTitle() { return projectTitle.get(); }
    public StringProperty projectTitleProperty() { return projectTitle; }

    public double getAmount() { return amount.get(); }
    public DoubleProperty amountProperty() { return amount; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getPaymentDate() { return paymentDate.get(); }
    public StringProperty paymentDateProperty() { return paymentDate; }
}