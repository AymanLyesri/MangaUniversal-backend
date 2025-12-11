package com.mangareader.dto.mangadex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * DTO for MangaDex statistics API response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MangaDexStatisticsDTO {
    private String result;
    private Map<String, MangaStatistics> statistics;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<String, MangaStatistics> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, MangaStatistics> statistics) {
        this.statistics = statistics;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MangaStatistics {
        private CommentsInfo comments;
        private RatingInfo rating;
        private Integer follows;

        public CommentsInfo getComments() {
            return comments;
        }

        public void setComments(CommentsInfo comments) {
            this.comments = comments;
        }

        public RatingInfo getRating() {
            return rating;
        }

        public void setRating(RatingInfo rating) {
            this.rating = rating;
        }

        public Integer getFollows() {
            return follows;
        }

        public void setFollows(Integer follows) {
            this.follows = follows;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentsInfo {
        private Integer threadId;
        private Integer repliesCount;

        public Integer getThreadId() {
            return threadId;
        }

        public void setThreadId(Integer threadId) {
            this.threadId = threadId;
        }

        public Integer getRepliesCount() {
            return repliesCount;
        }

        public void setRepliesCount(Integer repliesCount) {
            this.repliesCount = repliesCount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RatingInfo {
        private Double average;
        private Integer bayesian;

        public Double getAverage() {
            return average;
        }

        public void setAverage(Double average) {
            this.average = average;
        }

        public Integer getBayesian() {
            return bayesian;
        }

        public void setBayesian(Integer bayesian) {
            this.bayesian = bayesian;
        }
    }
}
