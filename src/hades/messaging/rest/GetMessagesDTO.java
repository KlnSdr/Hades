package hades.messaging.rest;

import hades.messaging.Message;

import java.util.List;

public record GetMessagesDTO(List<Message> messages) {
    public GetMessagesDTO() {
        this(List.of());
    }

}
