package org.example.service.file_search.ranking;

public class CombinedRanking implements RankingStrategy{

    @Override
    public String getOrderByString(Boolean contentSearchWithQuery) {
        if (!contentSearchWithQuery) {
            return "rank_info.combined_score DESC";
        }
        else{
            return "(rank_info.combined_score * 0.2 + ts_rank_cd(content_info.searchable_content, q.query) * 0.8) DESC";
        }
    }
}
