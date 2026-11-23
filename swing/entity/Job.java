package swing.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 상담 업무(Job) 엔티티
 * 기존 jobs.Job을 기반으로 DB 테이블(jobs)과 매핑됩니다.
 */
public class Job {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id;
    private String jobName;
    private String description;
    private JobStatus status;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String callerPhoneNumber;
    private Integer userId;        // 비회원인 경우 null
    private int agentId;
    private Integer targetAgentId; // 시나리오 3(다른 상담사 연결) 대상 상담사 ID

    // JOIN 조회 시 화면 표시용 보조 필드
    private String userName;
    private String agentName;
    private String targetAgentName;

    public Job() {
    }

    public Job(String jobName, String description, JobStatus status,
               String callerPhoneNumber, Integer userId, int agentId) {
        this.jobName = jobName;
        this.description = description;
        this.status = status;
        this.callerPhoneNumber = callerPhoneNumber;
        this.userId = userId;
        this.agentId = agentId;
        if (status == JobStatus.INIT) {
            this.startDateTime = LocalDateTime.now();
        } else if (status == JobStatus.COMPLETED) {
            this.endDateTime = LocalDateTime.now();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        if (status == JobStatus.COMPLETED && this.endDateTime == null) {
            this.endDateTime = LocalDateTime.now();
        }
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getCallerPhoneNumber() {
        return callerPhoneNumber;
    }

    public void setCallerPhoneNumber(String callerPhoneNumber) {
        this.callerPhoneNumber = callerPhoneNumber;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public Integer getTargetAgentId() {
        return targetAgentId;
    }

    public void setTargetAgentId(Integer targetAgentId) {
        this.targetAgentId = targetAgentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getTargetAgentName() {
        return targetAgentName;
    }

    public void setTargetAgentName(String targetAgentName) {
        this.targetAgentName = targetAgentName;
    }

    public String getFormattedStartDateTime() {
        return startDateTime != null ? startDateTime.format(FORMATTER) : "-";
    }

    public String getFormattedEndDateTime() {
        return endDateTime != null ? endDateTime.format(FORMATTER) : "-";
    }

    @Override
    public String toString() {
        return "[JOB #" + id + "] 업무명: " + jobName
                + " | 설명: " + (description != null ? description : "없음")
                + " | 상태: " + status
                + " | 시작: " + getFormattedStartDateTime()
                + (endDateTime != null ? " | 종료: " + getFormattedEndDateTime() : "");
    }
}
