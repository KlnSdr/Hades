package hades.update.updates;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import hades.update.Update;
import hades.update.UpdateOrder;
import thot.connector.IConnector;

import java.time.LocalDateTime;

@RegisterFor(HadesInstalledUpdate.class)
public class HadesInstalledUpdate implements Update {
    private final IConnector connector;

    @Inject
    public HadesInstalledUpdate(IConnector connector) {
        this.connector = connector;
    }

    @Override
    public boolean run() {
        final LocalDateTime ldt = LocalDateTime.now().plusDays(1);
        return connector.write("system", "hadesInstallationDate", ldt);
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
