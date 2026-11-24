package swing.service;

import java.util.List;

import swing.entity.AgentWorkLog;
import swing.entity.User;

/**
 * 상담사 업무기록(로그인, 로그아웃, 업무중, 자리비움) 서비스 인터페이스
 */
public interface AgentWorkLogServiceIF {

    AgentWorkLog recordLogin(User agent);

    AgentWorkLog recordLogout(User agent);

    AgentWorkLog recordStatusChange(User agent, String status);

    List<AgentWorkLog> findMyWorkLogs(int agentId);

    List<AgentWorkLog> findMyWorkLogsByDetail(int agentId, String detail);
}
