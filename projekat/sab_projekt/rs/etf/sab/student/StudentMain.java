package rs.etf.sab.student;
/*
import operations.*;
import org.junit.Test;
import student.*;
import tests.TestHandler;
import tests.TestRunner;
*/
//import java.util.Calendar;
//import sab_projekat.*;
import rs.etf.sab.operations.ArticleOperations;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;
import rs.etf.sab.operations.ShopOperations;
import rs.etf.sab.operations.TransactionOperations;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

public class StudentMain {

    public static void main(String[] args) {

        ArticleOperations articleOperations = new article(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new buyer();
        CityOperations cityOperations = new city();
        GeneralOperations generalOperations = new general();
        OrderOperations orderOperations = new order();
        ShopOperations shopOperations = new shop();
        TransactionOperations transactionOperations = new transact();

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }
}
