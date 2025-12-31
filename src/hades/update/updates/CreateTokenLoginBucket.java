package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.service.TokenLoginService;
import thot.connector.IConnector;

@RegisterFor(CreateTokenLoginBucket.class)
public class CreateTokenLoginBucket implements Update {
    private final IConnector connector;

    @Inject
    public CreateTokenLoginBucket(IConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean run() {
        return connector.write(TokenLoginService.BUCKET_NAME, "TEST", "") && connector.delete(TokenLoginService.BUCKET_NAME, "TEST");
    }

    @Override
    public String getName() {
        return "CreateTokenLoginBucket";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.TOKEN_LOGIN_BUCKET.getOrder();
    }
}
