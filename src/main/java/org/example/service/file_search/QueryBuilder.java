package org.example.service.file_search;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.search.SearchParams;
import org.example.model.search.SearchQuery;
import org.example.repository.persistence.RankInfoPersistence;
import org.example.service.file_search.ranking.CombinedRanking;
import org.example.service.file_search.ranking.RankingStrategy;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**Class for processing a SearchParams object and transforming it into a SearchQuery object*/
@Component
public class QueryBuilder {
    private static final int PREVIEW_WORDS_BEFORE = 15;
    private static final int PREVIEW_WORDS_AFTER = 15;
    private final RankInfoPersistence rankInfoPersistence;

    @Setter
    private RankingStrategy rankingStrategy;

    public QueryBuilder(RankInfoPersistence rankInfoPersistence) {
        rankingStrategy = new CombinedRanking();
        this.rankInfoPersistence = rankInfoPersistence;
    }

    /**this function accomplishes the goal of the class
     * Input: SearchParams object
     * Return: SearchQuery with the parameterized SQL and the parameters
     */
    public SearchQuery buildSearchQuery(SearchParams params) {
        StringBuilder sql = new StringBuilder();
        boolean contentSearchWithQuery = params.isNeedsContent();
        if (contentSearchWithQuery) {
            sql.append("WITH q as (SELECT to_tsquery('simple', ?) AS query)\n")
                    .append("SELECT *, ts_headline('simple', content_info.raw_content, q.query, ")
                    .append("'MaxWords=").append(PREVIEW_WORDS_BEFORE + PREVIEW_WORDS_AFTER + 10)
                    .append(", MinWords=15, MaxFragments=3, FragmentDelimiter=\" ... \"') AS preview_content FROM file_info\n");
        } else {
            sql.append("SELECT * FROM file_info\n");
        }
        if (params.isNeedsMetadata()) {
            sql.append(" JOIN metadata ON file_info.file_id = metadata.file_id\n");
        }
        if (params.isNeedsContent()) {
            sql.append(" JOIN content_info ON file_info.file_id = content_info.file_id\n");
        }
        sql.append(" JOIN rank_info ON file_info.file_id = rank_info.file_id\n");
        if(contentSearchWithQuery){
            sql.append(" CROSS JOIN q\n");
        }

        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        appendContentConditions(params, conditions, parameters);
        appendFileInfoConditions(params, conditions, parameters);
        appendMetadataConditions(params, conditions, parameters);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(conditions.stream().collect(Collectors.joining(" AND ")));
        }

        appendRanking(sql, contentSearchWithQuery);

        //limit the results to 50
        sql.append(" LIMIT 50\n");
        return new SearchQuery(sql.toString(), parameters);
    }

    private void appendFileInfoConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (params.getQueryFileName() != null && !params.getQueryFileName().isEmpty()) {
            List<String> orConditions = new ArrayList<>();
            for(String queryFileName: params.getQueryFileName()) {
                orConditions.add("file_info.file_name LIKE ?");
                parameters.add("%" + queryFileName + "%");
            }
            conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
        }
        if (params.getQueryFileExtension() != null && !params.getQueryFileExtension().isEmpty()) {
            List<String> orConditions = new ArrayList<>();
            for(String queryFileExtension: params.getQueryFileExtension()) {
                orConditions.add("file_info.file_extension LIKE ?");
                parameters.add(queryFileExtension);
            }
            conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
        }
        if (params.getQueryFilePath() != null && !params.getQueryFilePath().isEmpty()) {
            List<String> orConditions = new ArrayList<>();
            for(String queryFilePath: params.getQueryFilePath()) {
                orConditions.add("file_info.parent_directory_path LIKE ?");
                parameters.add("%" + queryFilePath + "%");
            }
            conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
        }
    }

    private void appendMetadataConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (!params.isNeedsMetadata()) {
            return;
        }
        if (params.getQuerySize() != null && !params.getQuerySize().isEmpty()) {
            for(int i = 0; i < params.getQuerySize().size(); i++) {
                List<Long> sizes = params.getQuerySize().get(i);
                List<Character> signs = params.getQuerySizeSigns().get(i);
                List<String> orConditions = new ArrayList<>();
                for(int j = 0; j < sizes.size(); j++) {
                    orConditions.add("metadata.size " + signs.get(j) + " ?");
                    parameters.add(sizes.get(j));
                }
                conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
            }
        }
        if (params.getQueryCreated() != null && !params.getQueryCreated().isEmpty()) {
            for(int i = 0; i < params.getQueryCreated().size(); i++) {
                List<FileTime> created = params.getQueryCreated().get(i);
                List<Character> signs = params.getQueryCreatedSigns().get(i);
                List<String> orConditions = new ArrayList<>();
                for(int j = 0; j < created.size(); j++) {
                    orConditions.add("metadata.creation_time " + signs.get(j) + " ?");
                    parameters.add(toTimestamp(created.get(j)));
                }
                conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
            }
        }
        if (params.getQueryLastModified() != null && !params.getQueryLastModified().isEmpty()) {
            for(int i = 0; i < params.getQueryLastModified().size(); i++) {
                List<FileTime> lastModified = params.getQueryLastModified().get(i);
                List<Character> signs = params.getQueryLastModifiedSigns().get(i);
                List<String> orConditions = new ArrayList<>();
                for(int j = 0; j < lastModified.size(); j++) {
                    orConditions.add("metadata.last_modified_time " + signs.get(j) + " ?");
                    parameters.add(toTimestamp(lastModified.get(j)));
                }
                conditions.add(" (" + orConditions.stream().collect(Collectors.joining(" OR ")) + ") ");
            }
        }
    }

    private void appendContentConditions(SearchParams params, List<String> conditions, List<Object> parameters) {
        if (!params.isNeedsContent()) {
            return;
        }
        if (params.getQueryContent() != null && !params.getQueryContent().isEmpty()) {
            conditions.add("content_info.searchable_content @@ q.query");

            List<String> orConditions = new ArrayList<>();
            for(List<String> orContent: params.getQueryContent()){
                orConditions.add(" (" + orContent.stream().collect(Collectors.joining(" | ")) + ") ");
            }
            parameters.add(orConditions.stream().collect(Collectors.joining(" & ")));
        }
    }

    private void appendRanking(StringBuilder sql, Boolean contentSearchWithQuery){
        sql.append(" ORDER BY ");
        sql.append(rankingStrategy.getOrderByString(contentSearchWithQuery));
        sql.append("\n");
    }

    private static Timestamp toTimestamp(FileTime fileTime) {
        return fileTime == null ? null : new Timestamp(fileTime.toMillis());
    }

}
