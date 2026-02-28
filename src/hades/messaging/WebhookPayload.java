package hades.messaging;

import dobby.util.json.NewJson;

public interface WebhookPayload {
    String getContent();
    NewJson getJsonContent();
}
