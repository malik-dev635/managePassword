package passwordmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import java.sql.*;
import passwordmanager.CurrentUser;
import passwordmanager.PasswordEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.scene.layout.BorderPane;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<PasswordEntry> passwordTable;
    @FXML private TableColumn<PasswordEntry, String> siteColumn;
    @FXML private TableColumn<PasswordEntry, String> loginColumn;
    @FXML private TableColumn<PasswordEntry, String> passwordColumn;
    @FXML private TableColumn<PasswordEntry, String> createdColumn;
    @FXML private TableColumn<PasswordEntry, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label userInfoLabel;
    @FXML private Button addButton;
    @FXML private Button fullscreenButton;

    // Liste maître contenant toutes les entrées de mot de passe
    private ObservableList<PasswordEntry> masterData = FXCollections.observableArrayList();
    private FilteredList<PasswordEntry> filteredData;
    private boolean isFullscreen = false;

    @FXML
    public void initialize() {
        // Initialisation des colonnes avec les propriétés de PasswordEntry
        siteColumn.setCellValueFactory(new PropertyValueFactory<>("site"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        // Affiche mot de passe masqué ou visible selon l'état dans PasswordEntry
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("maskedPassword"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Politique de redimensionnement pour occuper toute la largeur disponible
        passwordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ajout des boutons d'action dans la colonne dédiée
        actionsColumn.setCellFactory(getActionsCellFactory());

        // Chargement initial des mots de passe depuis la base de données
        loadPasswordsFromDatabase();

        // Affichage info utilisateur connecté
        if (userInfoLabel != null) {
            userInfoLabel.setText("Connecté en tant que : " + CurrentUser.getUsername());
        }

        // Mise en place de la recherche dynamique sur site et login
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));

        setupFullscreenButton();
    }

    private void setupFullscreenButton() {
        fullscreenButton.setText("⛶");
        fullscreenButton.setTooltip(new Tooltip("Mode plein écran"));
        fullscreenButton.setOnAction(e -> toggleFullscreen());
    }

    private void toggleFullscreen() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        isFullscreen = !isFullscreen;
        
        if (isFullscreen) {
            stage.setFullScreen(true);
            fullscreenButton.setText("⮽");
            fullscreenButton.setTooltip(new Tooltip("Quitter le mode plein écran"));
        } else {
            stage.setFullScreen(false);
            fullscreenButton.setText("⛶");
            fullscreenButton.setTooltip(new Tooltip("Mode plein écran"));
        }
    }

    private void loadPasswordsFromDatabase() {
        masterData.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT site, login, mot_de_passe, created_at FROM passwords WHERE utilisateur_id = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, CurrentUser.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    PasswordEntry entry = new PasswordEntry(
                        rs.getString("site"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        rs.getString("created_at")
                    );
                    masterData.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
           
        }
        // Actualiser les données visibles dans la table
        passwordTable.setItems(masterData);
    }

    private void filterTable(String filter) {
        if (filter == null || filter.isEmpty()) {
            passwordTable.setItems(masterData);
        } else {
            String lowerFilter = filter.toLowerCase();
            ObservableList<PasswordEntry> filtered = FXCollections.observableArrayList();
            for (PasswordEntry entry : masterData) {
                if (entry.getSite().toLowerCase().contains(lowerFilter) ||
                    entry.getLogin().toLowerCase().contains(lowerFilter)) {
                    filtered.add(entry);
                }
            }
            passwordTable.setItems(filtered);
        }
    }

    private Callback<TableColumn<PasswordEntry, Void>, TableCell<PasswordEntry, Void>> getActionsCellFactory() {
        return col -> new TableCell<>() {
            private final Button showBtn = new Button("👁");
            private final Button copyBtn = new Button("📋");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");

            {
                // Ajout des tooltips pour chaque bouton
                showBtn.setTooltip(new Tooltip("Afficher/Masquer"));
                copyBtn.setTooltip(new Tooltip("Copier"));
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

                // Affiche / masque le mot de passe dans la table
                showBtn.setOnAction(e -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    entry.toggleShowPassword();
                   
                    passwordTable.refresh();
                });

                // Copie le mot de passe dans le presse-papier système
                copyBtn.setOnAction(e -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(entry.getPassword());
                    clipboard.setContent(content);
                    
                    // Afficher une notification de confirmation
                    copyBtn.setText("✓");
                    copyBtn.setStyle("-fx-text-fill: #00ff00;");
                    
                    // Restaurer le bouton après 1 seconde
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            javafx.application.Platform.runLater(() -> {
                                copyBtn.setText("📋");
                                copyBtn.setStyle("");
                            });
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                });

                // Ouvre la fenêtre de modification
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                // Supprime l'entrée après confirmation
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                // Ajout d'une classe CSS commune pour le style
                showBtn.getStyleClass().add("action-btn");
                copyBtn.getStyleClass().add("action-btn");
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("action-btn");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(4, showBtn, copyBtn, editBtn, deleteBtn);
                    setGraphic(box);
                }
            }
        };
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PasswordModal.fxml"));
            DialogPane dialogPane = loader.load();
            PasswordModalController modalController = loader.getController();
            modalController.setDialogPane(dialogPane);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter un mot de passe");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(passwordTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
                if (modalController.validateAndGetResult()) {
                    String site = modalController.getSite();
                    String login = modalController.getLogin();
                    String password = modalController.getPassword();

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String insertSql = "INSERT INTO passwords (utilisateur_id, site, login, mot_de_passe) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, CurrentUser.getId());
                            insertStmt.setString(2, site);
                            insertStmt.setString(3, login);
                            insertStmt.setString(4, password);
                            int rowsAffected = insertStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Mot de passe enregistré avec succès !");
                            } else {
                                System.out.println("Échec de l'enregistrement du mot de passe.");
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    loadPasswordsFromDatabase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre d'ajout.");
            alert.setContentText("Veuillez réessayer.");
            alert.showAndWait();
        }
    }

    private void handleEdit(PasswordEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PasswordModal.fxml"));
            DialogPane dialogPane = loader.load();
            PasswordModalController modalController = loader.getController();

            modalController.setEditMode(entry);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier le mot de passe");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(passwordTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonData.OK_DONE) {
                if (modalController.validateAndGetResult()) {
                    String site = modalController.getSite();
                    String login = modalController.getLogin();
                    String password = modalController.getPassword();

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        // Attention : on identifie la ligne par utilisateur + site + login d'origine (avant modification)
                        String updateSql = "UPDATE passwords SET site = ?, login = ?, mot_de_passe = ? WHERE utilisateur_id = ? AND site = ? AND login = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, site);
                            updateStmt.setString(2, login);
                            updateStmt.setString(3, password);
                            updateStmt.setInt(4, CurrentUser.getId());
                            updateStmt.setString(5, entry.getSite());
                            updateStmt.setString(6, entry.getLogin());

                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Mot de passe modifié avec succès !");
                            } else {
                                System.out.println("Échec de la modification du mot de passe.");
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // Possibilité d'alerte utilisateur ici aussi
                    }

                    loadPasswordsFromDatabase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre de modification.");
            alert.setContentText("Veuillez réessayer.");
            alert.showAndWait();
        }
    }

    private void handleDelete(PasswordEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le mot de passe");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le mot de passe pour " + entry.getSite() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String deleteSql = "DELETE FROM passwords WHERE utilisateur_id = ? AND site = ? AND login = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, CurrentUser.getId());
                    deleteStmt.setString(2, entry.getSite());
                    deleteStmt.setString(3, entry.getLogin());
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Mot de passe supprimé avec succès !");
                    } else {
                        System.out.println("Échec de la suppression du mot de passe.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Alerte possible
            }

            loadPasswordsFromDatabase();
        }
    }
}
