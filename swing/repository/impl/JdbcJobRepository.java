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
import java.util.Optional;

import swing.db.DatabaseManager;
import swing.entity.Job;
import swing.entity.JobStatus;
import swing.repository.JobRepository;

public class JdbcJobRepository implements JobRepository {

    private static final String BASE_SELECT =
            "SELECT j.id, j.job_name, j.description, j.status, j.start_date_time, j.end_date_time, "
                    + "j.caller_phone_number, j.user_id, j.agent_id, j.target_agent_id, "
                    + "u.name AS user_name, a.name AS agent_name, ta.name AS target_agent_name "
                    + "FROM jobs j "
                    + "LEFT JOIN users u ON j.user_id = u.id "
                    + "LEFT JOIN users a ON j.agent_id = a.id "
                    + "LEFT JOIN users ta ON j.target_agent_id = ta.id ";

    private final DatabaseManager dbManager;

    public JdbcJobRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Job save(Job job) {
        String sql = "INSERT INTO jobs (job_name, description, status, start_date_time, end_date_time, "
                + "caller_phone_number, user_id, agent_id, target_agent_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (job.getStartDateTime() == null) {
            job.setStartDateTime(LocalDateTime.now());
        }
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, job.getJobName());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(job.getStartDateTime()));
            if (job.getEndDateTime() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(job.getEndDateTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setString(6, job.getCallerPhoneNumber());
            if (job.getUserId() != null) {
                ps.setInt(7, job.getUserId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, job.getAgentId());
            if (job.getTargetAgentId() != null) {
                ps.setInt(9, job.getTargetAgentId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    job.setId(keys.getInt(1));
                }
            }
            return job;
        } catch (SQLException e) {
            throw new RuntimeException("업무(Job) 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Job job) {
        String sql = "UPDATE jobs SET description = ?, status = ?, end_date_time = ?, "
                + "user_id = ?, agent_id = ?, target_agent_id = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getDescription());
            ps.setString(2, job.getStatus().name());
            if (job.getEndDateTime() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(job.getEndDateTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            if (job.getUserId() != null) {
                ps.setInt(4, job.getUserId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, job.getAgentId());
            if (job.getTargetAgentId() != null) {
                ps.setInt(6, job.getTargetAgentId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, job.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("업무(Job) 수정 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Job> findById(int id) {
        String sql = BASE_SELECT + "WHERE j.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("업무(Job) 단건 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Job> findAll() {
        String sql = BASE_SELECT + "ORDER BY j.id DESC";
        List<Job> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("업무(Job) 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Job> findByStatus(JobStatus status) {
        String sql = BASE_SELECT + "WHERE j.status = ? ORDER BY j.id DESC";
        List<Job> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("상태별 업무(Job) 조회 실패: " + e.getMessage(), e);
        }
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setJobName(rs.getString("job_name"));
        job.setDescription(rs.getString("description"));
        job.setStatus(JobStatus.fromString(rs.getString("status")));

        Timestamp startTs = rs.getTimestamp("start_date_time");
        if (startTs != null) {
            job.setStartDateTime(startTs.toLocalDateTime());
        }
        Timestamp endTs = rs.getTimestamp("end_date_time");
        if (endTs != null) {
            job.setEndDateTime(endTs.toLocalDateTime());
        }

        job.setCallerPhoneNumber(rs.getString("caller_phone_number"));

        int uid = rs.getInt("user_id");
        job.setUserId(rs.wasNull() ? null : uid);

        job.setAgentId(rs.getInt("agent_id"));

        int targetId = rs.getInt("target_agent_id");
        job.setTargetAgentId(rs.wasNull() ? null : targetId);

        job.setUserName(rs.getString("user_name"));
        job.setAgentName(rs.getString("agent_name"));
        job.setTargetAgentName(rs.getString("target_agent_name"));
        return job;
    }
}
