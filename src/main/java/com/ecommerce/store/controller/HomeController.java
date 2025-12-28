package com.ecommerce.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Home controller for root endpoint.
 */
@RestController
public class HomeController {

    /**
     * Welcome endpoint for the API root.
     *
     * GET /
     *
     * Returns basic API information.
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = Map.of(
            "message", "Welcome to Ecommerce Store API",
            "version", "1.0.0",
            "documentation", "http://localhost:8080/swagger-ui/index.html",
            "api-docs", "http://localhost:8080/api-docs",
            "status", "running"
        );
        return ResponseEntity.ok(response);
    }
}