package hades.apps.installer;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.annotations.Post;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.update.service.UpdateService;

@RegisterFor(InstallerResource.class)
public class InstallerResource {
    private static final String BASE_PATH = "/installer";
    private final UpdateService updateService;

    @Inject
    public InstallerResource(UpdateService updateService) {
        this.updateService = updateService;
    }

    @ApiDoc(
            summary = "Run installation procedure",
            description = "Run the installation procedure to install Hades",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Hades has been successfully installed"
    )
    @ApiResponse(
            code = 400,
            message = "Missing admin password"
    )
    @ApiResponse(
            code = 409,
            message = "Hades is already installed"
    )
    @ApiResponse(
            code = 500,
            message = "Failed to install Hades"
    )
    @Post(BASE_PATH + "/run")
    public void runInstallationProcedure(HttpContext context) {
        if (updateService.isInstalled()) {
            context.getResponse().setCode(ResponseCodes.CONFLICT);
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

        final boolean updatesRanSuccessfully = updateService.runUpdates(new String[]{"SetUserDefinedAdminPassword"}, new String[][]{new String[]{adminPassword}});

        if (updatesRanSuccessfully) {
            context.getResponse().setBody("Hades has been successfully installed.");
            updateService.setInstalled(true);
        } else {
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            context.getResponse().setBody("Failed to install Hades.");
        }
    }
}
