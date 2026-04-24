package org.example.model.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@Getter
public class SearchEvent {
    private String searchRequest;
    private List<String> filePaths;
    private Timestamp timestamp;
}
