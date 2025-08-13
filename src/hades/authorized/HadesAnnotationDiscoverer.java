package hades.authorized;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import common.logger.Logger;
import common.util.Classloader;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.util.RouteHelper;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.authorized.service.AuthorizedRoutesService;
import hades.authorized.service.PermissionCheckService;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@RegisterFor(HadesAnnotationDiscoverer.class)
public class HadesAnnotationDiscoverer extends Classloader<Object> {
    private static final Logger LOGGER = new Logger(HadesAnnotationDiscoverer.class);
    private final PermissionCheckService permissionCheckService;
    private final AuthorizedRoutesService authorizedRoutesService;

    @Inject
    public HadesAnnotationDiscoverer(PermissionCheckService permissionCheckService, AuthorizedRoutesService authorizedRoutesService) {
        this.packageName = "";
        this.permissionCheckService = permissionCheckService;
        this.authorizedRoutesService = authorizedRoutesService;
    }

    private HadesAnnotationDiscoverer(String packageName, PermissionCheckService permissionCheckService, AuthorizedRoutesService authorizedRoutesService) {
        this.packageName = packageName;
        this.permissionCheckService = permissionCheckService;
        this.authorizedRoutesService = authorizedRoutesService;
    }

    /**
     * Discovers routes in a given package
     *
     * @param rootPackage Root package
     */
    public static void discoverRoutes(String rootPackage, PermissionCheckService permissionCheckService, AuthorizedRoutesService authorizedRoutesService) {
        if (rootPackage.startsWith(".")) {
            rootPackage = rootPackage.substring(1);
        }
        HadesAnnotationDiscoverer discoverer = new HadesAnnotationDiscoverer(rootPackage, permissionCheckService, authorizedRoutesService);
        discoverer.loadClasses().forEach(discoverer::analyzeClassAndMethods);
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> HadesAnnotationDiscoverer.discoverRoutes(finalRootPackage +
                "." + subpackage, permissionCheckService, authorizedRoutesService));
    }

    public void discoverRoutes() {
        discoverRoutes("", permissionCheckService, authorizedRoutesService);
    }

    private void analyzeClassAndMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!isValidHttpHandler(method)) {
                continue;
            }

            if (method.isAnnotationPresent(AuthorizedOnly.class)) {
                final String route = getRoute(method);
                if (route == null) {
                    LOGGER.error("Route not found for method: " + method.getName());
                    continue;
                }
                final String processedRoute = RouteHelper.extractPathParams(route)._1();

                if (processedRoute != null) {
                    authorizedRoutesService.addAuthorizedRoute(processedRoute);
                    LOGGER.debug("Added route to authorized only: " + processedRoute);
                }
            }
            if (method.isAnnotationPresent(PermissionCheck.class)) {
                final String route = getRoute(method);
                if (route == null) {
                    LOGGER.error("Route not found for method: " + method.getName());
                    continue;
                }
                final String processedRoute = RouteHelper.extractPathParams(route)._1();

                if (processedRoute != null) {
                    permissionCheckService.addPermissionCheckRoute(processedRoute);
                    LOGGER.debug("Added route to permission check: " + processedRoute);
                }
            }
        }
    }

    private String getRoute(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return method.getAnnotation(Get.class).value();
        } else if (method.isAnnotationPresent(Post.class)) {
            return method.getAnnotation(Post.class).value();
        } else if (method.isAnnotationPresent(Put.class)) {
            return method.getAnnotation(Put.class).value();
        } else if (method.isAnnotationPresent(Delete.class)) {
            return method.getAnnotation(Delete.class).value();
        } else {
            return null;
        }
    }

    private boolean isValidHttpHandler(Method method) {
        Type[] types = method.getParameterTypes();
        return types.length == 1 && types[0].equals(HttpContext.class);
    }

    @Override
    protected Class<?> filterClasses(String s) {
        return defaultClassFilter(s);
    }
}
