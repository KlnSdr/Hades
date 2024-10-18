package hades.update.updates;

import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.Connector;

import java.time.LocalDateTime;

public class HadesInstalledUpdate implements Update {
    @Override
    public boolean run() {
        final LocalDateTime ldt = LocalDateTime.now().plusDays(1);
        return Connector.write("system", "hadesInstallationDate", ldt);
    }

    @Override
    public String getName() {
        return "Hades_Installed";
    }

    @Override
    public int getOrder() {
        return UpdateOrder.HADES_INSTALLED.getOrder();
    }
}
