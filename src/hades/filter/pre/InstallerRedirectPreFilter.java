package hades.filter.pre;

import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.Config;
import hades.filter.FilterOrder;
import hades.update.service.UpdateService;

public class InstallerRedirectPreFilter implements Filter {
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
        if (UpdateService.getInstance().isInstalled()) {
            return true;
        }

        if (httpContext.getRequest().getPath().equalsIgnoreCase("/hades/installer") || httpContext.getRequest().getPath().equalsIgnoreCase("/installer/run") || httpContext.getRequest().getPath().endsWith(".css") || httpContext.getRequest().getPath().endsWith(".js") || httpContext.getRequest().getPath().endsWith(".png")) {
            return true;
        }

        httpContext.getResponse().setCode(ResponseCodes.FOUND);
        httpContext.getResponse().setHeader("Location", Config.getInstance().getString("hades.context", "") + "/hades/installer");
        return false;
    }
}
