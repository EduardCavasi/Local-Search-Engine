package org.example.model.search;

import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Object containing the actual parameterized SQL query and the parameters used by the search engine
 * Constructed from the SearchParams object by the QueryBuilder
 */
@Getter
public class SearchQuery {
    private final String sql; //parameterized SQL query
    private final List<Object> parameters; //parameters

    public SearchQuery(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters == null ? Collections.emptyList() : Collections.unmodifiableList(parameters);
    }

    /**
     * Injects the parameters in an SQL statement building the final usable query
     */
    public void bindParameters(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            stmt.setObject(i + 1, parameters.get(i));
        }
        //System.out.println(stmt);
    }
}
