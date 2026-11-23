package jobs;

 

public interface JobsIF {

    jobs.Job jobsCallINIT(String jobName, String description, String status);

    void setStatus(Job job, String string);

    void setDescription(Job job_01, String string);
}
