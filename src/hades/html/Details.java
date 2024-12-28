package hades.html;

public class Details extends HtmlElement {
    private final Summary summary = new Summary();

    public Details() {
        super("details");
        addChild(summary);
    }

    public Details(String summaryText) {
        this();
        setSummary(summaryText);
    }

    public void setSummary(String text) {
        summary.setValue(text);
    }
}
