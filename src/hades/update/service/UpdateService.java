package hades.update.service;

import dobby.util.logging.Logger;
import hades.update.Update;
import hades.update.UpdateDiscoverer;
import hades.update.UpdateRan;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateService {
    private static final Logger LOGGER = new Logger(UpdateService.class);
    public static final String BUCKET_NAME = "hades_updates";
    private static UpdateService instance;

    private static final List<Update> updates = new ArrayList<>();

    private UpdateService() {
        UpdateDiscoverer.discoverRoutes("");
        sortUpdates();
    }

    public static UpdateService getInstance() {
        if (instance == null) {
            instance = new UpdateService();
        }
        return instance;
    }

    public static void addUpdate(Update update) {
        updates.add(update);
    }

    private static void sortUpdates() {
        updates.sort(Comparator.comparingInt(Update::getOrder));
    }

    public void runUpdates() {
        AtomicInteger updatesRan = new AtomicInteger();
        updates.forEach(update -> {
            if (!didUpdateRun(update)) {
                LOGGER.info("Running update: " + update.getName());
                if (runUpdate(update)) {
                    markUpdateRan(update);
                    updatesRan.getAndIncrement();
                    LOGGER.info("done");
                } else {
                    LOGGER.error("Failed to run update: " + update.getName());
                    System.exit(1);
                }
            }
        });
        LOGGER.info("Ran " + updatesRan.get() + " updates.");
    }

    private boolean runUpdate(Update update) {
        return update.run();
    }

    private void markUpdateRan(Update update) {
        final UpdateRan updateRan = new UpdateRan(update.getName());
        Connector.write(BUCKET_NAME, updateRan.getKey(), updateRan);
    }

    private boolean didUpdateRun(Update update) {
        final String updateName = update.getName();

        final UpdateRan didRun = Connector.read(BUCKET_NAME, updateName, UpdateRan.class);

        return didRun != null;
    }
}
