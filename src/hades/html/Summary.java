package hades.html;

public class Summary extends HtmlElement {
    public Summary() {
        super("summary");
    }

    public Summary(String text) {
        super("summary");
        setValue(text);
    }
}
