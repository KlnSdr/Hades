package hades.html;

public class Script extends HtmlElement {
    public Script() {
        super("script");
    }

    public Script(String src) {
        super("script");
        setSrc(src);
    }

    public void setSrc(String src) {
        addAttribute("src", src);
    }
}
