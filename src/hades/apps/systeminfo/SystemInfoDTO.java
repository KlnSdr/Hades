package hades.apps.systeminfo;

public record SystemInfoDTO(String os, String os_version, String os_arch, String java_version, String java_home,
                            String hades_version, String dobby_version, String app_name, String app_version,
                            String app_context, String heap_max, String heap_total, String heap_free, String heap_used,
                            String cpu_cores) {

    public SystemInfoDTO() {
        this("", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}
