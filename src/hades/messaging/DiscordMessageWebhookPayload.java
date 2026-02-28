package hades.messaging;

import dobby.util.json.NewJson;
import hades.user.User;
import hades.user.service.UserService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiscordMessageWebhookPayload implements WebhookPayload {
    private final Message message;
    private final UserService userService;

    public DiscordMessageWebhookPayload(Message message, UserService userService) {
        this.message = message;
        this.userService = userService;
    }

    @Override
    public String getContent() {
        return getJsonContent().toString();
    }

    @Override
    public NewJson getJsonContent() {
        final User sender = userService.find(message.getFrom());
        final NewJson root = new NewJson();

        final NewJson embed = new NewJson();
        embed.setString("title", "Message from " + sender.getDisplayName());

        List<Object> fields = new ArrayList<>();

        final NewJson field1 = new NewJson();
        field1.setString("name", Instant.ofEpochMilli(Long.parseLong(message.getDateSent())).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")));
        field1.setString("value", message.getMessage());
        field1.setBoolean("inline", true);
        fields.add(field1);

        embed.setList("fields", fields);

        final List<Object> embeds = new ArrayList<>();
        embeds.add(embed);
        root.setList("embeds", embeds);

        return root;
    }
}
