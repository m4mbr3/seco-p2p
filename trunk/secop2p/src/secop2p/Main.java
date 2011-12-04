/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javaapplication1;

import java.sql.SQLException;

/**
 *
 * @author eros
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ServiceRepository sr = new ServiceRepository();
        Service[] sl = sr.getServicesList();
        for(Service s : sl)
            System.out.println( s.getName() );
        EngineInfo[] el = sr.getEnginesList();
        for(EngineInfo e : el)
            System.out.println( e.getName() );
    }

}
