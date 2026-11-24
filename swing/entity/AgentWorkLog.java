package swing.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 상담사 업무기록 엔티티 (DB 테이블: agent_work_logs)
 * 로그인, 로그아웃, 업무중, 자리비움 시각을 기록합니다.
 */
public class AgentWorkLog {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;
    private int agentId;
    private String agentName; // JOIN 조회용 (users.name)
    private String category;  // 항목: "업무기록"
    private String detail;    // 상세 내용: "로그인", "로그아웃", "업무중", "자리비움"
    private LocalDateTime recordedAt;

    public AgentWorkLog() {
    }

    public AgentWorkLog(int agentId, String agentName, String category, String detail, LocalDateTime recordedAt) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.category = category;
        this.detail = detail;
        this.recordedAt = (recordedAt != null) ? recordedAt : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getFormattedRecordedAt() {
        return recordedAt != null ? recordedAt.format(FORMATTER) : "-";
    }

    @Override
    public String toString() {
        String nameLabel = (agentName != null) ? agentName : ("Agent#" + agentId);
        return "[업무기록 #" + id + "] 담당상담사: " + nameLabel
                + " | 항목: " + category
                + " | 상세 내용: " + detail
                + " | 기록시각: " + getFormattedRecordedAt();
    }
}
