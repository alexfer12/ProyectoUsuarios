package com.example.mediumRoles.repositories;

import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.entities.UserPdf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPdfRepository extends JpaRepository<UserPdf, Long> {
    // Aquí puedes agregar métodos personalizados si lo necesitas
	 // Método para encontrar el primer PDF por usuario
    UserPdf findFirstByUser(User user);
}