package hades.authorized.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AuthorizedRoutesService {
    private static AuthorizedRoutesService instance;
    private final List<String> authorizedRoutes = new ArrayList<>();

    private AuthorizedRoutesService() {

    }

    public static AuthorizedRoutesService getInstance() {
        if (instance == null) {
            instance = new AuthorizedRoutesService();
        }

        return instance;
    }

    public void addAuthorizedRoute(String route) {
        authorizedRoutes.add(route);
    }

    public boolean isAuthorizedOnly(String route) {
        if (authorizedRoutes.contains(route)) {
            return true;
        }
        return containsMatching(route);
    }

    /**
     * Copied from dobby.RouteManager
     * <br>
     * TODO rewrite after update
     */
    private boolean containsMatching(String route) {
        List<String> patternPaths = authorizedRoutes.stream().filter(p -> p.contains("*")).collect(Collectors.toList());

        for (String p : patternPaths) {
            final Pattern pattern = prepareRoutePattern(p);
            if (pattern.matcher(route).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copied from dobby.RouteManager
     * <br>
     * TODO rewrite after update
     */
    private Pattern prepareRoutePattern(String path) {
        path = path.replace("*", "[^/]*");
        return Pattern.compile(path);
    }
}
