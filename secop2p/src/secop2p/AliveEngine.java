/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import secop2p.util.PortChecker;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.Listener;
import secop2p.util.ListenerCallback;
import secop2p.util.Sender;
import secop2p.util.Serializer;

/**
 *
 * @author eros
 */
public class AliveEngine extends EngineInfo implements ListenerCallback {
    
    private final static transient Set<AliveEngine> aliveEngines =
            Collections.synchronizedSet(
                new HashSet<AliveEngine>()
            );
    private static final transient int start_port = 8000;
    private final transient Set<RemoteEngine> others;

    public AliveEngine(){
        this("Engine "+(aliveEngines.size()+1), "127.0.0.1", start_port+aliveEngines.size());
    }

    @SuppressWarnings({"ResultOfObjectAllocationIgnored", "LeakingThisInConstructor"})
    public AliveEngine(String name, String host, int port){
        super(name, host, port);
        System.out.println("creating engine "+this.getName());
        others = Collections.synchronizedSet(
            new HashSet<RemoteEngine>(aliveEngines)
        );
        try {
            synchronized(aliveEngines){
                aliveEngines.add(this);
            }
            Method l_cb = this.getClass().getMethod("receivedMessage", new byte[0].getClass());
            ServerSocketChannel ssc = PortChecker.getBoundedServerSocketChannel();
            setPort( ssc.socket().getLocalPort() );
            setHost(ssc.socket().getInetAddress().getHostAddress());
            new Listener(ssc, this);
            others.remove(this);
            Method s_cb = this.getClass().getMethod("generateMessage");
            new Sender(others, s_cb, this);
            for(AliveEngine ae : aliveEngines){
                if( ae != this)
                    synchronized(ae.others){
                        ae.others.add(this);
                    }
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] generateMessage() throws IOException{
        //return this.getName()+" - I'm alive at "+(int)(System.currentTimeMillis()/1000);
        Metrics m = new LocalMetrics(0.5);
        try{
            byte[] msg = Serializer.serialize(new Message(this, m));
            return msg;
        }catch(IOException ex){
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }catch(Exception ex){
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void handleRequest(SocketChannel client) {
        synchronized(System.out){
            Message m;
            try {
                m = Serializer.deserialize(client.socket().getInputStream(), Message.class);
                System.out.println(m);
            } catch (IOException ex) {
                Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}



