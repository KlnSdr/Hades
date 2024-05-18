package hades.update.updates;

import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

public class AddAdminLoginToken implements Update {
    @Override
    public boolean run() {
        final User[] adminRead = UserService.getInstance().findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        return TokenLoginService.getInstance().setTokenForUser(admin, TokenLoginService.getInstance().generateTokenForUser());
    }

    @Override
    public String getName() {
        return "addAdminLoginToken";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.ADD_ADMIN_LOGIN_TOKEN.getOrder();
    }
}
