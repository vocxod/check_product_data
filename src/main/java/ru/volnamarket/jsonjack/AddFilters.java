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
                                System.out.println(item);
                                UpsertFilter(item);
                                // @todo
                                /*
                                int iFilterId = UpsertFilter(attributeItem);
                                setFilterToProduct(Product productTwo, int iFilterId);
                                int iCategoryId = getCategoryId(Product productTwo);
                                setFilterToCategory(int iCategoryId, int iFilterId);
                                 */
                                System.exit(0);
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

    public static int UpsertFilter(FullAttribute item) {
        // Insert new filter (or select exist)
        // and return filter_id value
        int iFilterGroupId = 0;
        int iFilterId = 0;
        String sFilterName = item.getValue().substring(0, 1).toUpperCase()
                + item.getValue().substring(1).toLowerCase();
        String sFilterGroupName = item.getName().substring(0, 1).toUpperCase()
                + item.getName().substring(1).toLowerCase();
        String sSqlSelect ="SELECT fg.filter_group_id, fgd.name, fd.name FROM oc_filter_group AS fg " +
        " LEFT JOIN oc_filter_group_description AS fgd ON fg.filter_group_id=fgd.filter_group_id " +
        " LEFT JOIN oc_filter AS f ON fg.filter_group_id=f.filter_group_id " +
        " LEFT JOIN oc_filter_description AS fd ON f.filter_id=fd.filter_id  " +
        " WHERE fgd.name =\"" + sFilterGroupName  + "\" AND fd.name=\"" + sFilterName  + "\"";
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection.setAutoCommit(false); // disable auto commit
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(sSqlSelect);

            while (resultSet.next()) {
                iFilterGroupId = resultSet.getInt("filter_group_id");
                // prn("FGid:" + iFilterGroupId);
                break;
            }
            if (iFilterGroupId == 0) {
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
                        String sSqlInsertGroupDescription = "INSERT INTO " +
                                "oc_filter_group_description (filter_group_id, language_id, name)" +
                                " VALUES (" + iFilterGroupId + ", 1, " + "\"" + sFilterGroupName
                                + "\"" + ")";
                        prn(sSqlInsertGroupDescription);
                        iAffectedRows = statement.executeUpdate(sSqlInsertGroupDescription,
                                Statement.RETURN_GENERATED_KEYS);
                        if(iAffectedRows != 0){
                            String sSqlInsertFilter = "INSERT INTO oc_filter " +
                                    "(filter_id, filter_group_id, sort_order)" +
                                    " VALUES(null, " + iFilterGroupId + ", 100)";
                            iAffectedRows = statement.executeUpdate(sSqlInsertFilter,
                                    Statement.RETURN_GENERATED_KEYS);
                            if(iAffectedRows != 0){
                                try(ResultSet keys2 = statement.getGeneratedKeys()){
                                    if(keys2.next()){
                                        iFilterId = keys2.getInt(1);
                                        prn("FILTER_ID=" + iFilterId);
                                    }
                                }
                                String sInsertFilterDescription = "INSERT INTO " +
                                        "oc_filter_description (filter_id, language_id, " +
                                        "filter_group_id, name ) VALUES (?, ?, ?, ?)";
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
            
            if (iFilterId == 0) {
                connection.rollback();
                return iFilterId;
            }            
            connection.commit();
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

}
