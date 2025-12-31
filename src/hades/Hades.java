package hades;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import common.logger.Logger;
import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.IConfig;
import dobby.files.service.IStaticFileService;
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
import thot.connector.IConnector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RegisterFor(Hades.class)
public class Hades implements DobbyEntryPoint {
    private static final String version = "v2.4-snapshot";
    private static final Logger LOGGER = new Logger(Hades.class);
    private final PermissionService permissionService;
    private final UpdateService updateService;
    private final UserService userService;
    private final MessageService messageService;
    private final SecurityService securityService;
    private final IStaticFileService staticFileService;
    private final HadesAnnotationDiscoverer hadesAnnotationDiscoverer;
    private final ReplaceContextInFilesObserver replaceContextInFilesObserver;
    private final StaticContentDir staticContentDir;
    private final IConfig config;
    private final IConnector connector;

    public Hades(HadesDependencyProvider hadesDependencyProvider) {
        if (hadesDependencyProvider == null) {
            LOGGER.error("HadesDependencyProvider is null. Please ensure it is properly injected.");
            System.exit(1);
        }
        this.permissionService = hadesDependencyProvider.getPermissionService();
        this.updateService = hadesDependencyProvider.getUpdateService();
        this.userService = hadesDependencyProvider.getUserService();
        this.messageService = hadesDependencyProvider.getMessageService();
        this.securityService = hadesDependencyProvider.getSecurityService();
        this.staticFileService = hadesDependencyProvider.getStaticFileService();
        this.hadesAnnotationDiscoverer = hadesDependencyProvider.getHadesAnnotationDiscoverer();
        this.replaceContextInFilesObserver = hadesDependencyProvider.getReplaceContextInFilesObserver();
        this.staticContentDir = hadesDependencyProvider.getStaticContentDir();
        this.config = hadesDependencyProvider.getConfig();
        this.connector = hadesDependencyProvider.getConnector();
    }

    @Inject
    public Hades(PermissionService permissionService,
                 UpdateService updateService,
                 UserService userService,
                 MessageService messageService,
                 SecurityService securityService,
                 IStaticFileService staticFileService,
                 HadesAnnotationDiscoverer hadesAnnotationDiscoverer,
                 ReplaceContextInFilesObserver replaceContextInFilesObserver,
                 StaticContentDir staticContentDir,
                 IConfig config,
                 IConnector connector) {
        this.permissionService = permissionService;
        this.updateService = updateService;
        this.userService = userService;
        this.messageService = messageService;
        this.securityService = securityService;
        this.staticFileService = staticFileService;
        this.hadesAnnotationDiscoverer = hadesAnnotationDiscoverer;
        this.replaceContextInFilesObserver = replaceContextInFilesObserver;
        this.staticContentDir = staticContentDir;
        this.config = config;
        this.connector = connector;
    }

    public static String getVersion() {
        return version;
    }

    public static void main(String[] args) {
        startApplication(Hades.class);
    }

    public static void startApplication(Class<?> clazz) {
        if (clazz == null) {
            LOGGER.error("No class provided to start the application.");
            System.exit(1);
        }
        Dobby.startApplication(clazz);
    }

    private void ensureThotIsRunning() {
        final boolean isThotUp = connector.write("system", "isRunning", true);

        if (!isThotUp) {
            LOGGER.error("Thot is not running. Please start Thot.");
            System.exit(1);
        }
    }

    @Override
    public void preStart() {
        System.out.println("powered by Hades " + version);
        System.out.println();

        if (config.getBoolean("hades.enableEncryption", false)) {
            warmupSecurityService();
        }
        ensureThotIsRunning();
        registerStaticContentRoot();
        addUnAuthorizedRedirectPaths();
        addObservers();

        if (Dobby.getMainClass().isAnnotationPresent(DisablePermissionCheck.class)) {
            permissionService.setEnabled(false);
            LOGGER.info("Permission check is disabled.");
        }

        if (updateService.isInstalled()) {
            LOGGER.info("running updates...");
            if (!updateService.runUpdates()) {
                System.exit(1);
            }
        }

        LOGGER.info("discovering protected routes...");
        hadesAnnotationDiscoverer.discoverRoutes();
    }

    private void warmupSecurityService() {
        try {
            securityService.init();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize SecurityService");
            LOGGER.trace(e);
            System.exit(1);
        }
    }

    private void addUnAuthorizedRedirectPaths() {
        final List<Object> redirectPaths = config.getList("hades.unauthorizedRedirectPaths", List.of());

        for (Object path : redirectPaths) {
            HadesAuthorizedRedirectPreFilter.addRedirectPath(path.toString());
        }
    }

    private void addObservers() {
        LOGGER.info("registering observers...");
        ((StaticFileService)staticFileService).addObserver(replaceContextInFilesObserver);
    }

    private void registerStaticContentRoot() {
        staticContentDir.appendToContentDir(Hades.class, "static");
    }

    @Override
    public void postStart() {
        if (updateService.isInstalled()) {
            sendWelcomeMessage();
        } else {
            LOGGER.error("Hades is not installed. Please run the installer:");
            LOGGER.error("access the installer at: http://localhost:" + config.getInt("dobby.port", 3000) + "/hades/installer");
        }
    }

    private void sendWelcomeMessage() {
        if (config.getBoolean("hades.disableWelcomeMessage", false)) {
            return;
        }

        final User admin = userService.getAdminUser();
        final String content = "Hades " + version + " started on the " + LocalDate.now() + " at " + LocalTime.now();

        if (admin == null) {
            LOGGER.error("Admin user not found. Cannot send startup message.");
            return;
        }

        final Message message = messageService.newSystemMessage(admin.getId(), content);
        messageService.update(message);
    }
}
