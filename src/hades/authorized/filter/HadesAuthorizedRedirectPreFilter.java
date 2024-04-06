package hades.authorized.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import hades.filter.FilterOrder;
import hades.user.service.UserService;

public class HadesAuthorizedRedirectPreFilter implements Filter {
    @Override
    public String getName() {
        return "authorized-redirect-pre-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.AUTHORIZED_REDIRECT_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        final Session session = httpContext.getSession();

        if (UserService.getInstance().isLoggedIn(session)) {
            return true;
        }

        if (path.equalsIgnoreCase("/hades") || path.equalsIgnoreCase("/hades/") || path.equalsIgnoreCase("/hades/index.html")) {
            final Response response = httpContext.getResponse();

            response.setHeader("location", "/hades/login/");
            response.setCode(ResponseCodes.FOUND);

            return false;
        }
        return true;
    }
}
