package hades.authorized.service;

import hades.authorized.Group;
import hades.authorized.UserGroupAssociation;
import thot.connector.Connector;

import java.util.UUID;

public class GroupService {
    public static final String GROUP_BUCKET = "hades_groups";
    public static final String USER_GROUP_ASSOCIATION_BUCKET = "hades_user_group_association";
    private static GroupService instance;

    private GroupService() {
    }

    public static GroupService getInstance() {
        if (instance == null) {
            instance = new GroupService();
        }
        return instance;
    }

    public boolean update(Group group) {
        return Connector.write(GROUP_BUCKET, group.getKey(), group);
    }

    public Group find(String key) {
        return Connector.read(GROUP_BUCKET, key, Group.class);
    }

    public boolean delete(String key) {
        return Connector.delete(GROUP_BUCKET, key);
    }

    public Group[] findAll() {
        return Connector.readPattern(GROUP_BUCKET, ".*", Group.class);
    }

    public boolean addUserToGroup(String userId, String groupId) {
        return Connector.write(USER_GROUP_ASSOCIATION_BUCKET, userId + "_" + groupId, new UserGroupAssociation(userId, groupId));
    }

    public boolean removeUserFromGroup(String userId, String groupId) {
        return Connector.delete(USER_GROUP_ASSOCIATION_BUCKET, userId + "_" + groupId);
    }

    public Group[] findGroupsByUser(UUID userId) {
        final UserGroupAssociation[] userGroupAssociations = Connector.readPattern(USER_GROUP_ASSOCIATION_BUCKET, userId + "_.*", UserGroupAssociation.class);
        final String[] groupIds = new String[userGroupAssociations.length];
        for (int i = 0; i < userGroupAssociations.length; i++) {
            groupIds[i] = userGroupAssociations[i].getKey().split("_")[1];
        }

        final Group[] groups = new Group[groupIds.length];

        for (int i = 0; i < groupIds.length; i++) {
            groups[i] = find(groupIds[i]);
        }

        return groups;
    }
}
