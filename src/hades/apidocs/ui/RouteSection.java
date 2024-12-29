package hades.apidocs.ui;

import dobby.io.request.RequestTypes;
import hades.html.Details;
import hades.html.HtmlElement;
import hades.html.Label;

import java.util.List;

public class RouteSection extends HtmlElement {
    private final RequestTypes requestType;
    private final String path;

    public RouteSection(RequestTypes requestType, String path) {
        super("section");
        this.requestType = requestType;
        this.path = path;
    }

    @Override
    public String toHtml() {
        final Details details = new Details();
        details.addStyle("routeSection");
        details.addStyle(requestType.name().toLowerCase() + "Route");

        final Label requestTypeLabel = new Label(requestType.name().toUpperCase());
        requestTypeLabel.addStyle("requestTypeLabel");

        details.setSummaryContent(List.of(
                requestTypeLabel,
                new Label(path)
        ));

        return details.toHtml();
    }
}
