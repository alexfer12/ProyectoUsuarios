package com.example.mediumRoles.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "users", key = "'allUsers'")
    public List<UserDTO> getAllUsers() {
        // Lógica para obtener usuarios de la base de datos
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Método para obtener usuarios cacheados
    @Cacheable(value = "users", key = "'cachedUsers'")
    public List<UserDTO> getCachedUsers() {
        // Simplemente llama a getAllUsers() para obtener los usuarios en caché
        return getAllUsers();
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

    // Método para encontrar un usuario por ID
    public UserDTO findById(Long userId) {
        Optional<User> user = userRepository.findById(userId); // Busca el usuario por ID
        return user.isPresent() ? convertToDTO(user.get()) : null; // Convierte a UserDTO si existe
    }

    // Método adicional para buscar por nombre y paginación
    public Page<UserDTO> findByFullName(String fullName, Pageable pageable) {
        return userRepository.findByFullNameContainingIgnoreCase(fullName, pageable)
                             .map(this::convertToDTO);
    }
}