package hades.apidocs.ui;

import common.html.HtmlElement;

import java.util.List;

public class Table extends HtmlElement {
    public Table(List<String> columns) {
        super("table");
        this.addChild(new TableHeader(columns));
    }
    public void addRow(TableRow row) {
        this.addChild(row);
    }
}
