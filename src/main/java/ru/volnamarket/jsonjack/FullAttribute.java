/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

/**
 *
 * @author vragos
 */
public class FullAttribute {
    private String name;
    private String value;
    private boolean is_link;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isIs_link() {
        return is_link;
    }

    public void setIs_link(boolean is_link) {
        this.is_link = is_link;
    }

    @Override
    public String toString() {
        return "FullATTR:{" + "name=" + name + ", value=" + value + ", is_link=" + is_link + '}';
    }
    
    public boolean isValidData(){
        Boolean bResult = false;
        if( this.name != null && !this.name.trim().isEmpty()){
            bResult = true;
        }
        return bResult;
            
    }
    
}
