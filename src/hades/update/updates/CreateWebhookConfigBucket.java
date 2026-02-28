package hades.update.updates;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import hades.messaging.service.WebhookService;
import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.IConnector;

@RegisterFor(CreateWebhookConfigBucket.class)
public class CreateWebhookConfigBucket implements Update {
    private final IConnector connector;

    @Inject
    public CreateWebhookConfigBucket(IConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean run() {
        return connector.write(WebhookService.BUCKET_NAME, "TEST", "") && connector.delete(WebhookService.BUCKET_NAME, "TEST");
    }

    @Override
    public String getName() {
        return "CreateWebhookConfigBucket";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_BASIC_BUCKETS.getOrder();
    }
}
