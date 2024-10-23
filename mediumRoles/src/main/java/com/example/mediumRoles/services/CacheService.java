package com.example.mediumRoles.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 0 0 * * MON") // Cada lunes a la medianoche
    public void updateCache() {
        // Limpiar la caché
        cacheManager.getCache("users").clear();

        // Volver a cargar la caché
        userService.getAllUsers();
    }
}