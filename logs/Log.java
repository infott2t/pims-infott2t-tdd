package logs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jobs.Job;
import users.Users;

public class Log {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Users user;        // 회원정보를 조회한 상담사
    private Job job;            
    private String logName;         // 회원 성함 확인, 회원 생년월일 확인, 회원 주소 확인
    private String logDescription;  //회원의 이름, 생년월일, 주소 등의 정보
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public Log(Users user, Job job, String logName, String logDescription, LocalDateTime startDateTime) {
        this.user = user;
        this.job = job;
        this.logName = logName;
        this.logDescription = logDescription;
        this.startDateTime = startDateTime;

        String agentName = (user != null) ? user.getName() : "알 수 없음";
        String timeStr = (startDateTime != null) ? startDateTime.format(FORMATTER) : "-";
        System.out.println("[LOG 기록] 담당자: " + agentName + " | 항목: " + logName + " | 내용: " + logDescription + " | 시각: " + timeStr);
    }

    @Override
    public String toString() {
        String agentName = (user != null) ? user.getName() : "알 수 없음";
        String timeStr = (startDateTime != null) ? startDateTime.format(FORMATTER) : "-";
        return "[LOG] 담당자: " + agentName + " | 항목: " + logName + " | 내용: " + logDescription + " | 시각: " + timeStr;
    }
}
