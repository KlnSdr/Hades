package hades.update;

import dobby.util.Json;
import hades.update.service.UpdateService;
import janus.DataClass;
import thot.annotations.Bucket;

@Bucket(UpdateService.BUCKET_NAME)
public class UpdateRan implements DataClass {
    private final String updateName;

    public UpdateRan(String updateName) {
        this.updateName = updateName;
    }

    @Override
    public String getKey() {
        return updateName;
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setString("updateName", updateName);
        return json;
    }
}
