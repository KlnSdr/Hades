package hades;

import common.logger.Logger;
import dobby.Config;
import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.files.service.StaticFileService;
import dobby.util.StaticContentDir;
import hades.annotations.DisablePermissionCheck;
import hades.authorized.HadesAnnotationDiscoverer;
import hades.authorized.filter.HadesAuthorizedRedirectPreFilter;
import hades.authorized.service.PermissionService;
import hades.filter.post.ReplaceContextInFilesObserver;
import hades.messaging.Message;
import hades.messaging.service.MessageService;
import hades.security.service.SecurityService;
import hades.update.service.UpdateService;
import hades.user.User;
import hades.user.service.UserService;
import thot.connector.Connector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Hades implements DobbyEntryPoint {
    private static final String version = "v2.3-snapshot";
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

        warmupSecurityService();
        ensureThotIsRunning();
        registerStaticContentRoot();
        addUnAuthorizedRedirectPaths();
        addObservers();

        if (Dobby.getMainClass().isAnnotationPresent(DisablePermissionCheck.class)) {
            PermissionService.getInstance().setEnabled(false);
            LOGGER.info("Permission check is disabled.");
        }

        if (UpdateService.getInstance().isInstalled()) {
            LOGGER.info("running updates...");
            if (!UpdateService.getInstance().runUpdates()) {
                System.exit(1);
            }
        }

        LOGGER.info("discovering protected routes...");
        HadesAnnotationDiscoverer.discoverRoutes("");
    }

    private void warmupSecurityService() {
        try {
            SecurityService.getInstance().init();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize SecurityService");
            LOGGER.trace(e);
            System.exit(1);
        }
    }

    private void addUnAuthorizedRedirectPaths() {
        final List<Object> redirectPaths = Config.getInstance().getList("hades.unauthorizedRedirectPaths", List.of());

        for (Object path : redirectPaths) {
            HadesAuthorizedRedirectPreFilter.addRedirectPath(path.toString());
        }
    }

    private void addObservers() {
        LOGGER.info("registering observers...");
        StaticFileService.getInstance().addObserver(new ReplaceContextInFilesObserver());
    }

    private void registerStaticContentRoot() {
        StaticContentDir.appendToContentDir(Hades.class, "static");
    }

    @Override
    public void postStart() {
        if (UpdateService.getInstance().isInstalled()) {
            sendWelcomeMessage();
        } else {
            LOGGER.error("Hades is not installed. Please run the installer:");
            LOGGER.error("access the installer at: http://localhost:" + Config.getInstance().getInt("dobby.port", 3000) + "/hades/installer");
        }
    }

    private void sendWelcomeMessage() {
        if (Config.getInstance().getBoolean("hades.disableWelcomeMessage", false)) {
            return;
        }

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
