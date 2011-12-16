/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 *
 * @author eros
 */
public class AliveTest {

    private ServiceRepository sr;

    public AliveTest() throws SQLException, ClassNotFoundException{
        sr = new ServiceRepository();
        for(EngineInfo e : sr.getEnginesList())
            sr.delEngine(e);
    }

    public void registerNewEngine(EngineInfo e) throws SQLException{
        sr.addNewEngine(e);
        System.out.println("Registered new Engine with id="+e.getId());
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, ClassNotFoundException{
        /*Sender.DEFAULT_INTERVAL = 3000;
        new AliveEngine();
        System.out.println("Press any key to create a new test engine");
        System.out.println("Press CTRL+C to exit");
        while(true){
            new AliveEngine();
            System.in.read();
        }*/
        AliveTest at = new AliveTest();
        at.registerNewEngine(new EngineInfo("Test", "0.0.0.0", 8000));
        at.registerNewEngine(new EngineInfo("Test", "0.0.0.0", 8001));
    }


}

