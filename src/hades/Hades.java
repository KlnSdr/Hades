package hades;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.util.Config;
import dobby.util.StaticContentDir;
import dobby.util.logging.Logger;
import hades.annotations.DisablePermissionCheck;
import hades.authorized.HadesAnnotationDiscoverer;
import hades.authorized.filter.HadesAuthorizedRedirectPreFilter;
import hades.authorized.service.PermissionService;
import hades.messaging.Message;
import hades.messaging.service.MessageService;
import hades.update.service.UpdateService;
import hades.user.User;
import hades.user.service.UserService;
import thot.connector.Connector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Hades implements DobbyEntryPoint {
    private static final String version = "v0.5";
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
        addUnAuthorizedRedirectPaths();

        if (Dobby.getMainClass().isAnnotationPresent(DisablePermissionCheck.class)) {
            PermissionService.getInstance().setEnabled(false);
            LOGGER.info("Permission check is disabled.");
        }

        LOGGER.info("running updates...");
        UpdateService.getInstance().runUpdates();

        LOGGER.info("discovering protected routes...");
        HadesAnnotationDiscoverer.discoverRoutes("");
    }

    private void addUnAuthorizedRedirectPaths() {
        final List<Object> redirectPaths = Config.getInstance().getList("hades.unauthorizedRedirectPaths", List.of());

        for (Object path : redirectPaths) {
            HadesAuthorizedRedirectPreFilter.addRedirectPath(path.toString());
        }
    }

    private void registerStaticContentRoot() {
        StaticContentDir.appendToContentDir(Hades.class, "static");
    }

    @Override
    public void postStart() {
        sendWelcomeMessage();
    }

    private void sendWelcomeMessage() {
        final MessageService messageService = MessageService.getInstance();
        final User admin = UserService.getInstance().getAdminUser();
        final String content = "Hades " + version + " started on the " + LocalDate.now() + " at " + LocalTime.now();

        if (admin == null) {
            LOGGER.error("Admin user not found. Cannot send startup message.");
            return;
        }

        final Message message = messageService.newSystemMessage(admin.getId(), content);
        messageService.update(message);
    }
}
