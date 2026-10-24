package log;

 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import job.Job;
import users.Users;

public class Log {
    
    private Users user;
    private Job job;
    private String phoneData;
    private String message;
    private LocalDateTime date;

    public Log(Users user2, Job job1, String string, String string2, LocalDate today, LocalTime nowTime) {
        this.user = user2;
        this.job = job1;
        this.phoneData = string;
        this.message = string2;
        this.date = LocalDateTime.of(today, nowTime);
    }
}
