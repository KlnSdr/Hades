package hades.authorized;


import dobby.util.Json;
import hades.authorized.service.GroupService;
import janus.DataClass;
import janus.annotations.JanusString;
import thot.annotations.Bucket;

@Bucket(GroupService.USER_GROUP_ASSOCIATION_BUCKET)
public class UserGroupAssociation implements DataClass {
    @JanusString("userId")
    private String userId;
    @JanusString("groupId")
    private String groupId;

    public UserGroupAssociation() {

    }

    public UserGroupAssociation(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    @Override
    public String getKey() {
        return userId + "_" + groupId;
    }

    @Override
    public Json toJson() {
        final Json json = new Json();
        json.setString("userId", userId);
        json.setString("groupId", groupId);
        return json;
    }
}
