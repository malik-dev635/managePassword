package passwordmanager;

import passwordmanager.CurrentUser;

public class CurrentUser {
    private static int id;
    private static String username;
    private static String email;

    public static void set(int id, String username, String email) {
        CurrentUser.id = id;
        CurrentUser.username = username;
        CurrentUser.email = email;
    }
    public static int getId() { return id; }
    public static String getUsername() { return username; }
    public static String getEmail() { return email; }
    public static void clear() {
        id = 0;
        username = null;
        email = null;
    }
} 