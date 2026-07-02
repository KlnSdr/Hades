package common.logger;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.sql.exceptions.SqlException;

import java.util.List;

@RegisterFor(LogService.class)
public class LogService {
    private final LogRepository repo;

    @Inject
    public LogService(LogRepository repo) {
        this.repo = repo;
    }

    public void save(LogEvent event) {
        try {
            repo.save(event);
        } catch (SqlException e) {
            System.err.println("Failed to save log event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<LogEvent> getAll() {
        try {
            return repo.findAll().stream().sorted((log1, log2) -> log2.getTimestamp().compareTo(log1.getTimestamp())).toList();
        } catch (SqlException e) {
            return List.of();
        }
    }
}
