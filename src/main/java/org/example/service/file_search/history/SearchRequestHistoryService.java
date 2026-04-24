package org.example.service.file_search.history;

import org.example.model.search.SearchEvent;
import org.example.repository.history.SearchRequestPersistence;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchRequestHistoryService {

    private final SearchRequestPersistence persistence;

    public SearchRequestHistoryService(SearchRequestPersistence persistence) {
        this.persistence = persistence;
    }

    @EventListener(classes = SearchEvent.class)
    public void handleNewSearch(SearchEvent event) {
        persistence.save(event.getSearchRequest(), event.getTimestamp());
    }

    public void deleteAllSearchHistory() {
        persistence.deleteAll();
    }

    public Map<String, Long> getTopSearchHistory(int nrResults) {
        return persistence.getTopEntries(nrResults);
    }

    public List<String> getTopSearchSuggestions(int nrResults, String currentInput){
        return persistence.getTopSuggestions(nrResults, currentInput);
    }
}
