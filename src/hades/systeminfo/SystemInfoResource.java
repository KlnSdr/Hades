package hades.systeminfo;

import dobby.Dobby;
import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.util.Config;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import hades.Hades;
import hades.annotations.AuthorizedOnly;
import hades.annotations.PermissionCheck;

import java.lang.reflect.Field;

public class SystemInfoResource {
    private static final String BASE_PATH = "/systeminfo";
    private static final Logger LOGGER = new Logger(SystemInfoResource.class);

    @AuthorizedOnly
    @PermissionCheck
    @Get(BASE_PATH)
    public void getSystemInfo(HttpContext context) {
        final NewJson response = new NewJson();
        response.setString("os", System.getProperty("os.name"));
        response.setString("os_version", System.getProperty("os.version"));
        response.setString("os_arch", System.getProperty("os.arch"));
        response.setString("java_version", System.getProperty("java.version"));
        response.setString("java_home", System.getProperty("java.home"));
        response.setString("hades_version", Hades.getVersion());
        response.setString("dobby_version", "v" + getDobbyVersion());
        response.setString("app_name", Config.getInstance().getString("application.name", "<APP_NAME>"));
        response.setString("app_version", Config.getInstance().getString("application.version", "<APP_VERSION>"));
        response.setString("app_context", Config.getInstance().getString("hades.context", "/"));
        response.setString("heap_max", formatSize(Runtime.getRuntime().maxMemory()));
        response.setString("heap_total", formatSize(Runtime.getRuntime().totalMemory()));
        response.setString("heap_free", formatSize(Runtime.getRuntime().freeMemory()));
        response.setString("heap_used", formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        response.setString("cpu_cores", String.valueOf(Runtime.getRuntime().availableProcessors()));

        context.getResponse().setBody(response);
    }

    private static String getDobbyVersion() {
        try {
            final Field versionField = Dobby.class.getDeclaredField("version");
            versionField.setAccessible(true);
            return (String) versionField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Failed to get Dobby version.");
            LOGGER.trace(e);
            return "";
        }
    }

    private static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
