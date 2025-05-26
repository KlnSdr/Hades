package hades.authorized.service;

import common.inject.annotations.RegisterFor;
import dobby.util.RouteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RegisterFor(AuthorizedRoutesService.class)
public class AuthorizedRoutesService {
    private final List<String> authorizedRoutes = new ArrayList<>();

    public AuthorizedRoutesService() {

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
