package hades.apidocs.ui;

import common.html.HtmlElement;

public class TableRow extends HtmlElement {
    public TableRow() {
        super("tr");
    }

    public void addColumn(String content) {
        this.addChild(new Td(content));
    }

    public void addColumn(HtmlElement element) {
        this.addChild(new Td(element));
    }
}
