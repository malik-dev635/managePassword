package passwordmanager;

public class PasswordEntry {
    private final String site;
    private final String login;
    private final String password;
    private final String createdAt;
    private boolean showPassword = false;

    public PasswordEntry(String site, String login, String password, String createdAt) {
        this.site = site;
        this.login = login;
        this.password = password;
        this.createdAt = createdAt;
    }
    public String getSite() { return site; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getCreatedAt() { return createdAt; }
    public String getMaskedPassword() {
        return showPassword ? password : "••••••••";
    }
    public void toggleShowPassword() {
        showPassword = !showPassword;
    }
}