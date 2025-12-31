package hades.user;

import common.inject.InjectorService;
import dobby.IConfig;
import dobby.util.json.NewJson;
import hades.user.service.UserService;
import thot.api.annotations.v2.Bucket;
import thot.janus.DataClass;
import thot.janus.annotations.JanusBoolean;
import thot.janus.annotations.JanusInteger;
import thot.janus.annotations.JanusString;
import thot.janus.annotations.JanusUUID;

import java.util.UUID;

@Bucket(UserService.LIMIT_LOGIN_BUCKET)
public class LoginAttempt implements DataClass {
    private static int MAX_LOGIN_ATTEMPTS;
    private static int LOCK_DURATION;
    @JanusUUID("userId")
    private UUID userId;
    @JanusInteger("loginAttempts")
    private Integer loginAttempts = 0;

    @JanusBoolean("isLocked")
    private boolean isLocked = false;

    @JanusString("lockedUntil")
    private String lockedUntil;

    public LoginAttempt() {
        final IConfig config = InjectorService.getInstance().getInstance(IConfig.class);
        MAX_LOGIN_ATTEMPTS = config.getInt("maxLoginAttempts", 5);
        LOCK_DURATION = config.getInt("lockDuration", 300000);
    }

    public LoginAttempt(UUID userId, Integer loginAttempts) {
        this();
        this.userId = userId;
        this.loginAttempts = loginAttempts;
    }

    public void incrementLoginAttempts() {
        loginAttempts++;

        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockForDuration(LOCK_DURATION);
        }
    }

    public boolean isLocked() {
        if (isLocked) {
            final long lockedUntilLong = Long.parseLong(lockedUntil);
            if (lockedUntilLong < System.currentTimeMillis()) {
                isLocked = false;
                lockedUntil = null;
            }
        }

        return isLocked;
    }

    private void lockForDuration(int duration) {
        isLocked = true;
        lockedUntil = String.valueOf(System.currentTimeMillis() + duration);
    }

    @Override
    public String getKey() {
        return userId.toString();
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("userId", userId.toString());
        json.setInt("loginAttempts", loginAttempts);
        json.setInt("isLocked", isLocked ? 1 : 0);
        json.setString("lockedUntil", lockedUntil);
        return json;
    }

    public NewJson toStoreJson() {
        final NewJson json = new NewJson();
        json.setString("userId", userId.toString());
        json.setInt("loginAttempts", loginAttempts);
        json.setString("isLocked", isLocked ? "true" : "false");
        json.setString("lockedUntil", lockedUntil);
        return json;
    }
}
