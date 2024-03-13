package hades.update.updates;

import hades.update.Update;
import hades.user.User;
import hades.user.service.UserService;

import static hades.common.Security.hashPassword;

public class SetupAdminUser implements Update {
    public boolean run() {
        final String hashedPassword = hashPassword("root");
        final User admin = new User();
        admin.setDisplayName("admin");

        admin.setPassword(hashedPassword);
        admin.setMail("admin@localhost");

        return UserService.getInstance().update(admin);
    }

    public String getName() {
        return "SetupAdminUser";
    }

    public int getOrder() {
        return 0;
    }
}
