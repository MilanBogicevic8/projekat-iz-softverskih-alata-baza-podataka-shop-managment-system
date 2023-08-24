/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

//import sab_projekat.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Korisnik
 */
public class DB {
     private static final String username = "sa";
    private static final String password = "123";
    private static final String database = "OnlineProdavnica";
    private static final int port = 1433;
    private static final String server = "DESKTOP-NSQE9SJ";

    private static final String connectionUrl
            = "jdbc:sqlserver://" + server + ":" + port
            + ";databaseName=" + database
            + ";encrypt=true"
            + ";trustServerCertificate=true";
    private static final String conn2="jdbc:sqlserver://DESKTOP-NSQE9SJ;Database=OnlineProdavnica;IntegratedSecurity=true";
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    private DB() {
        try {
            connection = DriverManager.getConnection(conn2);
        } catch (SQLException ex) {
            System.out.println("Greska");
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static DB db = null;

    public static DB getInstance() {
        if (db == null) {
            db = new DB();
        }
        return db;
    }
}
