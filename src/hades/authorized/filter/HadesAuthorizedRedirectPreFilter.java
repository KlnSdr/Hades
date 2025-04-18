package hades.authorized.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import dobby.Config;
import hades.filter.FilterOrder;
import hades.user.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class HadesAuthorizedRedirectPreFilter implements Filter {
    private static final ArrayList<String> redirectPaths = new ArrayList<>(Arrays.asList("/hades", "/hades/", "/hades/index.html"));

    public static void addRedirectPath(String path) {
        redirectPaths.add(path);
    }

    public static void removeRedirectPath(String path) {
        redirectPaths.remove(path);
    }

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
        final String path = httpContext.getRequest().getPath().toLowerCase();
        final Session session = httpContext.getSession();

        if (UserService.getInstance().isLoggedIn(session)) {
            return true;
        }

        if (redirectPaths.contains(path)) {
            final Response response = httpContext.getResponse();
            final String context = Config.getInstance().getString("hades.context", "");
            String targetPath = Config.getInstance().getString("hades.unauthorizedRedirectTarget", "/hades/login");

            if (targetPath.equals("/hades/login")) {
                targetPath += "?src=" + Base64.getEncoder().encodeToString((context + path).getBytes());
            }

            response.setHeader("location", context + targetPath);
            response.setCode(ResponseCodes.FOUND);

            return false;
        }
        return true;
    }
}
