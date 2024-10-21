package com.example.mediumRoles.dtos;

public class UserDTO {
    private Integer id;
    private String fullName;
    private String email;
    // No incluir imágenes aquí

    // Constructor
    public UserDTO(Integer id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
}