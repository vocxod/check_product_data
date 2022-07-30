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
public class SetSizes {

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

    public static void main(String args[]) throws IOException {
        prn("Start");
        // createFilters("Start create filters...");
        setSizes("Set sizes to products");
    }

    public static void setSizes(String sMessage) {
        prn(sMessage);
        try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) {
            Statement statement;
            statement = connection.createStatement();
            ResultSet resultSet;

            int iProductId = 0;
            int iProductCount = 0;
            int iMatchesCount = 0;
            int iAttributeId = 0;
            int[] aDimension = new int[2]; // for place parsed data

            String sSql;
            String sProductAttribute;
            String[] regex = {"([0-9]+)([x|X|х|Х])([0-9]+)(.*)", "([0-9]+)(.*)"};
            Pattern pattern;
            Matcher matcher;
            int iProductSizes = 0; // count size data, exist in one attrribute
            boolean matchResult;
                sSql = "SELECT pa.product_id, pa.text, pa.attribute_id FROM oc_product_attribute AS pa "
                    + "WHERE pa.attribute_id IN (11, 12, 40, 125, 217)";
//                      + "WHERE pa.attribute_id IN (40) LIMIT 10";
               resultSet = statement.executeQuery(sSql);

            int badCount40 = 0;
            int iSizeX = 0;
            int iSizeY = 0;
            int iSizeZ = 0;
            ArrayList<String> aSqlCommandSet = new ArrayList<String>();
            ArrayList<String> aSmallSet = new ArrayList<String>();
            while (resultSet.next()) {
                iProductCount += 1;
                iProductId = resultSet.getInt("product_id");
                sProductAttribute = resultSet.getString("text");
                iAttributeId = resultSet.getInt("attribute_id");
                //System.out.println(ANSI_GREEN + iProductId + ANSI_RESET);

                for (int ii = 0; ii < regex.length; ii++) {
                    // prn("Try REGEX: " + regex[ii]);
                    pattern = Pattern.compile(regex[ii], Pattern.CASE_INSENSITIVE);
                    // System.out.println(ANSI_RED + regex[ii] + ANSI_RESET);
                    matcher = pattern.matcher(sProductAttribute);
                     
                    matcher = pattern.matcher(sProductAttribute);
                    // case 1
                    matchResult = matcher.matches();
                    // case 2
                    // matchResult = Pattern.compile(regex[ii]).matcher(sProductAttribute).matches();
                    // case 3
                    matchResult = Pattern.matches(regex[ii], sProductAttribute);
                    if (matchResult) {
                        iMatchesCount += 1;
                        iProductSizes = 0;
                        // System.out.print(sProductAttribute + " ");
                        if(iAttributeId == 40){
                          if(matcher.groupCount() != 4){
                            badCount40 += -1;
                            System.out.println("PID:" + iProductId + ":G:" + matcher.group( matcher.groupCount() ));
                          } else {
                            for (int g = 1; g <= matcher.groupCount(); g++) {
                                if (matcher.group(g) != null) {
                                    iProductSizes += 1;
                                    /*
                                    System.out.print("[" + ANSI_GREEN + g + ANSI_RESET + "]" 
                                        + matcher.group(g));
                                        */
                                }
                            }
                            iSizeX = (int)Double.parseDouble(matcher.group(1));
                            iSizeY = (int)Double.parseDouble(matcher.group(3));
                            // System.out.print(" [" + iSizeX + " : " + iSizeY + "]");
                            // set size data in Product
                            aSmallSet = upsertProduct(iProductId, iAttributeId, iSizeX, iSizeY);
                            for(String sCommand: aSmallSet){
                              aSqlCommandSet.add(sCommand);
                            }
                            // upsert in oc_product_attribute data to length(217) and width(12)
                            aSmallSet = upsertProductAttribute(iProductId, iAttributeId, iSizeX, iSizeY);
                            for(String sCommand: aSmallSet){
                              aSqlCommandSet.add(sCommand);
                            }
                          }
                        } else {
                          for (int g = 1; g <= matcher.groupCount(); g++) {
                              if (matcher.group(g) != null) {
                                  iProductSizes += 1;
                                  /*
                                  System.out.print("[" + ANSI_BLUE + g + ANSI_RESET + "]" 
                                      + matcher.group(g));
                                      */
                              }
                          }
                          iSizeX = (int)Double.parseDouble(matcher.group(1));
                          // System.out.print(" {" + iSizeX + "}");
                          aSmallSet = upsertProduct(iProductId, iAttributeId, iSizeX);
                          for(String sCommand: aSmallSet){
                            aSqlCommandSet.add(sCommand);
                          }
                          // upsert in oc_product_attribute data to length(217) and width(12)
                          // aSmallSet = upsertProductAttribute(iProductId, iAttributeId, iSizeX);
                          /*
                          for(String sCommand: aSmallSet){
                            aSqlCommandSet.add(sCommand);
                          }
                          */
                        }
                          
                        // System.out.print("\n");

                        break;
                        // System.exit(0);
                    } else {
                      /*
                        System.out.println(ANSI_BLUE + iProductId + " : " + ANSI_GREEN + sProductAttribute
                                + " : " + ANSI_YELLOW + matcher.matches() + ANSI_RESET);
                                */
                    }
                    
                }
            }
            sqlExecutor(aSqlCommandSet);
            prn("Products: " + iProductCount + " Matches: " + iMatchesCount + " BadCount40:" + badCount40);

            // Patterns
            // (\d{1,3})\s(см{0,2})
            // (\d{0,3})(\.{0,1})(x{0,1})(\d{0,3})(\s{0,1})(см{0,1})
            // (\d{1,3})(\.*)(\d{0,3}) 65 см
            // First stage - check ALL values to pattern
        } catch (Exception e) {
            prn("SQL error: " + e.toString());
        }
    }

    // System.out.print(" {" + iSizeX + "}");
    public static ArrayList<String> upsertProduct(int iProductId, int iAttributeId, int iSizeX){
      int iLength = 0;
      int iWidth = 0;
      ArrayList aSmallSet = new ArrayList<String>();

      java.util.Date dt = new java.util.Date();
      java.text.SimpleDateFormat sdf = 
      new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String sDateModified = sdf.format(dt);

      if(iAttributeId == 11){
        aSmallSet.add("UPDATE oc_product SET date_modified=\"" + sDateModified + "\", height=" + iSizeX
            + " WHERE product_id=" + iProductId);
      }
      if(iAttributeId == 12){
        aSmallSet.add("UPDATE oc_product SET date_modified=\"" + sDateModified + "\",  width=" + iSizeX
            + " WHERE product_id=" + iProductId);
      }
      if(iAttributeId == 125){
        aSmallSet.add("UPDATE oc_product SET date_modified=\"" + sDateModified + "\",  length=" + iSizeX 
            + " WHERE product_id=" + iProductId);
      }
      if(iAttributeId == 217){
        aSmallSet.add("UPDATE oc_product SET date_modified=\"" + sDateModified + "\", length=" + iSizeX 
            + " WHERE product_id=" + iProductId);
      }
      return aSmallSet;
    }


    // upsert in oc_product_attribute data to length(217) and width(12)
    // @deprecated not need to invoke!
    public static ArrayList<String> upsertProductAttribute(int iProductId, int iAttributeId, int iSizeX){
      int iLength = 0;
      int iWidth = 0;
      ArrayList<String> aSmallSet = new ArrayList<String>();
      return aSmallSet;
      /*
      if(iSizeX >= iSizeY){
        iLength = iSizeX;
        iWidth = iSizeY;
      } else {
        iLength = iSizeY;
        iWidth = iSizeX;
      }
      // delete first
      aSmallSet.add("DELETE FROM oc_product_attribute WHERE product_id=" + iProductId
          + " AND attribute_id IN (" + iAttributeId + ") ");
      // insert after
      aSmallSet.add("INSERT INTO oc_product_attribute SET product_id=" + iProductId
          + ", attribute_id=" + iAttributeId + ", text=" + iSizeX + ", language_id=1");
      return aSmallSet;
      */
    }


    // System.out.print(" {" + iSizeX + "}");
    public static ArrayList<String> upsertProduct(int iProductId, int iAttributeId, int iSizeX, int iSizeY){
      int iLength = 0;
      int iWidth = 0;
      ArrayList aSmallSet = new ArrayList<String>();
      if(iSizeX >= iSizeY){
        iLength = iSizeX;
        iWidth = iSizeY;
      } else {
        iLength = iSizeY;
        iWidth = iSizeX;
      }

      java.util.Date dt = new java.util.Date();
      java.text.SimpleDateFormat sdf = 
      new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String sDateModified = sdf.format(dt);
      
      if(iAttributeId == 40){
        aSmallSet.add("UPDATE oc_product SET date_modified=\"" + sDateModified + "\", length=" + iSizeX 
            + ", width=" + iSizeY + " WHERE product_id=" + iProductId);
      }
      return aSmallSet;
    }
    // upsert in oc_product_attribute data to length(217) and width(12)
    public static ArrayList<String> upsertProductAttribute(int iProductId, int iAttributeId, int iSizeX, int iSizeY){
      int iLength = 0;
      int iWidth = 0;

      ArrayList<String> aSmallSet = new ArrayList<String>();
      if(iSizeX >= iSizeY){
        iLength = iSizeX;
        iWidth = iSizeY;
      } else {
        iLength = iSizeY;
        iWidth = iSizeX;
      }
      if(iAttributeId == 40){
        // delete first
        aSmallSet.add("DELETE FROM oc_product_attribute WHERE product_id=" + iProductId
            + " AND attribute_id IN (12, 217, " + iAttributeId + ") ");
        // insert after
        aSmallSet.add("INSERT INTO oc_product_attribute SET product_id=" + iProductId
            + ", attribute_id=" + "217" + ", text=" + iSizeX + ", language_id=1");
        aSmallSet.add("INSERT INTO oc_product_attribute SET product_id=" + iProductId
            + ", attribute_id=" + "12" + ", text=" + iSizeY + ", language_id=1");
      }
      return aSmallSet;

    }

    public static int getProductId(Product product) {
        return product.getProduct_id();
    }

    public static void sqlExecutor(ArrayList<String> aSqlCommandSet){
     try ( Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/volna",
                "volna", "bBD65855ZLzl@@@###");) 
      {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // connection.setAutoCommit(false); // disable auto commit
        Statement statement;
        statement = connection.createStatement();
        ResultSet resultSet;
        for(String item: aSqlCommandSet){
          // System.out.println(item);
          statement.executeUpdate(item);
        }
      } catch (Exception e) {
        System.out.println(e.toString());
      }
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
                if (iFilterGroupId > 0 && iFilterId > 0) {
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

    public static void upsertFilterToCategory(int iFilterId, Product product) {

    }

    public static void upsertFilterToProduct(int iFilterId, Product product) {
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
            if (resultSet.next()) {
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
        } catch (Exception e) {
            prn(ANSI_RED + "ERROR:" + e.toString() + ANSI_RESET);
        }
    }

}
