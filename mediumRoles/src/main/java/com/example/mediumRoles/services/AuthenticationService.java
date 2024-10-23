package com.example.mediumRoles.services;

import com.example.mediumRoles.dtos.LoginUserDto;
import com.example.mediumRoles.dtos.RegisterUserDto;
import com.example.mediumRoles.entities.RoleEnum;
import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.exceptions.InvalidCredentialsException;
import com.example.mediumRoles.exceptions.UserAlreadyExistsException;
import com.example.mediumRoles.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AsyncService asyncService; // Asegúrate de inyectar el AsyncService

    @Autowired
    public AuthenticationService(UserRepository userRepository, 
                                  BCryptPasswordEncoder passwordEncoder,
                                  AsyncService asyncService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncService = asyncService; // Inyectar el servicio asíncrono
    }

    // Método de autenticación (login)
    public UserDetails authenticate(LoginUserDto loginUserDto) throws InvalidCredentialsException {
        Optional<User> optionalUser = userRepository.findByEmail(loginUserDto.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Verifica la contraseña
            if (passwordEncoder.matches(loginUserDto.getPassword(), user.getPassword())) {
                // Retorna un CustomUserDetails que contiene los roles
                return new CustomUserDetails(user);
            }
        }

        throw new InvalidCredentialsException("Credenciales inválidas.");
    }

    // Método de registro (signup)
    public User signup(RegisterUserDto registerUserDto, RoleEnum role) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("El usuario ya existe.");
        }

        User newUser = new User();
        newUser.setFullName(registerUserDto.getFullName());
        newUser.setEmail(registerUserDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword())); // Encriptar la contraseña
        newUser.setRole(role); // Establecer el rol

        User registeredUser = userRepository.save(newUser);

        // Llama al servicio asincrónico para generar el archivo
        asyncService.generateUserFile(newUser.getFullName(), newUser.getEmail(), registeredUser.getId());

        return registeredUser;
    }
}