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
import java.util.Scanner;

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
    
    public static void main(String args[]) throws IOException {
        System.out.println(ANSI_GREEN + "Start set manufacturer_id in oc_product" + ANSI_RESET);
        Scanner scanner = new Scanner(System.in);
        String sManufName = scanner.nextLine();
        upsertManuf(sManufName);
    } 
    
    public static void upsertManuf(String sManufName) throws IOException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Statement statement;
            String sSql = "SELECT m.manufacturer_id, m.name, ms.store_id FROM "
                        + "oc_manufacturer AS m LEFT JOIN oc_manufacturer_to_store AS ms "
                        + "ON m.manufacturer_id=ms.manufacturer_id WHERE m.name=\"" 
                        + sManufName + "\" ";
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sSql);
            if(resultSet.last()){
                System.out.println(ANSI_RED + "Manufacturer " + sManufName + " not found" 
                        + ANSI_RESET);
                
            } else {
                System.out.println(ANSI_GREEN + "Manufacturer " + sManufName + " present." 
                        + ANSI_RESET);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SQL error: " + e.toString());
        }
        
    }
    
    public static void updateProduct(String sSql) throws IOException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
       {
           Class.forName("com.mysql.cj.jdbc.Driver");
           Statement statement = connection.createStatement();
           statement.executeUpdate(sSql);
       } catch (Exception e){
           e.printStackTrace();
       }
    }
    
}
