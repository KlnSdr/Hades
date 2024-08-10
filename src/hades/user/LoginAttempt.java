package hades.user;

import dobby.util.Config;
import dobby.util.json.NewJson;
import hades.user.service.UserService;
import janus.DataClass;
import janus.annotations.JanusBoolean;
import janus.annotations.JanusInteger;
import janus.annotations.JanusString;
import janus.annotations.JanusUUID;
import thot.annotations.v2.Bucket;

import java.util.UUID;

@Bucket(UserService.LIMIT_LOGIN_BUCKET)
public class LoginAttempt implements DataClass {
    @JanusUUID("userId")
    private UUID userId;
    @JanusInteger("loginAttempts")
    private Integer loginAttempts = 0;

    @JanusBoolean("isLocked")
    private boolean isLocked = false;

    @JanusString("lockedUntil")
    private String lockedUntil;

    public LoginAttempt() {
    }

    public LoginAttempt(UUID userId, Integer loginAttempts) {
        this.userId = userId;
        this.loginAttempts = loginAttempts;
    }

    public void incrementLoginAttempts() {
        loginAttempts++;

        if (loginAttempts >= Config.getInstance().getInt("maxLoginAttempts", 5)) {
            lockForDuration(Config.getInstance().getInt("lockDuration", 300000));
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
