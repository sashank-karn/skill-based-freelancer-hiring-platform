package com.freelancer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileShare {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty skill = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();

    public FileShare(String name, String skill, String date) {
        this.name.set(name);
        this.skill.set(skill);
        this.date.set(date);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty skillProperty() { return skill; }
    public StringProperty dateProperty() { return date; }

    // These methods should reference existing properties instead of undefined variables
    public StringProperty fileNameProperty() {
        return name; // Use the existing name property
    }

    public StringProperty uploadDateProperty() {
        return date; // Use the existing date property
    }
}