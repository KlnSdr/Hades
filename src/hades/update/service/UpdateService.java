package hades.update.service;

import dobby.util.logging.Logger;
import hades.update.Update;
import hades.update.UpdateDiscoverer;
import hades.update.updates.HadesInstalledUpdate;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateService {
    private static final Logger LOGGER = new Logger(UpdateService.class);
    public static final String BUCKET_NAME = "hades_updates";
    private static UpdateService instance;
    private Boolean isInstalled = null;

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

    private boolean checkIfInstalled() {
        return didUpdateRun(new HadesInstalledUpdate());
    }

    public boolean isInstalled() {
        if (isInstalled == null) {
            isInstalled = checkIfInstalled();
        }
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public boolean runUpdates(String[] argUpdateNames, String[][] args) {
        if (argUpdateNames.length != args.length) {
            LOGGER.error("Invalid number of arguments");
            return false;
        }
        final ArrayList<String> updateNames = new ArrayList<>(Arrays.asList(argUpdateNames));
        final ArrayList<String[]> updateArgs = new ArrayList<>(Arrays.asList(args));


        AtomicInteger updatesRan = new AtomicInteger();
        AtomicBoolean ranSuccessfully = new AtomicBoolean(true);
        updates.forEach(update -> {
            if (!didUpdateRun(update)) {
                LOGGER.info("Running update: " + update.getName());
                boolean updateRanSuccessfully;
                if (updateNames.contains(update.getName())) {
                    updateRanSuccessfully = update.run(updateArgs.get(updateNames.indexOf(update.getName())));
                } else {
                    updateRanSuccessfully = runUpdate(update);
                }
                if (updateRanSuccessfully) {
                    markUpdateRan(update);
                    updatesRan.getAndIncrement();
                    LOGGER.info("done");
                } else {
                    LOGGER.error("Failed to run update: " + update.getName());
                    ranSuccessfully.set(false);
                }
            }
        });
        LOGGER.info("Ran " + updatesRan.get() + " updates.");
        return ranSuccessfully.get();
    }

    public boolean runUpdates() {
        return runUpdates(new String[0], new String[0][0]);
    }

    private boolean runUpdate(Update update) {
        return update.run();
    }

    private void markUpdateRan(Update update) {
        Connector.write(BUCKET_NAME, update.getName(), true);
    }

    private boolean didUpdateRun(Update update) {
        final String updateName = update.getName();

        final Object didRun = Connector.read(BUCKET_NAME, updateName, Object.class);

        return didRun != null;
    }
}
