package hades.user.rest;

import dobby.io.request.Request;
import hades.user.User;

public class CreateUserDTO {
    private String displayName;
    private String mail;
    private String password;
    private String passwordRepeat;

    public CreateUserDTO() {
    }

    public CreateUserDTO(String displayName, String mail, String password, String passwordRepeat) {
        this.displayName = displayName;
        this.mail = mail;
        this.password = password;
        this.passwordRepeat = passwordRepeat;
    }

    public static CreateUserDTO fromRequest(Request request) {
        final String displayName = request.getBody().getString("displayName").toLowerCase();
        final String mail = request.getBody().getString("mail");
        final String password = request.getBody().getString("password");
        final String passwordRepeat = request.getBody().getString("passwordRepeat");

        return new CreateUserDTO(displayName, mail, password, passwordRepeat);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * This method is used to convert CreateUserDTO object to User object.
     * It creates a new User object and sets the displayName, mail, and password from the CreateUserDTO object.
     * Note: The password is set as unhashed.
     *
     * @return User object with the details set from CreateUserDTO object.
     */
    public User toUser() {
        final User user = new User();
        user.setDisplayName(displayName);
        user.setMail(mail);
        user.setPassword(password); // unhashed

        return user;
    }
}
