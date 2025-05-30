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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;

public class MainController {
    @FXML private TableView<PasswordEntry> passwordTable;
    @FXML private TableColumn<PasswordEntry, String> siteColumn;
    @FXML private TableColumn<PasswordEntry, String> loginColumn;
    @FXML private TableColumn<PasswordEntry, String> passwordColumn;
    @FXML private TableColumn<PasswordEntry, String> createdColumn;
    @FXML private TableColumn<PasswordEntry, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Label userInfoLabel;

    private ObservableList<PasswordEntry> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Définir les colonnes
        siteColumn.setCellValueFactory(new PropertyValueFactory<>("site"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("maskedPassword"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Responsive : colonnes qui s'ajustent à la largeur de la table
        passwordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Actions (modifier, supprimer, copier, afficher)
        actionsColumn.setCellFactory(getActionsCellFactory());

        // Charger les mots de passe depuis la base
        loadPasswordsFromDatabase();

        // Afficher l'info utilisateur
        if (userInfoLabel != null) {
            userInfoLabel.setText("Connecté en tant que : " + CurrentUser.getUsername());
        }

        // Recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTable(newVal));
    }

    private void loadPasswordsFromDatabase() {
        masterData.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT site, login, mot_de_passe, created_at FROM passwords WHERE utilisateur_id = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, CurrentUser.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    masterData.add(new PasswordEntry(
                        rs.getString("site"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            // Afficher une erreur dans l'UI si besoin
            e.printStackTrace();
        }
        passwordTable.setItems(masterData);
    }

    private void filterTable(String filter) {
        if (filter == null || filter.isEmpty()) {
            passwordTable.setItems(masterData);
        } else {
            ObservableList<PasswordEntry> filtered = FXCollections.observableArrayList();
            for (PasswordEntry entry : masterData) {
                if (entry.getSite().toLowerCase().contains(filter.toLowerCase()) ||
                    entry.getLogin().toLowerCase().contains(filter.toLowerCase())) {
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
                showBtn.setOnAction(e -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    entry.toggleShowPassword();
                    passwordTable.refresh();
                });
                copyBtn.setOnAction(e -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(entry.getPassword());
                    clipboard.setContent(content);
                });
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
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
            Parent root = loader.load();
            PasswordModalController modalController = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) root);
            dialog.setTitle("Ajouter un mot de passe");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(passwordTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (modalController.validateAndGetResult()) {
                    // TODO: Enregistrer le nouveau mot de passe dans la BDD
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
                                // TODO: Afficher une erreur à l'utilisateur
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // TODO: Afficher une erreur à l'utilisateur
                    }

                    // Après enregistrement, rafraîchir la table
                    loadPasswordsFromDatabase();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Afficher une erreur à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la fenêtre d'ajout.");
            alert.setContentText("Veuillez réessayer.");
            alert.showAndWait();
        }
    }

    private void handleEdit(PasswordEntry entry) {
        // TODO: ouvrir la fenêtre/modale d'édition (utilisera la même modale avec setEditMode)
    }
    private void handleDelete(PasswordEntry entry) {
        // TODO: confirmation puis suppression
    }
} 