package hades.update.updates;

import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.service.TokenLoginService;
import thot.connector.Connector;

public class CreateTokenLoginBucket implements Update {
    @Override
    public boolean run() {
        return Connector.write(TokenLoginService.BUCKET_NAME, "TEST", "") && Connector.delete(TokenLoginService.BUCKET_NAME, "TEST");
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
