package hades.apidocs.ui;

import common.html.HtmlElement;

public class Th extends HtmlElement {
    public Th(String content) {
        super("th");
        this.setValue(content);
    }
}
