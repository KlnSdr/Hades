package hades.user.rest;

import hades.user.User;

import java.util.UUID;

public record GetUserDTO(UUID id, String displayName, String mail) {

    public GetUserDTO() {
        this(null, null, null);
    }

    public static GetUserDTO fromUser(User user) {
        return new GetUserDTO(user.getId(), user.getDisplayName(), user.getMail());
    }
}
