package hades.user.service;

import dobby.session.Session;
import dobby.util.Json;
import hades.user.User;
import janus.Janus;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.UUID;

public class UserService {
    public static final String USER_BUCKET = "hades_users";
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
        return Janus.parse(Connector.read(USER_BUCKET, id.toString(), Json.class), User.class);
    }

    public User[] findByName(String displayName) {
        final Json[] result = Connector.readPattern(USER_BUCKET, ".*", Json.class);
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
        final Json[] result = Connector.readPattern(USER_BUCKET, ".*", Json.class);
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
}
