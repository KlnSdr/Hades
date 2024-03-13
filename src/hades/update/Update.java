package hades.update;

public interface Update {
    public boolean run();

    String getName();

    int getOrder();
}
