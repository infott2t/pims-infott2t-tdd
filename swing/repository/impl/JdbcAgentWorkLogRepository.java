package swing.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import swing.db.DatabaseManager;
import swing.entity.AgentWorkLog;
import swing.repository.AgentWorkLogRepository;

public class JdbcAgentWorkLogRepository implements AgentWorkLogRepository {

    private static final String BASE_SELECT =
            "SELECT w.id, w.agent_id, u.name AS agent_name, w.category, w.detail, w.recorded_at "
                    + "FROM agent_work_logs w "
                    + "LEFT JOIN users u ON w.agent_id = u.id ";

    private final DatabaseManager dbManager;

    public JdbcAgentWorkLogRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public AgentWorkLog save(AgentWorkLog workLog) {
        String sql = "INSERT INTO agent_work_logs (agent_id, category, detail, recorded_at) "
                + "VALUES (?, ?, ?, ?)";
        if (workLog.getRecordedAt() == null) {
            workLog.setRecordedAt(LocalDateTime.now());
        }
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, workLog.getAgentId());
            ps.setString(2, workLog.getCategory());
            ps.setString(3, workLog.getDetail());
            ps.setTimestamp(4, Timestamp.valueOf(workLog.getRecordedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    workLog.setId(keys.getInt(1));
                }
            }
            return workLog;
        } catch (SQLException e) {
            throw new RuntimeException("상담사 업무기록 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AgentWorkLog> findByAgentId(int agentId) {
        String sql = BASE_SELECT + "WHERE w.agent_id = ? ORDER BY w.id DESC";
        List<AgentWorkLog> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("상담사 업무기록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AgentWorkLog> findByAgentIdAndDetail(int agentId, String detail) {
        String sql = BASE_SELECT + "WHERE w.agent_id = ? AND w.detail = ? ORDER BY w.id DESC";
        List<AgentWorkLog> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            ps.setString(2, detail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("상담사 업무기록 필터 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AgentWorkLog> findAll() {
        String sql = BASE_SELECT + "ORDER BY w.id DESC";
        List<AgentWorkLog> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("전체 상담사 업무기록 조회 실패: " + e.getMessage(), e);
        }
    }

    private AgentWorkLog mapRow(ResultSet rs) throws SQLException {
        AgentWorkLog log = new AgentWorkLog();
        log.setId(rs.getInt("id"));
        log.setAgentId(rs.getInt("agent_id"));
        log.setAgentName(rs.getString("agent_name"));
        log.setCategory(rs.getString("category"));
        log.setDetail(rs.getString("detail"));
        Timestamp ts = rs.getTimestamp("recorded_at");
        if (ts != null) {
            log.setRecordedAt(ts.toLocalDateTime());
        }
        return log;
    }
}
