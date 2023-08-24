/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import rs.etf.sab.operations.ArticleOperations;

/**
 *
 * @author Korisnik
 */
public class article implements ArticleOperations {

    public article() {
    }

    private final Connection connection = DB.getInstance().getConnection();
    
    @Override
    public int createArticle(int shopId, String articleName, int articlePrice) {
        try {
            // Proveri postojanje prodavnice s zadanim ID-om
            if (!shopExists(shopId)) {
                return -1; // Ako ne postoji prodavnica s zadanim ID-om, vrati -1
            }

            // Kreiraj novi artikal
            String insertQuery = "INSERT INTO Artikal (Naziv, Cena, NaStanju, IdP) VALUES (?, ?, 0, ?)";
            PreparedStatement statement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, articleName);
            statement.setInt(2, articlePrice);
            statement.setInt(3, shopId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Dohvati generisani ID artikla
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Vrati ID novog artikla
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Ako se dogodila greška ili nije kreiran artikal, vrati -1
    }

    private boolean shopExists(int shopId) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM Prodavnica WHERE IdP = ?";
        PreparedStatement statement = connection.prepareStatement(selectQuery);
        statement.setInt(1, shopId);
        int count = 0;
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        }

        return count > 0; // Vrati true ako postoji prodavnica s zadanim ID-om
    }
    
}
