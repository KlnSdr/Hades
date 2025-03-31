package hades.filter.pre;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.Config;
import dobby.util.json.NewJson;
import hades.filter.FilterOrder;
import hades.template.TemplateEngine;

public class ContextPreFilter implements Filter {
    @Override
    public String getName() {
        return "ContextPreFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.CONTEXT_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String path = httpContext.getRequest().getPath();
        String appContext = Config.getInstance().getString("hades.context", null);

        if (appContext == null) {
            return true;
        }
        appContext = appContext.toLowerCase();

        if (!path.toLowerCase().startsWith(appContext)) {
            final NewJson data = new NewJson();
            data.setString("INVALIDCONTEXT", getContextFromPath(path.toLowerCase()));
            httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);

            final StaticFile contextNotFoundFile = StaticFileService.getInstance().get("/error/ContextNotFound.html");
            if (contextNotFoundFile != null) {
                httpContext.getResponse().setBody(TemplateEngine.render(contextNotFoundFile, data).getContent());
                httpContext.getResponse().setHeader("Content-Type", contextNotFoundFile.getContentType());
            }
            return false;
        }

        final String newPath = path.substring(appContext.length());
        if (newPath.isEmpty()) {
            httpContext.getResponse().setHeader("Location", appContext + "/index.html");
            httpContext.getResponse().setCode(ResponseCodes.FOUND);
            return false;
        }

        httpContext.getRequest().setPath(newPath);
        return true;
    }

    private String getContextFromPath(String path) {
        final String[] splitPath = path.split("/");
        if (splitPath.length < 2) {
            return "/";
        }
        return "/" + splitPath[1];
    }
}
