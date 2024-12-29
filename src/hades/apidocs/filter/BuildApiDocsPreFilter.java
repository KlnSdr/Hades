package hades.apidocs.filter;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.RequestTypes;
import dobby.routes.RouteManager;
import dobby.util.Config;
import dobby.util.logging.Logger;
import hades.apidocs.RouteDocumentation;
import hades.apidocs.RouteDocumentationDiscoverer;
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

        staticFileService.storeFile(docsPath, apiDocs);

        return true;
    }

    private StaticFile buildApiDocs() {
        final StaticFile file = new StaticFile();
        file.setContentType("text/html");

        RouteDocumentationDiscoverer.discoverRoutes();
        final Map<String, List<RouteDocumentation>> routeDocumentationMap = RouteDocumentationDiscoverer.getRouteDocumentationMap();

        final Document doc = new Document();
        doc.setTitle("API Docs");
        doc.addChild(new Headline(1, "API Docs of " + Config.getInstance().getString("application.name", "\t&lt;APP_NAME\t&gt;")));
        doc.addStyle("{{CONTEXT}}/apidocs/index.css");

        for (Map.Entry<String, List<RouteDocumentation>> entry : routeDocumentationMap.entrySet()) {
            doc.addAllChildren(buildRouteElements(entry.getKey(), entry.getValue()));
        }

        file.setContent(doc.toHtml().getBytes());

        return file;
    }

    private List<HtmlElement> buildRouteElements(String path, List<RouteDocumentation> routeDocumentations) {
        final List<HtmlElement> elements = new ArrayList<>();

        for (RouteDocumentation routeDocumentation : routeDocumentations) {
            elements.add(new RouteSection(path, routeDocumentation));
        }

        return elements;
    }
}
