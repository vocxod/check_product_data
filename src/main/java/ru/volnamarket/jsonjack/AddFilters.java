/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set; // Import the Set class
import java.util.HashSet; // Import the HashSet class
import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class AddFilters {

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
    Scan JSON files and create filters on OpenCard store
    where ttribute is LINK
     */
    public static void prn(String sData) {
        System.out.println(sData);
    }

    public static void createFilters(String sStart) throws IOException {
        prn(sStart);
        int fullAttributeErrors = 0;
        int iFilesCompleted = 0;
        ArrayList<String> badFiles = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<String> startDirs = new ArrayList<>();
            File jsonDir = new File(ROOT_JSON_PATH);
            if (jsonDir.isDirectory()) {
                File arr[] = jsonDir.listFiles();
                for (File startDir : arr) {
                    if (startDir.isDirectory()) {
                        // place dirs for compute late
                        // System.out.println(ANSI_GREEN + startDir.getName() + ANSI_RESET);
                        startDirs.add(ROOT_JSON_PATH + startDir.getName());
                    }
                }
                int iLast = startDirs.size();
                for (int i = 0; i < iLast - 1; i++) {
                    File currentDir = new File(startDirs.get(i));
                    System.out.println(ANSI_GREEN + "StartDir:" + ANSI_YELLOW
                            + currentDir.getName()
                            + ANSI_RESET);
                    File[] jsonProductFiles = currentDir.listFiles();
                    if (jsonProductFiles == null) {
                        continue;
                    } else {
                        System.out.print(".");
                    }
                    // continue;
                    for (File jsonProductFile : jsonProductFiles) {

                        iFilesCompleted = iFilesCompleted + 1;

                        if (!jsonProductFile.isFile()) {
                            // compute files only
                            System.out.println(ANSI_PURPLE + jsonProductFile.getName()
                                    + ANSI_RESET);
                            continue;
                        }

                        BufferedReader br = new BufferedReader(new FileReader(jsonProductFile));
                        StringBuilder fileContent = new StringBuilder();
                        String st;
                        while ((st = br.readLine()) != null) {
                            fileContent.append(st);
                        }

                        Product productTwo = objectMapper.readValue(fileContent.toString(),
                                Product.class);

                        FullAttribute[] aFullAttributes = productTwo.getFull_attributes();
                        for (FullAttribute item : aFullAttributes) {
                            if (!item.isValidData()) {
                                fullAttributeErrors = fullAttributeErrors + 1;
                                badFiles.add(jsonProductFile.getAbsolutePath());
                            }
                            if (item.isIs_link() && item.isValidData() && !(item.getName().equals("Категория")
                                    || item.getName().equals("Производитель"))) {
                                // Это наш фильтр (в терминах опенкарта, или "срез" по нашему)
                                // System.out.println(item);
                                UpsertFilter(item, productTwo);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (NullPointerException ex2) {
            System.out.println(ANSI_RED + ex2 + ANSI_RESET);
        }
        System.out.println(ANSI_YELLOW + "Completed " + iFilesCompleted + " files");
        System.out.println(ANSI_GREEN + fullAttributeErrors + " errors founded" + ANSI_RESET);
        Set<String> uniqBadFiles = new HashSet<>(badFiles);
        System.out.println(ANSI_RED + "Bad files: " + uniqBadFiles.size() + " item(s)."
                + ANSI_RESET);
        /*
        FileWriter output = new FileWriter("bad_products_list.txt");
        for(String sItem: uniqBadFiles){
            output.write(sItem + '\n');
        }
        output.close();
         */
    }

    public static void main(String[] args) throws IOException {
        prn("Start");
        createFilters("Start create filters...");
    }

    public static int getProductId(Product product) {
        return product.getProduct_id();
    }

    public static int UpsertFilter(FullAttribute item, Product product) {
        // Insert new filter (or select exist)
        // and return filter_id value
        int iFilterGroupId = 0;
        int iFilterId = 0;
        String sFilterName = item.getValue().substring(0, 1).toUpperCase()
                + item.getValue().substring(1).toLowerCase();
        String sFilterGroupName = item.getName().substring(0, 1).toUpperCase()
                + item.getName().substring(1).toLowerCase();
        String sSqlSelect = "SELECT fg.filter_group_id, f.filter_id, fgd.name, fd.name FROM oc_filter_group AS fg "
                + " LEFT JOIN oc_filter_group_description AS fgd ON fg.filter_group_id=fgd.filter_group_id "
                + " LEFT JOIN oc_filter AS f ON fg.filter_group_id=f.filter_group_id "
                + " LEFT JOIN oc_filter_description AS fd ON f.filter_id=fd.filter_id  "
                + " WHERE fgd.name =\"" + sFilterGroupName + "\" AND fd.name=\"" + sFilterName + "\"";
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // connection.setAutoCommit(false); // disable auto commit
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(sSqlSelect);

            while (resultSet.next()) {
                iFilterGroupId = resultSet.getInt("filter_group_id");
                iFilterId = resultSet.getInt("filter_id");
                // check ounce
                if(iFilterGroupId>0 && iFilterId>0){
                    // prn(ANSI_GREEN + " WELL: " + iFilterGroupId + ":" + iFilterId + ANSI_RESET);
                    // System.exit(0);
                }
                break;
            }

            // BRANCH 2or3 (else) or 1 (if >0)
            // Try get filter_group_id if not exist on first stage
            if (iFilterGroupId == 0) {
                prn(ANSI_GREEN + "Branch 2 or 3" + ANSI_RESET);
                String sSqlFGroup = "SELECT fgd.filter_group_id, fgd.name FROM "
                        + "oc_filter_group AS fg LEFT JOIN oc_filter_group_description AS fgd "
                        + "ON fg.filter_group_id=fgd.filter_group_id LEFT JOIN oc_filter AS f "
                        + "ON fg.filter_group_id=f.filter_group_id LEFT JOIN oc_filter_description "
                        + "AS fd ON f.filter_id=fd.filter_id "
                        + "WHERE fgd.name = \"" + sFilterGroupName + "\"";
                resultSet = statement.executeQuery(sSqlFGroup);
                while (resultSet.next()) {
                    iFilterGroupId = resultSet.getInt("filter_group_id");
                    break;
                }
                if (iFilterGroupId > 0) {
                    prn(ANSI_GREEN + "Branch 2" + ANSI_RESET);
                    // BRANCH 2
                    // в данной СУЩЕСТВУЮЩЕЙ группе  нет значения этого фильтра
                    // need add filter with this filter+group_id
                    String sSqlInsertFilter = "INSERT INTO oc_filter "
                            + "(filter_id, filter_group_id, sort_order)"
                            + " VALUES(null, " + iFilterGroupId + ", 100)";
                    int iFgAffectedRows = statement.executeUpdate(sSqlInsertFilter,
                            Statement.RETURN_GENERATED_KEYS);
                    if (iFgAffectedRows != 0) {
                        try ( ResultSet keys2 = statement.getGeneratedKeys()) {
                            if (keys2.next()) {
                                iFilterId = keys2.getInt(1);
                                // prn("FILTER_ID=" + iFilterId);
                            }
                        }
                        String sInsertFilterDescription = "INSERT INTO "
                                + "oc_filter_description (filter_id, language_id, "
                                + "filter_group_id, name ) VALUES (?, ?, ?, ?)";
                        PreparedStatement pstmt = connection.prepareStatement(sInsertFilterDescription);
                        pstmt.setInt(1, iFilterId);
                        pstmt.setInt(2, 1);
                        pstmt.setInt(3, iFilterGroupId);
                        pstmt.setString(4, sFilterName);
                        iFgAffectedRows = pstmt.executeUpdate();
                    }
                    // end insert FILTER with FG_ID
                } else {
                    prn(ANSI_YELLOW + "Branch 3" + ANSI_RESET);
                    // BRANCH 3 
                    // CREATE GROUP+FILTER(by VALUE)
                    // need insert new filter_group and filter
                    String sSqlInsertFilterGroup = "INSERT INTO oc_filter_group "
                            + "(filter_group_id, sort_order) VALUES (null, \"100\");";
                    int iAffectedRows = statement.executeUpdate(sSqlInsertFilterGroup,
                            Statement.RETURN_GENERATED_KEYS);
                    if (iAffectedRows != 0) {
                        try ( ResultSet keys = statement.getGeneratedKeys()) {
                            if (keys.next()) {
                                iFilterGroupId = keys.getInt(1);
                            }
                            String sSqlInsertGroupDescription = "INSERT INTO "
                                    + "oc_filter_group_description (filter_group_id, language_id, name)"
                                    + " VALUES (" + iFilterGroupId + ", 1, " + "\"" + sFilterGroupName
                                    + "\"" + ")";
                            // prn(sSqlInsertGroupDescription);
                            iAffectedRows = statement.executeUpdate(sSqlInsertGroupDescription,
                                    Statement.RETURN_GENERATED_KEYS);
                            if (iAffectedRows != 0) {
                                String sSqlInsertFilter = "INSERT INTO oc_filter "
                                        + "(filter_id, filter_group_id, sort_order)"
                                        + " VALUES(null, " + iFilterGroupId + ", 100)";
                                iAffectedRows = statement.executeUpdate(sSqlInsertFilter,
                                        Statement.RETURN_GENERATED_KEYS);
                                if (iAffectedRows != 0) {
                                    try ( ResultSet keys2 = statement.getGeneratedKeys()) {
                                        if (keys2.next()) {
                                            iFilterId = keys2.getInt(1);
                                            // prn("FILTER_ID=" + iFilterId);
                                        }
                                    }
                                    String sInsertFilterDescription = "INSERT INTO "
                                            + "oc_filter_description (filter_id, language_id, "
                                            + "filter_group_id, name ) VALUES (?, ?, ?, ?)";
                                    PreparedStatement pstmt = connection.prepareStatement(sInsertFilterDescription);
                                    pstmt.setInt(1, iFilterId);
                                    pstmt.setInt(2, 1);
                                    pstmt.setInt(3, iFilterGroupId);
                                    pstmt.setString(4, sFilterName);
                                    iAffectedRows = pstmt.executeUpdate();
                                }
                            }
                        }
                    } else {
                        throw new Exception("Error add new filter in oc_filter_group");
                    }
                }
            } else {
                // prn(ANSI_GREEN + "Branch 1" + ANSI_RESET);
                // in this place we doing link betwwen filter_id and product_id
                // and we doing link between filter_id and category_id
                upsertFilterToCategory(iFilterId, product);
                // upsertFilterToProduct(iFilterId, product);
            }
/*
            if (iFilterId == 0) {
                connection.rollback();
                return iFilterId;
            }
            connection.commit();
            */
        } catch (Exception e) {
            prn("Some JDBC error");
            prn(e.getMessage());
        }
        /*
        catch (SQLException e) {
            JDBCTutorialUtilities.printSQLException(e);
            if (con != null) {
              try {
                System.err.print("Transaction is being rolled back");
                connection.rollback();
              } catch (SQLException excep) {
                JDBCTutorialUtilities.printSQLException(excep);
              }
            }
        }
         */
        return iFilterId;
    }
    
    public static void upsertFilterToCategory(int iFilterId, Product product){
        
    }
    
    public static void upsertFilterToProduct(int iFilterId, Product product){
        int iProductId = product.getProduct_id();
        // String sSql = "SELECT product_id FROM oc_product_filter WHERE product_id=? AND filter_id=?";
        String sSql = "SELECT product_id FROM oc_product_filter WHERE product_id="
                + iProductId + " AND filter_id="
                + iFilterId;
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            ResultSet resultSet;           
            /*
            Class.forName("com.mysql.cj.jdbc.Driver");
            // connection.setAutoCommit(false); // disable auto commit
            */
            
            /*
            PreparedStatement pstmt = connection.prepareStatement(sSql);
            pstmt.setInt(1, iProductId);
            pstmt.setInt(2, iFilterId);
            resultSet = pstmt.executeQuery();
            `*/
            
            Statement statement;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sSql);
            if(resultSet.next()){
                // prn("Finded!");
            } else {
                String sSqlInsert = "INSERT INTO oc_product_filter (product_id, filter_id) "
                        + "VALUES (?, ?)";
                PreparedStatement pstmt = connection.prepareStatement(sSqlInsert);
                pstmt = connection.prepareStatement(sSqlInsert);
                pstmt.setInt(1, iProductId);
                pstmt.setInt(2, iFilterId);
                pstmt.executeUpdate();
            }
        } catch (Exception e){
            prn(ANSI_RED + "ERROR:" + e.toString() + ANSI_RESET);
        }
    }

}
