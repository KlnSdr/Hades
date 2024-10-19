package hades.apps.installer;

import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.update.service.UpdateService;

public class InstallerResource {
    private static final String BASE_PATH = "/installer";
    private static final UpdateService updateService = UpdateService.getInstance();

    @Post(BASE_PATH + "/run")
    public void runInstallationProcedure(HttpContext context) {
        if (updateService.isInstalled()) {
            context.getResponse().setBody("Hades is already installed.");
            return;
        }

        final NewJson body = context.getRequest().getBody();

        if (!body.hasKeys("adminPassword")) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            context.getResponse().setBody("Missing admin password.");
            return;
        }

        final String adminPassword = body.getString("adminPassword");

        final boolean updatesRanSuccessfully = UpdateService.getInstance().runUpdates(new String[]{"SetUserDefinedAdminPassword"}, new String[][]{new String[]{adminPassword}});

        if (updatesRanSuccessfully) {
            context.getResponse().setBody("Hades has been successfully installed.");
            UpdateService.getInstance().setInstalled(true);
        } else {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody("Failed to install Hades.");
        }
    }
}