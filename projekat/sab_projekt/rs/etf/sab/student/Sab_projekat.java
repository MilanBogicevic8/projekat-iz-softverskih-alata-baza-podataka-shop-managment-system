/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rs.etf.sab.student;

//import sab_projekat.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Korisnik
 */
public class Sab_projekat {

    /**
     * @param args the command line arguments
     */
    
    static Connection conn=DB.getInstance().getConnection();
    
    public static void proba(){
        try (PreparedStatement stmt=conn.prepareStatement("select * from Grad");){
            ResultSet rs=stmt.executeQuery();
            if(rs.next()){
                System.out.println(rs.getString(2));
            }else{
                System.out.println("Prazno");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sab_projekat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public static void main(String[] args) {
        // TODO code application logic here
        proba();
    }
    
}
