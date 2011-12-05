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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ServiceRepository sr = new ServiceRepository();

       sr.addNewService(new Service("ciao"));
       System.out.println("Add new Service ok");
        

        sr.addNewEngine(new EngineInfo("prova", "192.168.1.1",90));
                System.out.println("Add new Engine ok");
        

        Service[] sl = sr.getServicesList();

        for(Service s : sl)
            System.out.println( s.getName()+" "+s.getId() );

        EngineInfo[] el = sr.getEnginesList();
        for(EngineInfo e : el)
            System.out.println( e.getName() );
        sl = sr.getServicesMappedToEngine(el[0]);

        for(Service s : sl)
            System.out.println( s.getName() );
        el = sr.getEnginesMappedToService(sl[0]);

        for(EngineInfo e : el)
            System.out.println( e.getName() );
    }

}
