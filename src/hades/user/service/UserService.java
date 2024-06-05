package hades.user.service;

import dobby.io.HttpContext;
import dobby.session.Session;
import dobby.session.service.SessionService;
import dobby.util.json.NewJson;
import hades.user.LoginAttempt;
import hades.user.User;
import janus.Janus;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.UUID;

public class UserService {
    public static final String USER_BUCKET = "hades_users";
    public static final String LIMIT_LOGIN_BUCKET = "hades_limit_login";
    private static UserService instance;

    private UserService() {
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }

        return instance;
    }

    public User find(UUID id) {
        return Janus.parse(Connector.read(USER_BUCKET, id.toString(), NewJson.class), User.class);
    }

    public User[] findByName(String displayName) {
        final NewJson[] result = Connector.readPattern(USER_BUCKET, ".*", NewJson.class);
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
        final NewJson[] result = Connector.readPattern(USER_BUCKET, ".*", NewJson.class);
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
        return Connector.delete(USER_BUCKET, id.toString());
    }

    public boolean update(User user) {
        return Connector.write(USER_BUCKET, user.getKey(), user.toStoreJson());
    }

    public void incrementLoginAttempts(UUID userId) {
        final LoginAttempt loginAttempt = Janus.parse(Connector.read(LIMIT_LOGIN_BUCKET, userId.toString(), NewJson.class),
                LoginAttempt.class);
        if (loginAttempt == null) {
            Connector.write(LIMIT_LOGIN_BUCKET, userId.toString(), new LoginAttempt(userId, 1).toStoreJson());
        } else {
            loginAttempt.incrementLoginAttempts();
            Connector.write(LIMIT_LOGIN_BUCKET, userId.toString(), loginAttempt.toStoreJson());
        }
    }

    public boolean isLocked(UUID userId) {
        final LoginAttempt loginAttempt = Janus.parse(Connector.read(LIMIT_LOGIN_BUCKET, userId.toString(), NewJson.class),
                LoginAttempt.class);
        return loginAttempt != null && loginAttempt.isLocked();
    }

    public void resetLoginAttempts(UUID userId) {
        Connector.delete(LIMIT_LOGIN_BUCKET, userId.toString());
    }

    public boolean isLoggedIn(Session session) {
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
        final Session session = SessionService.getInstance().newSession();
        session.set("userId", user.getId().toString());
        context.setSession(session);
    }

    public User getSystemUser() {
        final User system = new User();
        system.setMail("system@system");
        system.setDisplayName("System");
        return system;
    }
}
