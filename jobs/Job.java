package jobs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import users.Users;

public class Job {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private int id;
    private String jobName;     // 전화상담
    private String description;  // 단순상담, 본인확인 상담 완료
    private String status;       // INIT, PENDING, IN_PROGRESS, COMPLETED
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String userPhoneNumber;
    private Users agent;

    public Job(String jobName, String description, String status) {
    
        this.jobName = jobName;
        this.description = description;
        this.status = status;
        
        if ("INIT".equals(status)) {
            this.startDateTime = LocalDateTime.now();
        } else if ("COMPLETED".equals(status)) {
            this.endDateTime = LocalDateTime.now();
        }

        String descText = (description != null) ? description : "없음";
        String startText = (this.startDateTime != null) ? " | 시작시각: " + this.startDateTime.format(FORMATTER) : "";
        System.out.println("[JOB 생성] 업무명: " + this.jobName + " | 설명: " + descText + " | 상태: " + this.status + startText);
    }

    public void setDescription(String string) {
        this.description = string;
        System.out.println("[JOB 설명] " + this.jobName + " -> " + this.description);
    }

    public void setStatus(String string) {
        this.status = string;

        if ("COMPLETED".equals(status)) {
            this.endDateTime = LocalDateTime.now();
            System.out.println("[JOB 상태] " + this.jobName + " -> " + this.status + " (종료시각: " + this.endDateTime.format(FORMATTER) + ")");
        } else {
            System.out.println("[JOB 상태] " + this.jobName + " -> " + this.status);
        }
    }

    @Override
    public String toString() {
        return "[JOB] 업무명: " + jobName + " | 설명: " + description + " | 상태: " + status;
    }
}
