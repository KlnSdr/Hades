package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import common.logger.Logger;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;

import java.util.Arrays;

import static hades.security.PasswordHasher.hashPassword;

@RegisterFor(SetUserDefinedAdminPassword.class)
public class SetUserDefinedAdminPassword implements Update {
    private static final Logger LOGGER = new Logger(SetUserDefinedAdminPassword.class);

    private final UserService userService;

    @Inject
    public SetUserDefinedAdminPassword(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean run(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Invalid number of arguments");
            return false;
        }

        return runUpdate(args[0]);
    }

    @Override
    public boolean run() {
        final char[] password = System.console().readPassword("Admin password:");
        final char[] confirmPassword = System.console().readPassword("Confirm admin password:");

        if (!Arrays.equals(password, confirmPassword)) {
            LOGGER.error("Passwords do not match");
            return false;
        }

        return runUpdate(new String(password));
    }

    private boolean runUpdate(String password) {
        final User[] adminRead = userService.findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        admin.setPassword(hashPassword(password));

        return userService.update(admin);
    }

    @Override
    public String getName() {
        return "SetUserDefinedAdminPassword";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.SET_ADMIN_PASSWORD.getOrder();
    }
}
