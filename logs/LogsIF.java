package logs;

import java.time.LocalDateTime;

import jobs.Job;
import users.Users;

public interface LogsIF {
    logs.Log logsChkUser(users.Users user, jobs.Job job, String logName, String logDescription, LocalDateTime startDateTime);

    void logChkName(Users agent, Job job_01, String string, String name, LocalDateTime now);

    void logChkBirthDay(Users agent, Job job_01, String string, String birthDay, LocalDateTime now);

    void chkAddress(Users agent, Job job_01, String string, String address, LocalDateTime now);

    void logAnotherNumber(Users agent, Job job_01, String logName, String phoneNumber, LocalDateTime now);

    void logTransferAgent(Users agent, Job job_01, String logName, String targetAgentInfo, LocalDateTime now);

    void logSimpleAnswer(Users agent, Job job_01, String logName, String answer, LocalDateTime now);
}
