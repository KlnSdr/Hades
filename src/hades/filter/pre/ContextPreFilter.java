package hades.filter.pre;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.Config;
import hades.filter.FilterOrder;

public class ContextPreFilter implements Filter {
    @Override
    public String getName() {
        return "ContextPreFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.CONTEXT_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        String appContext = Config.getInstance().getString("hades.context", null);

        if (appContext == null) {
            return true;
        }
        appContext = appContext.toLowerCase();

        if (!path.toLowerCase().startsWith(appContext)) {
            httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return false;
        }

        final String newPath = path.substring(appContext.length());
        if (newPath.isEmpty()) {
            httpContext.getResponse().setHeader("Location", appContext + "/index.html");
            httpContext.getResponse().setCode(ResponseCodes.FOUND);
            return false;
        }

        httpContext.getRequest().setPath(newPath);
        return true;
    }
}
