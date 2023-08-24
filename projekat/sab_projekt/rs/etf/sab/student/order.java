/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import rs.etf.sab.operations.ArticleOperations;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;
import rs.etf.sab.operations.ShopOperations;
import rs.etf.sab.operations.TransactionOperations;
//import sab_projekat.Graph.Pair;

/**
 *
 * @author Korisnik
 */
public class order implements OrderOperations {

    public order() {
    }
    
    private final Connection connection = DB.getInstance().getConnection();

    @Override
public int addArticle(int orderId, int articleId, int count) {
    try {
        // Provera da li postoji porudzbina sa datim ID-jem
        PreparedStatement checkOrderStatement = connection.prepareStatement("SELECT COUNT(*) FROM Porudbina WHERE IdPor = ?");
        checkOrderStatement.setInt(1, orderId);
        ResultSet orderResultSet = checkOrderStatement.executeQuery();
        if (!orderResultSet.next()) {
            return -1; // Porudzbina ne postoji
        }

        // Provera da li postoji artikal sa datim ID-jem
        PreparedStatement checkArticleStatement = connection.prepareStatement("SELECT * FROM Artikal WHERE IdA = ?");
        checkArticleStatement.setInt(1, articleId);
        ResultSet articleResultSet = checkArticleStatement.executeQuery();
        if (!articleResultSet.next()) {
            return -1; // Artikal ne postoji
        }
        if(articleResultSet.getInt("NaStanju")<count){//ako nema artikala na stanju
            return -1;
        }
        
        //smanjenje kolicine artikala koji su dostupni sad
        String smanjenje=   "update Artikal\n" +
                            "set NaStanju=NaStanju-?\n" +
                            "where IdA=?";
        PreparedStatement stsmanjenje=connection.prepareStatement(smanjenje);
        stsmanjenje.setInt(1, count);
        stsmanjenje.setInt(2, articleId);
        stsmanjenje.executeUpdate();
        
        // Provera da li postoji stavka u porudzbini sa datim artiklom
        PreparedStatement checkItemStatement = connection.prepareStatement("SELECT * FROM Stavka WHERE IdPor = ? AND IdA = ?");
        checkItemStatement.setInt(1, orderId);
        checkItemStatement.setInt(2, articleId);
        ResultSet itemResultSet = checkItemStatement.executeQuery();
        if (itemResultSet.next()) {
            // Ako postoji stavka, samo povecaj broj artikala i azuriraj cenu
            int itemId = itemResultSet.getInt("IdS");
            int currentCount = itemResultSet.getInt("Kolicina");
            double itemPrice=itemResultSet.getDouble("CenaStavke");//ukupna  cena
            double pravaCena=itemResultSet.getDouble("PravaCenaStavki");
            int newCount = currentCount + count;
            double articlePrice = articleResultSet.getDouble("Cena");
            double totalPrice = itemPrice;
            
            pravaCena=pravaCena+count*articlePrice;
            
            // Pronalazenje poslednjeg popusta za datu prodavnicu
            int shopId = articleResultSet.getInt("IdP");
            PreparedStatement discountStatement = connection.prepareStatement("SELECT TOP 1 Vrednost FROM Popust WHERE IdP = ? ORDER BY IdPop DESC");
            discountStatement.setInt(1, shopId);
            ResultSet discountResultSet = discountStatement.executeQuery();
            double discount = discountResultSet.next() ? discountResultSet.getDouble("Vrednost") : 0.0;

            // Racunanje cene stavke sa popustom
            double discountedPrice = totalPrice + (count*articlePrice * (100-discount) / 100.0);

            // Azuriranje kolicine i cene stavke
            PreparedStatement updateItemStatement = connection.prepareStatement("UPDATE Stavka SET Kolicina = ?, CenaStavke = ?,PravaCenaStavki=? WHERE IdS = ?");
            updateItemStatement.setInt(1, newCount);
            updateItemStatement.setDouble(2, discountedPrice);
            updateItemStatement.setDouble(3,pravaCena);
            updateItemStatement.setInt(4, itemId);
            updateItemStatement.executeUpdate();

            return itemId;
        } else {
            // Ako ne postoji stavka, dodaj je u porudzbinu
            double articlePrice = articleResultSet.getDouble("Cena");
            double totalPrice = articlePrice * count;
            double pravaCena=totalPrice;
            
            // Pronalazenje poslednjeg popusta za datu prodavnicu
            int shopId = articleResultSet.getInt("IdP");
            PreparedStatement discountStatement = connection.prepareStatement("SELECT TOP 1 Vrednost FROM Popust WHERE IdP = ? ORDER BY IdPop DESC");
            discountStatement.setInt(1, shopId);
            ResultSet discountResultSet = discountStatement.executeQuery();
            double discount = discountResultSet.next() ? discountResultSet.getDouble("Vrednost") : 0.0;

            // Racunanje cene stavke sa popustom
            double discountedPrice = totalPrice - (totalPrice * discount / 100);

            // Dodavanje stavke u porudzbinu
            PreparedStatement insertItemStatement = connection.prepareStatement("INSERT INTO Stavka (Kolicina, IdA, IdPor, CenaStavke,PravaCenaStavki) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            insertItemStatement.setInt(1, count);
            insertItemStatement.setInt(2, articleId);
            insertItemStatement.setInt(3, orderId);
            insertItemStatement.setDouble(4, discountedPrice);
            insertItemStatement.setDouble(5, pravaCena);
            insertItemStatement.executeUpdate();

            // Dobavljanje generisanog ID-ja stavke
            ResultSet generatedKeys = insertItemStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return -1; // Neuspešno dodavanje stavke
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return -1; // Greška prilikom izvršavanja upita
    }
}

    @Override
public int removeArticle(int orderId, int articleId) {
    int result = -1;

    try {
        // Provera da li postoji porudžbina
        String checkOrderQuery = "SELECT * FROM Porudbina WHERE IdPor = ? and Stanje='created'";
        PreparedStatement checkOrderStmt = connection.prepareStatement(checkOrderQuery);
        checkOrderStmt.setInt(1, orderId);
        ResultSet orderResult = checkOrderStmt.executeQuery();

        if (orderResult.next()) {
            // Provera da li postoji stavka u porudžbini
            String checkItemQuery = "SELECT * FROM Stavka WHERE IdPor = ? AND IdA = ?";
            PreparedStatement checkItemStmt = connection.prepareStatement(checkItemQuery);
            checkItemStmt.setInt(1, orderId);
            checkItemStmt.setInt(2, articleId);
            ResultSet itemResult = checkItemStmt.executeQuery();
            
            if (itemResult.next()) {
                // Uklanjanje stavke iz porudžbine
                int brojStavki=itemResult.getInt("Kolicina");
                String upd="update Artikal\n" +
                            "set NaStanju=NaStanju+?\n" +
                            "where IdA=?";
                
                PreparedStatement stmt1=connection.prepareStatement(upd);
                stmt1.setInt(1, brojStavki);
                stmt1.setInt(2,articleId);
                stmt1.executeUpdate();
                String removeItemQuery = "DELETE FROM Stavka WHERE IdPor = ? AND IdA = ?";
                PreparedStatement removeItemStmt = connection.prepareStatement(removeItemQuery);
                removeItemStmt.setInt(1, orderId);
                removeItemStmt.setInt(2, articleId);
                int rowsAffected = removeItemStmt.executeUpdate();

                if (rowsAffected > 0) {
                    result = 1; // Uspesno uklonjeno
                }
                removeItemStmt.close();
            }

            itemResult.close();
            checkItemStmt.close();
        }

        orderResult.close();
        checkOrderStmt.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return result;
}

    @Override
public List<Integer> getItems(int orderId) {
    List<Integer> itemList = new ArrayList<>();

    try {
        // Provera da li postoji porudžbina
        String checkOrderQuery = "SELECT * FROM Porudbina WHERE IdPor = ?";
        PreparedStatement checkOrderStmt = connection.prepareStatement(checkOrderQuery);
        checkOrderStmt.setInt(1, orderId);
        ResultSet orderResult = checkOrderStmt.executeQuery();

        if (orderResult.next()) {
            // Dohvatanje stavki za porudžbinu
            String getItemQuery = "SELECT IdS FROM Stavka WHERE IdPor = ?";
            PreparedStatement getItemStmt = connection.prepareStatement(getItemQuery);
            getItemStmt.setInt(1, orderId);
            ResultSet itemResult = getItemStmt.executeQuery();

            while (itemResult.next()) {
                int itemId = itemResult.getInt("IdS");
                itemList.add(itemId);
            }

            itemResult.close();
            getItemStmt.close();
        }

        orderResult.close();
        checkOrderStmt.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return itemList;
}

    //vraca prodavnicu koja je najbliza
    private int pronadjiNajblizuProdavnicuZaKupca(int gradKupca){
        
        Graph graph=new Graph(connection);
        String str1="select IdP,IdG\n" +
                    "  from Prodavnica";
        int prodavnica=0;
        int rastojanje=100000000;
        int gradP=0;
        try(PreparedStatement stmt1=connection.prepareStatement(str1);
                ResultSet set1=stmt1.executeQuery();){
                    while(set1.next()){
                        int pr=set1.getInt(1);
                        int gr=set1.getInt(2);
                        int trenutno=graph.rastojanje(gr, gradKupca);
                        if(trenutno<rastojanje){
                            rastojanje=trenutno;
                            prodavnica=pr;
                            gradP=gr;
                        }
                    }
                    
                /*List<Transport> lista=graph.findShortestPath(gradP, gradKupca)
                for(Transport t:lista){
                //System.out.println(t.getCity1()+" "+t.getCity2()+" "+t.getDistance());
                rastojanje=rastojanje+t.getDistance();
               }*/
            
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prodavnica;
    }
    
    public int gradNaOsnovuProdavnice(int prodavnica){
        int gradProdavnice=0;
        try{
        PreparedStatement gr=connection.prepareStatement("select IdG from Prodavnica where IdP=?");
        gr.setInt(1, prodavnica);
        ResultSet gr1=gr.executeQuery();
        
        if(gr1.next()){
            gradProdavnice=gr1.getInt(1);
        }else{
            return -1;
        }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gradProdavnice;
    }
    
    private BigDecimal cenaPorudbine(int orderId){
                Date currentDate = new Date(general.getCalendar().getTimeInMillis());

		try (CallableStatement statement = connection.prepareCall(
			"{CALL dbo.CalculateTotalPrice(?, ?, ?)}")) {
			statement.setInt(1, orderId);
			statement.setDate(2, currentDate);
			statement.registerOutParameter(3, Types.DECIMAL);

			statement.execute();

			return statement.getBigDecimal(3).setScale(3);
		} catch (SQLException e) {
			return null;
		}

    }
    
    
    @Override
    public int completeOrder(int orderId) {
        
        
        
        
        //provera da li kupac ima dovoljno novca na racunu
        
        String strDovoljno="select k.Racun\n" +
                            "from Porudbina p join Kupac k on p.IdK=k.IdK\n" +
                            "where p.IdPor=?";
        BigDecimal novac=BigDecimal.ZERO;
        try(PreparedStatement stDovoljno=connection.prepareStatement(strDovoljno);){
            stDovoljno.setInt(1, orderId);
            try(ResultSet set=stDovoljno.executeQuery()){
                if(set.next()){
                    novac=set.getBigDecimal(1);
                    //System.out.println("Novac na racunu:"+orderId+" "+novac);
                }else{
                    return -1;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        //pronalazak sume porudbina koje su u stanju sent za kupca te porudbine orderId
        String sumsent="select coalesce(sum(UkupnaCena),0)\n" +
                        "from Porudbina \n" +
                        "where Stanje='sent' and IdK=(select IdK from Porudbina where IdPor=?)";
        BigDecimal ssent=BigDecimal.valueOf(0);
        try(PreparedStatement stmtsumsent=connection.prepareStatement(sumsent)){
            stmtsumsent.setInt(1, orderId);
            try(ResultSet set=stmtsumsent.executeQuery()){
                if(set.next()){
                    ssent=set.getBigDecimal(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        BigDecimal sumazaorderId=cenaPorudbine(orderId);
        
        if(novac.compareTo(ssent.add(sumazaorderId))<0){
            //System.out.println("Nema dovoljno novca ::"+novac+" "+ssent.add(sumazaorderId)+"  ord"+orderId);
            return -1;
        }
        
        BigDecimal finalPrice=getFinalPrice1(orderId);
        
        
        //smanji novac na racunu kupca
        
        /*String smanjenNovac="update Kupac\n" +
                            "set Racun=Racun-?\n" +
                            "where IdK=(select IdK from Porudbina where IdPor=?)";
        
        try(PreparedStatement smanjistmt=connection.prepareStatement(smanjenNovac)){
            smanjistmt.setBigDecimal(1, finalPrice);
            smanjistmt.setInt(2, orderId);
            smanjistmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        //transakcija za kupca
        String strtrans="insert into Transakcija(Iznos,IdPor,Datum)\n" +
                        "values (?,?,?)";
        
        try(PreparedStatement sttransakcija=connection.prepareStatement(strtrans);){
            sttransakcija.setBigDecimal(1,finalPrice);
            sttransakcija.setInt(2,orderId);
            sttransakcija.setDate(3, new Date(general.getCalendar().getTimeInMillis()));
            sttransakcija.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //pronalazak prodavnice koja je najbliza kupcu
        int prodavnica=0;
        int gradKupca=0;
        try {
            String str1="select IdG\n" +
                    "from Porudbina p join Kupac k on p.IdK=k.IdK\n" +
                    "where p.IdPor=?";
            PreparedStatement stmt1=connection.prepareStatement(str1);
            stmt1.setInt(1, orderId);
            ResultSet set1=stmt1.executeQuery();
            
            if(set1.next()){
                gradKupca=set1.getInt(1);
            }else{
                return -1;
            }
            prodavnica=pronadjiNajblizuProdavnicuZaKupca(gradKupca);

        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        //grad najblize prodavnice
        int gradProdavnice=0;
        try{
        PreparedStatement gr=connection.prepareStatement("select IdG from Prodavnica where IdP=?");
        gr.setInt(1, prodavnica);
        ResultSet gr1=gr.executeQuery();
        
        if(gr1.next()){
            gradProdavnice=gr1.getInt(1);
        }else{
            return -1;
        }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println("Grad prodavnice najblize kupcu "+gradProdavnice);
        //pronalazak najveceg rastojanja od grada sa prodavnicom najblizeg kupcu do grada stavke te porudbine
        
        int najvecerastojanje=0;
        
        try{
            Graph graph=new Graph(connection);

            String str2="select pr.IdG\n" +
                        "from Stavka s join Porudbina p on s.IdPor=p.IdPor join Artikal a on s.IdA=a.IdA join Prodavnica pr on a.IdP=pr.IdP\n" +
                        "where p.IdPor=?";
            PreparedStatement stmt2=connection.prepareStatement(str2);
            stmt2.setInt(1, orderId);

            ResultSet set2=stmt2.executeQuery();

            while(set2.next()){
                int gr=set2.getInt(1);
               int trenutno=graph.rastojanje(gr, gradProdavnice);
               //System.out.println("od grada "+gr+" je rastojanje "+trenutno);
               if(trenutno>najvecerastojanje){
                   najvecerastojanje=trenutno;
               }
            }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //updejt porudbine
        
        String str5="update Porudbina\n" +
                    "set Stanje='sent',DatumKreiranja=?,NajblizaGradProdavnica=?,Rastojanje=?,DaniPutovanja=? "+
                    "where IdPor=?";
        try(PreparedStatement stmt5=connection.prepareStatement(str5)){
            
            Graph graph=new Graph(connection);
            //int daniPutovanja=graph.rastojanje(orderId, prodavnica)
            stmt5.setDate(1, new Date(general.getCalendar().getTimeInMillis()));
            stmt5.setInt(2, gradProdavnice);
            stmt5.setInt(3, najvecerastojanje);
            stmt5.setInt(4,najvecerastojanje+graph.rastojanje(gradProdavnice, gradKupca));
            stmt5.setInt(5, orderId);
            
            int rs5=stmt5.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 1;
    }

//nije dobro ispravi proceduru u bazi 6.6.
    
    public BigDecimal getFinalPrice1(int orderId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
       Date currentDate = new Date(general.getCalendar().getTimeInMillis());

		try (CallableStatement statement = connection.prepareCall(
			"{CALL dbo.SP_FINAL_PRICE(?, ?, ?)}")) {
			statement.setInt(1, orderId);
			statement.setDate(2, currentDate);
			statement.registerOutParameter(3, Types.DECIMAL);

			statement.execute();

			return statement.getBigDecimal(3).setScale(3);
		} catch (SQLException e) {
			return null;
		}

		
    
    }
    
    @Override
    public BigDecimal getFinalPrice(int orderId) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        
       String query="select coalesce(UkupnaCena,0) as cena\n" +
                    "from Porudbina \n" +
                    "where IdPor=? and Stanje<>'created'";
       try(PreparedStatement stmt=connection.prepareStatement(query)){
           stmt.setInt(1,orderId);
           try(ResultSet rs=stmt.executeQuery()){
               if(rs.next()){
                   return rs.getBigDecimal(1).setScale(3);
               }else{
                   return BigDecimal.valueOf(-1).setScale(3);
               }
           }
       } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }

	return BigDecimal.valueOf(-1).setScale(3);	
    
    }

    //ovde je greska, treba proveriti da li je porudbina sent i onda izracunati popust
    @Override
    public BigDecimal getDiscountSum(int orderId) {//-------ispravljeno
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    
        String sql1="select coalesce(PravaCenaStavki-UkupnaCena,0) as Popust\n" +
                    "from Porudbina "+
                    "where IdPor=? and Stanje<>'created'";
        BigDecimal cena=BigDecimal.valueOf(-1).setScale(3);
        try(PreparedStatement stmt1=connection.prepareStatement(sql1)){
            stmt1.setInt(1, orderId);
            try(ResultSet set1=stmt1.executeQuery()){
                if(set1.next()){
                    cena=set1.getBigDecimal(1);
                }else{
                    return BigDecimal.valueOf(-1).setScale(3);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(order.class.getName()).log(Level.SEVERE, null, ex);
        }
    
        return cena;
    }

    @Override
public String getState(int orderId) {
    String state = null;
    try {
        PreparedStatement statement = connection.prepareStatement("SELECT Stanje FROM Porudbina WHERE IdPor = ?");
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            state = resultSet.getString("Stanje");
        }
        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return state;
}

    @Override
public Calendar getSentTime(int orderId) {
    try {
        String query = "SELECT DatumKreiranja FROM Porudbina WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            Date timestamp = resultSet.getDate("DatumKreiranja");
            if (timestamp != null) {
                Calendar sentTime = Calendar.getInstance();
                sentTime.setTime(timestamp);
                return sentTime;
            }
        }
        
        return null; // Order not found or DatumPrijema is null
    } catch (SQLException e) {
        e.printStackTrace();
        return null; // Failure
    }
}


@Override
public Calendar getRecievedTime(int orderId) {
    try {
        String query = "SELECT DatumPrijema FROM Porudbina WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            Date timestamp = resultSet.getDate("DatumPrijema");
            if (timestamp != null) {
                Calendar recievedTime = Calendar.getInstance();
                recievedTime.setTime(timestamp);
                return recievedTime;
            }
        }
        
        return null; // Order not found or DatumPrijema is null
    } catch (SQLException e) {
        e.printStackTrace();
        return null; // Failure
    }
}

@Override
public int getBuyer(int orderId) {
    int buyerId = -1;
    try {
        PreparedStatement statement = connection.prepareStatement("SELECT IdK FROM Porudbina WHERE IdPor = ?");
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            buyerId = resultSet.getInt("IdK");
        }else{
            return -1;
        }
        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
        return -1;
    }
    return buyerId;
}






    @Override
public int getLocation(int orderId) {
    try {
        String query = "SELECT Stanje, NajblizaGradProdavnica, Rastojanje, Od, Do FROM Porudbina WHERE IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            String stanje = resultSet.getString("Stanje");
            int najblizaGradProdavnica = resultSet.getInt("NajblizaGradProdavnica");
            int rastojanje = resultSet.getInt("Rastojanje");
            Integer od = resultSet.getInt("Od");
            Integer dog = resultSet.getInt("Do");

            if (stanje.equals("created")) {
                return -1;
            } else if (stanje.equals("sent") && najblizaGradProdavnica!=0 && od == 0 && dog == 0) {
                return najblizaGradProdavnica;
            } else if (stanje.equals("arrived")) {
                return dog;
            } else if (stanje.equals("sent") && od != 0 && rastojanje >= 0) {
                return od;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return -1; // Povratna vrednost u slučaju neuspeha

}
 

    public static void main(String[] args) {
        /*order o=new order();
        Graph graph=new Graph(o.connection);
        List<Transport> path=graph.findShortestPath(27,26);
        
        for(Transport p:path){
            System.out.println(p.getCity1()+" "+p.getCity2()+" "+p.getDistance());
        }
        */
        ////////////////
        
        
        ArticleOperations articleOperations = new article(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new buyer();
        CityOperations cityOperations = new city();
        GeneralOperations generalOperations = new general();
        OrderOperations orderOperations = new order();
        ShopOperations shopOperations = new shop();
        TransactionOperations transactionOperations = new transact();
        
        generalOperations.eraseAll();
        final Calendar initialTime = Calendar.getInstance();
        initialTime.clear();
        initialTime.set(2018, 0, 1);
        generalOperations.setInitialTime(initialTime);
        final Calendar receivedTime = Calendar.getInstance();
        receivedTime.clear();
        receivedTime.set(2018, 0, 22);
        final int cityB = cityOperations.createCity("B");
        final int cityC1 = cityOperations.createCity("C1");
        final int cityA = cityOperations.createCity("A");
        final int cityC2 = cityOperations.createCity("C2");
        final int cityC3 = cityOperations.createCity("C3");
        final int cityC4 = cityOperations.createCity("C4");
        final int cityC5 = cityOperations.createCity("C5");
        final int cityC6 = cityOperations.createCity("C6");
        final int cityC7 = cityOperations.createCity("C7");
       
        
        
        
        cityOperations.connectCities(cityB, cityC1, 8);
        cityOperations.connectCities(cityC1, cityA, 10);
        cityOperations.connectCities(cityA, cityC2, 3);
        cityOperations.connectCities(cityC2, cityC3, 2);
        cityOperations.connectCities(cityC3, cityC4, 1);
        cityOperations.connectCities(cityC4, cityA, 3);
        cityOperations.connectCities(cityA, cityC5, 15);
        cityOperations.connectCities(cityC5, cityB, 2);
        cityOperations.connectCities(cityC6, cityC3, 7);
        cityOperations.connectCities(cityC7, cityC4, 5);
        cityOperations.connectCities(cityC7, cityC6, 1);
        final int shopA = shopOperations.createShop("shopA", "A");
        final int shopC2 = shopOperations.createShop("shopC2", "C2");
        final int shopC3 = shopOperations.createShop("shopC3", "C6");
        shopOperations.setDiscount(shopA, 20);
        shopOperations.setDiscount(shopC2, 50);
        final int laptop = articleOperations.createArticle(shopA, "laptop", 2600);
        final int monitor = articleOperations.createArticle(shopC2, "monitor", 200);
        final int stolica = articleOperations.createArticle(shopC3, "stolica", 100);
        final int sto = articleOperations.createArticle(shopC3, "sto", 200);
        shopOperations.increaseArticleCount(laptop, 10);
        shopOperations.increaseArticleCount(monitor, 10);
        shopOperations.increaseArticleCount(stolica, 10);
        shopOperations.increaseArticleCount(sto, 10);
        final int buyer = buyerOperations.createBuyer("kupac", cityC4);
        final int buyer2 = buyerOperations.createBuyer("kupac2", cityB);
        
        
        order o=new order();
        System.out.println("Najbliza prodavnica je:"+o.pronadjiNajblizuProdavnicuZaKupca(cityC4));
        
        
        
        buyerOperations.increaseCredit(buyer, new BigDecimal("20000"));
        buyerOperations.increaseCredit(buyer2, new BigDecimal("20000"));
        final int order = buyerOperations.createOrder(buyer);
        final int order2 = buyerOperations.createOrder(buyer2);
        final int order3 = buyerOperations.createOrder(buyer);
        orderOperations.addArticle(order, laptop, 5);
        orderOperations.addArticle(order, monitor, 4);
        orderOperations.addArticle(order, stolica, 10);
        orderOperations.addArticle(order, sto, 40);
        orderOperations.addArticle(order2, sto, 4);
        orderOperations.addArticle(order3, laptop, 2);
        

        
        System.out.println(orderOperations.getSentTime(order)==null);
        System.out.println("created".equals(orderOperations.getState(order)));
        
        // do ovde sam stigao
        orderOperations.completeOrder(order);
        orderOperations.completeOrder(order2);
        orderOperations.completeOrder(order3);
        System.out.println("sent".equals(orderOperations.getState(order)));
        
        final int buyerTransactionId = transactionOperations.getTransationsForBuyer(buyer).get(0);
        System.out.println(new Date(initialTime.getTimeInMillis())+" "+" ----");
        System.out.println(new Date(transactionOperations.getTimeOfExecution(buyerTransactionId).getTimeInMillis()));

        
        //Assert.assertEquals(initialTime, transactionOperations.getTimeOfExecution(buyerTransactionId));
        System.out.println( transactionOperations.getTransationsForShop(shopA)==null);
        
        final BigDecimal shopAAmount = new BigDecimal("5").multiply(new BigDecimal("1000")).setScale(3);
        final BigDecimal shopAAmountWithDiscount = new BigDecimal("0.8").multiply(shopAAmount).setScale(3);
        final BigDecimal shopC2Amount = new BigDecimal("4").multiply(new BigDecimal("200")).setScale(3);
        final BigDecimal shopC2AmountWithDiscount = new BigDecimal("0.5").multiply(shopC2Amount).setScale(3);
        final BigDecimal shopC3AmountWithDiscount;
        final BigDecimal shopC3Amount = shopC3AmountWithDiscount = new BigDecimal("10").multiply(new BigDecimal("100")).add(new BigDecimal("4").multiply(new BigDecimal("200"))).setScale(3);
        final BigDecimal amountWithoutDiscounts = shopAAmount.add(shopC2Amount).add(shopC3Amount).setScale(3);
        final BigDecimal amountWithDiscounts = shopAAmountWithDiscount.add(shopC2AmountWithDiscount).add(shopC3AmountWithDiscount).setScale(3);
        final BigDecimal systemProfit = amountWithDiscounts.multiply(new BigDecimal("0.05")).setScale(3);
        final BigDecimal shopAAmountReal = shopAAmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        final BigDecimal shopC2AmountReal = shopC2AmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        final BigDecimal shopC3AmountReal = shopC3AmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        
        System.out.println(amountWithDiscounts.equals(orderOperations.getFinalPrice(order)));
        System.out.println(amountWithoutDiscounts.subtract(amountWithDiscounts)+" "+orderOperations.getDiscountSum(order)+"greska----------------------");
        
        System.out.println(amountWithDiscounts+" "+ transactionOperations.getBuyerTransactionsAmmount(buyer));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopA)+" "+new BigDecimal("0").setScale(3));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopC2)+" "+new BigDecimal("0").setScale(3));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopC3)+" "+new BigDecimal("0").setScale(3));
        System.out.println(new BigDecimal("0").setScale(3)+" "+transactionOperations.getSystemProfit());
        
        
        generalOperations.time(2);
        System.out.println(initialTime.equals(orderOperations.getSentTime(order)));
        System.out.println(orderOperations.getRecievedTime(order)==null);
        System.out.println(orderOperations.getLocation(order)==cityA);
        generalOperations.time(9);
        System.out.println(orderOperations.getLocation(order)==cityA);
        generalOperations.time(8);
        System.out.println(orderOperations.getLocation(order)==cityC5);
        generalOperations.time(5);
        System.out.println(orderOperations.getLocation(order)==cityB);
        System.out.println(new Date(receivedTime.getTimeInMillis())+" "+(new Date(orderOperations.getRecievedTime(order).getTimeInMillis())));
        System.out.println(shopAAmountReal+" "+ transactionOperations.getShopTransactionsAmmount(shopA));
        System.out.println(shopC2AmountReal+" "+ transactionOperations.getShopTransactionsAmmount(shopC2));
        System.out.println(shopC3AmountReal+" "+ transactionOperations.getShopTransactionsAmmount(shopC3));
        System.out.println(systemProfit+" "+ transactionOperations.getSystemProfit());
        final int shopATransactionId = transactionOperations.getTransactionForShopAndOrder(order, shopA);
        System.out.println(-1L +" "+ shopATransactionId);
        System.out.println(new Date(receivedTime.getTimeInMillis()));
        System.out.println(new Date( transactionOperations.getTimeOfExecution(shopATransactionId).getTimeInMillis()));
        generalOperations.time(2);
        
        //testirnaje procedura koje nisu testirane
        System.out.println("getTransactionforBuyer():");//+transactionOperations.getTransationsForBuyer(buyer).get(0));
        List<Integer> tfb=transactionOperations.getTransationsForBuyer(buyer);
        for(Integer t:tfb){
            System.out.println(t);
        }
        
        System.out.println("getTransactionForBuyersOrder():"+transactionOperations.getTransactionForBuyersOrder(order));
        
        System.out.println("getAmoundThatBuyerPaidForOrder"+transactionOperations.getAmmountThatBuyerPayedForOrder(order));
        System.out.println("getAmountThatShopReceivedForAnOrder():"+transactionOperations.getAmmountThatShopRecievedForOrder(1, 5));
        System.out.println("getBuyer():"+orderOperations.getBuyer(order2));
        /*System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();*/
        
        
    }

}
