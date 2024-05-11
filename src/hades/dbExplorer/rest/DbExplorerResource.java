package hades.dbExplorer.rest;

import dobby.annotations.Get;
import dobby.annotations.Post;
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

    @Post(BASE_PATH + "/keys")
    public void getKeys(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if (!body.hasKeys("bucket")) {
            sendMalformedRequest(context);
            return;
        }

        final String bucketName = body.getString("bucket");
        final String[] keys = Connector.getKeys(bucketName);

        final NewJson response = new NewJson();
        response.setList("keys", Arrays.stream(keys).map(key -> (Object) key).collect(Collectors.toList()));

        context.getResponse().setBody(response);
    }

    @Post(BASE_PATH + "/read")
    public void getValue(HttpContext context) {
        final NewJson body = context.getRequest().getBody();
        if(!body.hasKeys("bucket", "key")) {
            sendMalformedRequest(context);
            return;
        }

        final String bucketName = body.getString("bucket");
        final String key = body.getString("key");
        final Object[] value = Connector.readPattern(bucketName, key, Object.class);

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

    private static void sendMalformedRequest(HttpContext context) {
        context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
        final NewJson payload = new NewJson();
        payload.setString("error", "Malformed request");
        context.getResponse().setBody(payload);
    }
}
