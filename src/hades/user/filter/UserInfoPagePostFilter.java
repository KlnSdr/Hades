package hades.user.filter;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.util.TemplateEngine;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.filter.FilterOrder;
import hades.messaging.WebhookConfig;
import hades.messaging.service.WebhookService;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@RegisterFor(UserInfoPagePostFilter.class)
public class UserInfoPagePostFilter implements Filter {
    private static final Pattern USER_PAGE_PATTERN = Pattern.compile("^/user/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(/index.html)?$");
    private final IStaticFileService staticFileService;
    private final UserService userService;
    private final TokenLoginService tokenLoginService;
    private final TemplateEngine templateEngine;
    private final WebhookService webhookService;

    @Inject
    public UserInfoPagePostFilter(IStaticFileService staticFileService, UserService userService, TokenLoginService tokenLoginService, TemplateEngine templateEngine, WebhookService webhookService) {
        this.staticFileService = staticFileService;
        this.userService = userService;
        this.tokenLoginService = tokenLoginService;
        this.templateEngine = templateEngine;
        this.webhookService = webhookService;
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

            final String renderedContent = templateEngine.render(new String(userPage.getContent()), getUserInfoFromId(userId));
            userPage.setContent(renderedContent.getBytes(StandardCharsets.UTF_8));

            httpContext.getResponse().sendFile(userPage);
            httpContext.getResponse().setCode(ResponseCodes.OK);
        }

        return true;
    }

    private NewJson getUserInfoFromId(String userId) {
        final NewJson userInfo = new NewJson();

        final User user = userService.find(UUID.fromString(userId));
        final WebhookConfig webhookConfig = Arrays.stream(webhookService.findByOwner(UUID.fromString(userId))).findFirst().orElse(null);
        if (webhookConfig != null) {
            userInfo.setString("DISCORDWEBHOOK", webhookConfig.getUrl());
        } else {
            userInfo.setString("DISCORDWEBHOOK", "");
        }

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
