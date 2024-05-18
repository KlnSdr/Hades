package hades.user.service;

import hades.user.User;
import thot.connector.Connector;

import java.util.UUID;

public class TokenLoginService {
    public static final String BUCKET_NAME = "loginTokens";

    private static TokenLoginService instance;

    private TokenLoginService() {
    }

    public static TokenLoginService getInstance() {
        if (instance == null) {
            instance = new TokenLoginService();
        }
        return instance;
    }

    public User findByToken(String token) {
        final String userId = Connector.read(BUCKET_NAME, token, String.class);
        if (userId == null) {
            return null;
        }

        return UserService.getInstance().find(UUID.fromString(userId));
    }

    public boolean setTokenForUser(User user, String token) {
        return Connector.write(BUCKET_NAME, token, user.getKey());
    }

    public String findTokenForUser(User user) {
        final String[] tokens = Connector.getKeys(BUCKET_NAME);
        for (String token : tokens) {
            final String userId = Connector.read(BUCKET_NAME, token, String.class);
            if (userId.equals(user.getKey())) {
                return token;
            }
        }
        return "";
    }

    public boolean delete(String token) {
        return Connector.delete(BUCKET_NAME, token);
    }

    public String generateTokenForUser() {
        return UUID.randomUUID().toString() + UUID.randomUUID() + UUID.randomUUID();
    }
}
