package hades.update.updates;

import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.Connector;

public class AddLimitLoginTable implements Update {
    @Override
    public boolean run() {
        final String bucketName = "hades_limit_login";
        return Connector.write(bucketName, "TEST", "") && Connector.delete(bucketName, "TEST");
    }

    @Override
    public String getName() {
        return "AddLimitLoginTable";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_LIMIT_LOGIN_TABLE.getOrder();
    }
}
