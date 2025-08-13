package hades.user.filter;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.RequestTypes;
import hades.filter.FilterOrder;
import hades.user.User;
import hades.user.service.UserService;

import java.util.regex.Pattern;

@RegisterFor(ReplaceDisplayNamePreFilter.class)
public class ReplaceDisplayNamePreFilter implements Filter {
    private static final Pattern UUID_PATTER = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private final UserService userService;

    @Inject
    public ReplaceDisplayNamePreFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "replace-display-name";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REPLACE_DISPLAY_NAME_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String route = httpContext.getRequest().getPath();
        final RequestTypes requestType = httpContext.getRequest().getType();

        final String routePrefix;

        if (route.startsWith("/rest/users")) {
            if (requestType != RequestTypes.GET && requestType != RequestTypes.DELETE) {
                return true;
            }
            routePrefix = "/rest/users";
        } else if (route.startsWith("/rest/messages/send")) {
            if (requestType != RequestTypes.POST) {
                return true;
            }
            routePrefix = "/rest/messages/send";
        } else {
            return true;
        }

        final String[] parts = route.split(routePrefix);
        if (parts.length != 2) {
            return true;
        }

        final String id = parts[1].substring(1);
        if (UUID_PATTER.matcher(id).matches()) {
            return true;
        }

        final User[] user = userService.findByName(id);
        if (user.length != 1) {
            return true;
        }

        httpContext.getRequest().setPath(routePrefix + "/" + user[0].getId().toString());
        return true;
    }
}
