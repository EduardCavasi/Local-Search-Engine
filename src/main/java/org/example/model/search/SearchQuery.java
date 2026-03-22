package org.example.model.search;

import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Getter
public class SearchQuery {
    private final String sql;
    private final List<Object> parameters;

    public SearchQuery(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters == null ? Collections.emptyList() : Collections.unmodifiableList(parameters);
    }

    public void bindParameters(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            stmt.setObject(i + 1, parameters.get(i));
        }
        //System.out.println(stmt);
    }
}
