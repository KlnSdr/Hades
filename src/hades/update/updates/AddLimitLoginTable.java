package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.IConnector;

@RegisterFor(AddLimitLoginTable.class)
public class AddLimitLoginTable implements Update {
    private final IConnector connector;

    @Inject
    public AddLimitLoginTable(IConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean run() {
        final String bucketName = "hades_limit_login";
        return connector.write(bucketName, "TEST", "") && connector.delete(bucketName, "TEST");
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
