package hades.authorized.service;

import common.inject.annotations.RegisterFor;
import dobby.util.RouteHelper;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RegisterFor(PermissionCheckService.class)
public class PermissionCheckService {
    private static PermissionCheckService instance;
    private final HashSet<String> permissionCheckRoutes = new HashSet<>();

    public PermissionCheckService() {

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
