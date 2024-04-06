package hades.filter;

public enum FilterOrder {

    AUTHORIZED_ROUTE_PRE_FILTER(10),
    AUTHORIZED_REDIRECT_PRE_FILTER(11),
    BLOCK_STATIC_FILES_PRE_FILTER(12),
    REPLACE_DISPLAY_NAME_PRE_FILTER(13),
    REDIRECT_ME_TO_USER_PAGE_PRE_FILTER(14),
    USER_INFO_PAGE_PRE_FILTER(15);

    private final int i;
    FilterOrder(int i) {
        this.i = i;
    }

    public int getOrder() {
        return i;
    }
}
