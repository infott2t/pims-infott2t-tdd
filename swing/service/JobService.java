package swing.service;

import java.time.LocalDateTime;
import java.util.List;

import swing.entity.Job;
import swing.entity.JobStatus;
import swing.entity.User;
import swing.repository.JobRepository;

/**
 * JobsServiceIF 구현체 — 상담 업무 생성·상태 전이 및 DB 영속화 담당
 */
public class JobService implements JobsServiceIF {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job jobsCallINIT(String jobName, String description, String status,
                            String callerPhoneNumber, User customer, User agent) {
        JobStatus jobStatus = JobStatus.fromString(status);
        Integer userId = (customer != null) ? customer.getId() : null;
        Job job = new Job(jobName, description, jobStatus, callerPhoneNumber, userId, agent.getId());
        job.setStartDateTime(LocalDateTime.now());
        job.setAgentName(agent.getName());
        if (customer != null) {
            job.setUserName(customer.getName());
        }
        return jobRepository.save(job);
    }

    @Override
    public void setStatus(Job job, JobStatus status) {
        job.setStatus(status);
        if (status == JobStatus.COMPLETED) {
            job.setEndDateTime(LocalDateTime.now());
        } else if (status == JobStatus.IN_PROGRESS || status == JobStatus.PENDING) {
            job.setEndDateTime(null);
        }
        jobRepository.update(job);
    }

    @Override
    public void setStatus(Job job, String status) {
        setStatus(job, JobStatus.fromString(status));
    }

    @Override
    public void setDescription(Job job, String description) {
        job.setDescription(description);
        jobRepository.update(job);
    }

    @Override
    public void setTargetAgent(Job job, User targetAgent) {
        if (targetAgent != null) {
            job.setTargetAgentId(targetAgent.getId());
            job.setTargetAgentName(targetAgent.getName());
        } else {
            job.setTargetAgentId(null);
            job.setTargetAgentName(null);
        }
        jobRepository.update(job);
    }

    @Override
    public void attachCustomer(Job job, User customer) {
        if (customer != null) {
            job.setUserId(customer.getId());
            job.setUserName(customer.getName());
            jobRepository.update(job);
        }
    }

    @Override
    public List<Job> findAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public List<Job> findJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }
}
