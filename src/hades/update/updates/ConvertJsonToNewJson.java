package hades.update.updates;

import common.logger.Logger;
import dobby.util.Json;
import dobby.util.json.NewJson;
import hades.authorized.service.GroupService;
import hades.authorized.service.PermissionService;
import hades.update.Update;
import hades.update.UpdateOrder;
import hades.user.service.UserService;
import thot.connector.Connector;

import static dobby.util.JsonConverter.convert;

public class ConvertJsonToNewJson implements Update {
    private static final Logger LOGGER = new Logger(ConvertJsonToNewJson.class);

    @Override
    public boolean run() {
        final String[] bucketNames = new String[]{GroupService.GROUP_BUCKET, GroupService.USER_GROUP_ASSOCIATION_BUCKET, PermissionService.PERMISSION_BUCKET, UserService.USER_BUCKET};

        for (String bucketName : bucketNames) {
            final String[] keys = Connector.getKeys(bucketName);
            for (String key : keys) {
                final Object maybeJson = Connector.read(bucketName, key, Object.class);
                if (!(maybeJson instanceof Json)) {
                    continue;
                }
                final NewJson newJson = convert((Json) maybeJson);
                if (!Connector.write(bucketName, key, newJson)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "ConvertJsonToNewJson";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.CONVERT_JSON_TO_NEW_JSON.getOrder();
    }
}
