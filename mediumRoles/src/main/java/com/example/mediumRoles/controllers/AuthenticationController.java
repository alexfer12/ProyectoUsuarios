package com.example.mediumRoles.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mediumRoles.dtos.LoginUserDto;
import com.example.mediumRoles.dtos.RegisterUserDto;
import com.example.mediumRoles.dtos.UserDTO;
import com.example.mediumRoles.entities.RoleEnum;
import com.example.mediumRoles.entities.User;

import com.example.mediumRoles.exceptions.InvalidCredentialsException;
import com.example.mediumRoles.exceptions.UserAlreadyExistsException;
import com.example.mediumRoles.services.AuthenticationService;
import com.example.mediumRoles.services.JwtService;
import com.example.mediumRoles.services.UserService;

import jakarta.validation.Valid;
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto registerUserDto, BindingResult result) {
        // Verifica si hay errores de validación
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append(". "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors.toString());
        }

        try {
            String roleString = registerUserDto.getRole();
            RoleEnum requestedRole = RoleEnum.valueOf(roleString.toUpperCase());

            User registeredUser = authenticationService.signup(registerUserDto, requestedRole);
            
            UserDTO responseDto = new UserDTO(
                registeredUser.getId(),
                registeredUser.getFullName(),
                registeredUser.getEmail()
            );

            return ResponseEntity.ok(responseDto);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya existe.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rol inválido.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el usuario.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            UserDetails authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);

            LoginResponse loginResponse = new LoginResponse()
                    .setToken(jwtToken)
                    .setExpiresIn(jwtService.getExpirationTime());

            return ResponseEntity.ok(loginResponse);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar sesión.");
        }
    }
}