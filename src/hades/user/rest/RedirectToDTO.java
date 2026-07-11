package hades.user.rest;

public record RedirectToDTO(String redirectTo) {
    public RedirectToDTO() {
        this(null);
    }

}
