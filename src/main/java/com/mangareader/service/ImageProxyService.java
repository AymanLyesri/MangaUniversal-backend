package com.mangareader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImageProxyService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    // Cache for At-Home server responses (chapterId -> AtHomeData)
    private final Map<String, CachedAtHomeData> atHomeCache = new ConcurrentHashMap<>();

    // Cache TTL: 3 minutes
    private static final long CACHE_TTL_MS = 3 * 60 * 1000;

    public ImageProxyService(ObjectMapper mapper) {
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Fetch cover image from MangaDex CDN
     */
    public byte[] fetchCoverImage(String coverUrl) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(coverUrl))
                    .header("Referer", "https://mangadex.org/")
                    .header("User-Agent", "Java-Proxy/1.0")
                    .header("Cache-Control", "no-cache")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to fetch cover: HTTP " + response.statusCode());
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Cover fetch interrupted", e);
        }
    }

    /**
     * Fetch image bytes from MangaDex CDN
     */
    public byte[] fetchImage(String chapterId, String filename, boolean useDataSaver) throws IOException {
        // Get At-Home server data (cached)
        AtHomeData atHomeData = getAtHomeData(chapterId);

        if (atHomeData == null) {
            throw new IOException("Failed to fetch At-Home server data for chapter: " + chapterId);
        }

        // Verify filename exists in the data array
        boolean filenameExists = false;
        String[] targetArray = useDataSaver ? atHomeData.dataSaver : atHomeData.data;

        if (targetArray != null) {
            for (String file : targetArray) {
                if (file.equals(filename)) {
                    filenameExists = true;
                    break;
                }
            }
        }

        if (!filenameExists) {
            throw new IOException("Filename not found in chapter data: " + filename);
        }

        // Build image URL
        String quality = useDataSaver ? "data-saver" : "data";
        String imageUrl = String.format("%s/%s/%s/%s",
                atHomeData.baseUrl,
                quality,
                atHomeData.hash,
                filename);

        // Fetch image with proper headers
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .header("Referer", "https://mangadex.org/")
                    .header("User-Agent", "Java-Proxy/1.0")
                    .header("Cache-Control", "no-cache")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to fetch image: HTTP " + response.statusCode());
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Image fetch interrupted", e);
        }
    }

    /**
     * Get At-Home server data with caching
     */
    private AtHomeData getAtHomeData(String chapterId) throws IOException {
        // Check cache
        CachedAtHomeData cached = atHomeCache.get(chapterId);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        // Fetch fresh data
        String atHomeUrl = "https://api.mangadex.org/at-home/server/" + chapterId;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(atHomeUrl))
                    .header("User-Agent", "Java-Proxy/1.0")
                    .header("Cache-Control", "no-cache")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("At-Home server returned HTTP " + response.statusCode());
            }

            // Parse response
            JsonNode root = mapper.readTree(response.body());
            String baseUrl = root.path("baseUrl").asText();
            JsonNode chapter = root.path("chapter");
            String hash = chapter.path("hash").asText();

            JsonNode dataNode = chapter.path("data");
            JsonNode dataSaverNode = chapter.path("dataSaver");

            if (baseUrl.isEmpty() || hash.isEmpty()) {
                throw new IOException("Invalid At-Home response: missing baseUrl or hash");
            }

            String[] data = jsonArrayToStringArray(dataNode);
            String[] dataSaver = jsonArrayToStringArray(dataSaverNode);

            if (data == null || data.length == 0) {
                throw new IOException("No page data found in At-Home response");
            }

            AtHomeData atHomeData = new AtHomeData(baseUrl, hash, data, dataSaver);

            // Cache the result
            atHomeCache.put(chapterId, new CachedAtHomeData(atHomeData));

            // Clean up old cache entries
            cleanCache();

            return atHomeData;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("At-Home fetch interrupted", e);
        }
    }

    /**
     * Convert JSON array to String array
     */
    private String[] jsonArrayToStringArray(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return new String[0];
        }

        String[] result = new String[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            result[i] = arrayNode.get(i).asText();
        }
        return result;
    }

    /**
     * Clean expired cache entries
     */
    private void cleanCache() {
        atHomeCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * At-Home server data
     */
    private static class AtHomeData {
        final String baseUrl;
        final String hash;
        final String[] data;
        final String[] dataSaver;

        AtHomeData(String baseUrl, String hash, String[] data, String[] dataSaver) {
            this.baseUrl = baseUrl;
            this.hash = hash;
            this.data = data;
            this.dataSaver = dataSaver;
        }
    }

    /**
     * Cached At-Home data with expiration
     */
    private static class CachedAtHomeData {
        final AtHomeData data;
        final long timestamp;

        CachedAtHomeData(AtHomeData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}
