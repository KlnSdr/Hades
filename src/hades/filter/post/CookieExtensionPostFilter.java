package hades.filter.post;

import dobby.cookie.Cookie;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import hades.HadesCookie;
import hades.filter.FilterOrder;

import java.util.HashMap;
import java.util.Set;

public class CookieExtensionPostFilter implements Filter {
    @Override
    public String getName() {
        return "CookieExtensionPostFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.COOKIE_EXTENSION_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final Response response = httpContext.getResponse();
        final HashMap<String, Cookie> cookies = response.getCookies();

        final Set<String> cookieNames = cookies.keySet();
        for (String cookieName : cookieNames) {
            final Cookie cookie = cookies.get(cookieName);
            HadesCookie hadesCookie = (HadesCookie) cookie;
            hadesCookie.setSameSite("None");

            response.setCookie(cookieName, hadesCookie);
        }

        return true;
    }
}
