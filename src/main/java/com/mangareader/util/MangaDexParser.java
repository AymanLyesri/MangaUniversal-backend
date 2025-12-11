package com.mangareader.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;

@Component
public class MangaDexParser {

    private final ObjectMapper mapper;

    public MangaDexParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Parse MangaDex manga search results
     */
    public ArrayNode parseMangaSearchResults(String jsonResponse) throws Exception {
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode dataArray = root.get("data");

        ArrayNode results = mapper.createArrayNode();

        if (dataArray != null && dataArray.isArray()) {
            for (JsonNode manga : dataArray) {
                ObjectNode simplified = createSimplifiedManga(manga);
                results.add(simplified);
            }
        }

        return results;
    }

    /**
     * Parse a single manga detail response
     */
    public ObjectNode parseMangaDetail(String mangaJson, String coverJson) throws Exception {
        JsonNode mangaRoot = mapper.readTree(mangaJson);
        JsonNode mangaData = mangaRoot.get("data");

        ObjectNode result = createSimplifiedManga(mangaData);

        // If we have cover data, update the cover URL
        if (coverJson != null && !coverJson.isEmpty()) {
            JsonNode coverRoot = mapper.readTree(coverJson);
            JsonNode coverData = coverRoot.get("data");
            if (coverData != null) {
                String mangaId = getTextValue(mangaData, "id");
                String coverFileName = getTextValue(coverData.get("attributes"), "fileName");
                if (coverFileName != null && !coverFileName.isEmpty()) {
                    String coverUrl = String.format("https://uploads.mangadex.org/covers/%s/%s",
                            mangaId, coverFileName);
                    result.put("cover", coverUrl);
                }
            }
        }

        return result;
    }

    /**
     * Create simplified manga object from MangaDex response
     */
    private ObjectNode createSimplifiedManga(JsonNode mangaData) {
        ObjectNode result = mapper.createObjectNode();

        String id = getTextValue(mangaData, "id");
        result.put("id", id);

        JsonNode attributes = mangaData.get("attributes");
        if (attributes != null) {
            // Get English title
            String title = getEnglishTitle(attributes.get("title"));
            result.put("title", title);

            // Get English description
            String description = getEnglishDescription(attributes.get("description"));
            result.put("description", description);

            // Get tags
            ArrayNode tags = getTags(attributes.get("tags"));
            result.set("tags", tags);

            // Get status (ongoing, completed, hiatus, cancelled)
            String status = getTextValue(attributes, "status");
            result.put("status", status != null ? status : "");

            // Get year
            JsonNode year = attributes.get("year");
            if (year != null && !year.isNull()) {
                result.put("year", year.asInt());
            }

            // Get content rating
            String contentRating = getTextValue(attributes, "contentRating");
            result.put("contentRating", contentRating != null ? contentRating : "");

            // Get createdAt and updatedAt
            String createdAt = getTextValue(attributes, "createdAt");
            result.put("createdAt", createdAt != null ? createdAt : "");

            String updatedAt = getTextValue(attributes, "updatedAt");
            result.put("updatedAt", updatedAt != null ? updatedAt : "");
        }

        // Get cover ID and build URL
        String coverId = getCoverId(mangaData);
        if (coverId != null && !coverId.isEmpty()) {
            result.put("cover", ""); // Will be filled by separate cover request
            result.put("coverId", coverId);
        } else {
            result.put("cover", "");
        }

        return result;
    }

    /**
     * Parse chapters feed
     */
    public ArrayNode parseChapters(String jsonResponse) throws Exception {
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode dataArray = root.get("data");

        ArrayNode chapters = mapper.createArrayNode();

        if (dataArray != null && dataArray.isArray()) {
            for (JsonNode chapter : dataArray) {
                ObjectNode chapterObj = mapper.createObjectNode();

                String id = getTextValue(chapter, "id");
                chapterObj.put("id", id);

                JsonNode attributes = chapter.get("attributes");
                if (attributes != null) {
                    String number = getTextValue(attributes, "chapter");
                    String title = getTextValue(attributes, "title");
                    String volume = getTextValue(attributes, "volume");
                    String translatedLanguage = getTextValue(attributes, "translatedLanguage");
                    String publishAt = getTextValue(attributes, "publishAt");
                    String createdAt = getTextValue(attributes, "createdAt");
                    String updatedAt = getTextValue(attributes, "updatedAt");

                    chapterObj.put("chapter", number != null ? number : "");
                    chapterObj.put("title", title != null ? title : "");
                    chapterObj.put("volume", volume != null ? volume : "");
                    chapterObj.put("translatedLanguage", translatedLanguage != null ? translatedLanguage : "");
                    chapterObj.put("publishAt", publishAt != null ? publishAt : "");
                    chapterObj.put("createdAt", createdAt != null ? createdAt : "");
                    chapterObj.put("updatedAt", updatedAt != null ? updatedAt : "");

                    // Get pages count
                    JsonNode pages = attributes.get("pages");
                    if (pages != null && !pages.isNull()) {
                        chapterObj.put("pages", pages.asInt());
                    } else {
                        chapterObj.put("pages", 0);
                    }
                }

                chapters.add(chapterObj);
            }
        }

        return chapters;
    }

    /**
     * Parse chapter pages (at-home server response)
     */
    public ArrayNode parseChapterPages(String jsonResponse) throws Exception {
        JsonNode root = mapper.readTree(jsonResponse);

        String baseUrl = getTextValue(root, "baseUrl");
        JsonNode chapter = root.get("chapter");

        ArrayNode pages = mapper.createArrayNode();

        if (chapter != null && baseUrl != null) {
            String hash = getTextValue(chapter, "hash");
            JsonNode dataArray = chapter.get("data");

            if (dataArray != null && dataArray.isArray() && hash != null) {
                for (JsonNode filename : dataArray) {
                    String pageUrl = String.format("%s/data/%s/%s",
                            baseUrl, hash, filename.asText());
                    pages.add(pageUrl);
                }
            }
        }

        return pages;
    }

    /**
     * Get English title from title object
     */
    private String getEnglishTitle(JsonNode titleNode) {
        if (titleNode == null)
            return "";

        // Try English first
        if (titleNode.has("en")) {
            return titleNode.get("en").asText();
        }

        // Try Japanese romaji
        if (titleNode.has("ja-ro")) {
            return titleNode.get("ja-ro").asText();
        }

        // Fall back to first available
        if (titleNode.fields().hasNext()) {
            return titleNode.fields().next().getValue().asText();
        }

        return "";
    }

    /**
     * Get English description
     */
    private String getEnglishDescription(JsonNode descNode) {
        if (descNode == null)
            return "";

        if (descNode.has("en")) {
            return descNode.get("en").asText();
        }

        // Fall back to first available
        if (descNode.fields().hasNext()) {
            return descNode.fields().next().getValue().asText();
        }

        return "";
    }

    /**
     * Extract tags
     */
    private ArrayNode getTags(JsonNode tagsNode) {
        ArrayNode tags = mapper.createArrayNode();

        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                JsonNode attributes = tag.get("attributes");
                if (attributes != null) {
                    JsonNode name = attributes.get("name");
                    if (name != null && name.has("en")) {
                        tags.add(name.get("en").asText());
                    }
                }
            }
        }

        return tags;
    }

    /**
     * Get cover ID from relationships
     */
    private String getCoverId(JsonNode mangaData) {
        JsonNode relationships = mangaData.get("relationships");

        if (relationships != null && relationships.isArray()) {
            for (JsonNode rel : relationships) {
                String type = getTextValue(rel, "type");
                if ("cover_art".equals(type)) {
                    return getTextValue(rel, "id");
                }
            }
        }

        return null;
    }

    /**
     * Safely get text value from JSON node
     */
    private String getTextValue(JsonNode node, String fieldName) {
        if (node == null)
            return null;
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : null;
    }
}
