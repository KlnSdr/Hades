package hades;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.util.StaticContentDir;
import dobby.util.logging.Logger;
import hades.annotations.DisablePermissionCheck;
import hades.authorized.HadesAnnotationDiscoverer;
import hades.authorized.service.PermissionService;
import hades.update.service.UpdateService;
import thot.connector.Connector;

public class Hades implements DobbyEntryPoint {
    private static final String version = "v0.0.3";
    private static final Logger LOGGER = new Logger(Hades.class);

    public static String getVersion() {
        return version;
    }

    public static void main(String[] args) {
        new Hades().startApplication(Hades.class);
    }

    public void startApplication(Class<?> clazz) {
        if (clazz == null) {
            LOGGER.error("No class provided to start the application.");
            System.exit(1);
        }
        Dobby.startApplication(clazz);
    }

    private void ensureThotIsRunning() {
        final boolean isThotUp = Connector.write("system", "isRunning", true);

        if (!isThotUp) {
            LOGGER.error("Thot is not running. Please start Thot.");
            System.exit(1);
        }
    }

    @Override
    public void preStart() {
        System.out.println("powered by Hades " + version);
        System.out.println();

        ensureThotIsRunning();
        registerStaticContentRoot();

        if (Dobby.getMainClass().isAnnotationPresent(DisablePermissionCheck.class)) {
            PermissionService.getInstance().setEnabled(false);
            LOGGER.info("Permission check is disabled.");
        }

        LOGGER.info("running updates...");
        UpdateService.getInstance().runUpdates();

        LOGGER.info("discovering protected routes...");
        HadesAnnotationDiscoverer.discoverRoutes("");
    }

    private void registerStaticContentRoot() {
        StaticContentDir.appendToContentDir(Hades.class, "static");
    }

    @Override
    public void postStart() {

    }
}
