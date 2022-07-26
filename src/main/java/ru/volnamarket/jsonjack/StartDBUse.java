/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

import java.sql.*;

/**
 *
 * @author vragos
 */
public class StartDBUse {
    
   static final String DB_URL = "jdbc:mysql://localhost/volna";
   static final String USER = "volna";
   static final String PASS = "bBD65855ZLzl@@@###";
   static final String QUERY = "SELECT * FROM oc_filter_group_description WHERE language_id=1";

   public static void main(String[] args) {
      // This use BLOCK (throws for some resources)
      try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(QUERY);) 
      {
         // Extract data from result set
         while (rs.next()) {
            // Retrieve by column name
            System.out.print("ID: " + rs.getInt("filter_group_id"));
            System.out.println(", Name: " + rs.getString("name"));
         }
      } catch (SQLException e) {
         e.printStackTrace();
      } finally {
          // conn.close();
      } 
      
   }
}