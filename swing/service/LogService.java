package swing.service;

import java.time.LocalDateTime;
import java.util.List;

import swing.entity.Job;
import swing.entity.Log;
import swing.entity.User;
import swing.repository.LogRepository;

/**
 * LogsServiceIF 구현체 — 상담 과정의 모든 로그를 DB(logs 테이블)에 영속 저장
 */
public class LogService implements LogsServiceIF {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    private Log createAndSave(User agent, Job job, String logName, String logDescription, LocalDateTime now) {
        Log log = new Log(
                job.getId(),
                agent.getId(),
                job.getUserId(),
                logName,
                logDescription,
                now != null ? now : LocalDateTime.now()
        );
        log.setAgentName(agent.getName());
        log.setUserName(job.getUserName());
        return logRepository.save(log);
    }

    @Override
    public Log logsChkUser(User agent, Job job, String logName, String logDescription, LocalDateTime startDateTime) {
        return createAndSave(agent, job, logName, logDescription, startDateTime);
    }

    @Override
    public Log logChkName(User agent, Job job, String logName, String name, LocalDateTime now) {
        return createAndSave(agent, job, logName, name, now);
    }

    @Override
    public Log logChkBirthDay(User agent, Job job, String logName, String birthDay, LocalDateTime now) {
        return createAndSave(agent, job, logName, birthDay, now);
    }

    @Override
    public Log chkAddress(User agent, Job job, String logName, String address, LocalDateTime now) {
        return createAndSave(agent, job, logName, address, now);
    }

    @Override
    public Log logAnotherNumber(User agent, Job job, String logName, String phoneNumber, LocalDateTime now) {
        return createAndSave(agent, job, logName, phoneNumber, now);
    }

    @Override
    public Log logTransferAgent(User agent, Job job, String logName, String targetAgentInfo, LocalDateTime now) {
        return createAndSave(agent, job, logName, targetAgentInfo, now);
    }

    @Override
    public Log logSimpleAnswer(User agent, Job job, String logName, String answer, LocalDateTime now) {
        return createAndSave(agent, job, logName, answer, now);
    }

    @Override
    public List<Log> findAllLogs() {
        return logRepository.findAll();
    }

    @Override
    public List<Log> findLogsByJobId(int jobId) {
        return logRepository.findByJobId(jobId);
    }

    @Override
    public List<Log> searchLogs(String keyword) {
        return logRepository.search(keyword);
    }
}
