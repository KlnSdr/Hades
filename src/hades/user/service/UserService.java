package hades.user.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.io.HttpContext;
import dobby.session.ISession;
import dobby.session.service.ISessionService;
import dobby.util.json.NewJson;
import hades.user.LoginAttempt;
import hades.user.User;
import thot.connector.IConnector;
import thot.janus.Janus;

import java.util.ArrayList;
import java.util.UUID;

@RegisterFor(UserService.class)
public class UserService {
    public static final String USER_BUCKET = "hades_users";
    public static final String LIMIT_LOGIN_BUCKET = "hades_limit_login";
    private final ISessionService sessionService;
    private final IConnector connector;

    @Inject
    public UserService(ISessionService sessionService, IConnector connector) {
        this.sessionService = sessionService;
        this.connector = connector;
    }

    public User find(UUID id) {
        if (id.toString().equals("00000000-0000-0000-0000-000000000000")) {
            return getSystemUser();
        }
        return Janus.parse(connector.read(USER_BUCKET, id.toString(), NewJson.class), User.class);
    }

    public User[] findByName(String displayName) {
        final NewJson[] result = connector.readPattern(USER_BUCKET, ".*", NewJson.class);
        if (result == null) {
            return new User[0];
        }

        final User[] users = new User[result.length];
        for (int i = 0; i < result.length; i++) {
            users[i] = Janus.parse(result[i], User.class);
        }

        final ArrayList<User> usersWithName = new ArrayList<>();
        for (User user : users) {
            if (user.getDisplayName().equalsIgnoreCase(displayName)) {
                usersWithName.add(user);
            }
        }
        return usersWithName.toArray(new User[0]);
    }

    public User[] findAll() {
        final NewJson[] result = connector.readPattern(USER_BUCKET, ".*", NewJson.class);
        if (result == null) {
            return new User[0];
        }

        final User[] users = new User[result.length];
        for (int i = 0; i < result.length; i++) {
            users[i] = Janus.parse(result[i], User.class);
        }

        return users;
    }

    public boolean delete(UUID id) {
        return connector.delete(USER_BUCKET, id.toString());
    }

    public boolean update(User user) {
        return connector.write(USER_BUCKET, user.getKey(), user.toStoreJson());
    }

    public void incrementLoginAttempts(UUID userId) {
        final LoginAttempt loginAttempt = Janus.parse(connector.read(LIMIT_LOGIN_BUCKET, userId.toString(), NewJson.class),
                LoginAttempt.class);
        if (loginAttempt == null) {
            connector.write(LIMIT_LOGIN_BUCKET, userId.toString(), new LoginAttempt(userId, 1).toStoreJson());
        } else {
            loginAttempt.incrementLoginAttempts();
            connector.write(LIMIT_LOGIN_BUCKET, userId.toString(), loginAttempt.toStoreJson());
        }
    }

    public boolean isLocked(UUID userId) {
        final LoginAttempt loginAttempt = Janus.parse(connector.read(LIMIT_LOGIN_BUCKET, userId.toString(), NewJson.class),
                LoginAttempt.class);
        return loginAttempt != null && loginAttempt.isLocked();
    }

    public void resetLoginAttempts(UUID userId) {
        connector.delete(LIMIT_LOGIN_BUCKET, userId.toString());
    }

    public boolean isLoggedIn(ISession session) {
        final String sessionUserId = session.get("userId");

        if (sessionUserId == null) {
            return false;
        }

        final UUID userId;

        try {
            userId = UUID.fromString(sessionUserId);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return find(userId) != null;
    }

    public void logUserIn(User user, HttpContext context) {
        final ISession session = sessionService.newSession();
        session.set("userId", user.getId().toString());
        context.setSession(session);
    }

    public User getAdminUser() {
        final User[] admins = findByName("admin");

        if (admins.length == 0) {
            return null;
        }
        return admins[0];
    }

    public User getSystemUser() {
        final NewJson systemUserJson = new NewJson();
        systemUserJson.setString("id", "00000000-0000-0000-0000-000000000000");
        systemUserJson.setString("displayName", "System");
        systemUserJson.setString("mail", "system@system");
        systemUserJson.setString("password", "");

        return Janus.parse(systemUserJson, User.class);
    }
}
