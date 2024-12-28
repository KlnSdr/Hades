package hades.html;

public class Headline extends HtmlElement {
    public Headline(int level, String text) {
        super("h" + level);
        setValue(text);
    }

    public static Headline h1(String text) {
        return new Headline(1, text);
    }

    public static Headline h2(String text) {
        return new Headline(2, text);
    }

    public static Headline h3(String text) {
        return new Headline(3, text);
    }

    public static Headline h4(String text) {
        return new Headline(4, text);
    }

    public static Headline h5(String text) {
        return new Headline(5, text);
    }

    public static Headline h6(String text) {
        return new Headline(6, text);
    }
}
