package com.example.mediumRoles.controllers;

import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.entities.UserPdf;
import com.example.mediumRoles.repositories.UserPdfRepository;
import com.example.mediumRoles.repositories.UserRepository;
import com.example.mediumRoles.exceptions.CustomErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/users/pdfs")
public class UserPdfController {

    private static final Logger logger = LoggerFactory.getLogger(UserPdfController.class);
    private final UserPdfRepository userPdfRepository;
    private final UserRepository userRepository;
    private final String uploadDirPdfs = "C:/Users/PCalex/Desktop/mediumRoles/userPdfs/";

    public UserPdfController(UserPdfRepository userPdfRepository, UserRepository userRepository) {
        this.userPdfRepository = userPdfRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> uploadPdf(@RequestParam("pdf") MultipartFile file, @RequestParam("userId") Long userId) {
        logger.info("Iniciando el proceso de subida de PDF");

        // Buscar usuario por ID
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado.", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar un usuario con el ID proporcionado."));
        }

        // Validar que el archivo sea PDF
        if (!"application/pdf".equals(file.getContentType())) {
            logger.warn("El archivo proporcionado no es un PDF.");
            return ResponseEntity.badRequest()
                    .body(new CustomErrorResponse("Solo se permiten archivos PDF.", HttpStatus.BAD_REQUEST.value(), "El tipo de archivo proporcionado no es válido."));
        }

        // Crear directorio si no existe
        File directory = new File(uploadDirPdfs);
        if (!directory.exists() && !directory.mkdirs()) {
            logger.error("No se pudo crear el directorio: {}", uploadDirPdfs);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("No se pudo crear el directorio para guardar el PDF.", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al crear el directorio en el sistema de archivos."));
        }

        // Generar nombre del archivo con el nuevo formato
        String fileName = user.getFullName().replaceAll(" ", "") + "_" + userId + ".pdf"; 
        String pdfPath = uploadDirPdfs + fileName;

        // Guardar el PDF
        try {
            file.transferTo(new File(pdfPath));
            logger.info("PDF guardado en: {}", pdfPath);
        } catch (IOException e) {
            logger.error("Error al subir el PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error al subir el PDF.", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }

        // Guardar o actualizar el PDF en la base de datos
        UserPdf userPdf = userPdfRepository.findFirstByUser(user);
        if (userPdf == null) {
            userPdf = new UserPdf();
            userPdf.setUser(user);
        }
        userPdf.setPdfPath(pdfPath);
        userPdf.setFileName(fileName);
        userPdfRepository.save(userPdf);
        logger.info("PDF guardado correctamente para el usuario: {}", user.getUsername());

        return ResponseEntity.ok("PDF subido correctamente.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<?> updatePdf(@RequestParam("pdf") MultipartFile file, @RequestParam("userId") Long userId) {
        logger.info("Iniciando el proceso de actualización de PDF");

        // Buscar usuario por ID
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado.", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar un usuario con el ID proporcionado."));
        }

        // Validar que el archivo sea PDF
        if (!"application/pdf".equals(file.getContentType())) {
            logger.warn("El archivo proporcionado no es un PDF.");
            return ResponseEntity.badRequest()
                    .body(new CustomErrorResponse("Solo se permiten archivos PDF.", HttpStatus.BAD_REQUEST.value(), "El tipo de archivo proporcionado no es válido."));
        }

        // Obtener el primer PDF del usuario
        UserPdf userPdf = userPdfRepository.findFirstByUser(user);
        if (userPdf == null) {
            logger.warn("No se encontró PDF para el usuario: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("No se encontró PDF.", HttpStatus.NOT_FOUND.value(), "No se encontró PDF para el usuario."));
        }

        // Generar nuevo nombre de archivo con el nuevo formato
        String pdfPath = uploadDirPdfs + user.getFullName().replaceAll(" ", "") + "_" + userId + ".pdf";
        try {
            file.transferTo(new File(pdfPath));
            logger.info("PDF actualizado en: {}", pdfPath);    
            userPdf.setPdfPath(pdfPath);
            userPdfRepository.save(userPdf);
        } catch (IOException e) {
            logger.error("Error al actualizar el PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error al actualizar el PDF.", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }

        return ResponseEntity.ok("PDF actualizado correctamente.");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<?> getPdf(@RequestParam("userId") Long userId) {
        logger.info("Iniciando el proceso de obtención del PDF");

        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();

            // Buscar usuario por ID
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomErrorResponse("Usuario no encontrado.", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar un usuario con el ID proporcionado."));
            }

            // Verificar si el usuario está autorizado para acceder al PDF
            boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !user.getUsername().equals(loggedInUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new CustomErrorResponse("Acceso prohibido.", HttpStatus.FORBIDDEN.value(), "No tienes permiso para acceder a este PDF."));
            }

            // Obtener el PDF del usuario
            UserPdf userPdf = userPdfRepository.findFirstByUser(user);
            if (userPdf == null || userPdf.getPdfPath() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomErrorResponse("No se encontró PDF.", HttpStatus.NOT_FOUND.value(), "No se encontró PDF para el usuario."));
            }

            // Leer el PDF desde el sistema de archivos
            Path pdfPath = Paths.get(userPdf.getPdfPath());
            byte[] pdfBytes = Files.readAllBytes(pdfPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error de entrada/salida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error de entrada/salida.", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomErrorResponse("Error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deletePdf(@RequestParam("userId") Long userId) {
        logger.info("Iniciando el proceso de eliminación del PDF");

        // Buscar usuario por ID
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("Usuario no encontrado.", HttpStatus.NOT_FOUND.value(), "No se pudo encontrar un usuario con el ID proporcionado."));
        }

        // Obtener el primer PDF del usuario
        UserPdf userPdf = userPdfRepository.findFirstByUser(user);
        if (userPdf != null && userPdf.getPdfPath() != null) {
            File pdfFile = new File(userPdf.getPdfPath());
            if (pdfFile.exists() && pdfFile.delete()) {
                userPdfRepository.delete(userPdf); // Eliminar el registro de la base de datos
                logger.info("PDF eliminado correctamente para el usuario: {}", user.getUsername());
                return ResponseEntity.ok("PDF eliminado correctamente.");
            } else {
                logger.warn("El archivo de PDF no existe o no se pudo eliminar.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomErrorResponse("No se pudo eliminar el PDF.", HttpStatus.NOT_FOUND.value(), "No se encontró el archivo PDF para el usuario."));
            }
        } else {
            logger.warn("No se encontró PDF para el usuario: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomErrorResponse("No se encontró PDF.", HttpStatus.NOT_FOUND.value(), "No se encontró PDF para el usuario."));
        }
    }
}