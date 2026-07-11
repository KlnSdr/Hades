package hades.apidocs.data;

import dobby.util.json.NewJson;

import java.util.ArrayList;
import java.util.List;

public class ApiRoute {
    private String path;
    private String method;
    private String summary;
    private String description;
    private List<ApiResponse> responses;

    public ApiRoute() {
        responses = new ArrayList<>();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ApiResponse> getResponses() {
        return responses;
    }

    public void addResponse(ApiResponse response) {
        this.responses.add(response);
    }

    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("path", path);
        json.setString("method", method);
        json.setString("summary", summary);
        json.setString("description", description);

        final List<Object> responseList = new ArrayList<>();
        for (ApiResponse response : responses) {
            responseList.add(response.toJson());
        }
        json.setList("responses", responseList);

        return json;
    }
}
