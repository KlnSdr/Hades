package hades.user.rest;

import java.util.List;

public record GetUsersDTO(List<GetUserDTO> users) {
    public GetUsersDTO() {
        this(null);
    }
}
