package passwordmanager;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Connexion réussie !");
                conn.close();
            } else {
                System.out.println("Connexion échouée.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
