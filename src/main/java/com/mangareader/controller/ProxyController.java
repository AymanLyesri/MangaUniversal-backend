package com.mangareader.controller;

import com.mangareader.service.ImageProxyService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/proxy/mangadex")
@CrossOrigin(origins = "*")
public class ProxyController {

    private final ImageProxyService imageProxyService;

    public ProxyController(ImageProxyService imageProxyService) {
        this.imageProxyService = imageProxyService;
    }

    /**
     * Proxy manga cover images from MangaDex
     * GET /proxy/mangadex/cover/{mangaId}/{filename}
     */
    @GetMapping("/cover/{mangaId}/{filename}")
    public ResponseEntity<?> proxyCover(
            @PathVariable String mangaId,
            @PathVariable String filename) {

        try {
            // Validate inputs
            if (mangaId == null || mangaId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing manga ID"));
            }

            if (filename == null || filename.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing filename"));
            }

            // Validate filename format
            if (!filename.matches("^[a-zA-Z0-9\\-_\\.]+\\.(jpg|jpeg|png|gif|webp)$")) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Invalid filename format"));
            }

            // Build cover URL
            String coverUrl = String.format("https://uploads.mangadex.org/covers/%s/%s", mangaId, filename);

            // Fetch cover image
            byte[] imageBytes = imageProxyService.fetchCoverImage(coverUrl);

            // Determine content type
            MediaType contentType = getMediaTypeFromFilename(filename);

            // Build response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setCacheControl("public, max-age=604800"); // Cache for 7 days (covers don't change)
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Cover proxy error for manga " + mangaId + ", file " + filename + ": " + e.getMessage());

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = "Failed to fetch cover: " + e.getMessage();

            if (e.getMessage().contains("not found") || e.getMessage().contains("404")) {
                status = HttpStatus.NOT_FOUND;
                message = "Cover not found";
            }

            return ResponseEntity.status(status)
                    .body(createError(status.value(), message));
        }
    }

    /**
     * Proxy manga page images from MangaDex
     * GET /proxy/mangadex/{chapterId}/{filename}
     * GET /proxy/mangadex/{chapterId}/{filename}?dataSaver=true
     */
    @GetMapping("/{chapterId}/{filename}")
    public ResponseEntity<?> proxyImage(
            @PathVariable String chapterId,
            @PathVariable String filename,
            @RequestParam(required = false, defaultValue = "false") boolean dataSaver) {

        try {
            // Validate inputs
            if (chapterId == null || chapterId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing chapter ID"));
            }

            if (filename == null || filename.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Missing filename"));
            }

            // Validate filename format (should be like "x1-abc123.jpg" or "x1-abc123.png")
            if (!filename.matches("^[a-zA-Z0-9\\-_]+\\.(jpg|jpeg|png|gif|webp)$")) {
                return ResponseEntity.badRequest()
                        .body(createError(400, "Invalid filename format"));
            } // Fetch image bytes
            byte[] imageBytes = imageProxyService.fetchImage(chapterId, filename, dataSaver);

            // Determine content type from filename extension
            MediaType contentType = getMediaTypeFromFilename(filename);

            // Build response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setCacheControl("public, max-age=86400"); // Cache in browser for 1 day
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Log error (in production, use proper logging framework)
            System.err.println("Proxy error for chapter " + chapterId + ", file " + filename + ": " + e.getMessage());

            // Determine appropriate status code
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = "Failed to fetch image: " + e.getMessage();

            if (e.getMessage().contains("not found") || e.getMessage().contains("404")) {
                status = HttpStatus.NOT_FOUND;
                message = "Image not found";
            } else if (e.getMessage().contains("Filename not found")) {
                status = HttpStatus.NOT_FOUND;
                message = "Filename not found in chapter data";
            } else if (e.getMessage().contains("At-Home")) {
                status = HttpStatus.BAD_GATEWAY;
                message = "Failed to connect to MangaDex servers";
            }

            return ResponseEntity.status(status)
                    .body(createError(status.value(), message));
        }
    }

    /**
     * Determine media type from filename extension
     */
    private MediaType getMediaTypeFromFilename(String filename) {
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowerFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerFilename.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }

        // Default to JPEG
        return MediaType.IMAGE_JPEG;
    }

    /**
     * Create error response
     */
    private Map<String, Object> createError(int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("status", status);
        return error;
    }
}
