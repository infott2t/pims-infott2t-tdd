package swing.service;

import java.util.List;

import swing.entity.Job;
import swing.entity.JobStatus;
import swing.entity.User;

/**
 * 기존 jobs.JobsIF 인터페이스를 swing.entity 기반으로 계승·확장한 업무 서비스 인터페이스
 */
public interface JobsServiceIF {

    Job jobsCallINIT(String jobName, String description, String status,
                     String callerPhoneNumber, User customer, User agent);

    void setStatus(Job job, JobStatus status);

    void setStatus(Job job, String status);

    void setDescription(Job job, String description);

    void setTargetAgent(Job job, User targetAgent);

    void attachCustomer(Job job, User customer);

    List<Job> findAllJobs();

    List<Job> findJobsByStatus(JobStatus status);
}
