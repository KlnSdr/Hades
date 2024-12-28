package hades.html;

public class Ul extends HtmlElement {
    public Ul() {
        super("ul");
    }

    public void addItem(String text) {
        addChild(new Li(text));
    }
}
