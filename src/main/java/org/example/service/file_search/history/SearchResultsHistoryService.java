package org.example.service.file_search.history;

import org.example.model.search.SearchEvent;
import org.example.repository.history.SearchResultsPersistence;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchResultsHistoryService {

    private final SearchResultsPersistence persistence;

    public SearchResultsHistoryService(SearchResultsPersistence persistence) {
        this.persistence = persistence;
    }

    @EventListener(classes = SearchEvent.class)
    public void handleNewSearch(SearchEvent event) {
        for(String filePath: event.getFilePaths()) {
            persistence.save(filePath, event.getTimestamp());
        }
    }

    public void deleteAllSearchHistory() {
        persistence.deleteAll();
    }

    public List<String> getTopSearchHistory(int nrResults) {
        return persistence.getTopEntries(nrResults);
    }
}
