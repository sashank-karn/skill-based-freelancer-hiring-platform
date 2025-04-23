package com.freelancer;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class FileRecord {
    private final StringProperty fileId;
    private final StringProperty fileName;
    private final StringProperty uploadDate;

    public FileRecord(String fileId, String fileName, String uploadDate) {
        this.fileId = new SimpleStringProperty(fileId);
        this.fileName = new SimpleStringProperty(fileName);
        this.uploadDate = new SimpleStringProperty(uploadDate);
    }

    public String getFileId() {
        return fileId.get();
    }

    public StringProperty fileIdProperty() {
        return fileId;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public String getUploadDate() {
        return uploadDate.get();
    }

    public StringProperty uploadDateProperty() {
        return uploadDate;
    }
}
