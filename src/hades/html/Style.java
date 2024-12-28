package hades.html;

public class Style extends HtmlElement {
    public Style() {
        super("link");
        addAttribute("rel", "stylesheet");
    }

    public Style(String href) {
        this();
        setHref(href);
    }


    public void setHref(String href) {
        addAttribute("href", href);
    }
}
