/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

//import sab_projekat.*;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.student.Graph.Transport;


/**
 *
 * @author Korisnik
 */
public class general implements GeneralOperations {

    public general() {
    }

    private final Connection connection = DB.getInstance().getConnection();
    private static Calendar currentTime=Calendar.getInstance();

    public static Calendar getCalendar() {
        return currentTime;
    }
    
    @Override
    public void setInitialTime(Calendar time) {
        general.currentTime.setTime(time.getTime());
    }

    @Override
public Calendar time(int days) {
    
    int pom8=days;
    Graph graph = new Graph(connection);
    
    try {
        String query = "SELECT * FROM Porudbina WHERE Stanje <> 'arrived' and Stanje<>'created'";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        int pom=days;
        while (resultSet.next()) {
            int rastojanje = resultSet.getInt("Rastojanje");
            int najblizaGradProdavnica = resultSet.getInt("NajblizaGradProdavnica");
            int idPorudbine = resultSet.getInt("IdPor");
            int od = resultSet.getInt("Od");
            int dog = resultSet.getInt("Do");
            days=pom;
            // Traženje grada kupca
            String gk = "SELECT IdG FROM Kupac k JOIN Porudbina p ON k.IdK = p.IdK WHERE p.IdPor = ?";
            PreparedStatement stmt1 = connection.prepareStatement(gk);
            stmt1.setInt(1, idPorudbine);
            ResultSet set1 = stmt1.executeQuery();
            int gradKupca=0;
            if(set1.next()){
                gradKupca = set1.getInt(1);
            }
            int remaining=days;
            int stat=0;
            //ako je porudbina u najblizojGradProdavnici, ali ta prodavnica nije u gradu kupca
            if(rastojanje==0 && !(najblizaGradProdavnica == gradKupca || dog == gradKupca)){
                    List<Transport> lista;
                    if(dog==0){
                        lista= graph.findShortestPath(najblizaGradProdavnica, gradKupca);
                    }else{
                        lista=graph.findShortestPath(dog, gradKupca);
                    }
                    if (lista != null) {
                        //System.out.println(lista.get(0));
                        Transport transport = lista.remove(0);
                        //System.out.println(transport);
                        rastojanje = transport.getDistance();
                        int gradOd = transport.getCity1();
                        int gradDo = transport.getCity2();
						od=gradOd;
                        dog=gradDo;
                        updatePorudbinaTransfers(idPorudbine, gradOd, gradDo, rastojanje);
                    }
            }
            while (rastojanje <= days && rastojanje >0) {
                if ((najblizaGradProdavnica == gradKupca || dog == gradKupca) && rastojanje-days<=0) {
                    updateRastojanje(idPorudbine, rastojanje);
                    // Postavi status porudbine na "arrived"
                    updatePorudbinaStatus(idPorudbine, "arrived");
                    // Obavi transakciju
                    performTransakcija(idPorudbine);
                    rastojanje=0;
                    stat=1;
                    break;
                } else {
                    remaining = days - rastojanje;
                    
                    updateRastojanje(idPorudbine, rastojanje);
                    List<Transport> lista;
                    //System.out.println("Najblgrpr "+najblizaGradProdavnica+" gradKupca"+gradKupca);
                    if(dog==0){
                        lista= graph.findShortestPath(najblizaGradProdavnica, gradKupca);
                    }else{
                        lista=graph.findShortestPath(dog, gradKupca);
                    }
                    if (lista != null && !lista.isEmpty()) {
                        //System.out.println(lista.get(0));
                        Transport transport = lista.remove(0);
                        //System.out.println(transport);
                        rastojanje = transport.getDistance();
                        int gradOd = transport.getCity1();
                        int gradDo = transport.getCity2();
                        od=gradOd;
                        dog=gradDo;
                        updatePorudbinaTransfers(idPorudbine, gradOd, gradDo, rastojanje);
                    }
                    
                    days = remaining;
                }
            }
            
            if (rastojanje > 0) {
                rastojanje=rastojanje-days;
                updateRastojanje(idPorudbine, days);
            }
            
           if ((najblizaGradProdavnica == gradKupca || dog == gradKupca) && rastojanje==0 && stat!=1) {
                    //updateRastojanje(idPorudbine, rastojanje);
                    // Postavi status porudbine na "arrived"
                    updatePorudbinaStatus(idPorudbine, "arrived");
                    // Obavi transakciju
                    performTransakcija(idPorudbine);
                    //rastojanje=0;
                    //stat=1;
            }
            
            
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    currentTime.add(Calendar.DATE, pom8);
    return currentTime;
}

    /*
    public void rekurzija(int days){
        Graph graph=new Graph(connection);
        
        try{
            
            String query = "SELECT * FROM Porudbina WHERE Rastojanje > 0 and Stanje<>'arrived'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            
            while(resultSet.next()){
                
                int rastojanje = resultSet.getInt("Rastojanje");
                int najblizaGradProdavnica = resultSet.getInt("NajblizaGradProdavnica");
                int idPorudbine = resultSet.getInt("IdPor");
                int od= resultSet.getInt("Od");
                int dog =resultSet.getInt("Do");
                //trzenje grada kupca
                String gk=  "select IdG\n" +
                            "from Kupac k join Porudbina p on k.IdK=p.IdK\n" +
                            "where p.IdPor=?";
                PreparedStatement stmt1=connection.prepareStatement(gk);
                stmt1.setInt(1, idPorudbine);
                ResultSet set1=stmt1.executeQuery();
                int gradKupca=set1.getInt(1);
                
                if(rastojanje<=days){
                    if(najblizaGradProdavnica==gradKupca || dog==gradKupca){
                        updateRastojanje(idPorudbine, rastojanje);
                        // Postavi status porudbine na "arrived"
                        updatePorudbinaStatus(idPorudbine, "arrived");
                        // Obavi transakciju
                        performTransakcija(idPorudbine);
                    }else{
                        int remaining=days-rastojanje;
                        
                        updateRastojanje(idPorudbine, rastojanje);
                        List<Transport> lista=graph.findShortestPath(dog, gradKupca);
                        
                        if(lista!=null){
                            Transport transport=lista.remove(0);
                            rastojanje=transport.getDistance();
                            int gradOd=transport.getCity1();
                            int gradDo=transport.getCity2();
                            updatePorudbinaTransfers(idPorudbine, gradOd, gradDo, rastojanje);
                        }
                        if(rastojanje>0 && remaining>0){
                            rekurzija(remaining);
                        }
                    }
                }else{
                    // Umanji rastojanje za broj dana
                    updateRastojanje(idPorudbine, days);
                }

                
                
            }
            
        }catch(Exception e){}
       
    }
    */
    private void updatePorudbinaStatus(int idPorudbine, String status) throws SQLException {
        String query = "UPDATE Porudbina SET Stanje = ?, DatumPrijema= DATEADD(DAY, DaniPutovanja, DatumKreiranja) WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, status);
        //statement.setDate(2,new Date(general.getCalendar().getTimeInMillis()));
        statement.setInt(2, idPorudbine);
        statement.executeUpdate();
    }

    private void performTransakcija(int idPorudbine) throws SQLException {
        // Azuriraj datum u transakciji
        String query = "UPDATE Transakcija SET Datum = (select DatumPrijema from Porudbina where IdPor=?) WHERE IdPor = ? and IdP is not NULL";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1,idPorudbine);
        statement.setInt(2, idPorudbine);
        statement.executeUpdate();
    }

    private void updateRastojanje(int idPorudbine, int days) throws SQLException {
        String query = "UPDATE Porudbina SET Rastojanje = Rastojanje - ? WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, days);
        statement.setInt(2, idPorudbine);
        statement.executeUpdate();
    }
    
    private void updatePorudbinaTransfers(int idPorudbine, int city1, int city2, int distance) throws SQLException {
        String query = "UPDATE Porudbina SET [Od] = ?, [Do] = ?, Rastojanje = ? WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, city1);
        statement.setInt(2, city2);
        statement.setInt(3, distance);
        statement.setInt(4, idPorudbine);
        statement.executeUpdate();
    }
    
    @Override
    public Calendar getCurrentTime() {
        return currentTime;
    }

    @Override
    public void eraseAll() {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
        String eraseAllQuery = "{ call eraseAll() }; ";
        
        try(CallableStatement cs = connection.prepareCall(eraseAllQuery)) {
            
            cs.execute();
            
        } catch (SQLException ex) {
            //Logger.getLogger(GeneralOperationsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        List<Integer> lista=new ArrayList<>();
        lista.add(1);
        lista.add(2);
        System.out.println(lista.remove(0));
        
        
        Graph graph=new Graph(new general().connection);
        System.out.println(graph.findShortestPath(3,1).get(0));
        
        System.out.println(BigDecimal.valueOf(-1).setScale(3));
        
        List<Transport> rastojanje=graph.findShortestPath(8, 1);
        
        for(Transport t:rastojanje){
            System.out.println(t);
        }
    }
    
}
