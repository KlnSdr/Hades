package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.User;
import hades.user.service.TokenLoginService;
import hades.user.service.UserService;

@RegisterFor(AddAdminLoginToken.class)
public class AddAdminLoginToken implements Update {
    private final UserService userService;
    private final TokenLoginService tokenLoginService;

    @Inject
    public AddAdminLoginToken(UserService userService, TokenLoginService tokenLoginService) {
        this.userService = userService;
        this.tokenLoginService = tokenLoginService;
    }

    @Override
    public boolean run() {
        final User[] adminRead = userService.findByName("admin");

        if (adminRead.length == 0) {
            return false;
        }
        final User admin = adminRead[0];

        return tokenLoginService.setTokenForUser(admin, tokenLoginService.generateTokenForUser());
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
