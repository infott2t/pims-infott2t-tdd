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

    public Log(Users agent, Job job1, String phoneData) {
        this.user = agent;
        this.job = job1;
        this.phoneData = phoneData;
        this.date = LocalDateTime.now();
    }

    public Log(Users agent, Job job1, String phoneData, String message) {
        this.user = agent;
        this.job = job1;
        this.phoneData = phoneData;
        this.message = message;
        this.date = LocalDateTime.now();
    }

    public Log(users.Users agent, job.Job job1, Object phoneNumber) {
        //TODO Auto-generated constructor stub
    }

    public Log(Users user2, Job job1, String string, String string2, LocalDate today, LocalTime nowTime) {
        //TODO Auto-generated constructor stub
    }
}
