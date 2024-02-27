package hades.authorized;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.routes.RouteManager;
import dobby.util.Classloader;
import dobby.util.RouteHelper;
import dobby.util.Tupel;
import dobby.util.logging.Logger;
import hades.annotations.AuthorizedOnly;
import hades.authorized.service.AuthorizedRoutesService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class AuthorizedRoutesDiscoverer extends Classloader<Object> {
    private static final Logger LOGGER = new Logger(AuthorizedRoutesDiscoverer.class);

    private AuthorizedRoutesDiscoverer(String packageName) {
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
        AuthorizedRoutesDiscoverer discoverer = new AuthorizedRoutesDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::analyzeClassAndMethods);
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> AuthorizedRoutesDiscoverer.discoverRoutes(finalRootPackage +
                "." + subpackage));
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
                    AuthorizedRoutesService.getInstance().addAuthorizedRoute(processedRoute);
                    LOGGER.debug("Added route to authorized only: " + processedRoute);
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
