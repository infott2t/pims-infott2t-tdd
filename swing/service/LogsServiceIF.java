package swing.service;

import java.time.LocalDateTime;
import java.util.List;

import swing.entity.Job;
import swing.entity.Log;
import swing.entity.User;

/**
 * 기존 logs.LogsIF 인터페이스를 swing.entity 기반으로 계승·확장한 로그 서비스 인터페이스
 */
public interface LogsServiceIF {

    Log logsChkUser(User agent, Job job, String logName, String logDescription, LocalDateTime startDateTime);

    Log logChkName(User agent, Job job, String logName, String name, LocalDateTime now);

    Log logChkBirthDay(User agent, Job job, String logName, String birthDay, LocalDateTime now);

    Log chkAddress(User agent, Job job, String logName, String address, LocalDateTime now);

    Log logAnotherNumber(User agent, Job job, String logName, String phoneNumber, LocalDateTime now);

    Log logTransferAgent(User agent, Job job, String logName, String targetAgentInfo, LocalDateTime now);

    Log logSimpleAnswer(User agent, Job job, String logName, String answer, LocalDateTime now);

    Log logArbitrarySearch(User agent, String searchKeyword, LocalDateTime now);

    List<Log> findAllLogs();

    List<Log> findLogsByJobId(int jobId);

    List<Log> searchLogs(String keyword);
}
