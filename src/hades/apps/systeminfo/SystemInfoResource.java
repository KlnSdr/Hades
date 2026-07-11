package hades.apps.systeminfo;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.Config;
import dobby.Dobby;
import dobby.IConfig;
import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.util.json.NewJson;
import hades.Hades;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.common.ErrorResponse;

import java.lang.reflect.Field;

import static hades.common.ErrorResponses.internalError;

@RegisterFor(SystemInfoResource.class)
public class SystemInfoResource {
    private static final Logger LOGGER = new Logger(SystemInfoResource.class);
    private final IConfig config;

    @Inject
    public SystemInfoResource(IConfig config) {
        this.config = config;
    }

    @AuthorizedOnly
    @PermissionCheck
    @ApiDoc(
            summary = "Get system information",
            description = "Get system information such as OS, Java, Hades, Dobby, and application versions, heap memory, CPU cores, and more."
    )
    @ApiResponse(
            code = 200,
            message = "Returns system information",
            responseBody = SystemInfoDTO.class
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @Get("/systeminfo")
    public void getSystemInfo(HttpContext context) {
        context.getResponse().setBody(new SystemInfoDTO(
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                System.getProperty("java.home"),
                Hades.getVersion(),
                "v" + getDobbyVersion(),
                config.getString("application.name", "<APP_NAME>"),
                config.getString("application.version", "<APP_VERSION>"),
                config.getString("hades.context", "/"),
                formatSize(Runtime.getRuntime().maxMemory()),
                formatSize(Runtime.getRuntime().totalMemory()),
                formatSize(Runtime.getRuntime().freeMemory()),
                formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                String.valueOf(Runtime.getRuntime().availableProcessors())
        ));
    }

    private static String getDobbyVersion() {
        return Dobby.getVersion();
    }

    @AuthorizedOnly
    @PermissionCheck
    @ApiDoc(
            summary = "Get config file content",
            description = "Get the content of the config file as a JSON object."
    )
    @ApiResponse(
            code = 200,
            message = "Returns the content of the config file"
    )
    @ApiResponse(
            code = 403,
            message = "User does not have permission to access this resource",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Could not get config file",
            responseBody = ErrorResponse.class
    )
    @Get("/configFile")
    public void getConfigContent(HttpContext context) {
        final NewJson configContent = getConfigFileAsJson();

        if (configContent == null) {
            internalError(context.getResponse(), "Could not get config file.");
            return;
        }

        context.getResponse().setBody(configContent.toString().replaceAll("\\\\", "\\\\\\\\"));
    }

    private NewJson getConfigFileAsJson() {
        try {
            final Field configContent = Config.class.getDeclaredField("configJson");
            configContent.setAccessible(true);
            return (NewJson) configContent.get(config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Config File Content");
            LOGGER.trace(e);
            return null;
        }
    }

    private static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
