package swing.repository;

import java.util.List;

import swing.entity.Log;

public interface LogRepository {

    Log save(Log log);

    List<Log> findAll();

    List<Log> findByJobId(int jobId);

    List<Log> search(String keyword);
}
