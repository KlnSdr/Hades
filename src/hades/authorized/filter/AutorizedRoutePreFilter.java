package hades.authorized.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.RemoveTemporaryFilesPostFilter;
import dobby.io.request.RequestTypes;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.AuthorizedRoutesService;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionCheckService;
import hades.authorized.service.PermissionService;
import hades.filter.FilterOrder;
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
        return FilterOrder.AUTHORIZED_ROUTE_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        final String matchingRoute = AuthorizedRoutesService.getInstance().getMatching(path);

        if (matchingRoute == null) {
            return true;
        }

        if (!UserService.getInstance().isLoggedIn(httpContext.getSession())) {
            httpContext.getResponse().setCode(ResponseCodes.FORBIDDEN);
            new RemoveTemporaryFilesPostFilter().run(httpContext);
            return false;
        }

        final UUID userId;

        try {
            userId = UUID.fromString(httpContext.getSession().get("userId"));
        } catch (Exception e) {
            httpContext.getResponse().setCode(ResponseCodes.BAD_REQUEST);

            final NewJson payload = new NewJson();
            payload.setString("error", "Invalid user id");
            httpContext.getResponse().setBody(payload);

            new RemoveTemporaryFilesPostFilter().run(httpContext);
            return false;
        }

        if (PermissionCheckService.getInstance().getMatching(matchingRoute) == null) {
            return true;
        }


        final RequestTypes requestMethod = httpContext.getRequest().getType();

        final Group[] groups = GroupService.getInstance().findGroupsByUser(userId);
        for (Group group : groups) {
            for (Permission permission : group.getPermissions()) {
                if (permission.getRoute().equalsIgnoreCase(matchingRoute) && permission.hasPermission(requestMethod)) {
                    return true;
                }
            }
        }

        if (!PermissionService.getInstance().hasPermission(userId, matchingRoute, requestMethod)) {
            httpContext.getResponse().setCode(ResponseCodes.FORBIDDEN);
            new RemoveTemporaryFilesPostFilter().run(httpContext);
            return false;
        }

        return true;
    }
}
