package hades.authorized.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import hades.authorized.service.AuthorizedRoutesService;
import hades.user.service.UserService;

import java.util.UUID;

public class AutorizedRoutePreFilter implements Filter {
    @Override
    public String getName() {
        return "authorized-route-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        final boolean isAuthorizedOnly = AuthorizedRoutesService.getInstance().isAuthorizedOnly(path);

        if (isAuthorizedOnly && !isLoggedIn(httpContext)) {
            httpContext.getResponse().setCode(ResponseCodes.FORBIDDEN);
            return false;
        }

        return true;
    }

    private boolean isLoggedIn(HttpContext context) {
        final Session session = context.getSession();
        final String sessionUserId = session.get("userId");

        if (sessionUserId == null) {
            return false;
        }

        final UUID userId;

        try {
            userId = UUID.fromString(sessionUserId);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return UserService.getInstance().find(userId) != null;
    }
}
