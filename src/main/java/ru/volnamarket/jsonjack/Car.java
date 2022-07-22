/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

/**
 *
 * @author vragos
 */
public class Car {
    private String color;
    private String type;

    Car(){
        super();
        this.color = "Unknow";
        this.type = "Unknow";
    }
    
    Car(String sColor, String sType){
        super();
        this.color = sColor;
        this.type = sType;
    }
    
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString(){
        return this.type + " : " + this.color;
    }
}
