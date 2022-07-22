/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package ru.volnamarket.jsonjack;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        System.out.println("Start app");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            
            BufferedReader br = new BufferedReader(new FileReader(ROOT_JSON_PATH
                    + "bide/10_107124.json"));
            StringBuilder fileContent = new StringBuilder();
            String st;
            while ((st = br.readLine()) != null) {
                fileContent.append(st);
            }
            
            Product productTwo = objectMapper.readValue(fileContent.toString(), Product.class);
            for (FullAttribute item: productTwo.getFull_attributes()){
                if(item.getName() == null){
                    // пустая строка
                    fullAttributeErrors = fullAttributeErrors + 1;
                }
            }
            // System.out.println(ANSI_YELLOW +  productTwo + ANSI_RESET);
            // File writeFileProduct = new File(ROOT_JSON_PATH + "java_json/10_107124.json.json");
            // objectMapper.writeValue(writeFileProduct, productTwo);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        System.out.println(ANSI_GREEN + fullAttributeErrors + " errors founded" + ANSI_RESET);
    }
}

