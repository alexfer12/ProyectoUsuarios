package com.example.mediumRoles.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AsyncService {

    // Cambia la ruta según sea necesario
    private static final String FILE_PATH = "C:/Users/PCalex/OneDrive/Desktop/mediumRoles/ficheros/";

    @Async
    public void generateUserFile(String fullName, String email, Integer userId) {
        // Espera 3 segundos
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restablece el estado de interrupción
        }

        // Sanitiza el nombre completo
        String sanitizedFullName = fullName.replaceAll("[^a-zA-Z0-9]", "_"); // Reemplaza caracteres no alfanuméricos
        String fileName = FILE_PATH + sanitizedFullName + "_" + userId + ".txt"; // Nombre del archivo
        System.out.println("Intentando generar archivo en: " + fileName); // Imprime la ruta del archivo

        // Crear la carpeta si no existe
        File directory = new File(FILE_PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // Crea el directorio
            if (created) {
                System.out.println("Directorio creado: " + directory.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio: " + directory.getAbsolutePath());
            }
        } else {
            System.out.println("El directorio ya existe: " + directory.getAbsolutePath());
        }

        String content = String.format("Full Name: %s\nEmail: %s\nUser ID: %d\nRegistered At: %s",
                fullName, email, userId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Intenta escribir en el archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) { // false para sobreescribir
            writer.write(content);
            writer.flush(); // Asegura que todo se escriba en el archivo
            System.out.println("Archivo generado: " + fileName); // Mensaje de confirmación en consola
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
            e.printStackTrace(); // Muestra el error en caso de que no se pueda escribir en el archivo
        }
    }
}