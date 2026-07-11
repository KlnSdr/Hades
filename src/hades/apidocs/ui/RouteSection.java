package hades.apidocs.ui;

import common.html.*;
import dobby.io.dto.Serializer;
import dobby.io.request.RequestTypes;
import dobby.util.json.NewJson;
import hades.apidocs.RouteDocumentation;
import hades.apidocs.annotations.ApiResponse;
import hades.apidocs.annotations.NoResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RouteSection extends HtmlElement {
    private final RequestTypes requestType;
    private final String path;
    private final List<String> params;
    private final RouteDocumentation routeDocumentation;

    public RouteSection(String path, RouteDocumentation routeDocumentation) {
        super("section");
        this.routeDocumentation = routeDocumentation;
        this.requestType = routeDocumentation.getRequestType();
        this.path = path;
        this.params = routeDocumentation.getParams();
    }

    @Override
    public String toHtml() {
        final Details details = new Details();
        details.addStyle("routeSection");
        details.addStyle(requestType.name().toLowerCase() + "Route");

        final Label requestTypeLabel = new Label(requestType.name().toUpperCase());
        requestTypeLabel.addStyle("requestTypeLabel");

        String modifiedPath = this.path;

        for (String param : params) {
            modifiedPath = modifiedPath.replaceFirst("\\*", "{" + param + "}");
        }

        final Label summaryLabel = new Label(routeDocumentation.getSummary());
        summaryLabel.addStyle("summaryLabel");

        final Label spacer = new Label();
        spacer.addStyle("spacer");

        final Label lockLabel = new Label();
        lockLabel.addStyle("fa");
        lockLabel.addStyle("fa-" + (routeDocumentation.isAuthOnly() ? "" : "un") + "lock");

        details.setSummaryContent(List.of(
                requestTypeLabel,
                new Label(modifiedPath),
                summaryLabel,
                spacer,
                lockLabel
        ));

        final Paragraph description = new Paragraph(routeDocumentation.getDescription());
        details.addChild(description);

        final Headline responsesHeadline = new Headline(3, "Responses");
        details.addChild(responsesHeadline);

        final Table responseTable = new Table(List.of("Code", "Description"));

        for (ApiResponse apiResponse : routeDocumentation.getApiResponses()) {
            NewJson responseBody = null;

            try {
                responseBody = apiResponse.responseBody() != null && apiResponse.responseBody() != NoResponseBody.class
                        ? new Serializer().getJsonType(apiResponse.responseBody().getDeclaredConstructor().newInstance())
                        : null;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException ignored) {
            }
            final ResponseRow responseRow = new ResponseRow(Integer.toString(apiResponse.code()), apiResponse.message(), responseBody);

            responseTable.addChild(responseRow.toRow());
        }
        details.addChild(responseTable);

        return details.toHtml();
    }
}
