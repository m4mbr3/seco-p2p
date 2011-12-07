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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eros
 */
public class AliveTest {

    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        int port = 8888;
        int interval = 2000;
        Method callback = System.out.getClass().getMethod("println", String.class);
        Object callee = System.out;
        Listener l = new Listener( port, callback, callee);
        l.start();
        ArrayList<EngineInfo> list = new ArrayList<EngineInfo>();
        list.add( new EngineInfo(0, "TestEngine", "127.0.0.1", port) );
        Sender s = new Sender(list);
        s.start(interval);
        System.in.read();
        s.stop();
        l.close();
    }

}

class Listener extends Thread {

    private int port;
    private boolean stopped = false;
    private Method callback;
    private Object callee;

    public Listener(int port, Method callback, Object callee){
        this.port = port;
        this.callback = callback;
        this.callee = callee;
    }

    @Override
    public void run(){
        try{
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(port));
            ssc.configureBlocking(false);
            while(!stopped){
                SocketChannel s = ssc.accept();
                if(s != null){
                    new RequestHandler(s).start();
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

    Timer t;
    MyTask mt;
    ArrayList<EngineInfo> targets;

    public Sender(ArrayList<EngineInfo> targets){
        t = new Timer();
        mt = new MyTask();
        this.targets = targets;
    }

    public void start(int interval){
        t.scheduleAtFixedRate(mt, 100, interval);
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            for(EngineInfo e : targets){
                try {
                    Socket s = new Socket(e.getHost(), e.getPort());
                    s.getOutputStream().write("I'm alive".getBytes());
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