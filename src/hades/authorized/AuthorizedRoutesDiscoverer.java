package hades.authorized;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.routes.RouteManager;
import dobby.util.Classloader;
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
                final String processedRoute = getProcessedRoute(route);

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

    /**
     * Workaround to get the actual registered path by opening the private method extractPathParams of RouteManager.
     * <br>
     * TODO rewrite when method is opened or the info is available in other ways
     *
     * @param route The route which should be processed.
     * @return The processed route with replaced path params.
     */
    private String getProcessedRoute(String route) {
        try {
            Method method = RouteManager.class.getDeclaredMethod("extractPathParams", String.class);
            method.setAccessible(true);
            Tupel<String, List<String>> result =
                    (Tupel<String, List<String>>) method.invoke(RouteManager.getInstance(), route);
            return result._1();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("failed to make method 'RouteManager$extractPathParams' accessible");
            LOGGER.trace(e);
            return null;
        }
    }

    @Override
    protected Class<?> filterClasses(String s) {
        return defaultClassFilter(s);
    }
}
