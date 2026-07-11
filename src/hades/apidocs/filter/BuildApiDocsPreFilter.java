package hades.apidocs.filter;

import common.html.Document;
import common.html.Headline;
import common.html.HtmlElement;
import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.IConfig;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.dto.Serializer;
import dobby.util.json.NewJson;
import hades.apidocs.RouteDocumentation;
import hades.apidocs.RouteDocumentationDiscoverer;
import hades.apidocs.annotations.ApiResponse;
import hades.apidocs.annotations.NoResponseBody;
import hades.apidocs.ui.JsonSchemaFormatter;
import hades.apidocs.ui.RouteSection;
import hades.filter.FilterOrder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RegisterFor(BuildApiDocsPreFilter.class)
public class BuildApiDocsPreFilter implements Filter {
    private final IStaticFileService staticFileService;
    private final IConfig config;
    private static final Logger LOGGER = new Logger(BuildApiDocsPreFilter.class);

    @Inject
    public BuildApiDocsPreFilter(IStaticFileService staticFileService, IConfig config) {
        this.staticFileService = staticFileService;
        this.config = config;
    }

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

        if (!config.getBoolean("hades.apidocs.enabled", false)) {
            return true;
        }

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
        final Map<String, Map<String, List<RouteDocumentation>>> routeDocumentationMap = sortRouteDocumentationByBaseUrl(RouteDocumentationDiscoverer.getRouteDocumentationMap());

        final Document doc = new Document();
        doc.setTitle("API Docs");
        doc.addChild(new Headline(1, "API Docs of " + config.getString("application.name", "\t&lt;APP_NAME\t&gt;")));
        doc.addStyle("{{CONTEXT}}/apidocs/index.css");
        doc.addStyle("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css");

        final List<String> baseUrls = new ArrayList<>(routeDocumentationMap.keySet());
        baseUrls.sort(String::compareTo);

        final Map<String, NewJson> schemas = new HashMap<>();
        final Set<Class<?>> visitedTypes = new HashSet<>();

        for (String baseUrl : baseUrls) {
            if (!baseUrl.isEmpty()) {
                final Headline baseUrlHeadline = new Headline(2, baseUrl);
                doc.addChild(baseUrlHeadline);
            }

            for (Map.Entry<String, List<RouteDocumentation>> entry : routeDocumentationMap.get(baseUrl).entrySet()) {
                for (RouteDocumentation rDoc : entry.getValue()) {
                    for (ApiResponse apiResponse : rDoc.getApiResponses()) {
                        collectSchemas(apiResponse.responseBody(), schemas, visitedTypes);
                    }
                }
                doc.addAllChildren(buildRouteElements(entry.getKey(), entry.getValue()));
            }
        }

        doc.addChild(new Headline(2, "Schemas"));
        final List<String> schemaNames = new ArrayList<>(schemas.keySet());
        schemaNames.sort(String::compareTo);
        for (String name : schemaNames) {
            doc.addChild(new Headline(3, name));
            doc.addChild(JsonSchemaFormatter.toHtmlElement(schemas.get(name)));
        }

        file.setContent(doc.toHtml().getBytes());

        return file;
    }

    private void collectSchemas(Class<?> type, Map<String, NewJson> schemas, Set<Class<?>> visitedTypes) {
        if (type == null || type == NoResponseBody.class || isSimpleType(type) || visitedTypes.contains(type)) {
            return;
        }
        visitedTypes.add(type);

        final Object instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException ignored) {
            return;
        }

        final NewJson schema = new Serializer().getJsonType(instance);
        if (schema == null) {
            return;
        }
        schemas.putIfAbsent(type.getSimpleName(), schema);

        for (Field field : type.getDeclaredFields()) {
            final Class<?> fieldType = field.getType();
            if (List.class.isAssignableFrom(fieldType)) {
                final Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType parameterizedType) {
                    final Type[] typeArgs = parameterizedType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementType) {
                        collectSchemas(elementType, schemas, visitedTypes);
                    }
                }
            } else {
                collectSchemas(fieldType, schemas, visitedTypes);
            }
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class
                || type == UUID.class
                || type == Void.class
                || type.isEnum();
    }

    private Map<String, Map<String, List<RouteDocumentation>>> sortRouteDocumentationByBaseUrl(Map<String, List<RouteDocumentation>> routeDocumentationMap) {
        final Map<String, Map<String, List<RouteDocumentation>>> sortedMap = new HashMap<>();

        for (Map.Entry<String, List<RouteDocumentation>> entry : routeDocumentationMap.entrySet()) {
            final RouteDocumentation routeDocumentation = entry.getValue().getFirst();
            final String baseUrl = routeDocumentation.getBaseUrl();

            final Map<String, List<RouteDocumentation>> map = sortedMap.getOrDefault(baseUrl, new HashMap<>());
            map.put(entry.getKey(), entry.getValue());
            sortedMap.put(baseUrl, map);
        }

        return sortedMap;
    }

    private List<HtmlElement> buildRouteElements(String path, List<RouteDocumentation> routeDocumentations) {
        final List<HtmlElement> elements = new ArrayList<>();

        for (RouteDocumentation routeDocumentation : routeDocumentations) {
            elements.add(new RouteSection(path, routeDocumentation));
        }

        return elements;
    }
}