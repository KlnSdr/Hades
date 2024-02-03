package hades;

import dobby.Dobby;
import dobby.util.logging.Logger;
import thot.connector.Connector;

public class Hades {
    public static void main(String[] args) {
        Dobby.startApplication(Hades.class);
        ensureThotIsRunning();
    }

    public static void ensureThotIsRunning() {
        final boolean isThotUp = Connector.write("system", "isRunning", true);

        if (!isThotUp) {
            new Logger(Hades.class).error("Thot is not running. Please start Thot.");
            System.exit(1);
        }
    }
}
