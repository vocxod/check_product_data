/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.BatchUpdateException;
import java.util.Arrays;
/**
 *
 * @author vragos
 */
public class SetManufacturer {
    // for colored text
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    public static void main(String args[]) throws SQLException {
        System.out.println(ANSI_GREEN + "Start set manufacturer_id in oc_product" + ANSI_RESET);
        Scanner scanner = new Scanner(System.in);
        String sManufName = scanner.nextLine();
        upsertManuf(sManufName);
    } 
   
    public static boolean ignoreSQLException(String sqlState) {

        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }

        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;

        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;

        return false;
    }

    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (ignoreSQLException(
                    ((SQLException)e).
                    getSQLState()) == false) 
                {

                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                        ((SQLException)e).getSQLState());

                    System.err.println("Error Code: " +
                        ((SQLException)e).getErrorCode());

                    System.err.println("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while(t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static void upsertManuf(String sManufName) throws SQLException {
        
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //connection.setAutoCommit(false);
            String sSql = "SELECT m.manufacturer_id, m.name, ms.store_id FROM "
                        + "oc_manufacturer AS m LEFT JOIN oc_manufacturer_to_store AS ms "
                        + "ON m.manufacturer_id=ms.manufacturer_id WHERE m.name= ? ";
            // Statement statement;
            // statement = connection.createStatement();
            PreparedStatement pStatement = connection.prepareStatement(sSql);
            pStatement.setString(1, sManufName);

            // System.out.println(ANSI_CYAN + pStatement + ANSI_RESET);
            // System.exit(0);

            ResultSet resultSet;

            // sSql = "SELECT * FROM oc_manufacturer";
            int iManufacturerId;
            // String sManufName;
            resultSet = pStatement.executeQuery();

            if(resultSet != null && resultSet.next()){
                System.out.println(ANSI_GREEN + "Manufacturer " + sManufName + " exist." 
                        + ANSI_RESET);
            } else {
                System.out.println(ANSI_RED + "Manufacturer " + sManufName + " not exist." 
                        + ANSI_RESET);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cause: " + e.getCause());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("SQL error code: " + e.getErrorCode());
            System.out.println("***");
            // printSQLException(e);
        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
           
        }

        
    }
    
    public static void updateProduct(String sSql) throws IOException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
       {
           Class.forName("com.mysql.cj.jdbc.Driver");
           connection.setAutoCommit(false);
           Statement statement = connection.createStatement();
           statement.executeUpdate(sSql);
       } catch (Exception e){
           e.printStackTrace();
       }
    }
    
}
