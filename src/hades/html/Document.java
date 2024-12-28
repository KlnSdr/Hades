package hades.html;

import java.util.ArrayList;
import java.util.List;

public class Document extends HtmlElement {
    private String title = "";
    private final List<String> scripts = new ArrayList<>();
    private final List<String> styles = new ArrayList<>();

    public Document() {
        super("document");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addScript(String src) {
        scripts.add(src);
    }

    public void addStyle(String src) {
        styles.add(src);
    }

    @Override
    public String toHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>").append(title).append("</title>");
        for (String scriptSrc : scripts) {
            sb.append(new Script(scriptSrc).toHtml());
        }
        for (String styleSrc : styles) {
            sb.append(new Style(styleSrc).toHtml());
        }
        sb.append("</head>");
        sb.append("<body>");
        for (HtmlElement child : getChildren()) {
            sb.append(child.toHtml());
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
}
