package hades.user.service;

import hades.user.User;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.UUID;

public class UserService {
    private static final String USER_BUCKET = "hades_users";
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
        return Connector.read(USER_BUCKET, id.toString(), User.class);
    }

    public User[] findByName(String displayName) {
        final User[] users = Connector.readPattern(USER_BUCKET, ".*", User.class);
        if (users == null) {
            return new User[0];
        }

        final ArrayList<User> usersWithName = new ArrayList<>();
        for (User user : users) {
            if (user.getDisplayName().equalsIgnoreCase(displayName)) {
                usersWithName.add(user);
            }
        }
        return usersWithName.toArray(new User[0]);
    }

    public boolean delete(UUID id) {
        return Connector.delete(USER_BUCKET, id.toString());
    }

    public boolean update(User user) {
        return Connector.write(USER_BUCKET, user.getId().toString(), user);
    }
}
