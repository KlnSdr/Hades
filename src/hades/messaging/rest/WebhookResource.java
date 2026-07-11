package hades.messaging.rest;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import dobby.annotations.Put;
import dobby.io.HttpContext;
import dobby.util.json.NewJson;
import hades.annotations.AuthorizedOnly;
import hades.apidocs.annotations.ApiDoc;
import hades.apidocs.annotations.ApiResponse;
import hades.common.ErrorResponse;
import hades.messaging.WebhookConfig;
import hades.messaging.service.WebhookService;
import hades.util.UserUtil;

import java.util.Arrays;
import java.util.UUID;

import static hades.common.ErrorResponses.badRequest;
import static hades.common.ErrorResponses.internalError;

@RegisterFor(WebhookResource.class)
public class WebhookResource {
    private static final String BASE_PATH = "/rest/webhook";
    private final WebhookService webhookService;

    @Inject
    public WebhookResource(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @ApiDoc(
            summary = "Create or update a webhook",
            description = "Create or update a webhook for the current user",
            baseUrl = BASE_PATH
    )
    @ApiResponse(
            code = 200,
            message = "Webhook created or updated successfully"
    )
    @ApiResponse(
            code = 400,
            message = "Invalid request body",
            responseBody = ErrorResponse.class
    )
    @ApiResponse(
            code = 500,
            message = "Failed to create or update webhook",
            responseBody = ErrorResponse.class
    )
    @AuthorizedOnly
    @Put(BASE_PATH)
    public void createOrUpdateWebhook(HttpContext context) {
        final UUID userId = UserUtil.getCurrentUserId(context);
        final NewJson body = context.getRequest().getBody();
        if (body == null || !body.hasKey("url") || body.getString("url") == null) {
            badRequest(context.getResponse(), "Missing or invalid 'url' in request body");
            return;
        }

        final boolean success = updateWebhook(userId, body.getString("url"));

        if (!success) {
            internalError(context.getResponse(), "Failed to create or update webhook");
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
