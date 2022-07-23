/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package ru.volnamarket.jsonjack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author vragos
 */
public class Jsonjack {

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

    public static void oldmain(String[] args) throws IOException {
        // 
    }

    public static void main(String[] args) throws IOException {
        int fullAttributeErrors = 0;
        int iFilesCompleted = 0;
        System.out.println("Start app");
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<String> startDirs = new ArrayList<String>();
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
                            + ANSI_RESET );                    
                    File[] jsonProductFiles = currentDir.listFiles();              
                    if (jsonProductFiles == null){
                        continue;
                    } else {
                        System.out.print(".");
                    }
                    // continue;
                    for (File jsonProductFile : jsonProductFiles) {
                        
                        iFilesCompleted = iFilesCompleted + 1;
                        
                        if( !jsonProductFile.isFile() ){
                            // compute files only
                            System.out.println(ANSI_PURPLE + jsonProductFile.getName() + ANSI_RESET);
                            continue;
                        }
                        
                        // System.out.println(ANSI_BLUE + jsonProductFile.getName() + ANSI_RESET);
                        BufferedReader br = new BufferedReader(new FileReader(jsonProductFile));
                        StringBuilder fileContent = new StringBuilder();
                        String st;
                        while ((st = br.readLine()) != null) {
                            fileContent.append(st);
                        }

                        Product productTwo = objectMapper.readValue(fileContent.toString(),
                                Product.class);
/*
                        Product productTwo = new Gson().fromJson(fileContent.toString(),
                                Product.class);
*/                        
                        FullAttribute[] aFullAttributes = productTwo.getFull_attributes();
                        for(FullAttribute item: aFullAttributes){
                            if(!item.isValidData()){
                                fullAttributeErrors = fullAttributeErrors + 1;
                            }
                        }
                        /*
                        for (FullAttribute item : productTwo.getFull_attributes()) {
                            if (item.getName() == null) {
                                // пустая строка
                                fullAttributeErrors = fullAttributeErrors + 1;
                            }
                        }
                        */
                    }
                    /*
                    System.out.println(ANSI_YELLOW +  productTwo + ANSI_RESET);
                    File writeFileProduct = new File(ROOT_JSON_PATH +
                            "java_json/10_107124.json.json");
                    objectMapper.writeValue(writeFileProduct, productTwo);                    
                    System.exit(0);
                     */
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (NullPointerException ex2){
            System.out.println(ANSI_RED + ex2 + ANSI_RESET);
        }
        System.out.println(ANSI_YELLOW + "Completed " + iFilesCompleted + " files");
        System.out.println(ANSI_GREEN + fullAttributeErrors + " errors founded" + ANSI_RESET);
    }
}
