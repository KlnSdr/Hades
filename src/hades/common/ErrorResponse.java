package hades.common;

public class ErrorResponse {
    private final String message;

    public ErrorResponse() {
        this("");
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
