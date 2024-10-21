package com.example.mediumRoles.repositories;

import com.example.mediumRoles.entities.User;
import com.example.mediumRoles.entities.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    // Método para encontrar la primera imagen de un usuario
    UserImage findFirstByUser(User user);
    // Método para encontrar la imagen por el nombre completo del usuario
    @Query("SELECT ui FROM UserImage ui WHERE ui.user.fullName = :fullName")
    UserImage findFirstByUserFullName(@Param("fullName") String fullName);
}

