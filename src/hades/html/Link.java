package hades.html;

public class Link extends HtmlElement {
    public Link() {
        super("a");
    }

    public Link(String href, String text) {
        this();
        setHref(href);
        setValue(text);
    }

    public void setHref(String href) {
        addAttribute("href", href);
    }
}
