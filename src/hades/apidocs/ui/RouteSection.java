package hades.apidocs.ui;

import common.html.Details;
import common.html.Headline;
import common.html.HtmlElement;
import common.html.Paragraph;
import common.html.Label;
import dobby.io.request.RequestTypes;
import hades.apidocs.RouteDocumentation;
import hades.apidocs.annotations.ApiResponse;

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

        for (ApiResponse apiResponse : routeDocumentation.getApiResponses()) {
            final Paragraph responseParagraph = new Paragraph();
            responseParagraph.setValue(apiResponse.code() + " - " + apiResponse.message());
            details.addChild(responseParagraph);
        }

        return details.toHtml();
    }
}
