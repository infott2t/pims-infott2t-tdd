package swing.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 상담 로그(Log) 엔티티
 * 기존 logs.Log를 기반으로 DB 테이블(logs)과 매핑됩니다.
 */
public class Log {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;
    private int jobId;
    private int agentId;
    private Integer userId; // 비회원인 경우 null
    private String logName;
    private String logDescription;
    private LocalDateTime createdAt;

    // JOIN 조회 시 화면 표시용 보조 필드
    private String agentName;
    private String userName;

    public Log() {
    }

    public Log(int jobId, int agentId, Integer userId, String logName, String logDescription, LocalDateTime createdAt) {
        this.jobId = jobId;
        this.agentId = agentId;
        this.userId = userId;
        this.logName = logName;
        this.logDescription = logDescription;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getLogDescription() {
        return logDescription;
    }

    public void setLogDescription(String logDescription) {
        this.logDescription = logDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "-";
    }

    @Override
    public String toString() {
        String agentLabel = (agentName != null) ? agentName : ("Agent#" + agentId);
        return "[LOG #" + id + "] Job#" + jobId
                + " | 담당자: " + agentLabel
                + " | 항목: " + logName
                + " | 내용: " + logDescription
                + " | 시각: " + getFormattedCreatedAt();
    }
}
