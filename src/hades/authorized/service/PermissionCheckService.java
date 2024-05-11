package hades.authorized.service;

import dobby.util.RouteHelper;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionCheckService {
    private static PermissionCheckService instance;
    private final HashSet<String> permissionCheckRoutes = new HashSet<>();

    private PermissionCheckService() {

    }

    public static PermissionCheckService getInstance() {
        if (instance == null) {
            instance = new PermissionCheckService();
        }

        return instance;
    }

    public void addPermissionCheckRoute(String route) {
        permissionCheckRoutes.add(route);
    }

    public String getMatching(String route) {
        route = route.toLowerCase();

        if (permissionCheckRoutes.contains(route)) {
            return route;
        }

        List<String> patternPaths =
                permissionCheckRoutes.stream().filter(p -> p.contains("*")).collect(Collectors.toList());

        for (String p : patternPaths) {
            if (RouteHelper.matches(route, p)) {
                return p;
            }
        }
        return null;
    }

    public List<Object> getPermissionCheckRoutes() {
        return List.of(permissionCheckRoutes.toArray());
    }
}
