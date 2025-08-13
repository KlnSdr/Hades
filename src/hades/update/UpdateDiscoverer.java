package hades.update;

import common.inject.InjectorService;
import common.logger.Logger;
import common.util.Classloader;
import hades.update.service.UpdateService;

import java.lang.reflect.InvocationTargetException;

public class UpdateDiscoverer extends Classloader<Update> {
    private static final Logger LOGGER = new Logger(UpdateDiscoverer.class);
    private static final InjectorService injectorService = InjectorService.getInstance();

    private UpdateDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Discovers routes in a given package
     *
     * @param rootPackage Root package
     */
    public static void discoverRoutes(String rootPackage) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        UpdateDiscoverer discoverer = new UpdateDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(update -> {
            try {
                final Update updateInstance = injectorService.getInstanceNullable(update);
                if (updateInstance != null) {
                    UpdateService.addUpdate(updateInstance);
                    return;
                }
                UpdateService.addUpdate(update.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                LOGGER.error("Failed to add update: " + update.getName());
                LOGGER.trace(e);
            }
        });
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> UpdateDiscoverer.discoverRoutes(finalRootPackage + "." + subpackage));
    }

    @Override
    protected Class<? extends Update> filterClasses(String s) {
        return defaultImplementsFilter(s, Update.class);
    }
}
