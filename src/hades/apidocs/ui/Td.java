package hades.apidocs.ui;

import common.html.HtmlElement;

public class Td extends HtmlElement {
    public Td(String content) {
        super("td");
        this.setValue(content);
    }

    public Td(HtmlElement element) {
        super("td");
        this.addChild(element);
    }
}
