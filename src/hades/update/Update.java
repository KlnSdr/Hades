package hades.update;

public interface Update {
    public boolean run() throws UpdateSkippedException;
    public default boolean run(String[] args) throws UpdateSkippedException {
        return run();
    }

    String getName();

    int getOrder();
}
