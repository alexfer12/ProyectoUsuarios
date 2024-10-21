package com.example.mediumRoles.exceptions;

import java.time.LocalDateTime;


public class CustomErrorResponse {
    private String title;
    private int status;
    private String description;
    private LocalDateTime timestamp; // Agregar campo de timestamp

    public CustomErrorResponse(String title, int status, String description) {
        this.title = title;
        this.status = status;
        this.description = description;
        this.timestamp = LocalDateTime.now(); // Establecer el timestamp en la creación
    }

    // Getters y Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}