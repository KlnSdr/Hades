package hades.user.rest;

public record GetTokenResponseDTO(String token) {
    public GetTokenResponseDTO() {
        this(null);
    }

}
