package hades.apidocs.data;

import dobby.util.json.NewJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiDocumentation {
    private final List<ApiRoute> routes;
    private final Map<String, NewJson> schemas;
    private String title;
    private String version;

    public ApiDocumentation() {
        routes = new ArrayList<>();
        schemas = new HashMap<>();
    }

    public Map<String, NewJson> getSchemas() {
        return schemas;
    }

    public List<ApiRoute> getRoutes() {
        return routes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addRoute(ApiRoute route) {
        this.routes.add(route);
    }

    public void addSchema(String name, NewJson schema) {
        this.schemas.put(name, schema);
    }

    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setString("title", title);
        json.setString("version", version);
        json.setList("routes", routes.stream().map(r -> (Object) r.toJson()).toList());
        final NewJson schemasJson = new NewJson();
        for (Map.Entry<String, NewJson> entry : schemas.entrySet()) {
            schemasJson.setJson(entry.getKey(), entry.getValue());
        }
        json.setJson("schemas", schemasJson);

        return json;
    }
}
