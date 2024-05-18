package hades.filter.pre;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.util.Config;
import hades.common.Util;
import hades.filter.FilterOrder;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

public class TokenLoginPreFilter implements Filter {
    @Override
    public String getName() {
        return "TokenLoginPreFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.TOKEN_LOGIN_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String headerName = Config.getInstance().getString("hades.login.tokenHeaderName", "Hades-Auth-Token");

        if (!Util.requestHasHeader(httpContext.getRequest(), headerName)) {
            return true;
        }

        final String loginToken = httpContext.getRequest().getHeader(headerName);

        final User user = TokenLoginService.getInstance().findByToken(loginToken);

        if (user == null) {
            return true;
        }

        UserService.getInstance().logUserIn(user, httpContext);

        httpContext.getRequest().setHeader("X-Hades-Token-Login-Filter-Ran", "");
        return true;
    }
}
