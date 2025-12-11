package com.mangareader.dto;

import java.util.List;

/**
 * DTO for the paginated popular manga response
 */
public class PopularMangaResponseDTO {
    private Integer total;
    private Integer limit;
    private Integer offset;
    private List<MangaItemDTO> results;

    public PopularMangaResponseDTO() {
    }

    public PopularMangaResponseDTO(Integer total, Integer limit, Integer offset, List<MangaItemDTO> results) {
        this.total = total;
        this.limit = limit;
        this.offset = offset;
        this.results = results;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
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

    public List<MangaItemDTO> getResults() {
        return results;
    }

    public void setResults(List<MangaItemDTO> results) {
        this.results = results;
    }
}
