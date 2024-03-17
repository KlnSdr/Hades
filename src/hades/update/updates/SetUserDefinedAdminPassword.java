package hades.update.updates;

import dobby.util.logging.Logger;
import hades.update.Update;
import hades.user.User;
import hades.user.service.UserService;

import java.util.Arrays;

import static hades.common.Security.hashPassword;

public class SetUserDefinedAdminPassword implements Update {
    private static final Logger LOGGER = new Logger(SetUserDefinedAdminPassword.class);

    @Override
    public boolean run() {
        final char[] password = System.console().readPassword("Admin password:");
        final char[] confirmPassword = System.console().readPassword("Confirm admin password:");

        if (!Arrays.equals(password, confirmPassword)) {
            LOGGER.error("Passwords do not match");
            return false;
        }

        final User[] adminRead = UserService.getInstance().findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        admin.setPassword(hashPassword(new String(password)));

        return UserService.getInstance().update(admin);
    }

    @Override
    public String getName() {
        return "SetUserDefinedAdminPassword";
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
