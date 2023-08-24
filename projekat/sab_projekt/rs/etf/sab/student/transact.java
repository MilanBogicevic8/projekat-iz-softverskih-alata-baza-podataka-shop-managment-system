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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.TransactionOperations;

/**
 *
 * @author Korisnik
 */
public class transact implements TransactionOperations {

    public transact() {
    }

    private final Connection connection = DB.getInstance().getConnection();
    @Override
public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
    BigDecimal sum = BigDecimal.valueOf(-1).setScale(3);

    try {
        String query = "SELECT COALESCE(SUM(Iznos),0) FROM Transakcija WHERE IdP IS NULL AND IdPor IN (SELECT IdPor FROM Porudbina WHERE IdK = ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, buyerId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            sum = resultSet.getBigDecimal(1);
            if (sum == null) {
                sum = BigDecimal.valueOf(-1).setScale(3);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return sum;
}

    @Override
public BigDecimal getShopTransactionsAmmount(int shopId) {
    BigDecimal sum = BigDecimal.ZERO.setScale(3);

    try {
        String query = "SELECT COALESCE(SUM(Iznos),0) FROM Transakcija WHERE IdP = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, shopId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            sum = resultSet.getBigDecimal(1).setScale(3);
            if (sum == null) {
                sum = BigDecimal.valueOf(-1).setScale(3);
            }
        }else{
            return BigDecimal.ZERO.setScale(3);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return sum;
}

    @Override
public List<Integer> getTransationsForBuyer(int buyerId) {
    List<Integer> transactionIds = new ArrayList<>();

    try {
        String query = "SELECT IdT FROM Transakcija WHERE IdP IS NULL AND IdPor IN (SELECT IdPor FROM Porudbina WHERE IdK = ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, buyerId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int transactionId = resultSet.getInt("IdT");
            transactionIds.add(transactionId);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }

    if (transactionIds.isEmpty()) {
        return null;
    }

    return transactionIds;
}

//ovde verovatno treba i transakcije za IdP znaci da se izbaci ovo is null?
    @Override
public int getTransactionForBuyersOrder(int orderId) {
    try {
        String query = "SELECT IdT FROM Transakcija WHERE IdPor = ? and IdP IS NULL";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getInt("IdT");
        } else {
            return -1; // Nema transakcije za dati order
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return -1; // Greška pri izvršavanju upita
    }
}

    @Override
public int getTransactionForShopAndOrder(int orderId, int shopId) {
    try {
        String query = "SELECT IdT FROM Transakcija WHERE IdPor = ? AND IdP = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        statement.setInt(2, shopId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getInt("IdT");
        } else {
            return -1; // Nema odgovarajuće transakcije za dati order i shop
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return -1; // Greška pri izvršavanju upita
    }
}

    @Override
public List<Integer> getTransationsForShop(int shopId) {
    List<Integer> transactionIds = new ArrayList<>();

    try {
        String query = "SELECT IdT FROM Transakcija WHERE IdP = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, shopId);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int transactionId = resultSet.getInt("IdT");
            transactionIds.add(transactionId);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return null; // Greška pri izvršavanju upita
    }

    if(transactionIds.isEmpty()){
        return null;
    }
    return transactionIds;
}

    @Override
public Calendar getTimeOfExecution(int transactionId) {
    Calendar executionTime = null;

    try {
        //String query = "SELECT Datum FROM Transakcija WHERE IdT = ?";
        /*String query1=  "select p.DatumKreiranja\n" +
                        "from Transakcija t join Porudbina p on t.IdPor=p.IdPor\n" +
                        "where t.IdT=?";*/
        String query2="select case \n" +
                        "when t.IdP is NULL then p.DatumKreiranja\n" +
                        "else p.DatumPrijema\n" +
                        "end as Datum\n" +
                        "from Porudbina p join Transakcija t on p.IdPor=t.IdPor\n" +
                        "where t.IdT=?";
        PreparedStatement statement = connection.prepareStatement(query2);
        statement.setInt(1, transactionId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Date timestamp = resultSet.getDate("Datum");
            if (timestamp != null) {
                executionTime = Calendar.getInstance();
                executionTime.setTime(timestamp);
            }
        }else{
            return null;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }

    return executionTime;
}

    @Override
public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
    BigDecimal amountPaid = BigDecimal.valueOf(-1).setScale(3);

    try {
        String query = "SELECT Iznos FROM Transakcija WHERE IdPor = ? AND IdP IS NULL";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, orderId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            amountPaid = resultSet.getBigDecimal("Iznos");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return BigDecimal.valueOf(-1).setScale(3);
    }

    return amountPaid.setScale(3);
}

    @Override
public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
    BigDecimal amountReceived = BigDecimal.ZERO.setScale(3);

    try {
        String query = "SELECT coalesce(sum(Iznos),0)  FROM Transakcija WHERE IdP = ? AND IdPor = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, shopId);
        statement.setInt(2, orderId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            amountReceived = resultSet.getBigDecimal(1).setScale(3);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }

    return amountReceived;
}

    @Override
public BigDecimal getTransactionAmount(int transactionId) {
    BigDecimal amount = null;

    try {
        String query = "SELECT Iznos FROM Transakcija WHERE IdT = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, transactionId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            amount = resultSet.getBigDecimal("Iznos").setScale(3);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return amount;
}


    @Override
public BigDecimal getSystemProfit() {
    BigDecimal profit = BigDecimal.ZERO.setScale(3);

    try {
        String query = "SELECT COALESCE(SUM(ZaSistem), 0) AS Profit FROM Porudbina WHERE Stanje = 'arrived'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        if (resultSet.next()) {
            profit = resultSet.getBigDecimal("Profit").setScale(3);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return profit;
}
    
}
