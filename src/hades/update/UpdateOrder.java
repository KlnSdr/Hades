package hades.update;

public enum UpdateOrder {
    SETUP_ADMIN_USER(0),
    SETUP_ADMIN_PERMISSIONS(1),
    MOVE_ADMIN_PERMISSIONS_TO_GROUP(3),
    ADD_GROUPS_REST_PERMISSIONS_TO_ADMIN_GROUP(4),
    SET_ADMIN_PASSWORD(5)
    ;

    private final int i;
    UpdateOrder(int i) {
        this.i = i;
    }

    public int getOrder() {
        return i;
    }
}
