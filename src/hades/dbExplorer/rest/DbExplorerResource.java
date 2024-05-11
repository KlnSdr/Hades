package hades.dbExplorer.rest;

import dobby.annotations.Get;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.Json;
import dobby.util.json.NewJson;
import thot.connector.Connector;

import java.util.Arrays;
import java.util.stream.Collectors;

import static hades.common.Util.convert;

public class DbExplorerResource {
    private static final String BASE_PATH = "/dbExplorer";

    @Get(BASE_PATH + "/buckets")
    public void getBuckets(HttpContext context) {
        final String[] bucketNames = Connector.getBuckets();


        final NewJson response = new NewJson();
        response.setList("buckets", Arrays.stream(bucketNames).map(bucketName -> (Object) bucketName).collect(Collectors.toList()));

        context.getResponse().setBody(response);
    }

    @Get(BASE_PATH + "/{bucketName}/keys")
    public void getKeys(HttpContext context) {
        final String bucketName = context.getRequest().getParam("bucketName");
        final String[] keys = Connector.getKeys(bucketName);

        final NewJson response = new NewJson();
        response.setList("keys", Arrays.stream(keys).map(key -> (Object) key).collect(Collectors.toList()));

        context.getResponse().setBody(response);
    }

    @Get(BASE_PATH + "/{bucketName}/key/{key}")
    public void getValue(HttpContext context) {
        final String bucketName = context.getRequest().getParam("bucketName");
        final String key = context.getRequest().getParam("key");
        final Object[] value = Connector.readPattern(bucketName, "(?i)" + key, Object.class);

        if (value == null || value.length == 0) {
            context.getResponse().setCode(ResponseCodes.NOT_FOUND);
            return;
        }

        final Object valueObj = value[0];

        final String type = valueObj.getClass().getName();

        final NewJson response = new NewJson();
        response.setString("type", type);

        switch (type) {
            case "java.lang.String":
                response.setString("value", (String) valueObj);
                break;
            case "java.lang.Integer":
                response.setInt("value", (Integer) valueObj);
                break;
            case "java.lang.Double":
                response.setFloat("value", (Double) valueObj);
                break;
            case "java.lang.Boolean":
                response.setBoolean("value", (Boolean) valueObj);
                break;
            case "dobby.util.json.NewJson":
                response.setJson("value", (NewJson) valueObj);
                break;
            case "dobby.util.Json":
                response.setJson("value", convert((Json) valueObj));
                break;
            default:
                response.setString("value", valueObj.toString());
                break;
        }

        context.getResponse().setBody(response);
    }
}
