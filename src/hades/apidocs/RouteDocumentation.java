package hades.apidocs;

import dobby.io.request.RequestTypes;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;

import java.util.List;

public class RouteDocumentation {
    private ApiDoc apiDoc;
    private ApiResponse[] apiResponses;
    private RequestTypes requestType;
    private List<String> params;
    private boolean isAuthOnly;

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

    public void setApiResponses(ApiResponse[] apiResponses) {
        this.apiResponses = apiResponses;
    }

    public void setAuthOnly(boolean isAuthOnly) {
        this.isAuthOnly = isAuthOnly;
    }

    public RequestTypes getRequestType() {
        return requestType;
    }

    public List<String> getParams() {
        return params;
    }

    public ApiResponse[] getApiResponses() {
        if (apiResponses == null) {
            return new ApiResponse[0];
        }
        return apiResponses;
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

    public boolean isAuthOnly() {
        return isAuthOnly;
    }
}
