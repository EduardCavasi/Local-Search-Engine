package org.example.service.file_search.ranking;

public class HistoryRanking implements RankingStrategy{
    @Override
    public String getOrderByString(Boolean contentSearchWithQuery) {
        return "search_result_history.apparitions DESC NULLS LAST";
    }
}
