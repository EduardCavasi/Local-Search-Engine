package org.example.service.file_search.ranking;

public class LastModifiedRanking implements RankingStrategy{
    @Override
    public String getOrderByString(Boolean contentSearchWithQuery) {
            return "rank_info.last_modified_time DESC";
    }
}
