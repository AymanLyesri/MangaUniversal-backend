package com.mangareader.dto.mangadex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * DTO for MangaDex API response structure
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MangaDexResponseDTO {
    private String result;
    private String response;
    private List<MangaDexMangaData> data;
    private Integer limit;
    private Integer offset;
    private Integer total;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<MangaDexMangaData> getData() {
        return data;
    }

    public void setData(List<MangaDexMangaData> data) {
        this.data = data;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangaDexMangaData {
        private String id;
        private String type;
        private MangaDexAttributes attributes;
        private List<MangaDexRelationship> relationships;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public MangaDexAttributes getAttributes() {
            return attributes;
        }

        public void setAttributes(MangaDexAttributes attributes) {
            this.attributes = attributes;
        }

        public List<MangaDexRelationship> getRelationships() {
            return relationships;
        }

        public void setRelationships(List<MangaDexRelationship> relationships) {
            this.relationships = relationships;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangaDexAttributes {
        private Map<String, String> title;
        private Map<String, String> description;

        public Map<String, String> getTitle() {
            return title;
        }

        public void setTitle(Map<String, String> title) {
            this.title = title;
        }

        public Map<String, String> getDescription() {
            return description;
        }

        public void setDescription(Map<String, String> description) {
            this.description = description;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangaDexRelationship {
        private String id;
        private String type;
        private CoverArtAttributes attributes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public CoverArtAttributes getAttributes() {
            return attributes;
        }

        public void setAttributes(CoverArtAttributes attributes) {
            this.attributes = attributes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoverArtAttributes {
        private String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
