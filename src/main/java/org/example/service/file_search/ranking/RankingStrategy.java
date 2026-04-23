package org.example.service.file_search.ranking;

public interface RankingStrategy {
    String getOrderByString(Boolean contentSearchWithQuery);
}
