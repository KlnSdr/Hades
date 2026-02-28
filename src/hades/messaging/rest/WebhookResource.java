package hades.messaging.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.io.response.ResponseCodes;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.messaging.WebhookConfig;
import hades.messaging.service.WebhookService;
import hades.util.UserUtil;

import java.util.Arrays;
import java.util.UUID;

@RegisterFor(WebhookResource.class)
public class WebhookResource {
    private static final String BASE_PATH = "/rest/webhook";
    private final WebhookService webhookService;

    @Inject
    public WebhookResource(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @AuthorizedOnly
    @Put(BASE_PATH)
    public void createOrUpdateWebhook(HttpContext context) {
        final UUID userId = UserUtil.getCurrentUserId(context);
        final NewJson body = context.getRequest().getBody();
        if (body == null || !body.hasKey("url") || body.getString("url") == null) {
            context.getResponse().setCode(ResponseCodes.BAD_REQUEST);
            return;
        }

        final boolean success = updateWebhook(userId, body.getString("url"));

        if (!success) {
            final NewJson errorResponse = new NewJson();
            errorResponse.setString("error", "Failed to create or update webhook");
            context.getResponse().setBody(errorResponse);
            context.getResponse().setCode(ResponseCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean updateWebhook(UUID owner, String url) {
        final WebhookConfig existingConfig = Arrays.stream(webhookService.findByOwner(owner)).findFirst().orElse(null);

        if (existingConfig != null) {
            existingConfig.setUrl(url);
            return webhookService.update(existingConfig);
        } else {
            final WebhookConfig newConfig = webhookService.create("Default Webhook", url, owner);
            return newConfig != null;
        }
    }
}
