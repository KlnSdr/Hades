package hades.apidocs.ui;

import dobby.io.request.RequestTypes;
import hades.apidocs.RouteDocumentation;
import hades.html.Details;
import hades.html.HtmlElement;
import hades.html.Label;
import hades.html.Paragraph;

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

        details.setSummaryContent(List.of(
                requestTypeLabel,
                new Label(modifiedPath),
                summaryLabel
        ));

        final Paragraph description = new Paragraph(routeDocumentation.getDescription());
        details.addChild(description);

        return details.toHtml();
    }
}
