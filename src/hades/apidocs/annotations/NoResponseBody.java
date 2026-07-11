package hades.apidocs.annotations;

import dobby.util.json.NewJson;
import thot.janus.DataClass;

public final class NoResponseBody implements DataClass {
    private NoResponseBody() {
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public NewJson toJson() {
        return null;
    }
}
