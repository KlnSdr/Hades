package hades.user.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.RequestTypes;
import hades.filter.FilterOrder;
import hades.user.User;
import hades.user.service.UserService;

import java.util.regex.Pattern;

public class ReplaceDisplayNamePreFilter implements Filter {
    private static final Pattern UUID_PATTER = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
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

        if (!route.startsWith("/rest/users") || (requestType != RequestTypes.GET && requestType != RequestTypes.DELETE)) {
            return true;
        }

        final String[] parts = route.split("/rest/users");
        if (parts.length != 2) {
            return true;
        }

        final String id = parts[1].substring(1);
        if (UUID_PATTER.matcher(id).matches()) {
            return true;
        }

        final User[] user = UserService.getInstance().findByName(id);
        if (user.length != 1) {
            return true;
        }

        httpContext.getRequest().setPath("/rest/users/" + user[0].getId().toString());
        return true;
    }
}
