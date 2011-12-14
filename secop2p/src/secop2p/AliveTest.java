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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eros
 */
public class AliveTest {

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Sender.DEFAULT_INTERVAL = 4000;
        new AliveEngine();
        System.out.println("Press any key to create a new test engine");
        System.out.println("Press CTRL+C to exit");
        while(true){
            new AliveEngine();
            System.in.read();
        }
    }

}

class AliveEngine extends EngineInfo {
    private final static Set<AliveEngine> aliveEngines =
            Collections.synchronizedSet(
                new HashSet<AliveEngine>()
            );
    private static final int start_port = 8000;
    private final Set<EngineInfo> others;

    public AliveEngine(){
        this("Engine "+(aliveEngines.size()+1), "127.0.0.1", start_port+aliveEngines.size());
    }

    @SuppressWarnings({"ResultOfObjectAllocationIgnored", "LeakingThisInConstructor"})
    public AliveEngine(String name, String host, int port){
        super(name, host, port);
        System.out.println("creating engine "+this.getName());
        others = Collections.synchronizedSet(
            new HashSet<EngineInfo>(aliveEngines)
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
        }
    }

    public void receivedMessage(byte[] msg) throws IOException, ClassNotFoundException{
        synchronized(System.out){
            Message m = Message.deserialize(msg);
            System.out.println(m);
        }
    }

}

final class Message implements Serializable {

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
            oos.reset();
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
     *
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

class Listener extends Thread {

    private int port;
    private boolean stopped = false;
    private Method callback;
    private Object callee;

    public Listener(int port, Method callback, Object callee){
        this(port, callback, callee, true);
    }

    public Listener(int port, Method callback, Object callee, boolean autostart){
        this.port = port;
        this.callback = callback;
        this.callee = callee;
        if(autostart){
            new Timer().schedule(
                new TimerTask(){
                    @Override
                    public void run() {
                        Listener.this.start();
                    }
                }, 0
            );
        }
    }

    @Override
    public void run(){
        try{
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run(){
                    Listener.this.stopped = true;
                }
            });
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while(!stopped){
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                while(!keys.isEmpty()){
                    SelectionKey k = keys.iterator().next();
                    keys.remove(k);
                    SocketChannel s = ssc.accept();
                    if(s != null){
                        new RequestHandler(s).start();
                    }
                }
            }
            ssc.close();
        }catch(Exception ex){
            Logger.getLogger(AliveTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close(){
        stopped = true;
    }

    class RequestHandler extends Thread {
        
        private SocketChannel sc;

        public RequestHandler(SocketChannel sc){
            this.sc = sc;
        }

        @Override
        public void run(){
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int rbytes;
                while( (rbytes = sc.socket().getInputStream().read(buf)) > -1){
                    baos.write(buf,0,rbytes);
                }
                baos.close();
                try{
                    sc.socket().getInputStream().close();
                }catch(SocketException e){}
                try{
                    sc.socket().getOutputStream().close();
                }catch(SocketException e){}
                try{
                    sc.socket().close();
                }catch(SocketException e){}
                try{
                    sc.close();
                }catch(SocketException e){}
                // invoke the callback with the received text
                callback.invoke(callee, baos.toByteArray());
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex.getCause());
            }
        }
        
    }

}

class Sender {

    public static int DEFAULT_INTERVAL = -1;
    public static int DEFAULT_DELAY = 2500;
    private Timer t;
    private MyTask mt;
    private Set<EngineInfo> targets;
    private Method callback;
    private Object callee;

    public Sender(Set<EngineInfo> targets, Method callback, Object callee){
        this(targets, callback, callee, DEFAULT_INTERVAL);
    }

    public Sender(Set<EngineInfo> targets, Method callback, Object callee, int interval){
        t = new Timer();
        mt = new MyTask();
        this.targets = Collections.synchronizedSet(targets);
        this.callback = callback;
        this.callee = callee;
        if(interval >= 0){
            this.privateStart(interval);
        }
    }

    private void privateStart(int interval){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                Sender.this.stop();
            }
        });
        t.scheduleAtFixedRate(mt, DEFAULT_DELAY, interval);
    }

    public void start(int interval){
        this.privateStart(interval);
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            byte[] msg = "I'm alive".getBytes();
            try {
                msg = (byte[]) callback.invoke(callee);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(EngineInfo e : targets.toArray(new EngineInfo[0])){
                try {
                    Socket s = new Socket(e.getHost(), e.getPort());
                    s.getOutputStream().write(msg);
                    try{
                        s.getOutputStream().close();
                    }catch(SocketException ex){}
                    try{
                        s.getInputStream().close();
                    }catch(SocketException ex){}
                    try{
                        s.close();
                    }catch(SocketException ex){}
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public void stop(){
        mt.cancel();
        t.cancel();
        t.purge();
    }
}