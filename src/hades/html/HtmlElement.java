package hades.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HtmlElement {
    private final List<HtmlElement> children = new ArrayList<>();
    private final Map<String, String> attributes = new HashMap<>();
    private final String tag;
    private String value;

    public HtmlElement(String tag) {
        this.tag = tag;
    }

    public void addChild(HtmlElement child) {
        children.add(child);
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public List<HtmlElement> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getAttributeString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        return sb.toString();
    }

    public String toHtml() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag).append(getAttributeString()).append(">");
        for (HtmlElement child : children) {
            sb.append(child.toHtml());
        }
        if (value != null) {
            sb.append(value);
        }
        sb.append("</").append(tag).append(">");
        return sb.toString();
    }
}
