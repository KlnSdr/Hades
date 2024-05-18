package hades.filter.post;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import hades.common.Util;
import hades.filter.FilterOrder;

public class TokenLoginPostFilter implements Filter {
    @Override
    public String getName() {
        return "TokenLoginPostFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.TOKEN_LOGIN_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        if (!Util.requestHasHeader(httpContext.getRequest(), "X-Hades-Token-Login-Filter-Ran")) {
            return true;
        }
        httpContext.getSession().destroy();
        return true;
    }
}
