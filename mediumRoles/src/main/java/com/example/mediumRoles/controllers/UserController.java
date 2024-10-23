package com.example.mediumRoles.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import com.example.mediumRoles.dtos.UserDTO;
import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.exceptions.CustomPagedResponse;
import com.example.mediumRoles.services.UserService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
 // Endpoint para buscar usuarios por userId o fullName, o devolver todos
    @GetMapping("/")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String fullName, // Parámetro opcional para búsqueda por nombre
            @RequestParam(required = false) Long userId,     // Parámetro opcional para búsqueda por ID
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // Determinar la dirección de ordenación
            Sort sortOrder = direction.equalsIgnoreCase("desc") ? Sort.by(sort).descending() : Sort.by(sort).ascending();
            Pageable pageable = PageRequest.of(page, size, sortOrder);

            // Si se proporciona un userId, buscar por ID
            if (userId != null) {
                UserDTO userDTO = userService.findById(userId);
                if (userDTO != null) {
                    return ResponseEntity.ok(userDTO); // Retorna el usuario si se encuentra por ID
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
                }
            }

            // Si se proporciona un fullName, buscar por nombre
            Page<UserDTO> usersPage;
            if (fullName != null && !fullName.isEmpty()) {
                usersPage = userService.findByFullName(fullName, pageable);
            } else {
                // Si no se proporciona ni userId ni fullName, devolver todos los usuarios
                usersPage = userService.findAllUsers(pageable);
            }

            // Si no se encontraron usuarios, retornar 404
            if (usersPage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron usuarios.");
            }

            // Crear modelo paginado HATEOAS
            CustomPagedResponse<UserDTO> response = new CustomPagedResponse<>(
                usersPage.getContent(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.getNumber()
            );

            // Agregar enlaces HATEOAS para cada usuario
            usersPage.getContent().forEach(user -> {
                Link selfLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .getUsers(user.getFullName(), null, page, size, sort, direction)).withSelfRel();
                response.add(selfLink);
            });

            // Añadir enlace a la página anterior y siguiente si existen
            if (usersPage.hasPrevious()) {
                Link previousLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .getUsers(fullName, null, page - 1, size, sort, direction)).withRel("previous");
                response.add(previousLink);
            }
            if (usersPage.hasNext()) {
                Link nextLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .getUsers(fullName, null, page + 1, size, sort, direction)).withRel("next");
                response.add(nextLink);
            }

            // Agregar enlaces para la primera y última página
            response.add(WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                    .getUsers(fullName, null, 0, size, sort, direction)).withRel("first"));
            if (usersPage.getTotalPages() > 0) {
                response.add(WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .getUsers(fullName, null, usersPage.getTotalPages() - 1, size, sort, direction)).withRel("last"));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud.");
        }
    }

    // Nuevo endpoint para obtener usuarios cacheados
    @GetMapping("/cache")
    public ResponseEntity<List<UserDTO>> getCachedUsers() {
        try {
            List<UserDTO> cachedUsers = userService.getCachedUsers();
            if (cachedUsers == null || cachedUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 si no hay usuarios en caché
            }
            return ResponseEntity.ok(cachedUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 en caso de error
        }
    }
}