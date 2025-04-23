package com.freelancer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Skill {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public Skill(String name, String status) {
        this.name.set(name);
        this.status.set(status);
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty statusProperty() { return status; }
}