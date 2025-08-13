package hades.user.filter;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
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

@RegisterFor(UserInfoPagePostFilter.class)
public class UserInfoPagePostFilter implements Filter {
    private static final Pattern USER_PAGE_PATTERN = Pattern.compile("^/user/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(/index.html)?$");
    private final IStaticFileService staticFileService;
    private final UserService userService;
    private final TokenLoginService tokenLoginService;
    private final TemplateEngine templateEngine;

    @Inject
    public UserInfoPagePostFilter(IStaticFileService staticFileService, UserService userService, TokenLoginService tokenLoginService, TemplateEngine templateEngine) {
        this.staticFileService = staticFileService;
        this.userService = userService;
        this.tokenLoginService = tokenLoginService;
        this.templateEngine = templateEngine;
    }

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
            final StaticFile userPage = staticFileService.get("/user/index.html");

            if (userPage == null) {
                return true;
            }

            final String userId = path.substring(6, 42);

            if (!userId.equalsIgnoreCase(httpContext.getSession().get("userId"))) {
                httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);
                return false;
            }

            final StaticFile renderedFile = templateEngine.render(userPage, getUserInfoFromId(userId));

            httpContext.getResponse().sendFile(renderedFile);
            httpContext.getResponse().setCode(ResponseCodes.OK);
        }

        return true;
    }

    private NewJson getUserInfoFromId(String userId) {
        final NewJson userInfo = new NewJson();

        final User user = userService.find(UUID.fromString(userId));

        if (user == null) {
            return new NewJson();
        }

        userInfo.setString("USERID", userId);
        userInfo.setString("USERNAME", user.getDisplayName());
        userInfo.setString("EMAIL", user.getMail());
        userInfo.setString("LOGINTOKEN", tokenLoginService.findTokenForUser(user));

        return userInfo;
    }
}
