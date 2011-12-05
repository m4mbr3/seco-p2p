/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.sql.SQLException;

/**
 *
 * @author eros
 */


public class Main {

    public void printAllServices() throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        Service[] sl = sr.getServicesList();
        for(Service s : sl)
            System.out.println( s.getId()+"-"+s.getName());
    }
    public void printAllEngines() throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        EngineInfo[] el = sr.getEnginesList();
        for(EngineInfo e : el)
            System.out.println( e.getName() );
    }

    public void printServiceofEngine(EngineInfo el) throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        Service[] sl = sr.getServicesList();
        sl = sr.getServicesMappedToEngine(el);
        for(Service s : sl)
            System.out.println( s.getName() );
    }

    public void printEnginesMappedToService(Service sl) throws SQLException, ClassNotFoundException
    {
        ServiceRepository sr = new ServiceRepository();
        EngineInfo[] el = sr.getEnginesList();
        el = sr.getEnginesMappedToService(sl);
         for(EngineInfo e : el)
            System.out.println( e.getName() );
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ServiceRepository sr = new ServiceRepository();
        
      
        
        

       
    }

}
