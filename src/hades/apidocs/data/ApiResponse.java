package hades.apidocs.data;

import dobby.util.json.NewJson;

public class ApiResponse {
    private int statusCode;
    private String description;
    private String schema;

    public ApiResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setInt("statusCode", statusCode);
        json.setString("description", description);
        json.setString("schema", schema);
        return json;
    }
}
