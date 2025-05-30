package passwordmanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {

    @FXML private VBox loginView;
    @FXML private VBox registerView;
    
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    @FXML private TextField registerEmail;
    @FXML private TextField registerUsername;
    @FXML private PasswordField registerPassword;
    @FXML private Label registerMessage;

    @FXML
    private void showLoginView() {
        loginView.setVisible(true);
        registerView.setVisible(false);
        clearLoginFields();
    }

    @FXML
    private void showRegisterView() {
        loginView.setVisible(false);
        registerView.setVisible(true);
        clearRegisterFields();
    }

    private void clearLoginFields() {
        loginUsername.clear();
        loginPassword.clear();
        loginMessage.setText("");
    }

    private void clearRegisterFields() {
        registerEmail.clear();
        registerUsername.clear();
        registerPassword.clear();
        registerMessage.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessage.setText("Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, password, email FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        loginMessage.setText("Connexion réussie !");
                        // Stocker l'utilisateur connecté
                        int userId = rs.getInt("id");
                        String email = rs.getString("email");
                        CurrentUser.set(userId, username, email);
                        loadMainView();
                    } else {
                        loginMessage.setText("Mot de passe incorrect.");
                    }
                } else {
                    loginMessage.setText("Utilisateur non trouvé.");
                }
            }
        } catch (SQLException e) {
            loginMessage.setText("Erreur lors de la connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String email = registerEmail.getText().trim();
        String username = registerUsername.getText().trim();
        String password = registerPassword.getText().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            registerMessage.setText("Tous les champs sont obligatoires.");
            return;
        }

        if (!isValidEmail(email)) {
            registerMessage.setText("Format d'email invalide.");
            return;
        }

        if (password.length() < 6) {
            registerMessage.setText("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT id FROM users WHERE username = ? OR email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    registerMessage.setText("Nom d'utilisateur ou email déjà utilisé.");
                } else {
                    String insertSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.setString(3, email);
                        insertStmt.executeUpdate();
                        registerMessage.setText("Compte créé avec succès !");
                        // Retour à la vue de connexion après 2 secondes
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                javafx.application.Platform.runLater(this::showLoginView);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            }
        } catch (SQLException e) {
            registerMessage.setText("Erreur lors de l'inscription.");
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginView.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setTitle("pswdManager - Tableau de bord");
            stage.show();
        } catch (Exception e) {
            loginMessage.setText("Erreur lors du chargement de la page principale.");
            e.printStackTrace();
        }
    }
}
