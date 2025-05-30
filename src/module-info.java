module myapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens passwordmanager to javafx.fxml;
    exports passwordmanager;
}
