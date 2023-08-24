/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author Korisnik
 */
public class buyer implements BuyerOperations {

    
    private final Connection connection = DB.getInstance().getConnection();
    
    
    public buyer() {
    }

    @Override
    public int createBuyer(String name, int i) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        String query = "INSERT INTO [Kupac] ([Ime], [Racun], [IdG]) VALUES (?, 0, ?)";

    try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, name);
        stmt.setInt(2, i);//city
        stmt.executeUpdate();

        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return -1;
    
    
    }

    @Override
    public int setCity(int i, int i1) {//buyerId,cityId
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        // Check if cityId exists in Grad table
    String checkQuery = "SELECT [IdG] FROM [Grad] WHERE [IdG] = ?";
    try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
        checkStmt.setInt(1, i1);
        ResultSet resultSet = checkStmt.executeQuery();

        if (!resultSet.next()) {
            // CityId does not exist in Grad table, return failure
            return -1;
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        return -1;
    }

    // Update the cityId for the buyer
    String updateQuery = "UPDATE [Kupac] SET [IdG] = ? WHERE [IdK] = ?";
    try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
        stmt.setInt(1, i1);
        stmt.setInt(2, i);
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            return 1; // Success
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return -1; // Failure
    
    }

    @Override
    public int getCity(int buyerId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        String query = "SELECT [IdG] FROM [Kupac] WHERE [IdK] = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, buyerId);
                ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("IdG");
             }
        } catch (SQLException ex) {
             ex.printStackTrace();
        }

        return -1; // Failure
    
    }

    
    
    private BigDecimal getBuyerCredit(int buyerId) {
    String query = "SELECT [Racun] FROM [Kupac] WHERE [IdK] = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, buyerId);
        ResultSet resultSet = stmt.executeQuery();

        if (resultSet.next()) {
            return resultSet.getBigDecimal("Racun").setScale(3);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return BigDecimal.ZERO; // Failure
}
    
    
    @Override
    public BigDecimal increaseCredit(int buyerId, BigDecimal credit) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        String updateQuery = "UPDATE [Kupac] SET [Racun] = [Racun] + ? WHERE [IdK] = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
        stmt.setBigDecimal(1, credit);
        stmt.setInt(2, buyerId);
        stmt.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    // Retrieve the updated credit for the buyer
    BigDecimal updatedCredit = getBuyerCredit(buyerId);
    return updatedCredit.setScale(3);
    
    }

    @Override
    public int createOrder(int buyerId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
         String insertQuery = "INSERT INTO [Porudbina] ([Stanje], [IdK]) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "created");
            stmt.setInt(2, buyerId);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

         return -1; // Failure
    
    }

    @Override
    public List<Integer> getOrders(int buyerId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
    List<Integer> orderIds = new ArrayList<>();

    String query = "SELECT [IdPor] FROM [Porudbina] WHERE [IdK] = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, buyerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int orderId = rs.getInt("IdPor");
            orderIds.add(orderId);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return orderIds;
    
    }

    @Override
    public BigDecimal getCredit(int buyerId) {
       // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
       String query = "SELECT [Racun] FROM [Kupac] WHERE [IdK] = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, buyerId);
        ResultSet resultSet = stmt.executeQuery();

        if (resultSet.next()) {
            return resultSet.getBigDecimal("Racun").setScale(3);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    return null; // Failure
    
    }
    
    
    
    public static void main(String[] args) {
        
        BuyerOperations b = new buyer();
        CityOperations c = new city();
        int cityId = c.createCity("Kragujevac");
        int buyerId = b.createBuyer("Pera", cityId);
        int orderId1 = b.createOrder(buyerId);
        int orderId2 = b.createOrder(buyerId);
        System.out.println(orderId1+" "+orderId2);
        
    }
}
