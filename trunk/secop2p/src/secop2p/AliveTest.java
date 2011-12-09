/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    private static Set<AliveEngine> aliveEngines =
            Collections.synchronizedSet(
                new HashSet<AliveEngine>()
            );
    private static final int start_port = 8000;
    private Set<EngineInfo> others;

    public AliveEngine(){
        this("Engine "+(aliveEngines.size()+1), "127.0.0.1", start_port+aliveEngines.size());
    }

    @SuppressWarnings({"ResultOfObjectAllocationIgnored", "LeakingThisInConstructor"})
    public AliveEngine(String name, String host, int port){
        super(name, host, port);
        System.out.println("creating engine "+this.getName());
        try {
            aliveEngines.add(this);
            Method l_cb = this.getClass().getMethod("receivedMessage", String.class);
            new Listener(port, l_cb, this);
            others = new HashSet<EngineInfo>(aliveEngines);
            others = Collections.synchronizedSet(others);
            others.remove(this);
            Method s_cb = this.getClass().getMethod("generateMessage");
            new Sender(others, s_cb, this);
            for(AliveEngine ae : aliveEngines){
                if( ae != this)
                    ae.others.add(this);
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AliveEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String generateMessage(){
        return this.getName()+" - I'm alive at "+(int)(System.currentTimeMillis()/1000);
    }

    public void receivedMessage(String msg){
        System.out.println(
            this.getName() + " - Received: " + msg
        );
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
            ssc.register(selector, ssc.validOps());
            while(!stopped){
                selector.select();
                for(SelectionKey sk : selector.selectedKeys()){
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
                InputStreamReader br = new InputStreamReader( sc.socket().getInputStream());
                StringBuilder sb = new StringBuilder();
                int i = br.read();
                while(i != -1){
                    sb.append( (char)i );
                    i = br.read();
                }
                sc.close();
                // invoke the callback with the received text
                callback.invoke(callee, sb.toString());
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

}

class Sender {

    public static int DEFAULT_INTERVAL = -1;
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
        this.targets = targets;
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
        t.scheduleAtFixedRate(mt, 100, interval);
    }

    public void start(int interval){
        this.privateStart(interval);
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            String msg = "I'm alive";
            try {
                msg = (String) callback.invoke(callee);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(EngineInfo e : targets){
                try {
                    Socket s = new Socket(e.getHost(), e.getPort());
                    s.getOutputStream().write(msg.getBytes());
                    s.close();
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