package hades.user.rest;

import dobby.io.request.Request;

public class LoginUserDTO {
    private String displayName;
    private String password;

    public LoginUserDTO() {
    }

    public LoginUserDTO(String displayName, String password) {
        this.displayName = displayName;
        this.password = password;
    }

    public static LoginUserDTO fromRequest(Request request) {
        final String displayName = request.getBody().getString("displayName");
        final String password = request.getBody().getString("password");

        return new LoginUserDTO(displayName, password);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
