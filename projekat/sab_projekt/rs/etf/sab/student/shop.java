/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rs.etf.sab.operations.ShopOperations;

/**
 *
 * @author Korisnik
 */
public class shop implements ShopOperations {

    public shop() {
    }

    private final Connection connection = DB.getInstance().getConnection();
     
   @Override
    public int createShop(String name, String cityName) {
        try {
            // Provjeri postojanje prodavnice s istim imenom
            if (shopExists(name)) {
                return -1; // Ako postoji prodavnica s istim imenom, vrati -1
            }

            // Umetanje nove prodavnice
            String insertQuery = "INSERT INTO Prodavnica (Naziv, Prihod, IdG) VALUES (?, 0, (SELECT IdG FROM Grad WHERE Naziv = ?))";
            PreparedStatement statement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, cityName);
            int rowsAffected = statement.executeUpdate();

            // Dohvati generirani ID prodavnice
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Vrati ID prodavnice
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Ako se dogodila greška, vrati -1
    }

    private boolean shopExists(String name) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM Prodavnica WHERE Naziv = ?";
        PreparedStatement statement = connection.prepareStatement(selectQuery);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            return count > 0; // Vrati true ako postoji prodavnica s istim imenom
        }

        return false;
    }


    @Override
    public int setCity(int shopId, String cityName) {
        try {
            // Provjeri postojanje prodavnice s zadanim ID-om
            if (!shopExists(shopId)) {
                return -1; // Ako ne postoji prodavnica s zadanim ID-om, vrati -1
            }

            // Ažuriraj grad prodavnice
            String updateQuery = "UPDATE Prodavnica SET IdG = (SELECT IdG FROM Grad WHERE Naziv = ?) WHERE IdP = ?";
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setString(1, cityName);
            statement.setInt(2, shopId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return 1; // Uspješno ažuriranje grada prodavnice
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Ako se dogodila greška ili nije bilo ažuriranja, vrati -1
    }

    private boolean shopExists(int shopId) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM Prodavnica WHERE IdP = ?";
        PreparedStatement statement = connection.prepareStatement(selectQuery);
        statement.setInt(1, shopId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            return count > 0; // Vrati true ako postoji prodavnica s zadanim ID-om
        }

        return false;
    }

    @Override
    public int getCity(int shopId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        try {
            // Provjeri postojanje prodavnice s zadanim ID-om
            if (!shopExists(shopId)) {
                return -1; // Ako ne postoji prodavnica s zadanim ID-om, vrati -1
            }

            // Dohvati ID grada za prodavnicu
            String selectQuery = "SELECT IdG FROM Prodavnica WHERE IdP = ?";
            PreparedStatement statement = connection.prepareStatement(selectQuery);
            statement.setInt(1, shopId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("IdG"); // Vrati ID grada
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Ako se dogodila greška ili nije pronađen ID grada, vrati -1
    
    }

    @Override
    public int setDiscount(int shopId, int discountPercentage) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        try {
            // Proveri postojanje prodavnice s zadanim ID-om
            if (!shopExists(shopId)) {
                return -1; // Ako ne postoji prodavnica s zadanim ID-om, vrati -1
            }

            // Postavi popust za prodavnicu
            String updateQuery = "INSERT INTO Popust(Vrednost,IdP) VALUES(?,?)";
            PreparedStatement statement = connection.prepareStatement(updateQuery);
            statement.setInt(1, discountPercentage);
            statement.setInt(2, shopId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return 1; // Ako je uspješno ažuriran popust, vrati 1
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Ako se dogodila greška ili nije ažuriran popust, vrati -1
    }

    @Override
public int increaseArticleCount(int articleId, int increment) {
    try {
        String query = "UPDATE Artikal SET NaStanju = NaStanju + ? WHERE IdA = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, increment);
        statement.setInt(2, articleId);

        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            // Vraćamo broj artikala nakon povećanja
            return getArticleCount(articleId);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; // Ako ne uspe povećanje broja artikala, vraćamo -1
}

// Metoda koja vraća broj artikala za dati articleId
public int getArticleCount(int articleId) {
    try {
        String query = "SELECT NaStanju FROM Artikal WHERE IdA = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, articleId);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("NaStanju");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1; // Ako ne uspe dobijanje broja artikala, vraćamo -1
}

    @Override
public List<Integer> getArticles(int shopId) {
    List<Integer> articleIds = new ArrayList<>();
    try {
        String query = "SELECT IdA FROM Artikal WHERE IdP = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, shopId);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int articleId = resultSet.getInt("IdA");
            articleIds.add(articleId);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return articleIds;
}

    @Override
    public int getDiscount(int shopId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        
        try {
            String query = "SELECT TOP 1 Vrednost FROM Popust WHERE IdP = ? order by IdPop desc";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, shopId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("Vrednost");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
            return -1; // Ako ne uspe dobijanje popusta, vraćamo -1
    
        }
    
}
