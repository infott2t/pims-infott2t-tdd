package swing.repository;

import java.util.List;

import swing.entity.AgentWorkLog;

public interface AgentWorkLogRepository {

    AgentWorkLog save(AgentWorkLog workLog);

    List<AgentWorkLog> findByAgentId(int agentId);

    List<AgentWorkLog> findByAgentIdAndDetail(int agentId, String detail);

    List<AgentWorkLog> findAll();
}
