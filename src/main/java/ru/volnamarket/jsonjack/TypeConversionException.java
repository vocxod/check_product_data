/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

/**
 *
 * @author vragos
 */

class First {
    String firstName;
}

class Second extends First {
    String secondName;
}

class Address {
    String postIndex;
}

public class TypeConversionException {
    public static void main(String[] args){
        First myName = new First();
        // Second mySecorndName = (First) myName;
        char charB = '1';
        byte byteB = 0x0F;
        short shortB = 1234;
        int intB = 65432;
        long longB = 1122334455L;
        float floatB = 2.71F;
        double doubleB = 12.4D;
              
        char myChar2 = (char)shortB;
        char myChar3 = (char)intB;
        char myChar4 = (char)longB;
        char myChar5 = (char)floatB;
        char myChar6 = (char)doubleB;
        
    }
}
