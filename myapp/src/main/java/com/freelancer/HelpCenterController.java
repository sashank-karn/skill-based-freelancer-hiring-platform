package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelpCenterController {
    @FXML private Accordion faqAccordion;

    @FXML
    public void initialize() {
        TitledPane faq1 = new TitledPane("How to post a project?", new Label("Go to the dashboard and click 'Post Project'."));
        TitledPane faq2 = new TitledPane("How to manage proposals?", new Label("Navigate to 'Manage Proposals' in the dashboard."));
        TitledPane faq3 = new TitledPane("How to contact support?", new Label("Use the 'Submit Feedback' button to contact support."));
        faqAccordion.getPanes().addAll(faq1, faq2, faq3);
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/freelancer_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) faqAccordion.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600)); // Set constant size
            stage.getScene().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setTitle("Freelancer Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFeedback() {
        System.out.println("Feedback submission is under development.");
    }
}
