package hades.user.filter;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.filter.FilterOrder;
import hades.template.TemplateEngine;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

import java.util.UUID;
import java.util.regex.Pattern;

public class UserInfoPagePostFilter implements Filter {
    private static final Pattern USER_PAGE_PATTERN = Pattern.compile("^/user/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(/index.html)?$");

    @Override
    public String getName() {
        return "user-info-page-pre-filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.USER_INFO_PAGE_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();

        if (USER_PAGE_PATTERN.matcher(path).matches()) {
            final StaticFile userPage = StaticFileService.getInstance().get("/user/index.html");

            if (userPage == null) {
                return true;
            }

            final String userId = path.substring(6, 42);

            if (!userId.equalsIgnoreCase(httpContext.getSession().get("userId"))) {
                httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);
                return false;
            }

            final StaticFile renderedFile = TemplateEngine.render(userPage, getUserInfoFromId(userId));

            httpContext.getResponse().sendFile(renderedFile);
            httpContext.getResponse().setCode(ResponseCodes.OK);
        }

        return true;
    }

    private NewJson getUserInfoFromId(String userId) {
        final NewJson userInfo = new NewJson();

        final User user = UserService.getInstance().find(UUID.fromString(userId));

        if (user == null) {
            return new NewJson();
        }

        userInfo.setString("USERID", userId);
        userInfo.setString("USERNAME", user.getDisplayName());
        userInfo.setString("EMAIL", user.getMail());
        userInfo.setString("LOGINTOKEN", TokenLoginService.getInstance().findTokenForUser(user));

        return userInfo;
    }
}
