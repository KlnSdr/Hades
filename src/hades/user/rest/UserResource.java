package hades.user.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import dobby.util.Config;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.authorized.Group;
import hades.authorized.service.GroupService;
import hades.common.ErrorResponses;
import hades.common.Security;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserResource {
    private static final Logger LOGGER = new Logger(UserResource.class);
    private static final String ROUTE_PREFIX = "/rest/users";

    private static String normalLoginRedirect = null;
    private static String adminLoginRedirect = null;

    private static String getNormalLoginRedirect() {
        if (normalLoginRedirect == null) {
            normalLoginRedirect = Config.getInstance().getString("hades.login.redirect.success", "/");
        }
        return normalLoginRedirect;
    }

    private static String getAdminLoginRedirect() {
        if (adminLoginRedirect == null) {
            adminLoginRedirect = Config.getInstance().getString("hades.login.redirect.successAdmin", getNormalLoginRedirect());
        }
        return adminLoginRedirect;
    }

    @Post(ROUTE_PREFIX)
    public void createUser(HttpContext context) {
        final Request request = context.getRequest();
        if (!UserResourceVerifier.verifyCreateUserRequest(request)) {
            UserResourceErrorResponses.malformedRequest(context.getResponse());
            return;
        }

        final CreateUserDTO createUserDTO = CreateUserDTO.fromRequest(request);

        if (!createUserDTO.getPassword().equals(createUserDTO.getPasswordRepeat())) {
            UserResourceErrorResponses.passwordsDoNotMatch(context.getResponse());
            return;
        }

        if (UserService.getInstance().findByName(createUserDTO.getDisplayName()).length > 0) {
            UserResourceErrorResponses.displayNameAlreadyTaken(context.getResponse(), createUserDTO.getDisplayName());
            return;
        }

        final User user = createUserDTO.toUser();

        final boolean userNameAlreadyTaken = UserService.getInstance().findByName(user.getDisplayName()).length > 0;

        if (userNameAlreadyTaken) {
            UserResourceErrorResponses.displayNameAlreadyTaken(context.getResponse(), user.getDisplayName());
            return;
        }

        final String hashedPassword = hashPassword(createUserDTO.getPassword());
        if (hashedPassword == null) {
            UserResourceErrorResponses.couldNotHashPassword(context.getResponse());
            return;
        }
        user.setPassword(hashedPassword);

        final boolean didStore = UserService.getInstance().update(user);

        final Response response = context.getResponse();

        if (!didStore) {
            UserResourceErrorResponses.couldNotSaveUser(response, createUserDTO.getDisplayName());
            return;
        }

        UserService.getInstance().logUserIn(user, context);
        TokenLoginService.getInstance().setTokenForUser(user, TokenLoginService.getInstance().generateTokenForUser());

        response.setCode(ResponseCodes.CREATED);
        final NewJson resPayload = new NewJson();
        resPayload.setJson("user", user.toJson());
        resPayload.setString("redirectTo",
                Config.getInstance().getString("hades.context", "") + getNormalLoginRedirect());

        context.getResponse().setBody(resPayload);
    }

    @Post(ROUTE_PREFIX + "/login")
    public void doLogin(HttpContext context) {
        final Request request = context.getRequest();
        if (!UserResourceVerifier.verifyLoginRequest(request)) {
            UserResourceErrorResponses.malformedRequest(context.getResponse());
            return;
        }

        final LoginUserDTO loginUserDTO = LoginUserDTO.fromRequest(request);

        final User[] users = UserService.getInstance().findByName(loginUserDTO.getDisplayName());

        if (users.length == 0) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), loginUserDTO.getDisplayName());
            return;
        }

        final User user = users[0];
        boolean isLocked = UserService.getInstance().isLocked(user.getId());

        if (isLocked) {
            UserResourceErrorResponses.userIsLocked(context.getResponse());
            return;
        }

        if (!Security.verifyPassword(loginUserDTO.getPassword(), user.getPassword())) {
            UserService.getInstance().incrementLoginAttempts(user.getId());
            UserResourceErrorResponses.wrongPassword(context.getResponse());
            return;
        }

        UserService.getInstance().logUserIn(user, context);
        UserService.getInstance().resetLoginAttempts(user.getId());

        final NewJson resPayload = new NewJson();
        resPayload.setJson("user", user.toJson());

        final Group[] groups = GroupService.getInstance().findGroupsByUser(user.getId());
        boolean isAdmin = false;
        for (Group group : groups) {
            if (group.getName().equals("admin")) {
                isAdmin = true;
                break;
            }
        }

        final String appContext = Config.getInstance().getString("hades.context", "");

        if (isAdmin) {
            resPayload.setString("redirectTo", appContext + getAdminLoginRedirect());
        } else {
            resPayload.setString("redirectTo", appContext + getNormalLoginRedirect());
        }

        context.getResponse().setBody(resPayload);
    }

    @AuthorizedOnly
    @Get(ROUTE_PREFIX + "/logout")
    public void doLogout(HttpContext context) {
        final Session session = context.getSession();
        if (session == null) {
            return;
        }

        session.destroy();
    }

    @PermissionCheck
    @AuthorizedOnly
    @Get(ROUTE_PREFIX + "/id/{id}")
    public void getUserById(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        LOGGER.info("Getting hades.user with id '" + idString + "'");

        final UUID id = uuidFromString(idString, context);

        if (id == null) {
            return;
        }

        final User user = UserService.getInstance().find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        context.getResponse().setBody(user.toJson());
    }

    @PermissionCheck
    @AuthorizedOnly
    @Delete(ROUTE_PREFIX + "/id/{id}")
    public void deleteUser(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        LOGGER.info("Deleting hades.user with id '" + idString + "'");

        final UUID id = uuidFromString(idString, context);

        if (id == null) {
            return;
        }

        final boolean didDelete = UserService.getInstance().delete(id);

        if (!didDelete) {
            UserResourceErrorResponses.couldNotDelete(context.getResponse(), idString);
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    @Get(ROUTE_PREFIX + "/loginUserInfo")
    public void getLoginUserInfo(HttpContext context) {
        final Session session = context.getSession();
        if (session == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), "unknown");
            return;
        }

        final String userId = session.get("userId");
        if (userId == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), "unknown");
            return;
        }

        final User user = UserService.getInstance().find(UUID.fromString(userId));
        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), userId);
            return;
        }

        context.getResponse().setBody(user.toJson());
    }

    @PermissionCheck
    @AuthorizedOnly
    @Get(ROUTE_PREFIX + "/all")
    public void getAllUsers(HttpContext context) {
        final User[] users = UserService.getInstance().findAll();

        final NewJson response = new NewJson();
        response.setList("users", List.of(Arrays.stream(users).map(User::toJson).toArray()));

        context.getResponse().setBody(response);
    }

    @AuthorizedOnly
    @Put(ROUTE_PREFIX + "/id/{id}/update/mail")
    public void updateMail(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = UserService.getInstance().find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final Request request = context.getRequest();
        final NewJson body = request.getBody();

        final String mail = body.getString("mail");

        if (mail == null) {
            UserResourceErrorResponses.malformedRequest(context.getResponse());
            return;
        }

        user.setMail(mail);

        final boolean didUpdate = UserService.getInstance().update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(user.toJson());
    }

    @AuthorizedOnly
    @Put(ROUTE_PREFIX + "/id/{id}/update/token")
    public void regenerateToken(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = UserService.getInstance().find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final String newToken = TokenLoginService.getInstance().generateTokenForUser();
        final String oldToken = TokenLoginService.getInstance().findTokenForUser(user);

        if (!oldToken.isEmpty()) {
            if (!TokenLoginService.getInstance().delete(oldToken)) {
                ErrorResponses.internalError(context.getResponse(), "Could not delete old token");
                return;
            }
        }

        final boolean didSave = TokenLoginService.getInstance().setTokenForUser(user, newToken);
        if (!didSave) {
            ErrorResponses.internalError(context.getResponse(), "Could not save new token");
            return;
        }

        final NewJson response = new NewJson();
        response.setString("token", newToken);
        context.getResponse().setBody(response);
    }

    @AuthorizedOnly
    @Put(ROUTE_PREFIX + "/id/{id}/update/name")
    public void updateDisplayName(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = UserService.getInstance().find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final Request request = context.getRequest();
        final NewJson body = request.getBody();

        final String name = body.getString("displayName");

        if (name == null) {
            UserResourceErrorResponses.malformedRequest(context.getResponse());
            return;
        }

        if (UserService.getInstance().findByName(name).length > 0) {
            UserResourceErrorResponses.displayNameAlreadyTaken(context.getResponse(), name);
            return;
        }

        user.setDisplayName(name);

        final boolean didUpdate = UserService.getInstance().update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(user.toJson());
    }

    @AuthorizedOnly
    @Put(ROUTE_PREFIX + "/id/{id}/update/password")
    public void updatePassword(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = UserService.getInstance().find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final Request request = context.getRequest();
        final NewJson body = request.getBody();

        final String oldPassword = body.getString("oldPassword");
        final String password = body.getString("password");
        final String passwordRepeat = body.getString("passwordRepeat");

        if (oldPassword == null || !Security.verifyPassword(oldPassword, user.getPassword())) {
            UserResourceErrorResponses.wrongPassword(context.getResponse());
            return;
        }

        if (password == null || !password.equals(passwordRepeat)) {
            UserResourceErrorResponses.passwordsDoNotMatch(context.getResponse());
            return;
        }

        final String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            UserResourceErrorResponses.couldNotHashPassword(context.getResponse());
            return;
        }

        user.setPassword(hashedPassword);

        final boolean didUpdate = UserService.getInstance().update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(user.toJson());
    }

    private UUID uuidFromString(String idString, HttpContext context) {
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            UserResourceErrorResponses.couldNotParseId(context.getResponse(), idString);
            return null;
        }
    }

    private String hashPassword(String password) {
        return Security.hashPassword(password);
    }
}
