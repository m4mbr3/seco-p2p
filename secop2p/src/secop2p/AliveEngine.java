/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import secop2p.util.Listener;
import secop2p.util.Sender;

/**
 *
 * @author eros
 */
public class AliveEngine extends EngineInfo {
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
            new Listener(port, l_cb, this);
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
        Metrics m = new Metrics(0.5, 50);
        try{
            byte[] msg = new Message(this, m).serialize();
            return msg;
        }catch(IOException ex){
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }catch(Exception ex){
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void receivedMessage(byte[] msg) throws IOException, ClassNotFoundException{
        synchronized(System.out){
            Message m = Message.deserialize(msg);
            System.out.println(m);
        }
    }

}

class Message implements Serializable {

    EngineInfo from;
    Metrics m;

    public Message(EngineInfo from, Metrics m){
        this.from = from;
        this.m = m;
    }

    @Override
    public String toString(){
        int time = (int)(System.currentTimeMillis()/1000);
        return "Engine "+from.getName()+" is alive at "+time+" with metrics "+m;
    }

    public byte[] serialize() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        synchronized(this){
            oos.writeObject(this);
            oos.close();
        }
        baos.close();
        return baos.toByteArray();
    }

    public static Message deserialize(byte[] arr) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Message m = (Message) ois.readObject();
        ois.close();
        bais.close();
        return m;
    }
}

final class Metrics implements Comparable, Serializable {

    public static int SCALE_FACTOR = 5000;
    double load;
    int rtt;

    /**
     * @param load Should be in range 0 to 1
     * @param rtt RoundTripTime in millis
     */
    public Metrics(double load, int rtt){
        this.load = load;
        this.rtt = rtt;
    }

    public int evaluate(){
        return (int) (Math.round(this.load*SCALE_FACTOR)+rtt);
    }

    public int compareTo(Object t) {
        if(t instanceof Metrics){
            return evaluate()- ((Metrics) t).evaluate();
        }else
            return Integer.MAX_VALUE;
    }

    @Override
    public String toString(){
        return "<Metrics:(load="+load+", rtt="+rtt+")="+evaluate()+">";
    }

}


