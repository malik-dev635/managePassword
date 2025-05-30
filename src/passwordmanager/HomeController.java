package passwordmanager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class HomeController {

    @FXML
    private Button mainButton;

    @FXML
    private void handleEnter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("pswdManager - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
