package hades.authorized.service;

import dobby.util.RouteHelper;

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

    public String getMatching(String route) {
        route = route.toLowerCase();

        if (authorizedRoutes.contains(route)) {
            return route;
        }

        List<String> patternPaths = authorizedRoutes.stream().filter(p -> p.contains("*")).collect(Collectors.toList());

        for (String p : patternPaths) {
            if (RouteHelper.matches(route, p)) {
                return p;
            }
        }
        return null;
    }
}
