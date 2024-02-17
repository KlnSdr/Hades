package hades.authorized.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import hades.authorized.service.AuthorizedRoutesService;
import hades.user.service.UserService;

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

        if (isAuthorizedOnly && !UserService.getInstance().isLoggedIn(httpContext.getSession())) {
            httpContext.getResponse().setCode(ResponseCodes.FORBIDDEN);
            return false;
        }

        return true;
    }
}
