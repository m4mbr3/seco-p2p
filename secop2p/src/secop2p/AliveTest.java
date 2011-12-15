/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import secop2p.util.Sender;

/**
 *
 * @author eros
 */
public class AliveTest {

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Sender.DEFAULT_INTERVAL = 3000;
        new AliveEngine();
        System.out.println("Press any key to create a new test engine");
        System.out.println("Press CTRL+C to exit");
        while(true){
            new AliveEngine();
            System.in.read();
        }
    }

}

