package hades.user.service;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.user.User;
import thot.connector.IConnector;

import java.util.UUID;

@RegisterFor(TokenLoginService.class)
public class TokenLoginService {
    public static final String BUCKET_NAME = "loginTokens";
    public UserService userService;
    private final IConnector connector;

    @Inject
    public TokenLoginService(UserService userService, IConnector connector) {
        this.userService = userService;
        this.connector = connector;
    }

    public User findByToken(String token) {
        final String userId = connector.read(BUCKET_NAME, token, String.class);
        if (userId == null) {
            return null;
        }

        return userService.find(UUID.fromString(userId));
    }

    public boolean setTokenForUser(User user, String token) {
        return connector.write(BUCKET_NAME, token, user.getKey());
    }

    public String findTokenForUser(User user) {
        final String[] tokens = connector.getKeys(BUCKET_NAME);
        for (String token : tokens) {
            final String userId = connector.read(BUCKET_NAME, token, String.class);
            if (userId.equals(user.getKey())) {
                return token;
            }
        }
        return "";
    }

    public boolean delete(String token) {
        return connector.delete(BUCKET_NAME, token);
    }

    public String generateTokenForUser() {
        return UUID.randomUUID().toString() + UUID.randomUUID() + UUID.randomUUID();
    }
}
