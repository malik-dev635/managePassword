module myapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    opens passwordmanager to javafx.fxml;
    exports passwordmanager;
}
