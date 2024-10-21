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

    @GetMapping("/") // Endpoint para buscar usuarios por nombre o devolver todos
    public ResponseEntity<?> allUsers(
            @RequestParam(required = false) String fullName, // Parámetro opcional para búsqueda
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) { // Añadir dirección de ordenación
        try {
            // Determinar la dirección de ordenación
            Sort sortOrder = direction.equalsIgnoreCase("desc") ? Sort.by(sort).descending() : Sort.by(sort).ascending();
            Pageable pageable = PageRequest.of(page, size, sortOrder); // Soporta ordenación
            Page<UserDTO> usersPage;

            // Si se proporciona el nombre, busca usuarios por ese nombre
            if (fullName != null && !fullName.isEmpty()) {
                // Busca un único usuario por nombre completo
                UserDTO userDTO = userService.findByFullName(fullName);
                if (userDTO == null) {
                    return ResponseEntity.notFound().build(); // Retornar 404 si no se encuentra el usuario
                }
                // Devolver el usuario encontrado sin HATEOAS
                return ResponseEntity.ok(EntityModel.of(userDTO)); // Esto no causará un error de tipo
            } else {
                // Si no se proporciona el nombre, obtiene todos los usuarios
                usersPage = userService.findAllUsers(pageable);
            }

            // Si la lista está vacía, retorna un mensaje adecuado
            if (usersPage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
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
            for (UserDTO user : usersPage.getContent()) {
                Link selfLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class).getUserByFullName(user.getFullName())).withSelfRel();
                response.add(selfLink);
            }

            // Añadir enlace a la página anterior y siguiente si existen
            if (usersPage.hasPrevious()) {
                Link previousLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .allUsers(fullName, page - 1, size, sort, direction)).withRel("previous");
                response.add(previousLink);
            }
            if (usersPage.hasNext()) {
                Link nextLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .allUsers(fullName, page + 1, size, sort, direction)).withRel("next");
                response.add(nextLink);
            }

            // Agregar enlaces para la primera y última página
            response.add(WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                    .allUsers(fullName, 0, size, sort, direction)).withRel("first"));
            if (usersPage.getTotalPages() > 0) {
                response.add(WebMvcLinkBuilder.linkTo(methodOn(UserController.class)
                        .allUsers(fullName, usersPage.getTotalPages() - 1, size, sort, direction)).withRel("last"));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/byFullName") // Endpoint para buscar usuario por nombre completo
    public ResponseEntity<EntityModel<UserDTO>> getUserByFullName(@RequestParam String fullName) {
        UserDTO userDTO = userService.findByFullName(fullName); // Llamamos al servicio
        if (userDTO == null) {
            return ResponseEntity.notFound().build();
        }

        EntityModel<UserDTO> userResource = EntityModel.of(userDTO);
        Link selfLink = WebMvcLinkBuilder.linkTo(methodOn(UserController.class).getUserByFullName(fullName)).withSelfRel();
        userResource.add(selfLink);

        return ResponseEntity.ok(userResource);
    }
}