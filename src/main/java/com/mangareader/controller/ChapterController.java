package com.mangareader.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mangareader.util.HttpClient;
import com.mangareader.util.MangaDexParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chapter")
@CrossOrigin(origins = "*")
public class ChapterController {

    private final HttpClient httpClient;
    private final MangaDexParser parser;

    public ChapterController(HttpClient httpClient, MangaDexParser parser) {
        this.httpClient = httpClient;
        this.parser = parser;
    }

    /**
     * Get chapter pages
     * GET /api/chapter/{id}/pages
     */
    @GetMapping("/{id}/pages")
    public ResponseEntity<?> getChapterPages(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing chapter ID"));
            }

            // Fetch chapter pages from at-home server
            String atHomeUrl = "https://api.mangadex.org/at-home/server/" + id;
            String atHomeResponse = httpClient.get(atHomeUrl);

            // Parse page URLs
            ArrayNode pages = parser.parseChapterPages(atHomeResponse);

            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("pages", pages);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createError(500, "Error fetching chapter pages: " + e.getMessage()));
        }
    }

    private Map<String, Object> createError(int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", status);
        return error;
    }
}
