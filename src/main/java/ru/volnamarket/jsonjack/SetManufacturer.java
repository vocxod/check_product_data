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
import java.time.LocalDateTime;
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

    public static String getManufFromAttr(int iProductId) throws SQLException {
      String sReturn="";
      String sSql = "SELECT pa.text FROM oc_product_attribute AS pa WHERE pa.product_id=? "
        +" AND pa.attribute_id=3";
      try ( Connection connection = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/volna",
        "volna", "bBD65855ZLzl@@@###");) 
      {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // int iManufacturerId = upsertManuf(sManufName);
        // connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement(sSql);
        statement.setInt(1, iProductId);
        // System.out.println(statement.toString());
        ResultSet resultSet = statement.executeQuery();
        while(resultSet.next()){
          sReturn = resultSet.getString("text");
          //System.out.print(ANSI_GREEN + "[" + sReturn + "]" + ANSI_RESET);
        }
      } catch (SQLException e) {
        System.out.println(e.getMessage());
       } catch (Exception e){
           e.printStackTrace();
       }
      return sReturn;
    }

    public static void main(String args[]) throws SQLException, IOException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
        {
           Class.forName("com.mysql.cj.jdbc.Driver");
           String sSql = "SELECT product_id FROM oc_product";
           PreparedStatement statement = connection.prepareStatement(sSql);
           ResultSet resultSet = statement.executeQuery();
           while(resultSet.next()){
              int iProductId = resultSet.getInt("product_id");
              // System.out.println(iProductId);
              String sManufName = getManufFromAttr(iProductId);
              if(!sManufName.equals("")){
                  int iManufacturerId = upsertManuf(sManufName);
                  updateProduct(iProductId, iManufacturerId);
              }
              // System.out.println("sManufName:" + sManufName);
              // System.exit(0);
           }
        } catch(SQLException e) {
            System.out.println(e.getMessage());  
        } catch(Exception e) {
            System.out.println(e.getMessage());  
        }
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

    public static int upsertManuf(String sManufName) throws SQLException {
        int iManufacturerId = 0;
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sSql = "SELECT m.manufacturer_id, m.name, ms.store_id FROM "
                        + "oc_manufacturer AS m LEFT JOIN oc_manufacturer_to_store AS ms "
                        + "ON m.manufacturer_id=ms.manufacturer_id WHERE m.name= ?";
            // Statement statement;
            PreparedStatement pStatement = connection.prepareStatement(sSql);
            pStatement.setString(1, sManufName);
            ResultSet resultSet;
            resultSet = pStatement.executeQuery();
            if(resultSet != null && resultSet.next()){
                iManufacturerId = resultSet.getInt("manufacturer_id");
            } else {
                String sSqlInsertManuf = "INSERT INTO oc_manufacturer (name, sort_order) " 
                    +"VALUES ( ?, 1500 )";
                PreparedStatement pInsertStatement = connection.prepareStatement(
                    sSqlInsertManuf, Statement.RETURN_GENERATED_KEYS);
                pInsertStatement.setString(1, sManufName);
                int iInsertedRows = pInsertStatement.executeUpdate();
                if ( iInsertedRows == 1){
                  try (ResultSet keys = pInsertStatement.getGeneratedKeys()) {
                      if (keys.next()){
                          iManufacturerId = keys.getInt(1);
                          String sSqlInsertManufToStore = "INSERT INTO oc_manufacturer_to_store " 
                            + "(manufacturer_id, store_id) VALUES (?, ?)";
                          PreparedStatement preparedLast = connection
                              .prepareStatement(sSqlInsertManufToStore);
                          preparedLast.setInt(1, iManufacturerId);
                          preparedLast.setInt(2, 0);
                          iInsertedRows = preparedLast.executeUpdate();
                       }
                  }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cause: " + e.getCause());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("SQL error code: " + e.getErrorCode());
        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        } finally {
           
        }
        return iManufacturerId;
    }
    
    public static void updateProduct(int iProductId, int iManufacturerId) throws SQLException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
       {
           Class.forName("com.mysql.cj.jdbc.Driver");
           String sDateTime = "2022-07-31 18:00:00"; 
           String sSql = "UPDATE oc_product SET manufacturer_id=?, date_modified=? WHERE product_id=?";
           PreparedStatement statement = connection.prepareStatement(sSql);
           statement.setInt(1, iManufacturerId);
           statement.setString(2, sDateTime);
           statement.setInt(3, iProductId);
           statement.executeUpdate();
       } catch (SQLException e) {
           e.printStackTrace();
           System.out.println(e.getMessage());
       } catch (Exception e){
           e.printStackTrace();
       }
    }
    
}
