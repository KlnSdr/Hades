package hades.apidocs.ui;

import common.html.Div;
import common.html.Paragraph;
import dobby.util.json.NewJson;

public class ResponseRow {
    private final String status;
    private final String description;
    private final NewJson responseType;

    public ResponseRow(String status, String description, NewJson responseType) {
        this.status = status;
        this.description = description;
        this.responseType = responseType;
    }

    public TableRow toRow() {
        TableRow row = new TableRow();
        row.addColumn(status);

        final Div div = new Div();
        final Paragraph p = new Paragraph(description);
        div.addChild(p);
        if (responseType != null) {
            div.addChild(JsonSchemaFormatter.toHtmlElement(responseType));
        }
        row.addColumn(div);

        return row;
    }
}
