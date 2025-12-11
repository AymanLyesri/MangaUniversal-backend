package com.mangareader.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * 
 * Purpose: Keep the server awake and provide health status information
 * 
 * This endpoint is pinged regularly by GitHub Actions to prevent
 * the server from going to sleep on free-tier hosting platforms like Render.
 * 
 * The endpoint is lightweight and returns server status and timestamp.
 */
@RestController
@RequestMapping("/healthcheck")
@CrossOrigin(origins = "*")
public class HealthCheckController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Health check endpoint
     * GET /healthcheck
     * 
     * Returns server status and current timestamp.
     * Used by automated pings to keep server awake.
     * 
     * Response:
     * {
     * "status": "OK",
     * "message": "Server is running",
     * "timestamp": "2025-12-11 10:30:00",
     * "uptime": "healthy"
     * }
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        String timestamp = LocalDateTime.now().format(formatter);

        // Log the health check request (helps with monitoring)
        System.out.println("[HEALTH CHECK] Server pinged at: " + timestamp);

        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Server is running");
        response.put("timestamp", timestamp);
        response.put("uptime", "healthy");

        return ResponseEntity.ok(response);
    }

    /**
     * Extended health check endpoint
     * GET /healthcheck/status
     * 
     * Provides detailed server health information.
     * Can be extended to include database connections, external service status,
     * etc.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> detailedStatus() {
        String timestamp = LocalDateTime.now().format(formatter);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", timestamp);
        response.put("server", "manga-universal-backend");
        response.put("version", "1.0.0");

        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        Map<String, Object> memory = new HashMap<>();
        memory.put("used", usedMemory + " MB");
        memory.put("max", maxMemory + " MB");
        response.put("memory", memory);

        // System properties
        Map<String, String> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        response.put("system", system);

        System.out.println("[HEALTH CHECK - DETAILED] Status checked at: " + timestamp);

        return ResponseEntity.ok(response);
    }
}
