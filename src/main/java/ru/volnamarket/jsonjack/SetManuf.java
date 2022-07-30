package ru.volnamarket.jsonjack;

import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set; // Import the Set class
import java.util.HashSet; // Import the HashSet class
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author vragos
 *
 */
public class SetManuf {

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

    private final static String ROOT_JSON_PATH = "/home/vragos/PyDev/DushEvo/json_data/lists/latest/";

    /*
    Scan JSON files and create manufacturers on OpenCard store
    where ttribute is LINK
     */
    public static void main(String args[]) throws IOException {
        System.out.println("Start set manufacturer_id in oc_product");
        // createFilters("Start create filters...");
        setManuf();
    }

    public static void setManuf() {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;

            int iProductId = 0;
            int iProductCount = 0;
            int iMatchesCount = 0;
            int iAttributeId = 0;

            String sSql;
            String sProductAttribute;
            boolean matchResult;

            ArrayList<String> aSqlCommandSet = new ArrayList<String>();
            ArrayList<String> aSmallSet = new ArrayList<String>();

            sSql = "SELECT DISTINCT(pa.text), pa.product_id FROM oc_product_attribute AS pa WHERE "
                    + "pa.attribute_id=3";
            resultSet = statement.executeQuery(sSql);
            
            String sSqlDelete;
            String sSqlInsert;
            String sSqlSelect;
            int iManufacturerId = 0;
            while (resultSet.next()) {
                iProductCount += 1;
                // iProductId = resultSet.getInt("product_id");
                sProductAttribute = resultSet.getString("text");
                iProductId = resultSet.getInt("product_id");
                // iAttributeId = resultSet.getInt("attribute_id");
                ResultSet resultSetSelect;
                sSqlSelect = "SELECT m.manufacturer_id, m.name, ms.store_id FROM "
                        + "oc_manufacturer AS m LEFT JOIN oc_manufacturer_to_store AS ms "
                        + "ON m.manufacturer_id=ms.manufacturer_id WHERE m.name=\"" 
                        + sProductAttribute + "\" LIMIT 1";
                
                resultSetSelect = statement.executeQuery(sSqlSelect);
                
                // System.out.println(sSqlSelect);
                
                if (resultSetSelect.next()) {
                    System.out.print(ANSI_GREEN + ".1" + ANSI_RESET);
                    // skip stis row
                    sSqlInsert = "UPDATE oc_product SET manufacturer_id=" 
                        + iManufacturerId + " WHERE product_id=" + iProductId;
                    updateProduct(sSqlInsert);
                    continue;       
                } else if ("1" == "2") {
                    System.out.print(ANSI_RED + ".2" + ANSI_RESET);
                    sSqlInsert = "INSERT INTO oc_manufacturer (name, image, sort_order) VALUES"
                            + " (\"" + sProductAttribute + "\", " + "\"\"" + ", 101 );";
                    // statement.executeUpdate(sSqlInsert);
                    int iManufAffectedRows = statement.executeUpdate(sSqlInsert,
                            Statement.RETURN_GENERATED_KEYS);
                    if (iManufAffectedRows != 0) {
                        try ( ResultSet keys2 = statement.getGeneratedKeys()) {
                            if (keys2.next()) {
                                iManufacturerId = keys2.getInt(1);
                                System.out.println("MID=" + iManufacturerId);
                                sSqlInsert = "INSERT INTO oc_manufacturer_to_store (manufacturer_id,"
                                        + "store_id) VALUES (" + iManufacturerId + ", 0 ) ";
                                statement.executeUpdate(sSqlInsert);
                                sSqlInsert = "UPDATE oc_product SET manufacturer_id=" 
                                        + iManufacturerId + " WHERE productid=" + iProductId;
                                statement.executeUpdate(sSqlInsert);
                            }
                        }                
                    }
                    System.out.println(sSqlInsert);
                }
                
                
                Runtime.getRuntime().halt(-100);
            }
            // sqlExecutor(aSqlCommandSet);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SQL error: " + e.toString());
        }
    }
    
    public static void updateProduct(String sSql) throws IOException {
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Statement statement;
            statement = connection.createStatement();
            statement.executeUpdate(sSql);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SQL error: " + e.toString());
        }
        
    }
    
}

