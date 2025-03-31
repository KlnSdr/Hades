package hades.update.updates;

import common.logger.Logger;
import hades.authorized.Group;
import hades.authorized.Permission;
import hades.authorized.service.GroupService;
import hades.messaging.service.MessageService;
import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessagesUpdate implements Update {
    private static final Logger LOGGER = new Logger(MessagesUpdate.class);
    @Override
    public boolean run() {
        final String bucket = MessageService.MESSAGE_BUCKET;
        if (!(Connector.write(bucket, "TEST", "") && Connector.delete(bucket, "TEST"))) {
            LOGGER.error("Failed to create messages bucket");
            return false;
        }

        final Group group = GroupService.getInstance().findByName("admin");

        if (group == null) {
            LOGGER.error("Failed to find admin user");
            return false;
        }

        final List<Permission> permissions = buildPermissions(group.getId());

        for (Permission permission : permissions) {
            group.addPermission(permission);
        }

        if (!GroupService.getInstance().update(group)) {
            LOGGER.error("Failed to update group");
            return false;
        }

        return true;
    }

    private List<Permission> buildPermissions(UUID groupId) {
        final List<Permission> permissions = new ArrayList<>();

        final Permission permission = new Permission();
        permission.setOwner(groupId);
        permission.setRoute("/rest/messages/send/*");
        permission.setPermissionGET(false);
        permission.setPermissionPOST(true);
        permission.setPermissionPUT(false);
        permission.setPermissionDELETE(false);
        permissions.add(permission);

        return permissions;
    }

    @Override
    public String getName() {
        return "MessagesUpdate";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.MESSAGES_UPDATE.getOrder();
    }
}
