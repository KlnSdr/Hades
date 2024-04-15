package hades;

import dobby.cookie.Cookie;

public class HadesCookie extends Cookie {
    private String sameSite = "None";
    public HadesCookie(String name, String value) {
        super(name, value);
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    public String getSameSite() {
        return sameSite;
    }

    @Override
    public String toString() {
        return super.toString() + "; SameSite=" + sameSite;
    }
}
