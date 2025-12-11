package com.mangareader.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mangareader.util.HttpClient;
import com.mangareader.util.MangaDexParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manga")
@CrossOrigin(origins = "*")
public class MangaController {

    private final HttpClient httpClient;
    private final MangaDexParser parser;
    private final ObjectMapper mapper;

    public MangaController(HttpClient httpClient, MangaDexParser parser, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.parser = parser;
        this.mapper = mapper;
    }

    /**
     * Search manga by title
     * GET /api/manga/search?q=<title>
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchManga(@RequestParam(required = false) String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing query parameter 'q'"));
            }

            // Encode query for URL
            String encodedQuery = URLEncoder.encode(q, StandardCharsets.UTF_8);

            // Call MangaDex API
            String apiUrl = "https://api.mangadex.org/manga?title=" + encodedQuery;
            String response = httpClient.get(apiUrl);

            // Parse and simplify response
            ArrayNode results = parser.parseMangaSearchResults(response);

            // For each manga, try to get cover URL
            for (int i = 0; i < results.size(); i++) {
                ObjectNode manga = (ObjectNode) results.get(i);
                String coverId = manga.has("coverId") ? manga.get("coverId").asText() : null;

                if (coverId != null && !coverId.isEmpty()) {
                    try {
                        String coverUrl = "https://api.mangadex.org/cover/" + coverId;
                        String coverResponse = httpClient.get(coverUrl);

                        // Extract cover filename
                        JsonNode coverRoot = mapper.readTree(coverResponse);
                        JsonNode coverData = coverRoot.get("data");

                        if (coverData != null) {
                            JsonNode attributes = coverData.get("attributes");
                            if (attributes != null && attributes.has("fileName")) {
                                String fileName = attributes.get("fileName").asText();
                                String mangaId = manga.get("id").asText();
                                String fullCoverUrl = String.format(
                                        "https://uploads.mangadex.org/covers/%s/%s",
                                        mangaId, fileName);
                                manga.put("cover", fullCoverUrl);
                            }
                        }
                    } catch (Exception e) {
                        // If cover fetch fails, just leave it empty
                        manga.put("cover", "");
                    }

                    // Remove coverId from final response
                    manga.remove("coverId");
                }
            }

            // Build final response
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("results", results);

            return ResponseEntity.ok(finalResponse);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createError(500, "Error searching manga: " + e.getMessage()));
        }
    }

    /**
     * Get manga details by ID
     * GET /api/manga/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMangaDetails(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing manga ID"));
            }

            // Fetch manga details
            String mangaUrl = "https://api.mangadex.org/manga/" + id;
            String mangaResponse = httpClient.get(mangaUrl);

            // Parse to get cover ID
            JsonNode mangaRoot = mapper.readTree(mangaResponse);
            JsonNode mangaData = mangaRoot.get("data");

            if (mangaData == null) {
                return ResponseEntity.status(404)
                        .body(createError(404, "Manga not found"));
            }

            // Get cover ID from relationships
            String coverId = null;
            JsonNode relationships = mangaData.get("relationships");
            if (relationships != null && relationships.isArray()) {
                for (JsonNode rel : relationships) {
                    if ("cover_art".equals(rel.get("type").asText())) {
                        coverId = rel.get("id").asText();
                        break;
                    }
                }
            }

            // Fetch cover details if available
            String coverResponse = null;
            if (coverId != null && !coverId.isEmpty()) {
                try {
                    String coverUrl = "https://api.mangadex.org/cover/" + coverId;
                    coverResponse = httpClient.get(coverUrl);
                } catch (Exception e) {
                    // Cover fetch failed, continue without it
                }
            }

            // Parse and merge
            ObjectNode result = parser.parseMangaDetail(mangaResponse, coverResponse);

            // Remove coverId from final response
            result.remove("coverId");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createError(500, "Error fetching manga: " + e.getMessage()));
        }
    }

    /**
     * List chapters for a manga
     * GET /api/manga/{id}/chapters
     */
    @GetMapping("/{id}/chapters")
    public ResponseEntity<?> getMangaChapters(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing manga ID"));
            }

            // Fetch chapters feed
            String chaptersUrl = String.format(
                    "https://api.mangadex.org/manga/%s/feed?translatedLanguage[]=en&order[chapter]=asc&limit=500",
                    id);
            String chaptersResponse = httpClient.get(chaptersUrl);

            // Parse chapters
            ArrayNode chapters = parser.parseChapters(chaptersResponse);

            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("chapters", chapters);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createError(500, "Error fetching chapters: " + e.getMessage()));
        }
    }

    /**
     * Get chapter pages
     * GET /api/manga/chapter/{chapterId}/pages
     */
    @GetMapping("/chapter/{chapterId}/pages")
    public ResponseEntity<?> getChapterPages(@PathVariable String chapterId) {
        try {
            if (chapterId == null || chapterId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing chapter ID"));
            }

            // Fetch chapter pages from at-home server
            String atHomeUrl = "https://api.mangadex.org/at-home/server/" + chapterId;
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
