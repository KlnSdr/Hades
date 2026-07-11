package hades.user.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.IConfig;
import dobby.annotations.Delete;
import dobby.annotations.Get;
import dobby.annotations.Post;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;
import dobby.session.ISession;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.authorized.Group;
import hades.authorized.service.GroupService;
import hades.common.ErrorResponse;
import hades.common.ErrorResponses;
import hades.security.PasswordHasher;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

import java.util.Arrays;
import java.util.UUID;

@RegisterFor(UserResource.class)
public class UserResource {
    private static final Logger LOGGER = new Logger(UserResource.class);
    private static final String ROUTE_PREFIX = "/rest/users";

    private static String normalLoginRedirect = null;
    private static String adminLoginRedirect = null;

    private final UserService userService;
    private final TokenLoginService tokenLoginService;
    private final GroupService groupService;
    private final IConfig config;

    @Inject
    public UserResource(UserService userService, TokenLoginService tokenLoginService, GroupService groupService, IConfig config) {
        this.userService = userService;
        this.tokenLoginService = tokenLoginService;
        this.groupService = groupService;
        this.config = config;
    }

    private String getNormalLoginRedirect() {
        if (normalLoginRedirect == null) {
            normalLoginRedirect = config.getString("hades.login.redirect.success", "/");
        }
        return normalLoginRedirect;
    }

    private String getAdminLoginRedirect() {
        if (adminLoginRedirect == null) {
            adminLoginRedirect = config.getString("hades.login.redirect.successAdmin", getNormalLoginRedirect());
        }
        return adminLoginRedirect;
    }

    @ApiDoc(
            summary = "Create a new user",
            description = "Create a new user with the given display name, mail, and password",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 201, message = "User created successfully", responseBody = CreateUserSuccessResponseDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 409, message = "Display name already taken", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not save user", responseBody = ErrorResponse.class)
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

        if (userService.findByName(createUserDTO.getDisplayName()).length > 0) {
            UserResourceErrorResponses.displayNameAlreadyTaken(context.getResponse(), createUserDTO.getDisplayName());
            return;
        }

        final User user = createUserDTO.toUser();

        final boolean userNameAlreadyTaken = userService.findByName(user.getDisplayName()).length > 0;

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

        final boolean didStore = userService.update(user);

        final Response response = context.getResponse();

        if (!didStore) {
            UserResourceErrorResponses.couldNotSaveUser(response, createUserDTO.getDisplayName());
            return;
        }

        userService.logUserIn(user, context);
        tokenLoginService.setTokenForUser(user, tokenLoginService.generateTokenForUser());

        response.setCode(ResponseCodes.CREATED);
        context.getResponse().setBody(new CreateUserSuccessResponseDTO(GetUserDTO.fromUser(user), config.getString("hades.context", "") + getNormalLoginRedirect()));
    }

    @ApiDoc(
            summary = "Login a user",
            description = "Login a user with the given display name and password",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "User logged in successfully", responseBody = CreateUserSuccessResponseDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "User not found", responseBody = ErrorResponse.class)
    @ApiResponse(code = 409, message = "User is locked", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not verify password", responseBody = ErrorResponse.class)
    @Post(ROUTE_PREFIX + "/login")
    public void doLogin(HttpContext context) {
        final Request request = context.getRequest();
        if (!UserResourceVerifier.verifyLoginRequest(request)) {
            UserResourceErrorResponses.malformedRequest(context.getResponse());
            return;
        }

        final LoginUserDTO loginUserDTO = LoginUserDTO.fromRequest(request);

        final User[] users = userService.findByName(loginUserDTO.getDisplayName());

        if (users.length == 0) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), loginUserDTO.getDisplayName());
            return;
        }

        final User user = users[0];
        boolean isLocked = userService.isLocked(user.getId());

        if (isLocked) {
            UserResourceErrorResponses.userIsLocked(context.getResponse());
            return;
        }

        if (!PasswordHasher.verifyPassword(loginUserDTO.getPassword(), user.getPassword())) {
            userService.incrementLoginAttempts(user.getId());
            UserResourceErrorResponses.wrongPassword(context.getResponse());
            return;
        }

        userService.logUserIn(user, context);
        userService.resetLoginAttempts(user.getId());

        final Group[] groups = groupService.findGroupsByUser(user.getId());
        boolean isAdmin = false;
        for (Group group : groups) {
            if (group.getName().equals("admin")) {
                isAdmin = true;
                break;
            }
        }

        final String appContext = config.getString("hades.context", "");
        final String redirectTo;

        if (isAdmin) {
            redirectTo = appContext + getAdminLoginRedirect();
        } else {
            redirectTo = appContext + getNormalLoginRedirect();
        }

        context.getResponse().setBody(new CreateUserSuccessResponseDTO(GetUserDTO.fromUser(user), redirectTo));
    }

    @ApiDoc(
            summary = "Logout the current user",
            description = "Logout the current user by destroying the session",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "User logged out")
    @Get(ROUTE_PREFIX + "/logout")
    public void doLogout(HttpContext context) {
        final ISession session = context.getSession();
        if (session == null) {
            return;
        }

        session.destroy();
    }

    @ApiDoc(
            summary = "Get a user by id",
            description = "Returns a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetUserDTO.class)
    @ApiResponse(code = 400, message = "The id is not a valid UUID", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
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

        final User user = userService.find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        context.getResponse().setBody(GetUserDTO.fromUser(user));
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Delete a user by id",
            description = "Delete a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 204, message = "The user was deleted")
    @ApiResponse(code = 400, message = "The id is not a valid UUID", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @Delete(ROUTE_PREFIX + "/id/{id}")
    public void deleteUser(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        LOGGER.info("Deleting hades.user with id '" + idString + "'");

        final UUID id = uuidFromString(idString, context);

        if (id == null) {
            return;
        }

        final boolean didDelete = userService.delete(id);

        if (!didDelete) {
            UserResourceErrorResponses.couldNotDelete(context.getResponse(), idString);
            return;
        }

        context.getResponse().setCode(ResponseCodes.NO_CONTENT);
    }

    @ApiDoc(
            summary = "Get the currently logged in user",
            description = "Returns the user that is currently logged in",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetUserDTO.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @Get(ROUTE_PREFIX + "/loginUserInfo")
    public void getLoginUserInfo(HttpContext context) {
        final ISession session = context.getSession();
        if (session == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), "unknown");
            return;
        }

        final String userId = session.get("userId");
        if (userId == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), "unknown");
            return;
        }

        final User user = userService.find(UUID.fromString(userId));
        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), userId);
            return;
        }

        context.getResponse().setBody(GetUserDTO.fromUser(user));
    }

    @PermissionCheck
    @AuthorizedOnly
    @ApiDoc(
            summary = "Get all users",
            description = "Returns a list containing all users",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The users were found and their data is returned", responseBody = GetUsersDTO.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @Get(ROUTE_PREFIX + "/all")
    public void getAllUsers(HttpContext context) {
        final User[] users = userService.findAll();

        context.getResponse().setBody(new GetUsersDTO(Arrays.stream(users).map(GetUserDTO::fromUser).toList()));
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Update the mail of a user",
            description = "Update the mail of a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetUserDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not save user", responseBody = ErrorResponse.class)
    @Put(ROUTE_PREFIX + "/id/{id}/update/mail")
    public void updateMail(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = userService.find(id);

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

        final boolean didUpdate = userService.update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(GetUserDTO.fromUser(user));
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Update the login token of a user",
            description = "Update the login token of a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetTokenResponseDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not save user", responseBody = ErrorResponse.class)
    @Put(ROUTE_PREFIX + "/id/{id}/update/token")
    public void regenerateToken(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = userService.find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final String newToken = tokenLoginService.generateTokenForUser();
        final String oldToken = tokenLoginService.findTokenForUser(user);

        if (!oldToken.isEmpty()) {
            if (!tokenLoginService.delete(oldToken)) {
                ErrorResponses.internalError(context.getResponse(), "Could not delete old token");
                return;
            }
        }

        final boolean didSave = tokenLoginService.setTokenForUser(user, newToken);
        if (!didSave) {
            ErrorResponses.internalError(context.getResponse(), "Could not save new token");
            return;
        }

        context.getResponse().setBody(new GetTokenResponseDTO(newToken));
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Update the name of a user",
            description = "Update the name of a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetUserDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not save user", responseBody = ErrorResponse.class)
    @Put(ROUTE_PREFIX + "/id/{id}/update/name")
    public void updateDisplayName(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = userService.find(id);

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

        if (userService.findByName(name).length > 0) {
            UserResourceErrorResponses.displayNameAlreadyTaken(context.getResponse(), name);
            return;
        }

        user.setDisplayName(name);

        final boolean didUpdate = userService.update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(GetUserDTO.fromUser(user));
    }

    @AuthorizedOnly
    @ApiDoc(
            summary = "Update the password of a user",
            description = "Update the password of a user based on the given id",
            baseUrl = ROUTE_PREFIX
    )
    @ApiResponse(code = 200, message = "The user was found and its data is returned", responseBody = GetUserDTO.class)
    @ApiResponse(code = 400, message = "Malformed request", responseBody = ErrorResponse.class)
    @ApiResponse(code = 403, message = "User does not have permission to access this resource", responseBody = ErrorResponse.class)
    @ApiResponse(code = 404, message = "The user was not found", responseBody = ErrorResponse.class)
    @ApiResponse(code = 500, message = "Could not save user", responseBody = ErrorResponse.class)
    @Put(ROUTE_PREFIX + "/id/{id}/update/password")
    public void updatePassword(HttpContext context) {
        final String idString = context.getRequest().getParam("id");
        final UUID id = uuidFromString(idString, context);

        if (id == null || !idString.equalsIgnoreCase(context.getSession().get("userId"))) {
            return;
        }

        final User user = userService.find(id);

        if (user == null) {
            UserResourceErrorResponses.userNotFound(context.getResponse(), idString);
            return;
        }

        final Request request = context.getRequest();
        final NewJson body = request.getBody();

        final String oldPassword = body.getString("oldPassword");
        final String password = body.getString("password");
        final String passwordRepeat = body.getString("passwordRepeat");

        if (oldPassword == null || !PasswordHasher.verifyPassword(oldPassword, user.getPassword())) {
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

        final boolean didUpdate = userService.update(user);

        if (!didUpdate) {
            UserResourceErrorResponses.couldNotSaveUser(context.getResponse(), user.getDisplayName());
            return;
        }

        context.getResponse().setBody(GetUserDTO.fromUser(user));
    }

    public static UUID uuidFromString(String idString, HttpContext context) {
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            UserResourceErrorResponses.couldNotParseId(context.getResponse(), idString);
            return null;
        }
    }

    private String hashPassword(String password) {
        return PasswordHasher.hashPassword(password);
    }
}
