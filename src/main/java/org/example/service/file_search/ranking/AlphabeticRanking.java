package org.example.service.file_search.ranking;

public class AlphabeticRanking implements RankingStrategy{
    @Override
    public String getOrderByString(Boolean contentSearchWithQuery) {
        return "regexp_replace(rank_info.file_path, '^.*/', '') ASC";
    }
}
