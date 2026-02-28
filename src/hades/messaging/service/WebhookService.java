package hades.messaging.service;

import common.http.Client;
import common.http.HttpResponse;
import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.messaging.WebhookConfig;
import hades.messaging.WebhookPayload;
import thot.connector.IConnector;
import thot.janus.Janus;

import java.util.UUID;

@RegisterFor(WebhookService.class)
public class WebhookService {
    public static final String BUCKET_NAME = "hades_webhooks";
    private static final Logger LOGGER = new Logger(WebhookService.class);
    private final IConnector connector;

    @Inject
    public WebhookService(IConnector connector) {
        this.connector = connector;
    }

    public WebhookConfig find(UUID owner, UUID id) {
        final String key = owner + "_" + id;
        return Janus.parse(connector.read(BUCKET_NAME, key, NewJson.class), WebhookConfig.class);
    }

    public WebhookConfig create(String name, String url, UUID owner) {
        final WebhookConfig config = new WebhookConfig();
        config.setName(name);
        config.setUrl(url);
        config.setOwner(owner);

        final boolean success = connector.write(BUCKET_NAME, config.getKey(), config.toJson());

        if (!success) {
            return null;
        }

        return config;
    }

    public boolean update(WebhookConfig config) {
        return connector.write(BUCKET_NAME, config.getKey(), config.toJson());
    }

    public WebhookConfig[] findByOwner(UUID owner) {
        final NewJson[] jsons = connector.readPattern(BUCKET_NAME, owner.toString() + "_.*", NewJson.class);
        final WebhookConfig[] configs = new WebhookConfig[jsons.length];
        for (int i = 0; i < jsons.length; i++) {
            configs[i] = Janus.parse(jsons[i], WebhookConfig.class);
        }
        return configs;
    }

    public boolean delete(WebhookConfig config) {
        return connector.delete(BUCKET_NAME, config.getKey());
    }

    public boolean publishWebhook(WebhookConfig config, WebhookPayload payload) {
        if (config == null || payload == null) {
            LOGGER.warn("Cannot publish webhook: config or payload is null");
            return false;
        }

        if (config.getUrl() == null || config.getUrl().isEmpty()) {
            return true;
        }

        final Client client = new Client();
        final HttpResponse response = client.post(config.getUrl(), payload.getJsonContent());
        if (response.getResponseCode() != ResponseCodes.NO_CONTENT) {
            LOGGER.warn("Failed to send webhook to " + config.getUrl() + ". Response code: " + response.getResponseCode());
        }
        return response.getResponseCode() == ResponseCodes.NO_CONTENT;
    }
}
