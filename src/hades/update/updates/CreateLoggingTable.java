package hades.update.updates;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.LogRepository;
import common.logger.Logger;
import common.sql.exceptions.SqlException;
import hades.Hades;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.update.UpdateSkippedException;

@RegisterFor(CreateLoggingTable.class)
public class CreateLoggingTable implements Update {
    private final LogRepository logRepository;

    @Inject
    public CreateLoggingTable(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public boolean run() throws UpdateSkippedException {
        if (!Hades.isSqlDatabaseConfigured()) {
            throw new UpdateSkippedException();
        }
        try {
            logRepository.createTable();
            return true;
        } catch (SqlException e) {
            final Logger LOGGER = new Logger(CreateLoggingTable.class);
            LOGGER.error("Failed to create logging table, skipping update");
            LOGGER.trace(e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "hades_logs_table_update";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_LOGGING_TABLE.getOrder();
    }
}
