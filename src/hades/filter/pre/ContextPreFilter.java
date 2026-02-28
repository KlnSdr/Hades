package hades.filter.pre;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.util.TemplateEngine;
import dobby.IConfig;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.filter.FilterOrder;

import java.nio.charset.StandardCharsets;

@RegisterFor(ContextPreFilter.class)
public class ContextPreFilter implements Filter {
    private final IStaticFileService staticFileService;
    private final TemplateEngine templateEngine;
    private final IConfig config;

    @Inject
    public ContextPreFilter(IStaticFileService staticFileService, TemplateEngine templateEngine, IConfig config) {
        this.staticFileService = staticFileService;
        this.templateEngine = templateEngine;
        this.config = config;
    }

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
        String appContext = config.getString("hades.context", null);

        if (appContext == null) {
            return true;
        }
        appContext = appContext.toLowerCase();

        if (!path.toLowerCase().startsWith(appContext)) {
            final NewJson data = new NewJson();
            data.setString("INVALIDCONTEXT", getContextFromPath(path.toLowerCase()));
            httpContext.getResponse().setCode(ResponseCodes.NOT_FOUND);

            final StaticFile contextNotFoundFile = staticFileService.get("/error/ContextNotFound.html");
            if (contextNotFoundFile != null) {
                final String renderedContent = templateEngine.render(new String(contextNotFoundFile.getContent()), data);
                contextNotFoundFile.setContent(renderedContent.getBytes(StandardCharsets.UTF_8));
                httpContext.getResponse().sendFile(contextNotFoundFile);
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
