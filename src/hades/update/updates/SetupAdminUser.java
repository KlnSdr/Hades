package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.UserService;

import static hades.security.PasswordHasher.hashPassword;

@RegisterFor(SetupAdminUser.class)
public class SetupAdminUser implements Update {
    private final UserService userService;

    @Inject
    public SetupAdminUser(UserService userService) {
        this.userService = userService;
    }

    public boolean run() {
        final String hashedPassword = hashPassword("root");
        final User admin = new User();
        admin.setDisplayName("admin");

        admin.setPassword(hashedPassword);
        admin.setMail("admin@localhost");

        return userService.update(admin);
    }

    public String getName() {
        return "SetupAdminUser";
    }

    public int getOrder() {
        return UpdateOrder.SETUP_ADMIN_USER.getOrder();
    }
}
