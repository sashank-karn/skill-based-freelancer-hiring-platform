package com.freelancer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Skill {
    private final StringProperty name;
    private final StringProperty status;
    
    public Skill(String name, String status) {
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
}