package swing.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import swing.db.DatabaseManager;
import swing.entity.Log;
import swing.repository.LogRepository;

public class JdbcLogRepository implements LogRepository {

    private static final String BASE_SELECT =
            "SELECT l.id, l.job_id, l.agent_id, l.user_id, l.log_name, l.log_description, l.created_at, "
                    + "a.name AS agent_name, u.name AS user_name "
                    + "FROM logs l "
                    + "LEFT JOIN users a ON l.agent_id = a.id "
                    + "LEFT JOIN users u ON l.user_id = u.id ";

    private final DatabaseManager dbManager;

    public JdbcLogRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Log save(Log log) {
        String sql = "INSERT INTO logs (job_id, agent_id, user_id, log_name, log_description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getJobId() != null) {
                ps.setInt(1, log.getJobId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (log.getAgentId() != null) {
                ps.setInt(2, log.getAgentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (log.getUserId() != null) {
                ps.setInt(3, log.getUserId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, log.getLogName());
            ps.setString(5, log.getLogDescription());
            ps.setTimestamp(6, Timestamp.valueOf(log.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    log.setId(keys.getInt(1));
                }
            }
            return log;
        } catch (SQLException e) {
            throw new RuntimeException("로그(Log) 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Log> findAll() {
        String sql = BASE_SELECT + "ORDER BY l.id DESC";
        List<Log> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("로그(Log) 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Log> findByJobId(int jobId) {
        String sql = BASE_SELECT + "WHERE l.job_id = ? ORDER BY l.id ASC";
        List<Log> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("업무별 로그(Log) 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Log> search(String keyword) {
        String trimmed = (keyword == null) ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return findAll();
        }
        String pattern = "%" + trimmed + "%";
        String sql = BASE_SELECT + "WHERE l.log_name LIKE ? OR l.log_description LIKE ? ORDER BY l.id DESC";
        List<Log> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("로그(Log) 검색 실패: " + e.getMessage(), e);
        }
    }

    private Log mapRow(ResultSet rs) throws SQLException {
        Log log = new Log();
        log.setId(rs.getInt("id"));
        int jid = rs.getInt("job_id");
        log.setJobId(rs.wasNull() ? null : jid);
        int aid = rs.getInt("agent_id");
        log.setAgentId(rs.wasNull() ? null : aid);
        int uid = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : uid);
        log.setLogName(rs.getString("log_name"));
        log.setLogDescription(rs.getString("log_description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            log.setCreatedAt(ts.toLocalDateTime());
        }
        log.setAgentName(rs.getString("agent_name"));
        log.setUserName(rs.getString("user_name"));
        return log;
    }
}
