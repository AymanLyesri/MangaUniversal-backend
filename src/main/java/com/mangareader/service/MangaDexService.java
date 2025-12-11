package com.mangareader.service;

import com.mangareader.dto.MangaItemDTO;
import com.mangareader.dto.PopularMangaResponseDTO;
import com.mangareader.dto.mangadex.MangaDexResponseDTO;
import com.mangareader.dto.mangadex.MangaDexStatisticsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with MangaDex API
 */
@Service
public class MangaDexService {

    private static final Logger logger = LoggerFactory.getLogger(MangaDexService.class);
    private static final String MANGADEX_API_BASE = "https://api.mangadex.org";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    @Value("${app.proxy.base-url:}")
    private String proxyBaseUrl;

    public MangaDexService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Fetch popular manga from MangaDex API
     *
     * @param limit  Number of results (default 20)
     * @param offset Pagination offset (default 0)
     * @param order  Sort order: "asc" or "desc" (default "desc")
     * @param sortBy Sort field (default "followedCount")
     * @return PopularMangaResponseDTO with total, limit, offset, and results
     */
    public PopularMangaResponseDTO getPopularManga(Integer limit, Integer offset, String order, String sortBy) {
        // Build the MangaDex API URL
        String url = buildMangaDexUrl(limit, offset, order, sortBy);

        logger.info("Fetching manga from: {}", url);

        try {
            // Fetch manga list from MangaDex
            MangaDexResponseDTO mangaDexResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(MangaDexResponseDTO.class)
                    .timeout(REQUEST_TIMEOUT)
                    .doOnError(WebClientResponseException.class, ex -> {
                        logger.error("MangaDex API error: status={}, body={}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    })
                    .onErrorResume(Exception.class, ex -> {
                        logger.error("Error fetching manga from MangaDex: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (mangaDexResponse == null || mangaDexResponse.getData() == null) {
                logger.warn("Empty response from MangaDex API for URL: {}", url);
                return new PopularMangaResponseDTO(0, limit, offset, Collections.emptyList());
            }

            logger.info("Received {} manga from MangaDex", mangaDexResponse.getData().size());

            // Extract manga IDs for statistics fetch
            List<String> mangaIds = mangaDexResponse.getData().stream()
                    .map(MangaDexResponseDTO.MangaDexMangaData::getId)
                    .collect(Collectors.toList());

            // Fetch statistics (followers count) for all manga
            Map<String, Integer> statisticsMap = fetchStatistics(mangaIds);

            // Parse manga data into DTOs
            List<MangaItemDTO> results = mangaDexResponse.getData().stream()
                    .map(mangaData -> parseMangaItem(mangaData, statisticsMap))
                    .collect(Collectors.toList());

            // If sorting by followedCount, sort client-side since MangaDex doesn't support
            // it in order[]
            if ("followedCount".equals(sortBy)) {
                results.sort((a, b) -> {
                    Integer followersA = a.getFollowers() != null ? a.getFollowers() : 0;
                    Integer followersB = b.getFollowers() != null ? b.getFollowers() : 0;
                    return "desc".equals(order) ? followersB.compareTo(followersA) : followersA.compareTo(followersB);
                });
            }

            // Build response
            Integer total = mangaDexResponse.getTotal() != null ? mangaDexResponse.getTotal() : results.size();

            return new PopularMangaResponseDTO(total, limit, offset, results);

        } catch (Exception e) {
            logger.error("Unexpected error in getPopularManga: ", e);
            throw new RuntimeException("Failed to fetch popular manga: " + e.getMessage(), e);
        }
    }

    /**
     * Build MangaDex API URL with query parameters
     * Note: MangaDex API doesn't support order[followedCount], so we use
     * order[relevance] or fetch all
     */
    private String buildMangaDexUrl(Integer limit, Integer offset, String order, String sortBy) {
        // MangaDex doesn't support followedCount in order[], so we fetch by
        // relevance/rating and sort client-side
        if ("followedCount".equals(sortBy)) {
            // Fetch without specific order - we'll sort by statistics after
            return String.format(
                    "%s/manga?limit=%d&offset=%d&includes[]=cover_art&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica",
                    MANGADEX_API_BASE, limit, offset);
        }

        // For valid MangaDex order parameters (e.g., latestUploadedChapter, createdAt,
        // updatedAt, etc.)
        return String.format(
                "%s/manga?limit=%d&offset=%d&includes[]=cover_art&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&order[%s]=%s",
                MANGADEX_API_BASE, limit, offset, sortBy, order);
    }

    /**
     * Fetch statistics (followers) for multiple manga IDs
     */
    private Map<String, Integer> fetchStatistics(List<String> mangaIds) {
        if (mangaIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // Build statistics URL with manga IDs
            String statsUrl = MANGADEX_API_BASE + "/statistics/manga?manga[]="
                    + String.join("&manga[]=", mangaIds);

            MangaDexStatisticsDTO statsResponse = webClient.get()
                    .uri(statsUrl)
                    .retrieve()
                    .bodyToMono(MangaDexStatisticsDTO.class)
                    .timeout(REQUEST_TIMEOUT)
                    .onErrorResume(Exception.class, ex -> {
                        logger.warn("Failed to fetch statistics: {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (statsResponse != null && statsResponse.getStatistics() != null) {
                Map<String, Integer> result = new HashMap<>();
                statsResponse.getStatistics().forEach((mangaId, stats) -> {
                    if (stats != null && stats.getFollows() != null) {
                        result.put(mangaId, stats.getFollows());
                    }
                });
                return result;
            }

        } catch (Exception e) {
            logger.warn("Error fetching manga statistics: {}", e.getMessage());
        }

        return Collections.emptyMap();
    }

    /**
     * Parse a MangaDex manga data object into a simplified DTO
     */
    private MangaItemDTO parseMangaItem(MangaDexResponseDTO.MangaDexMangaData mangaData,
            Map<String, Integer> statisticsMap) {
        String id = mangaData.getId();
        String title = extractTitle(mangaData.getAttributes());
        String description = extractDescription(mangaData.getAttributes());
        Integer followers = statisticsMap.getOrDefault(id, null);
        String coverUrl = extractCoverUrl(mangaData, id);

        return new MangaItemDTO(id, title, description, followers, coverUrl);
    }

    /**
     * Extract title from manga attributes (prefer English)
     */
    private String extractTitle(MangaDexResponseDTO.MangaDexAttributes attributes) {
        if (attributes == null || attributes.getTitle() == null) {
            return "Unknown Title";
        }

        Map<String, String> titleMap = attributes.getTitle();

        // Try English first, then any available language
        if (titleMap.containsKey("en")) {
            return titleMap.get("en");
        }

        return titleMap.values().stream()
                .findFirst()
                .orElse("Unknown Title");
    }

    /**
     * Extract description from manga attributes (prefer English)
     */
    private String extractDescription(MangaDexResponseDTO.MangaDexAttributes attributes) {
        if (attributes == null || attributes.getDescription() == null) {
            return null;
        }

        Map<String, String> descMap = attributes.getDescription();

        // Try English first, then any available language
        if (descMap.containsKey("en")) {
            return descMap.get("en");
        }

        return descMap.values().stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Extract cover URL from manga relationships
     */
    private String extractCoverUrl(MangaDexResponseDTO.MangaDexMangaData mangaData, String mangaId) {
        if (mangaData.getRelationships() == null) {
            return null;
        }

        // Find cover_art relationship
        Optional<MangaDexResponseDTO.MangaDexRelationship> coverRelation = mangaData.getRelationships().stream()
                .filter(rel -> "cover_art".equals(rel.getType()))
                .findFirst();

        if (coverRelation.isPresent() && coverRelation.get().getAttributes() != null) {
            String fileName = coverRelation.get().getAttributes().getFileName();

            if (fileName != null && !fileName.isEmpty()) {
                // If proxy base URL is configured, use proxy URL
                if (proxyBaseUrl != null && !proxyBaseUrl.isEmpty()) {
                    return String.format("%s/proxy/mangadex/cover/%s/%s", proxyBaseUrl, mangaId, fileName);
                } else {
                    // Otherwise return direct MangaDex cover URL
                    return String.format("https://uploads.mangadex.org/covers/%s/%s", mangaId, fileName);
                }
            }
        }

        return null;
    }
}
