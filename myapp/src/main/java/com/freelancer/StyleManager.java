package com.freelancer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;

/**
 * Utility class for applying consistent styling across the application
 */
public class StyleManager {
    
    /**
     * Apply all application stylesheets to a scene
     */
    public static void applyStylesheets(Scene scene) {
        // Try multiple possible locations for the stylesheets
        String[] stylesheetPaths = {
            "/css/style.css", 
            "css/style.css",
            "/resources/css/style.css", 
            "resources/css/style.css",
            "../resources/css/style.css"
        };
        
        for (String path : stylesheetPaths) {
            try {
                URL url = StyleManager.class.getResource(path);
                if (url != null) {
                    scene.getStylesheets().add(url.toExternalForm());
                    System.out.println("Successfully loaded stylesheet: " + path);
                }
            } catch (Exception e) {
                System.out.println("Could not load stylesheet: " + path + " - " + e.getMessage());
            }
        }
        
        // If no stylesheets were loaded, add inline styles
        if (scene.getStylesheets().isEmpty()) {
            System.out.println("No external stylesheets found. Applying basic inline styles.");
            
            // Add minimal inline styles
            scene.getRoot().setStyle(
                "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
                "-fx-background-color: #f5f5f5;"
            );
        }
    }
    
    /**
     * Apply styling to an alert dialog
     */
    public static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        
        URL dialogStyleUrl = StyleManager.class.getResource("/css/dialog-style.css");
        if (dialogStyleUrl != null) {
            dialogPane.getStylesheets().add(dialogStyleUrl.toExternalForm());
        }
        
        // Add specific styling classes based on alert type
        switch (alert.getAlertType()) {
            case CONFIRMATION:
                dialogPane.getStyleClass().add("confirmation-alert");
                break;
            case ERROR:
                dialogPane.getStyleClass().add("error-alert");
                break;
            case INFORMATION:
                dialogPane.getStyleClass().add("info-alert");
                break;
            case WARNING:
                dialogPane.getStyleClass().add("warning-alert");
                break;
            default:
                break;
        }
        
        // Ensure the alert is in front
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.setAlwaysOnTop(true);
    }
    
    /**
     * Apply styling to a dialog
     */
    public static void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        
        URL dialogStyleUrl = StyleManager.class.getResource("/css/dialog-style.css");
        if (dialogStyleUrl != null) {
            dialogPane.getStylesheets().add(dialogStyleUrl.toExternalForm());
        }
        
        // Add generic dialog class if not already present
        if (!dialogPane.getStyleClass().contains("dialog-pane")) {
            dialogPane.getStyleClass().add("dialog-pane");
        }
        
        // Ensure the dialog is in front
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.initModality(Modality.APPLICATION_MODAL);
    }
    
    /**
     * Create a styled cell factory for status columns
     * @param column The TableColumn to style
     * @return A TableCell factory that applies appropriate styling based on status text
     */
    public static <T> TableCell<T, String> createStatusCell(TableColumn<T, String> column) {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-pending", "status-approved", "status-rejected", 
                                           "status-open", "status-in-progress", "status-completed", 
                                           "status-cancelled", "status-verified");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-pending", "status-approved", "status-rejected", 
                                          "status-open", "status-in-progress", "status-completed", 
                                          "status-cancelled", "status-verified");
                    
                    String status = item.toLowerCase();
                    
                    if (status.contains("pend") || status.contains("wait")) {
                        getStyleClass().add("status-pending");
                    } else if (status.contains("approv") || status.contains("accept")) {
                        getStyleClass().add("status-approved");
                    } else if (status.contains("reject") || status.contains("declin")) {
                        getStyleClass().add("status-rejected");
                    } else if (status.contains("open") || status.contains("active")) {
                        getStyleClass().add("status-open");
                    } else if (status.contains("progress") || status.contains("ongoing")) {
                        getStyleClass().add("status-in-progress");
                    } else if (status.contains("complet") || status.contains("done") || status.contains("finish")) {
                        getStyleClass().add("status-completed");
                    } else if (status.contains("cancel")) {
                        getStyleClass().add("status-cancelled");
                    } else if (status.contains("verif")) {
                        getStyleClass().add("status-verified");
                    }
                }
            }
        };
    }
    
    /**
     * Create a styled date cell factory that formats dates consistently
     */
    public static <T> TableCell<T, String> createDateCell(TableColumn<T, String> column) {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // If it's a timestamp with time, try to format as just the date
                    if (item.length() > 10 && item.contains(" ")) {
                        setText(item.split(" ")[0]);
                    } else {
                        setText(item);
                    }
                }
            }
        };
    }
    
    /**
     * Creates a styled form grid pane with consistent padding and spacing
     */
    public static GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        return grid;
    }
    
    /**
     * Create and style a tooltip
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #3f51b5; -fx-text-fill: white;");
        return tooltip;
    }
    
    /**
     * Apply styles to a button that should stand out as a primary action
     */
    public static void styleAsPrimaryButton(Button button) {
        button.getStyleClass().add("primary-button");
    }
    
    /**
     * Apply styles to a button that should be a secondary action
     */
    public static void styleAsSecondaryButton(Button button) {
        button.getStyleClass().add("secondary-button");
    }
    
    /**
     * Apply styles to a button that represents a dangerous action
     */
    public static void styleAsDangerButton(Button button) {
        button.getStyleClass().add("danger-button");
    }
    
    /**
     * Makes the passed textArea auto-resize its height based on content
     */
    public static void makeTextAreaAutoSize(TextArea textArea) {
        textArea.setWrapText(true);
        
        // Auto height based on content
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int lineCount = 2; // Minimum lines
            
            if (newValue != null) {
                String[] lines = newValue.split("\n");
                lineCount = Math.max(lines.length, lineCount);
            }
            
            // Set prefRowCount to adjust height
            textArea.setPrefRowCount(lineCount);
        });
    }
    
    /**
     * Creates a TableView with styled empty placeholder text
     */
    public static <T> TableView<T> createStyledTableView() {
        TableView<T> tableView = new TableView<>();
        
        // Create a placeholder with custom styling
        Label placeholderLabel = new Label("No data available");
        placeholderLabel.getStyleClass().add("table-placeholder");
        
        tableView.setPlaceholder(placeholderLabel);
        return tableView;
    }
    
    /**
     * Creates a custom header for use in views
     */
    public static HBox createViewHeader(String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("view-header-title");
        
        HBox header = new HBox(titleLabel);
        header.getStyleClass().add("view-header");
        header.setPadding(new Insets(15, 20, 15, 20));
        
        return header;
    }
    
    public static void applyStatusStyle(Label statusLabel, String status) {
        if (statusLabel == null || status == null) return;
        
        statusLabel.getStyleClass().removeAll("status-open", "status-pending", "status-in-progress", 
                                              "status-completed", "status-cancelled", "status-approved", 
                                              "status-rejected", "status-verified");
        
        String lowerStatus = status.toLowerCase();
        if (lowerStatus.contains("open") || lowerStatus.contains("active")) {
            statusLabel.getStyleClass().add("status-open");
        } else if (lowerStatus.contains("pend") || lowerStatus.contains("wait")) {
            statusLabel.getStyleClass().add("status-pending");
        } else if (lowerStatus.contains("progress") || lowerStatus.contains("ongoing")) {
            statusLabel.getStyleClass().add("status-in-progress");
        } else if (lowerStatus.contains("complet") || lowerStatus.contains("done") || lowerStatus.contains("finish")) {
            statusLabel.getStyleClass().add("status-completed");
        } else if (lowerStatus.contains("cancel")) {
            statusLabel.getStyleClass().add("status-cancelled");
        } else if (lowerStatus.contains("approv") || lowerStatus.contains("accept")) {
            statusLabel.getStyleClass().add("status-approved");
        } else if (lowerStatus.contains("reject") || lowerStatus.contains("declin")) {
            statusLabel.getStyleClass().add("status-rejected");
        } else if (lowerStatus.contains("verif")) {
            statusLabel.getStyleClass().add("status-verified");
        }
    }
}