package hades.authorized.filter;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
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

@RegisterFor(AutorizedRoutePreFilter.class)
public class AutorizedRoutePreFilter implements Filter {
    private final AuthorizedRoutesService authorizedRoutesService;
    private final UserService userService;
    private final PermissionCheckService permissionCheckService;
    private final GroupService groupService;
    private final PermissionService permissionService;

    @Inject
    public AutorizedRoutePreFilter(AuthorizedRoutesService authorizedRoutesService, UserService userService, PermissionCheckService permissionCheckService, GroupService groupService, PermissionService permissionService) {
        this.authorizedRoutesService = authorizedRoutesService;
        this.userService = userService;
        this.permissionCheckService = permissionCheckService;
        this.groupService = groupService;
        this.permissionService = permissionService;
    }

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
        final String matchingRoute = authorizedRoutesService.getMatching(path);

        if (matchingRoute == null) {
            return true;
        }

        if (!userService.isLoggedIn(httpContext.getSession())) {
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

        if (permissionCheckService.getMatching(matchingRoute) == null) {
            return true;
        }


        final RequestTypes requestMethod = httpContext.getRequest().getType();

        final Group[] groups = groupService.findGroupsByUser(userId);
        for (Group group : groups) {
            for (Permission permission : group.getPermissions()) {
                if (permission.getRoute().equalsIgnoreCase(matchingRoute) && permission.hasPermission(requestMethod)) {
                    return true;
                }
            }
        }

        if (!permissionService.hasPermission(userId, matchingRoute, requestMethod)) {
            httpContext.getResponse().setCode(ResponseCodes.FORBIDDEN);
            new RemoveTemporaryFilesPostFilter().run(httpContext);
            return false;
        }

        return true;
    }
}
