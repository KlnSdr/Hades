package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.update.service.UpdateService;
import hades.user.service.UserService;
import thot.connector.IConnector;

@RegisterFor(CreateBasicBuckets.class)
public class CreateBasicBuckets implements Update {
    private final IConnector connector;

    @Inject
    public CreateBasicBuckets(IConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean run() {
        final String[] bucketNames = new String[]{UpdateService.BUCKET_NAME, GroupService.GROUP_BUCKET,
                GroupService.USER_GROUP_ASSOCIATION_BUCKET, PermissionService.PERMISSION_BUCKET, UserService.USER_BUCKET};

        for (String bucketName : bucketNames) {
            if (!connector.write(bucketName, "TEST", "") || !connector.delete(bucketName, "TEST")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "CreateBasicBuckets";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CREATE_BASIC_BUCKETS.getOrder();
    }
}
