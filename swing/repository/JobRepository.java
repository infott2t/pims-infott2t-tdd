package swing.repository;

import java.util.List;
import java.util.Optional;

import swing.entity.Job;
import swing.entity.JobStatus;

public interface JobRepository {

    Job save(Job job);

    void update(Job job);

    Optional<Job> findById(int id);

    List<Job> findAll();

    List<Job> findByStatus(JobStatus status);
}
