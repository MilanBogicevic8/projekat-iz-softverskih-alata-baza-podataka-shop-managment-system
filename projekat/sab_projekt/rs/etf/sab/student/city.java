/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author Korisnik
 */
public class city implements CityOperations {

    private final Connection connection = DB.getInstance().getConnection();
    
    
    public city() {
    }

    @Override
    public int createCity(String name) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
        String sql="select * from Grad where Naziv=?";
        try (PreparedStatement stmt1=connection.prepareStatement(sql);){
            
            stmt1.setString(1, name);
            
            try(ResultSet rs=stmt1.executeQuery();){
            
                if(rs.next()){
                    return -1;
                }
            
            }catch(Exception e){
                return -1;
            }
                
            
            
        } catch (SQLException ex) {
            Logger.getLogger(city.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
     
        
        
        
        
      String insertCityQuery = "INSERT INTO [dbo].[Grad] " +
                                "           (Naziv) " +
                                "       VALUES (?); ";
      
      try(PreparedStatement ps = connection.prepareStatement(insertCityQuery, Statement.RETURN_GENERATED_KEYS);) {           
            ps.setString(1, name);

            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException ex) {
//            Logger.getLogger(CityOperationsImpl.class.getName()).log(Level.SEVERE, null, ex);            
        }
        return -1;
      
    }

    @Override
    public List<Integer> getCities() {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        List<Integer> list;
        list = new ArrayList<>();
        
        String sql = "SELECT IdG "
                                + " FROM [dbo].[Grad]; ";
       
        try(Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            
            while(rs.next()){
                list.add(rs.getInt(1));
            }
            
        } catch (SQLException ex) {
//            Logger.getLogger(CityOperationsImpl.class.getName()).log(Level.SEVERE, null, ex);            
        }

        if(list.isEmpty()){
            return null;
        }
        return list;
    
    }

    @Override
    public int connectCities(int i, int i1, int i2) {//cityId1,cityId2,distance
       //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
       PreparedStatement stmt=null;
       ResultSet rs=null;
        
        try {
            
            
            String query = "SELECT [Rastojanje], [IdG1], [IdG2] FROM [Linija] WHERE ([IdG1] = ? AND [IdG2] = ?) OR ([IdG1] = ? AND [IdG2] = ?)";
            stmt=connection.prepareStatement(query);
            
            stmt.setInt(1, i);
            stmt.setInt(2, i1);
            stmt.setInt(3, i1);
            stmt.setInt(4, i);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Line already exists, return the line id
                return rs.getInt("Rastojanje");
            } else {
                // Line does not exist, insert a new line into the database
                String insertQuery = "INSERT INTO [Linija] ([Rastojanje], [IdG1], [IdG2]) VALUES (?, ?, ?)";
                stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, i2);
                stmt.setInt(2, i);
                stmt.setInt(3, i1);
                stmt.executeUpdate();

                // Get the newly inserted line id
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(city.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            // Close the database resources
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
               
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return -1;
        
    
    }
//proveri
    @Override
    public List<Integer> getConnectedCities(int i) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        List<Integer> lista=new ArrayList<>();
        
        String str="SELECT IdG1, IdG2 FROM Linija WHERE IdG1 = ? OR IdG2 = ?";
        
        try(PreparedStatement stmt=connection.prepareStatement(str);){
            stmt.setInt(1, i);
            stmt.setInt(2, i);
            
            try(ResultSet rs= stmt.executeQuery()){
                
                while(rs.next()){
                     int idG1 = rs.getInt("IdG1");
                     int idG2 = rs.getInt("IdG2");
                     
                     if (idG1 != i) {
                        lista.add(idG1);
                     } else {
                        lista.add(idG2);
                     }
                }
                
                
            }catch(Throwable t){
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(city.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
   
        return lista;
    }

    @Override
    public List<Integer> getShops(int i) {
        
            //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            
            List<Integer> lista=new ArrayList<>();
            String query = "SELECT IdP FROM Prodavnica WHERE IdG = ?";
        
        try(PreparedStatement stmt=connection.prepareStatement(query);){
            
            stmt.setInt(1, i);
            
            try(ResultSet resultSet = stmt.executeQuery();){
                while (resultSet.next()) {
                    int shopId = resultSet.getInt("IdP");
                    lista.add(shopId);
                }
            }catch(Exception e){
                return null;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(city.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        if(lista.isEmpty()){
            return null;
        }
        return lista;
    
    }
    
    
    public static void main(String[] args) {
        CityOperations cityOperations = new city();
        
        int bgId = cityOperations.createCity("Beograd");        
        System.out.println(bgId); // 1

        int vaId = cityOperations.createCity("Valjevo");
        System.out.println(vaId); // 2

        int nsId = cityOperations.createCity("Novi Sad");    
        System.out.println(nsId); // 3

        int vaBothSameId = cityOperations.createCity("Valjevo");
        System.out.println(vaBothSameId); // -1
 
        int smSamePostalCodeId = cityOperations.createCity("Smederevo");
        System.out.println(smSamePostalCodeId); // -1

        int koSameNameId = cityOperations.createCity("Kovin");
        System.out.println(koSameNameId); // 4
        
        
        List<Integer> listOfIdC = cityOperations.getCities();
        System.out.println(listOfIdC.size()); // 4
        for (int i: listOfIdC) {
            System.out.print(i + " ");
        }
        System.out.println();
    }
}
