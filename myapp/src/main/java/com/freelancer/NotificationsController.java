package com.freelancer;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class NotificationsController {
    @FXML private ListView<String> notificationsList;

    @FXML
    public void initialize() {
        notificationsList.getItems().addAll(
            "New project invitation: Build a website",
            "Proposal accepted: Mobile App Development",
            "System update: Scheduled maintenance on April 30"
        );
    }
}