package passwordmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordModalController {
    @FXML private Label modalTitle;
    @FXML private TextField siteField;
    @FXML private TextField loginField;
    @FXML private TextField passwordField;
    @FXML private Button generateBtn;
    @FXML private Button showBtn;
    @FXML private Label errorLabel;
    @FXML private DialogPane dialogPane;
    @FXML private VBox passwordOptionsBox;
    @FXML private CheckBox uppercaseCheck;
    @FXML private CheckBox lowercaseCheck;
    @FXML private CheckBox numbersCheck;
    @FXML private CheckBox specialCheck;
    @FXML private Spinner<Integer> lengthSpinner;

    private boolean isEditMode = false;
    private PasswordEntry editingEntry = null;
    private boolean passwordVisible = false;
    private String currentPassword = "";

    @FXML
    public void initialize() {
        // Initialiser le bouton show/hide
        showBtn.setText("👁");
        setupTooltips();
        
        // Ajouter un listener pour mettre à jour currentPassword quand le champ est modifié
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                currentPassword = newVal;
            } else {
                // Si le champ est masqué, on met à jour currentPassword seulement si la longueur change
                if (newVal.length() != currentPassword.length()) {
                    currentPassword = newVal;
                }
            }
        });
    }

    private void setupDialogValidation() {
        if (dialogPane != null) {
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setOnAction(event -> {
                    if (!validateAndGetResult()) {
                        event.consume();
                    }
                });
            }
        }
    }

    private void setupTooltips() {
        generateBtn.setTooltip(new Tooltip("Générer un mot de passe sécurisé"));
        showBtn.setTooltip(new Tooltip("Afficher/Masquer le mot de passe"));
    }

    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        setupDialogValidation();
    }

    public void setEditMode(PasswordEntry entry) {
        isEditMode = true;
        editingEntry = entry;
        modalTitle.setText("Modifier le mot de passe");
        siteField.setText(entry.getSite());
        loginField.setText(entry.getLogin());
        currentPassword = entry.getPassword();
        updatePasswordDisplay();
        showBtn.setText("👁");
        passwordVisible = false;
        errorLabel.setText("");
    }

    @FXML
    private void handleGenerate() {
        // Vérifier qu'au moins une option est sélectionnée
        if (!uppercaseCheck.isSelected() && !lowercaseCheck.isSelected() && 
            !numbersCheck.isSelected() && !specialCheck.isSelected()) {
            errorLabel.setText("Sélectionnez au moins un type de caractères.");
            return;
        }

        currentPassword = generateStrongPassword(lengthSpinner.getValue());
        updatePasswordDisplay();
    }

    private void updatePasswordDisplay() {
        if (passwordVisible) {
            passwordField.setText(currentPassword);
        } else {
            passwordField.setText("•".repeat(currentPassword.length()));
        }
    }

    @FXML
    private void handleShowHide() {
        passwordVisible = !passwordVisible;
        updatePasswordDisplay();
        showBtn.setText(passwordVisible ? "🙈" : "👁");
    }

    public boolean validateAndGetResult() {
        errorLabel.setText(""); // Clear previous error

        String site = cleanInput(siteField.getText());
        String login = cleanInput(loginField.getText());
        String password = cleanInput(currentPassword);

        if (site.isEmpty()) {
            errorLabel.setText("Le champ 'Site' est requis.");
            return false;
        }
        if (login.isEmpty()) {
            errorLabel.setText("Le champ 'Identifiant' est requis.");
            return false;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Le champ 'Mot de passe' est requis.");
            return false;
        }

        return true;
    }

    public String getSite() {
        return cleanInput(siteField.getText());
    }

    public String getLogin() {
        return cleanInput(loginField.getText());
    }

    public String getPassword() {
        return cleanInput(currentPassword);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public PasswordEntry getEditingEntry() {
        return editingEntry;
    }

    public void resetFields() {
        siteField.clear();
        loginField.clear();
        passwordField.clear();
        errorLabel.setText("");
        modalTitle.setText("Ajouter un mot de passe");
        isEditMode = false;
        editingEntry = null;
        passwordVisible = false;
        currentPassword = "";
        showBtn.setText("👁");
    }

    private String cleanInput(String input) {
        if (input == null) return "";
        return input.replaceAll("\\p{C}", "").trim();
    }

    private String generateStrongPassword(int length) {
        StringBuilder allChars = new StringBuilder();
        if (uppercaseCheck.isSelected()) allChars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (lowercaseCheck.isSelected()) allChars.append("abcdefghijklmnopqrstuvwxyz");
        if (numbersCheck.isSelected()) allChars.append("0123456789");
        if (specialCheck.isSelected()) allChars.append("!@#$%^&*()_+-=");

        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // Garantir au moins un caractère de chaque type sélectionné
        if (uppercaseCheck.isSelected()) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(rnd.nextInt(26)));
        }
        if (lowercaseCheck.isSelected()) {
            sb.append("abcdefghijklmnopqrstuvwxyz".charAt(rnd.nextInt(26)));
        }
        if (numbersCheck.isSelected()) {
            sb.append("0123456789".charAt(rnd.nextInt(10)));
        }
        if (specialCheck.isSelected()) {
            sb.append("!@#$%^&*()_+-=".charAt(rnd.nextInt(14)));
        }

        // Remplir le reste
        int remainingLength = length - sb.length();
        for (int i = 0; i < remainingLength; i++) {
            sb.append(allChars.charAt(rnd.nextInt(allChars.length())));
        }

        return shuffleString(sb.toString(), rnd);
    }

    private String shuffleString(String input, SecureRandom rnd) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) characters.add(c);
        Collections.shuffle(characters, rnd);
        StringBuilder output = new StringBuilder(input.length());
        for (char c : characters) output.append(c);
        return output.toString();
    }
}
