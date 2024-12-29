package hades.apidocs;

import dobby.io.request.RequestTypes;
import hades.apidocs.annotations.ApiDoc;

import java.util.List;

public class RouteDocumentation {
    private ApiDoc apiDoc;
    private RequestTypes requestType;
    private List<String> params;

    public RouteDocumentation() {
    }

    public void setApiDoc(ApiDoc apiDoc) {
        this.apiDoc = apiDoc;
    }

    public void setRequestType(RequestTypes requestType) {
        this.requestType = requestType;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public RequestTypes getRequestType() {
        return requestType;
    }

    public List<String> getParams() {
        return params;
    }

    public String getSummary() {
        return apiDoc != null ? apiDoc.summary() : "";
    }

    public String getDescription() {
        return apiDoc != null ? apiDoc.description() : "";
    }

    public String getBaseUrl() {
        return apiDoc != null ? apiDoc.baseUrl() : "";
    }
}
