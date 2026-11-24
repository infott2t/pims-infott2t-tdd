package swing.service;

import java.time.LocalDateTime;
import java.util.List;

import swing.entity.AgentWorkLog;
import swing.entity.User;
import swing.repository.AgentWorkLogRepository;

/**
 * AgentWorkLogServiceIF 구현체 — 로그인한 상담사의 로그인/로그아웃/업무중/자리비움 시각을 DB에 기록
 */
public class AgentWorkLogService implements AgentWorkLogServiceIF {

    public static final String CATEGORY_WORK_LOG = "업무기록";
    public static final String DETAIL_LOGIN = "로그인";
    public static final String DETAIL_LOGOUT = "로그아웃";
    public static final String DETAIL_WORKING = "업무중";
    public static final String DETAIL_AWAY = "자리비움";

    private final AgentWorkLogRepository repository;

    public AgentWorkLogService(AgentWorkLogRepository repository) {
        this.repository = repository;
    }

    private AgentWorkLog saveRecord(User agent, String detail) {
        AgentWorkLog record = new AgentWorkLog(
                agent.getId(),
                agent.getName(),
                CATEGORY_WORK_LOG,
                detail,
                LocalDateTime.now()
        );
        return repository.save(record);
    }

    @Override
    public AgentWorkLog recordLogin(User agent) {
        return saveRecord(agent, DETAIL_LOGIN);
    }

    @Override
    public AgentWorkLog recordLogout(User agent) {
        return saveRecord(agent, DETAIL_LOGOUT);
    }

    @Override
    public AgentWorkLog recordStatusChange(User agent, String status) {
        String normalized = normalizeStatusDetail(status);
        return saveRecord(agent, normalized);
    }

    @Override
    public List<AgentWorkLog> findMyWorkLogs(int agentId) {
        return repository.findByAgentId(agentId);
    }

    @Override
    public List<AgentWorkLog> findMyWorkLogsByDetail(int agentId, String detail) {
        if (detail == null || detail.isBlank() || "전체".equals(detail)) {
            return findMyWorkLogs(agentId);
        }
        return repository.findByAgentIdAndDetail(agentId, detail);
    }

    private String normalizeStatusDetail(String status) {
        if (status == null) {
            return DETAIL_WORKING;
        }
        String compact = status.replaceAll("\\s+", "");
        if (compact.contains("자리비움")) {
            return DETAIL_AWAY;
        }
        if (compact.contains("업무중")) {
            return DETAIL_WORKING;
        }
        return status.trim();
    }
}
