package hades.apidocs.ui;

import common.html.HtmlElement;

import java.util.List;

public class TableHeader extends HtmlElement {
    public TableHeader(List<String> columns) {
        super("thead");
        for (String column : columns) {
            this.addChild(new Th(column));
        }
    }
}
