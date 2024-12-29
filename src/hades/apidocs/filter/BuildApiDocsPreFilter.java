package hades.apidocs.filter;

import dobby.DefaultHandler.MethodNotSupportedHandler;
import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.IRequestHandler;
import dobby.io.request.RequestTypes;
import dobby.io.response.ResponseCodes;
import dobby.routes.Route;
import dobby.routes.RouteManager;
import dobby.util.Config;
import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import hades.apidocs.ui.RouteSection;
import hades.filter.FilterOrder;
import hades.html.Document;
import hades.html.Headline;
import hades.html.HtmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildApiDocsPreFilter implements Filter {
    private static final StaticFileService staticFileService = StaticFileService.getInstance();
    private static final Logger LOGGER = new Logger(BuildApiDocsPreFilter.class);
    private static final RouteManager routeManager = RouteManager.getInstance();
    private static final List<RequestTypes> requestTypes = List.of(RequestTypes.GET, RequestTypes.POST, RequestTypes.PUT, RequestTypes.DELETE);

    @Override
    public String getName() {
        return "BuildApiDocsPreFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.BUILD_API_DOCS_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final String docsPath = "/apidocs/index.html";
        final String requestUri = httpContext.getRequest().getPath();

        if (!requestUri.equalsIgnoreCase(docsPath)) {
            return true;
        }

        final StaticFile docsFile = staticFileService.get(docsPath);

        if (docsFile != null) {
            return true;
        }

        LOGGER.info("building api docs");

        final StaticFile apiDocs = buildApiDocs();

        if (apiDocs == null) {
            httpContext.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
            final NewJson msg = new NewJson();
            msg.setString("msg", "Failed to build api docs");
            httpContext.getResponse().setBody(msg);
            return false;
        }

        staticFileService.storeFile(docsPath, apiDocs);

        return true;
    }

    private StaticFile buildApiDocs() {
        final StaticFile file = new StaticFile();
        file.setContentType("text/html");

        final Map<String, Route> routes = routeManager.getAllRoutes();
        final Document doc = new Document();
        doc.setTitle("API Docs");
        doc.addChild(new Headline(1, "API Docs of " + Config.getInstance().getString("application.name", "\t&lt;APP_NAME\t&gt;")));
        doc.addStyle("{{CONTEXT}}/apidocs/index.css");

        for (Map.Entry<String, Route> entry : routes.entrySet()) {
            doc.addAllChildren(buildRouteElements(entry.getKey(), entry.getValue()));
        }

        file.setContent(doc.toHtml().getBytes());

        return file;
    }

    private List<HtmlElement> buildRouteElements(String path, Route route) {
        final List<HtmlElement> elements = new ArrayList<>();

        for (RequestTypes requestType : requestTypes) {
            final IRequestHandler handler = route.getHandler(requestType);

            if (handler instanceof MethodNotSupportedHandler) {
                continue;
            }

            elements.add(new RouteSection(requestType, path));
        }

        return elements;
    }
}
