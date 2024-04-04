package hades.user.filter;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;

public class BlockStaticFilesPreFilter implements Filter {
    @Override
    public String getName() {
        return "block-static-files-pre-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();

        if (path.equalsIgnoreCase("/user") || path.equalsIgnoreCase("/user/") || path.equalsIgnoreCase("/user/index.html")) {
            httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return false;
        }
        return true;
    }
}
