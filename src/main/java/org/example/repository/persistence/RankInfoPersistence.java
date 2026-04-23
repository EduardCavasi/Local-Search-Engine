package org.example.repository.persistence;

import org.example.model.file.RankInfo;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Component
public class RankInfoPersistence implements IPersistence<Long, RankInfo> {
    private static final String INSERT_SQL =
            "INSERT INTO rank_info (file_id, file_path, depth_path, length_path, extension_priority, size, combined_score, last_modified_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE rank_info SET file_path = ?, depth_path = ?, length_path = ?, extension_priority = ?, size = ?, combined_score = ?, last_modified_time = ?" +
                    " WHERE file_id = ?";

    @Override
    public Optional<Long> save(Connection conn, Long id, RankInfo rankInfo) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setLong(1, id);
            stmt.setString(2, rankInfo.getFilePath());
            stmt.setInt(3, rankInfo.getDepthPath());
            stmt.setInt(4, rankInfo.getLengthPath());
            stmt.setInt(5, rankInfo.getExtensionPriority());
            stmt.setLong(6, rankInfo.getSize());
            stmt.setDouble(7, rankInfo.getCombinedScore());
            stmt.setTimestamp(8, Timestamp.from(rankInfo.getLastModified().toInstant()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                return Optional.empty();
            }
        }
        return Optional.of(id);
    }

    @Override
    public boolean update(Connection conn, Long id, RankInfo rankInfo) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, rankInfo.getFilePath());
            stmt.setInt(2, rankInfo.getDepthPath());
            stmt.setInt(3, rankInfo.getLengthPath());
            stmt.setInt(4, rankInfo.getExtensionPriority());
            stmt.setLong(5, rankInfo.getSize());
            stmt.setDouble(6, rankInfo.getCombinedScore());
            stmt.setTimestamp(7, Timestamp.from(rankInfo.getLastModified().toInstant()));
            stmt.setLong(8, id);

            int affected = stmt.executeUpdate();
            return affected != 0;
        }
    }
}