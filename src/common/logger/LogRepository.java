package common.logger;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.sql.IDatabaseService;
import common.sql.entity.AbstractRepository;
import common.sql.exceptions.SqlException;

@RegisterFor(LogRepository.class)
public class LogRepository extends AbstractRepository<LogEvent, Long> {
    @Inject
    public LogRepository(IDatabaseService databaseService) throws SqlException {
        super(LogEvent.class, databaseService);
    }
}
