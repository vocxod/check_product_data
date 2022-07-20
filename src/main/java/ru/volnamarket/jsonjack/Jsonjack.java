/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package ru.volnamarket.jsonjack;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author vragos
 */
public class Jsonjack {

    public static void main(String[] args) throws IOException {
        System.out.println("Start app");
        ObjectMapper objectMapper = new ObjectMapper();
        Car car = new Car("yellow", "renault");
        File ff = new File("target/car.json");
        objectMapper.writeValue(ff, car);
    }
}
