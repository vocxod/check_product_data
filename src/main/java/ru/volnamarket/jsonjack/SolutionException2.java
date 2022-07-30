package ru.volnamarket.jsonjack;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

/**
 *
 * @author vragos
 */
public class SolutionException2 {
    // main method  

    public static void main(String args[]) {

        Scanner scanner = new Scanner(System.in);        
        int iKey = scanner.nextInt();
        
        label:
        System.out.println("1 - System.exit(0)  2 - break. Check finally.");
        do
        try {
            
            if(iKey == 1){
                System.out.println("Exit");
                System.exit(0);
            }
            
            if(iKey == 3){
                break;
            }
            
            System.out.println("Start ");
            
            // instantiating the object of ArrayList  
            ArrayList<String> list = new ArrayList<>();
            // adding elements to the ArrayList  
            list.add("Nirnay");
            list.add("Anu");
            list.add("Swara");
            list.add("Pavan");

            // creating the iterator object to iterate the list  
            ListIterator<String> it = list.listIterator();
            // using the next() method before remove()  
            it.next();
            // removing the element without moving to first position  
            it.remove();
            // displaying new ArrayList  
            System.out.println("List after removing the first element: "
                    + list);
            System.out.println("FINISH");
        } catch (Exception e) {
            System.out.println("EXCEPTION: " + e.getMessage());
        }
        finally {
            System.out.println("FINALLY");
        }
        while(iKey != 3);
    }
}
