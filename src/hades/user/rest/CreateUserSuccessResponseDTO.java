package hades.user.rest;

public record CreateUserSuccessResponseDTO(GetUserDTO user, String redirectTo) {

    public CreateUserSuccessResponseDTO() {
        this(null, null);
    }
}
