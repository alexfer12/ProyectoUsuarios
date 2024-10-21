package com.example.mediumRoles.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.mediumRoles.dtos.UserDTO;
import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.repositories.UserRepository;



@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Método para encontrar un usuario por nombre completo
    public UserDTO findByFullName(String fullName) {
        User user = userRepository.findByFullName(fullName); // Busca el usuario por nombre completo
        return user != null ? convertToDTO(user) : null; // Convierte a UserDTO
    }

    // Método para obtener todos los usuarios con paginación
    public Page<UserDTO> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    // Método para convertir User a UserDTO
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getFullName(), user.getEmail());
    }

    // Método adicional para buscar por nombre y paginación
    public Page<UserDTO> findByFullName(String fullName, Pageable pageable) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName, pageable)
                             .map(this::convertToDTO);
    }
}