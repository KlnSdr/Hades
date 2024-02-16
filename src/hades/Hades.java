package hades;

import dobby.Dobby;
import dobby.DobbyEntryPoint;
import dobby.util.logging.Logger;
import hades.authorized.AuthorizedRoutesDiscoverer;
import thot.connector.Connector;

public class Hades implements DobbyEntryPoint {
    public static void main(String[] args) {
        new Hades().startApplication(Hades.class);
    }

    public void startApplication(Class<?> clazz) {
        Dobby.startApplication(clazz);
    }

    private void ensureThotIsRunning() {
        final boolean isThotUp = Connector.write("system", "isRunning", true);

        if (!isThotUp) {
            new Logger(Hades.class).error("Thot is not running. Please start Thot.");
            System.exit(1);
        }
    }

    @Override
    public void preStart() {
        ensureThotIsRunning();
        AuthorizedRoutesDiscoverer.discoverRoutes("");
    }

    @Override
    public void postStart() {

    }
}
