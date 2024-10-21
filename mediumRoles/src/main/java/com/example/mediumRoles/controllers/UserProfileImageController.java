package com.example.mediumRoles.controllers;
import com.example.mediumRoles.entities.RoleEnum;
import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.entities.UserImage;
import com.example.mediumRoles.repositories.UserImageRepository;
import com.example.mediumRoles.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.example.mediumRoles.exceptions.CustomErrorResponse;
@RestController
@RequestMapping("/users/images")
public class UserProfileImageController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileImageController.class);
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final String uploadDirImages = "C:/Users/PCalex/Desktop/mediumRoles/profileImg/";

    public UserProfileImageController(UserRepository userRepository, UserImageRepository userImageRepository) {
        this.userRepository = userRepository;
        this.userImageRepository = userImageRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile file,
                                                @RequestParam("userId") Long userId, // Cambiar fullName a userId
                                                @AuthenticationPrincipal User currentUser) {
        // Comprobando si el archivo es nulo o está vacío
        if (file == null || file.isEmpty()) {
            logger.warn("Archivo no proporcionado o vacío");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CustomErrorResponse("Archivo vacío", HttpStatus.BAD_REQUEST.value(), "Se requiere un archivo de imagen."));
        }

        // Verificando el usuario por ID
        User user = userRepository.findById(userId).orElse(null); // Usar findById en lugar de findByFullName
        if (user == null) {
            logger.warn("Usuario no encontrado: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar el usuario con el ID proporcionado."));
        }

        // Continuar con el manejo de la carga de la imagen
        return handleImageUpload(file, user);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<?> getProfileImage(@RequestParam("userId") Long userId) { // Cambiar fullName a userId
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("Usuario no encontrado: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar el usuario con el ID proporcionado."));
        }

        UserImage userImage = userImageRepository.findFirstByUser(user);
        if (userImage == null || userImage.getImagePath() == null) {
            logger.warn("No se encontró la imagen para el usuario: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Imagen no encontrada", HttpStatus.NOT_FOUND.value(), "No se encontró la imagen para el usuario proporcionado."));
        }

        Path imagePath = Paths.get(userImage.getImagePath());
        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(imagePath);
        } catch (IOException e) {
            logger.error("Error al leer la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error interno", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al leer la imagen."));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<?> updateProfileImage(@RequestParam("image") MultipartFile file,
                                                @RequestParam("userId") Long userId) {
        // Verifica que el archivo no esté vacío
        if (file.isEmpty()) {
            logger.warn("No se ha proporcionado ningún archivo.");
            return ResponseEntity.badRequest()
                    .body(new CustomErrorResponse("Error de carga", HttpStatus.BAD_REQUEST.value(), "No se ha proporcionado ningún archivo."));
        }

        // Verifica el usuario
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            logger.warn("Usuario no encontrado: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar el usuario con el ID proporcionado."));
        }
        User user = userOptional.get();

        // Maneja la carga de la imagen
        return handleImageUpload(file, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteProfileImage(@RequestParam("userId") Long userId) { // Cambiar fullName a userId
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("Usuario no encontrado: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar el usuario con el ID proporcionado."));
        }

        UserImage userImage = userImageRepository.findFirstByUser(user);
        if (userImage != null && userImage.getImagePath() != null) {
            File imageFile = new File(userImage.getImagePath());
            if (imageFile.exists() && imageFile.delete()) {
                userImageRepository.delete(userImage);
                logger.info("Imagen de perfil eliminada correctamente para el usuario: {}", userId);
                return ResponseEntity.ok("Imagen de perfil eliminada correctamente.");
            }
        }
        logger.warn("No se encontró la imagen de perfil para eliminar para el usuario: {}", userId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse("No encontrado", HttpStatus.NOT_FOUND.value(), "No se encontró la imagen de perfil para eliminar."));
    }

    private ResponseEntity<?> handleImageUpload(MultipartFile file, User user) {
        // Verifica que el archivo no esté vacío
        if (file.isEmpty()) {
            logger.warn("No se ha proporcionado ningún archivo.");
            return ResponseEntity.badRequest()
                    .body(new CustomErrorResponse("Error de carga", HttpStatus.BAD_REQUEST.value(), "No se ha proporcionado ningún archivo."));
        }

        // Verifica el tipo de contenido del archivo
        String contentType = file.getContentType();
        logger.info("Tipo de archivo recibido: {}", contentType);
        if (contentType == null || !contentType.equals(MediaType.IMAGE_JPEG_VALUE)) {
            logger.warn("Tipo de archivo no permitido: {}", contentType);
            return ResponseEntity.badRequest()
                    .body(new CustomErrorResponse("Tipo de archivo no permitido", HttpStatus.BAD_REQUEST.value(), "Solo se permiten archivos JPG."));
        }

        // Crea el directorio si no existe
        File directory = new File(uploadDirImages);
        if (!directory.exists()) {
            logger.info("Creando directorio: {}", uploadDirImages);
            if (!directory.mkdirs()) {
                logger.error("No se pudo crear el directorio: {}", uploadDirImages);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new CustomErrorResponse("Error interno", HttpStatus.INTERNAL_SERVER_ERROR.value(), "No se pudo crear el directorio para guardar la imagen."));
            }
        }

        // Define el nombre de la imagen basado en el nombre completo del usuario
        String imageName = user.getFullName().replace(" ", "").toLowerCase() + "_" + user.getId() + ".jpg"; // Ejemplo: alexFernandez_1.jpg // Ejemplo: alexFernandez.jpg
        String imagePath = uploadDirImages + imageName;

        logger.info("Guardando la imagen en: {}", imagePath);
        try {
            file.transferTo(new File(imagePath));
        } catch (IOException e) {
            logger.error("Error al guardar la imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error al subir la imagen", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al subir la imagen: " + e.getMessage()));
        }

        // Guardar o actualizar la información de la imagen en la base de datos
        UserImage userImage = userImageRepository.findFirstByUser(user);
        if (userImage != null) {
            userImage.setImagePath(imagePath);
            userImage.setImageName(imageName); // Establece el nombre de la imagen
        } else {
            userImage = new UserImage();
            userImage.setUser(user);
            userImage.setImagePath(imagePath);
            userImage.setImageName(imageName); // Establece el nombre de la imagen
        }

        try {
            userImageRepository.save(userImage);
        } catch (Exception e) {
            logger.error("Error al guardar el registro de la imagen en la base de datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error al guardar en la base de datos", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al actualizar la imagen de perfil en la base de datos."));
        }

        return ResponseEntity.ok("Imagen de perfil subida correctamente como " + imageName);
    }
}