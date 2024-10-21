package com.example.mediumRoles.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterUserDto {
    
	@Email(message = "El email debe tener un formato válido.")
    @NotBlank(message = "El email es obligatorio.")
	private String email;
	
	 // Validación de contraseña: al menos 8 caracteres, con al menos 1 mayúscula, 1 minúscula y 1 número
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", 
             message = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.")
    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
    
    @NotBlank(message = "El nombre completo es obligatorio.")
    private String fullName;
    
    private String role;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
    
    // getters and setters here...
    
}