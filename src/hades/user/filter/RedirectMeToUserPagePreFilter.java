package hades.user.filter;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.Config;
import hades.filter.FilterOrder;
import hades.user.service.UserService;

@RegisterFor(RedirectMeToUserPagePreFilter.class)
public class RedirectMeToUserPagePreFilter implements Filter {
    private final UserService userService;

    @Inject
    public RedirectMeToUserPagePreFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "redirect-me-to-user-page-pre-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REDIRECT_ME_TO_USER_PAGE_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        if (!path.equalsIgnoreCase("/me")){
            return true;
        }

        if (userService.isLoggedIn(httpContext.getSession())) {
            httpContext.getRequest().setPath("/user/" + httpContext.getSession().get("userId"));
            return true;
        } else {
            httpContext.getResponse().setCode(ResponseCodes.FOUND);
            httpContext.getResponse().setHeader("Location", Config.getInstance().getString("hades.context", "") +
                    "/hades/login/");
            return false;
        }
    }
}
