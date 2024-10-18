package hades.update;

public interface Update {
    public boolean run();
    public default boolean run(String[] args) {
        return run();
    }

    String getName();

    int getOrder();
}
