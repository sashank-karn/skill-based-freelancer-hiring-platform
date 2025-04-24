package com.freelancer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        
        // Apply main stylesheet
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        // Apply component-specific stylesheets
        scene.getStylesheets().add(getClass().getResource("/css/tables.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/dialog-style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/tabs.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("Freelancer Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
