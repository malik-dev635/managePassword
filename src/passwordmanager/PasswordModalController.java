package passwordmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import java.security.SecureRandom;
import passwordmanager.PasswordEntry;

public class PasswordModalController {
    @FXML private Label modalTitle;
    @FXML private TextField siteField;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button generateBtn;
    @FXML private Button showBtn;
    @FXML private Label errorLabel;
    @FXML private DialogPane dialogPane;

    private boolean isEdit = false;
    private PasswordEntry entryToEdit;
    private boolean passwordVisible = false;
    private TextField passwordVisibleField = new TextField();

    @FXML
    public void initialize() {
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.managedProperty().bind(showBtn.pressedProperty());
        passwordVisibleField.visibleProperty().bind(showBtn.pressedProperty());
        passwordVisibleField.getStyleClass().addAll(passwordField.getStyleClass());
        passwordVisibleField.setPrefWidth(passwordField.getPrefWidth());
        passwordVisibleField.setPromptText(passwordField.getPromptText());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        ((HBox) passwordField.getParent()).getChildren().add(1, passwordVisibleField);
    }

    public void setEditMode(PasswordEntry entry) {
        isEdit = true;
        entryToEdit = entry;
        modalTitle.setText("Modifier le mot de passe");
        siteField.setText(entry.getSite());
        loginField.setText(entry.getLogin());
        passwordField.setText(entry.getPassword());
    }

    @FXML
    private void handleGenerate() {
        passwordField.setText(generateStrongPassword(12));
    }

    @FXML
    private void handleShowHide() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
            showBtn.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            showBtn.setText("👁");
        }
    }

    public boolean validateAndGetResult() {
        errorLabel.setText("");
        if (siteField.getText().trim().isEmpty() || loginField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
            errorLabel.setText("Tous les champs sont obligatoires.");
            return false;
        }
        return true;
    }

    public String getSite() { return siteField.getText().trim(); }
    public String getLogin() { return loginField.getText().trim(); }
    public String getPassword() { return passwordField.getText().trim(); }

    private String generateStrongPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
