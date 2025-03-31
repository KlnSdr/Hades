package hades.authorized;

import dobby.util.json.NewJson;
import hades.authorized.service.GroupService;
import thot.janus.DataClass;
import thot.janus.annotations.JanusString;
import thot.api.annotations.v2.Bucket;

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
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("userId", userId);
        json.setString("groupId", groupId);
        return json;
    }
}
