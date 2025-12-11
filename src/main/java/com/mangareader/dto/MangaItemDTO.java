package com.mangareader.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing a single manga item with essential information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MangaItemDTO {
    private String id;
    private String title;
    private String description;
    private Integer followers;
    private String coverUrl;

    public MangaItemDTO() {
    }

    public MangaItemDTO(String id, String title, String description, Integer followers, String coverUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.followers = followers;
        this.coverUrl = coverUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFollowers() {
        return followers;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
