package hades.update;

public enum UpdateOrder {
    CREATE_BASIC_BUCKETS(-1),
    SETUP_ADMIN_USER(0),
    SETUP_ADMIN_PERMISSIONS(1),
    MOVE_ADMIN_PERMISSIONS_TO_GROUP(3),
    ADD_GROUPS_REST_PERMISSIONS_TO_ADMIN_GROUP(4),
    SET_ADMIN_PASSWORD(5),
    CREATE_LIMIT_LOGIN_TABLE(6),
    CONVERT_JSON_TO_NEW_JSON(7),
    CREATE_DB_EXPLORER_PERMISSIONS(8),
    SYSTEM_INFO_PERMISSION(9),
    TOKEN_LOGIN_BUCKET(10),
    ADD_ADMIN_LOGIN_TOKEN(11),
    ADD_CONFIG_FILE_PERMISSION(12)
    ;

    private final int i;
    UpdateOrder(int i) {
        this.i = i;
    }

    public int getOrder() {
        return i;
    }
}
