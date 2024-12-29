package hades.html;

public class Paragraph extends HtmlElement {
    public Paragraph() {
        super("p");
    }

    public Paragraph(String text) {
        super("p");
        setValue(text);
    }
}
