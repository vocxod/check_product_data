/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.volnamarket.jsonjack;

/**
 *
 * @author vragos
 */
public class Product {
    private String href;
    private String title;
    private int price;
    private int product_id;
    private Attribute[] attributes;
    private String[] images;
    private FullAttribute[] full_attributes;
    private String[] opencart_images;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public FullAttribute[] getFull_attributes() {
        return full_attributes;
    }

    public void setFull_attributes(FullAttribute[] full_attributes) {
        this.full_attributes = full_attributes;
    }

    public String[] getOpencart_images() {
        return opencart_images;
    }

    public void setOpencart_images(String[] opencart_images) {
        this.opencart_images = opencart_images;
    }
    
    
    
    Product(){
        this.product_id = 0;
    }
    
    Product(String href, int product_id){
        this.href = href;
        this.product_id = product_id;
    }
    
    Product(String href, String title, int price, int product_id){
        this.href = href;
        this.title = title;
        this.price = price;
        this.product_id = product_id;
    }

    @Override
    public String toString() {
        return "Product:{" + "HREF=" + href + " TITLE=" + title + " PRICE= " + price 
                + " PID=" + product_id + "}";
    }
    
    
            
}
