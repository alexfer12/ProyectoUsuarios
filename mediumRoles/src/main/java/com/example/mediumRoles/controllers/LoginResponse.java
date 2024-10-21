package com.example.mediumRoles.controllers;

public class LoginResponse {
    private String token;

    private long expiresIn;

    public String getToken() {
        return token;
    }

	public long getExpiresIn() {
		return expiresIn;
	}

	/*
	 * public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
	 * 
	 * public void setToken(String token) { this.token = token; }
	 * 
	 * // Getters and setters...
	 */    
	   public LoginResponse setToken(String token) {
	        this.token = token;
	        return this;  // Devuelve this para permitir el encadenamiento
	    }

	    public LoginResponse setExpiresIn(long expiresIn) {
	        this.expiresIn = expiresIn;
	        return this;  // Devuelve this para permitir el encadenamiento
	    }
}