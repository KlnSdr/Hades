package hades.html;

public class Label extends HtmlElement {
    public Label() {
        super("label");
    }

    public Label(String text) {
        super("label");
        setValue(text);
    }
}
