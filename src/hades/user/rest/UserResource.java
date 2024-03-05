package hades.user.rest;

import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.Session;
import dobby.session.service.SessionService;
import dobby.util.Json;
import dobby.util.logging.Logger;
import hades.annotations.AuthorizedOnly;
import hades.common.Security;
import hades.user.User;
import hades.user.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserResource {
    private static final Logger LOGGER = new Logger(UserResource.class);
    private static final String ROUTE_PREFIX = "/rest/users";

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

        logUserIn(user, context);

        response.setCode(ResponseCodes.CREATED);
        response.setBody(user.toJson());
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

        if (!Security.verifyPassword(loginUserDTO.getPassword(), user.getPassword())) {
            UserResourceErrorResponses.wrongPassword(context.getResponse());
            return;
        }

        logUserIn(user, context);

        context.getResponse().setBody(user.toJson());
    }

    private void logUserIn(User user, HttpContext context) {
        final Session session = SessionService.getInstance().newSession();
        session.set("userId", user.getId().toString());
        context.setSession(session);
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

    @AuthorizedOnly
    @Get(ROUTE_PREFIX + "/all")
    public void getAllUsers(HttpContext context) {
        final User[] users = UserService.getInstance().findAll();

        Json response = new Json();
        response.setList("users", List.of(Arrays.stream(users).map(User::toJson).toArray()));

        context.getResponse().setBody(response);
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
