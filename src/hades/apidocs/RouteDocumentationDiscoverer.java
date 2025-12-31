package hades.apidocs;

import common.inject.InjectorService;
import common.logger.Logger;
import common.util.Classloader;
import dobby.IConfig;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.request.RequestTypes;
import dobby.util.RouteHelper;
import dobby.util.Tupel;
import hades.annotations.AuthorizedOnly;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.apidocs.annotations.ApiResponses;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteDocumentationDiscoverer extends Classloader<Object> {
    private static final Logger LOGGER = new Logger(RouteDocumentationDiscoverer.class);
    private static final Map<String, List<RouteDocumentation>> routeDocumentationMap = new HashMap<>();

    public RouteDocumentationDiscoverer(String packageName) {
        this.packageName = packageName;
    }

    public static void discoverRoutes() {
        discoverRoutes("");
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

        if (InjectorService.getInstance().getInstance(IConfig.class).getBoolean("hades.apidocs.hideHadesRoutes", false) && rootPackage.startsWith("hades")) {
            return;
        }

        RouteDocumentationDiscoverer discoverer = new RouteDocumentationDiscoverer(rootPackage);
        discoverer.loadClasses().forEach(discoverer::analyzeClassAndMethods);
        String finalRootPackage = rootPackage;
        discoverer.getPackages().forEach(subpackage -> RouteDocumentationDiscoverer.discoverRoutes(finalRootPackage +
                "." + subpackage));
    }

    public static Map<String, List<RouteDocumentation>> getRouteDocumentationMap() {
        return routeDocumentationMap;
    }

    private void analyzeClassAndMethods(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!isValidHttpHandler(method)) {
                continue;
            }

            final RouteDocumentation routeDocumentation = new RouteDocumentation();

            final Tupel<RequestTypes, String> route = getRoute(method);
            if (route == null) {
                LOGGER.error("Route not found for method: " + method.getName());
                continue;
            }

            final Tupel<String, List<String>> pathParams = RouteHelper.extractPathParams(route._2());

            final String processedRoute = pathParams._1();
            final List<String> pathParamKeys = pathParams._2();
            if (processedRoute == null) {
                continue;
            }

            routeDocumentation.setRequestType(route._1());
            routeDocumentation.setParams(pathParamKeys);

            if (method.isAnnotationPresent(ApiDoc.class)) {
                routeDocumentation.setApiDoc(method.getAnnotation(ApiDoc.class));
            }
            if (method.isAnnotationPresent(ApiResponses.class)) {
                routeDocumentation.setApiResponses(method.getAnnotation(ApiResponses.class).value());
            }
            if (method.isAnnotationPresent(ApiResponse.class)) {
                routeDocumentation.setApiResponses(new ApiResponse[]{method.getAnnotation(ApiResponse.class)});
            }
            routeDocumentation.setAuthOnly(method.isAnnotationPresent(AuthorizedOnly.class));

            LOGGER.debug("Added route documentation for: " + processedRoute);
            final List<RouteDocumentation> routeDocs = routeDocumentationMap.getOrDefault(processedRoute, new ArrayList<>());
            routeDocs.add(routeDocumentation);
            routeDocumentationMap.put(processedRoute, routeDocs);
        }
    }

    private Tupel<RequestTypes, String> getRoute(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return new Tupel<>(RequestTypes.GET, method.getAnnotation(Get.class).value());
        } else if (method.isAnnotationPresent(Post.class)) {
            return new Tupel<>(RequestTypes.POST, method.getAnnotation(Post.class).value());
        } else if (method.isAnnotationPresent(Put.class)) {
            return new Tupel<>(RequestTypes.PUT, method.getAnnotation(Put.class).value());
        } else if (method.isAnnotationPresent(Delete.class)) {
            return new Tupel<>(RequestTypes.DELETE, method.getAnnotation(Delete.class).value());
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
