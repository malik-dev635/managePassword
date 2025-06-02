module myapp {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires java.sql;
    
    opens passwordmanager to javafx.fxml;
    exports passwordmanager;
}
