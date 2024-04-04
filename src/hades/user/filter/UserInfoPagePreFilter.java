package hades.user.filter;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;

import java.util.regex.Pattern;

public class UserInfoPagePreFilter implements Filter {
    private static final Pattern USER_PAGE_PATTERN = Pattern.compile("^/user/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(/index.html)?$");

    @Override
    public String getName() {
        return "user-info-page-pre-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();

        if (USER_PAGE_PATTERN.matcher(path).matches()) {
            final StaticFile userPage = StaticFileService.getInstance().get("/user/index.html");

            if (userPage == null) {
                return true;
            }

            httpContext.getResponse().setHeader("Content-Type", userPage.getContentType());
            httpContext.getResponse().setBody(userPage.getContent());
            return false;
        }

        return true;
    }
}
