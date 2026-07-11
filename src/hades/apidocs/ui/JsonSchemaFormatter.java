package hades.apidocs.ui;

import common.html.Details;
import common.html.Div;
import common.html.HtmlElement;
import common.html.Label;
import dobby.util.json.NewJson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonSchemaFormatter {

    private JsonSchemaFormatter() {
    }

    /**
     * Build the full HtmlElement tree for a schema.
     *
     * @param schema the NewJson type-schema
     * @return a Div with class "json-schema" containing one row per field
     */
    public static Div toHtmlElement(NewJson schema) {
        final Div root = new Div();
        root.addStyle("json-schema");
        appendRows(schema, root);
        return root;
    }

    /**
     * Convenience method: build the tree and immediately serialize it to an HTML string.
     */
    public static String toHtml(NewJson schema) {
        return toHtmlElement(schema).toHtml();
    }

    private static void appendRows(NewJson schema, HtmlElement parent) {
        final List<String> keys = new ArrayList<>(schema.getKeys());
        Collections.sort(keys);

        parent.addChild(buildLeafRow("{", null));
        for (String key : keys) {
            if (schema.getJsonKeys().contains(key)) {
                parent.addChild(buildNestedObjectRow(key, schema.getJson(key)));
            } else {
                parent.addChild(buildLeafRow(key, schema.getString(key)));
            }
        }
        parent.addChild(buildLeafRow("}", null));
    }

    private static Div buildLeafRow(String key, String type) {
        final Div row = new Div();
        row.addStyle("schema-row");

        final Label keyLabel = new Label(key);
        keyLabel.addStyle("schema-key");
        row.addChild(keyLabel);

        if (type != null) {
            final Label typeLabel = new Label(escape(type));
            typeLabel.addStyle("schema-type");
            typeLabel.addStyle(typeClass(type));
            row.addChild(typeLabel);
        }

        return row;
    }

    private static Details buildNestedObjectRow(String key, NewJson nested) {
        final Details details = new Details();
        details.addStyle("schema-row");
        details.addStyle("schema-object");
        details.setSummary(key + " : object");

        final Div nestedContainer = new Div();
        nestedContainer.addStyle("schema-nested");
        appendRows(nested, nestedContainer);

        details.addChild(nestedContainer);
        return details;
    }

    private static String typeClass(String type) {
        if (type == null) {
            return "type-unknown";
        }
        if (type.startsWith("List<")) {
            return "type-list";
        }
        return switch (type) {
            case "String" -> "type-string";
            case "Integer", "Double" -> "type-number";
            case "Boolean" -> "type-bool";
            default -> "type-custom";
        };
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}