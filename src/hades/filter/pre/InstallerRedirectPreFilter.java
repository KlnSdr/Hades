package hades.filter.pre;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.IConfig;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import hades.filter.FilterOrder;
import hades.update.service.UpdateService;

@RegisterFor(InstallerRedirectPreFilter.class)
public class InstallerRedirectPreFilter implements Filter {
    private final UpdateService updateService;
    private final IConfig config;

    @Inject
    public InstallerRedirectPreFilter(UpdateService updateService, IConfig config) {
        this.updateService = updateService;
        this.config = config;
    }

    @Override
    public String getName() {
        return "InstallerRedirectPreFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REDIRECT_TO_INSTALLER_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        if (updateService.isInstalled()) {
            return true;
        }

        if (httpContext.getRequest().getPath().equalsIgnoreCase("/hades/installer") || httpContext.getRequest().getPath().equalsIgnoreCase("/installer/run") || httpContext.getRequest().getPath().endsWith(".css") || httpContext.getRequest().getPath().endsWith(".js") || httpContext.getRequest().getPath().endsWith(".png")) {
            return true;
        }

        httpContext.getResponse().setCode(ResponseCodes.FOUND);
        httpContext.getResponse().setHeader("Location", config.getString("hades.context", "") + "/hades/installer");
        return false;
    }
}
